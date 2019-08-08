package com.company.googledrive.web.screens;

import com.company.googledrive.entity.ExtFileDescriptor;
import com.company.googledrive.entity.GoogleDriveFileEntity;
import com.company.googledrive.service.UploadHelperService;
import com.company.googledrive.storage.GoogleStorage;
import com.google.api.services.drive.model.File;
import com.haulmont.cuba.core.global.*;
import com.haulmont.cuba.gui.AppConfig;
import com.haulmont.cuba.gui.components.*;
import com.haulmont.cuba.gui.components.actions.BaseAction;
import com.haulmont.cuba.gui.data.DataSupplier;
import com.haulmont.cuba.gui.data.HierarchicalDatasource;
import com.haulmont.cuba.gui.export.ExportFormat;
import com.haulmont.cuba.gui.icons.Icons;
import com.haulmont.cuba.gui.upload.FileUploadingAPI;
import com.haulmont.cuba.gui.xml.layout.ComponentsFactory;
import org.apache.commons.io.input.ProxyInputStream;
import org.apache.commons.lang.StringUtils;

import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

public class GoogleDriveBrowse extends AbstractWindow {

    @Inject
    private FileMultiUploadField upload;

    @Inject
    private HierarchicalDatasource<GoogleDriveFileEntity, UUID> googleDriveDs;

    @Inject
    private GoogleStorage googleStorage;

    @Inject
    private Metadata metadata;

    @Inject
    private FileUploadingAPI fileUploadingAPI;

    @Inject
    private DataSupplier dataSupplier;

    @Inject
    private BrowserFrame docsViewerFrame;

    @Inject
    private ComponentsFactory componentsFactory;

    @Inject
    private TreeTable<GoogleDriveFileEntity> files;

    @Inject
    private UploadHelperService uploadHelperService;

    @Inject
    private FileLoader fileLoader;

    @Inject
    private HBoxLayout topButtonsHbox;

    @Inject
    private Button deleteBtn;

    private HashMap<String, String> mimeToResourcePath;
    private List<String> fileIds;
    private String directory = "";
    private HashMap<String, GoogleDriveFileEntity> includedEntityById = new HashMap<>();

    private boolean allowUpload = true;
    private boolean allowDownload = true;
    private boolean allowDelete = true;

    @Override
    public void init(Map<String, Object> params) {
        super.init(params);
        initParams(params);

        initDs();
        initUploadBtn();
        initDeleteBtn();
        initTable();

        mimeToResourcePath = initMimeToResourcePath();
        topButtonsHbox.setVisible(allowUpload || allowDelete);
    }

    private void initDeleteBtn() {

        deleteBtn.setAction(new BaseAction("delete") {
            @Override
            public void actionPerform(Component component) {
                if (files.getSelected().size() == 0) {
                    showNotification(getMessage("select_files"), NotificationType.HUMANIZED);
                    return;
                }
                if (fileIds != null) {
                    showOptionDialog(getMessage("removing")
                            , getMessage("remove_selected_files")
                            , MessageType.CONFIRMATION
                            , Arrays.asList(
                                    new BaseAction("delete_from_disk") {
                                        public void actionPerform(Component component) {
                                            deleteSelected(true);
                                        }
                                    },
                                    new BaseAction("delete_only_from_this_list") {
                                        public void actionPerform(Component component) {
                                            deleteSelected(false);
                                        }
                                    },
                                    new DialogAction(DialogAction.Type.NO, Status.NORMAL)
                            ));
                } else {
                    showOptionDialog(getMessage("removing")
                            , getMessage("remove_selected_files") + "?"
                            , MessageType.CONFIRMATION
                            , new Action[]{
                                    new DialogAction(DialogAction.Type.YES, Status.PRIMARY) {
                                        public void actionPerform(Component component) {
                                            deleteSelected(true);
                                        }
                                    },
                                    new DialogAction(DialogAction.Type.NO, Status.NORMAL)
                            });
                }
            }
        });
        deleteBtn.setVisible(allowDelete);
    }

    private void deleteSelected(boolean deleteFromDisk) {
        Collection<GoogleDriveFileEntity> entities = files.getSelected();
        List<String> ids = new ArrayList<>();
        entities.forEach(entity -> ids.add(entity.getFileId()));

        LoadContext<ExtFileDescriptor> loadContext = LoadContext.create(ExtFileDescriptor.class)
                .setQuery(LoadContext.createQuery("Select e from sys$FileDescriptor e  where e.gdriveId in :ids")
                        .setParameter("ids", ids));


        List<ExtFileDescriptor> filesToDelete = dataSupplier.loadList(loadContext);
        if ((filesToDelete == null) || (filesToDelete.size() == 0)) {
            showNotification(getMessage("there_is_no_file_descriptors-for_selected_files"), NotificationType.ERROR);
            return;
        }
        List<ExtFileDescriptor> deleteErrors = new ArrayList<>();
        List<GoogleDriveFileEntity> entitiesToDelete = new ArrayList<>();
        filesToDelete.forEach(file -> {
            try {
                GoogleDriveFileEntity entityToDelete = includedEntityById.get(file.getGdriveId());
                if (deleteFromDisk) {
                    fileLoader.removeFile(file);
                    dataSupplier.remove(file);
                }
                if (fileIds != null) fileIds.remove(file.getGdriveId());
                entitiesToDelete.add(entityToDelete);
            } catch (Exception e) {
                deleteErrors.add(file);
            }
        });

        entitiesToDelete.forEach(item -> googleDriveDs.removeItem(item));

        if (deleteErrors.size() > 0) {
            showNotification(getMessage("error_deleting_some_files"), NotificationType.ERROR);
        }
    }

    private void initParams(Map<String, Object> params) {
        if (params.get("allowUpload") != null) {
            allowUpload = (boolean) params.get("allowUpload");
        }
        if (params.get("allowDownload") != null) {
            allowDownload = (boolean) params.get("allowDownload");
        }
        if (params.get("allowDelete") != null) {
            allowDelete = (boolean) params.get("allowDelete");
        }

        if (params.get("fileIds") != null) {
            @SuppressWarnings("unchecked")
            List<String> idsFromParams = (List<String>) params.get("fileIds");
            fileIds = new ArrayList<>(idsFromParams);
        }
        if (params.get("directory") != null) {
            directory = (String) params.get("directory");
        }
    }

    private void initDs() {
        List<File> files = fileIds == null ? googleStorage.getFiles() : googleStorage.getFiles(fileIds);

        if (files == null) {
            return;
        }

        List<GoogleDriveFileEntity> googleDriveFileEntities = new ArrayList<>();

        for (File file :
                files) {
            String fileId = file.getId();
            if (includedEntityById.containsKey(fileId)) {
                continue;
            }
            GoogleDriveFileEntity entity = createEntity(file);
            googleDriveFileEntities.add(entity);
            includedEntityById.put(fileId, entity);
        }

        googleDriveFileEntities.forEach(item -> {
            if ((item.getParents() == null) || (StringUtils.isEmpty(item.getParents()))) return;
            String[] parents = item.getParents().split(", ");
            if (parents.length == 0) return;
            String parent = parents[0];
            GoogleDriveFileEntity parentEntity = includedEntityById.get(parent);
            item.setParent(parentEntity);
            googleDriveDs.addItem(item);
        });
    }

    private void initUploadBtn() {
        upload.setVisible(allowUpload);
        upload.addQueueUploadCompleteListener(() -> {
            for (Map.Entry<UUID, String> entry : upload.getUploadsMap().entrySet()) {
                UUID fileId = entry.getKey();
                String fileName = entry.getValue();
                ExtFileDescriptor fd = (ExtFileDescriptor) fileUploadingAPI.getFileDescriptor(fileId, directory + "/" + fileName);

                // save file to FileStorage
                // FileContent fileContent = new FileContent(null, fd);
                try {
                    fileUploadingAPI.putFileIntoStorage(fileId, fd);
                } catch (FileStorageException e) {
                    throw new RuntimeException("Error saving file to FileStorage", e);
                }
                // save file descriptor to database

                String googleFileId = uploadHelperService.getGoogleFileId(Objects.requireNonNull(fd).getName());

                if (googleFileId != null) {
                    fd.setGdriveId(googleFileId);
                    dataSupplier.commit(fd);

                    List<File> justUploadedFiles = googleStorage.getFiles(Collections.singletonList(googleFileId));
                    if (justUploadedFiles.size() == 1) {
                        File justUploadedFile = justUploadedFiles.get(0);
                        GoogleDriveFileEntity entity = createEntity(justUploadedFile);
                        String[] parents = entity.getParents().split(", ");

                        if (parents.length != 0) {
                            String parent = parents[0];
                            GoogleDriveFileEntity parentEntity = includedEntityById.get(parent);
                            entity.setParent(parentEntity);
                        }
                        googleDriveDs.addItem(entity);
                        includedEntityById.put(entity.getFileId(), entity);

                        GoogleDriveFileEntity parent = entity.getParent();
                        while (parent != null) {
                            this.files.expand(parent.getId());
                            parent = parent.getParent();
                        }
                        this.files.setSelected(Collections.singletonList(entity));
                        if (fileIds != null) fileIds.add(googleFileId);

                    }

                }
            }
            showNotification(formatMessage("uploaded", upload.getUploadsMap().values()), NotificationType.HUMANIZED);
            upload.clearUploads();
        });

        upload.addFileUploadErrorListener(event ->
                showNotification("File upload error", NotificationType.HUMANIZED));

    }

    private void initTable() {
        googleDriveDs.addItemChangeListener(e -> {
            if (e.getItem() == null) {
                docsViewerFrame.setVisible(false);
                return;
            }
            docsViewerFrame.setVisible(true);
            String fileId = e.getItem().getFileId();
            String urlString = String.format("https://drive.google.com/file/d/%s/preview", fileId);

            URL url = null;
            try {
                url = new URL(urlString);
            } catch (MalformedURLException e1) {
                e1.printStackTrace();
            }
            docsViewerFrame.setSource(UrlResource.class).setUrl(url);
        });


        if (allowDownload) this.files.addGeneratedColumn(" ", entity -> {
            Button downloadBtn = componentsFactory.createComponent(Button.class);
            downloadBtn.setStyleName("icon-only");
            downloadBtn.setIcon(AppBeans.get(Icons.class).get("DOWNLOAD"));

            downloadBtn.setAction(new BaseAction("download") {

                @Override
                public void actionPerform(Component component) {
                    InputStream contentStream;
                    try {
                        contentStream = googleStorage.getInputStreamForFile(entity.getFileId());
                    } catch (Exception e) {
                        showNotification(getMessage("error_downloading_file"), NotificationType.ERROR);
                        return;
                    }

                    final InputStream finalContentStream = contentStream;
                    AppConfig.createExportDisplay(GoogleDriveBrowse.this).show(() -> new ProxyInputStream(finalContentStream) {
                        @Override
                        public void close() throws IOException {
                            super.close();

                        }
                    }, entity.getName(), ExportFormat.OCTET_STREAM);
                }
            });

            return downloadBtn;
        });
    }

    private GoogleDriveFileEntity createEntity(File file) {
        GoogleDriveFileEntity fileEntity = metadata.create(GoogleDriveFileEntity.class);
        fileEntity.setFileId(file.getId());
        fileEntity.setName(file.getName());
        fileEntity.setExt(file.getFileExtension());
        fileEntity.setKind(file.getKind());
        fileEntity.setMime(file.getMimeType());
        List<String> parents = file.getParents();
        String parentsString = parents == null ? "null" : String.join(", ", file.getParents());
        fileEntity.setParents(parentsString);

        return fileEntity;
    }

    private HashMap<String, String> initMimeToResourcePath() {
        HashMap<String, String> mtr = new HashMap<>();
        String formatString = "com/company/googledrive/web/screens/images/%s";
        mtr.put("application/vnd.google-apps.document", String.format(formatString, "doc.png"));
        mtr.put("application/vnd.google-apps.spreadsheet", String.format(formatString, "xls.png"));
        mtr.put("application/vnd.ms-excel", String.format(formatString, "xls.png"));
        mtr.put("application/vnd.google-apps.folder", String.format(formatString, "folder.png"));
        mtr.put("application/pdf", String.format(formatString, "pdf.png"));
        mtr.put("application/zip", String.format(formatString, "zip.png"));
        mtr.put("text/plain", String.format(formatString, "txt.png"));
        mtr.put("image/gif", String.format(formatString, "gif.png"));
        mtr.put("image/jpeg", String.format(formatString, "jpg.png"));
        mtr.put("image/png", String.format(formatString, "png.png"));
        mtr.put("image/bmp", String.format(formatString, "bmp.png"));
        return mtr;
    }

    public Component generateIconCell(GoogleDriveFileEntity entity) {
        String mime = entity.getMime();
        if (!mimeToResourcePath.containsKey(mime)) {
            return null;
        }

        Image image = componentsFactory.createComponent(Image.class);
        image.setScaleMode(Image.ScaleMode.SCALE_DOWN);
        image.setSource(ClasspathResource.class)
                .setPath(mimeToResourcePath.get(mime));

        return image;
    }

    @SuppressWarnings("unused")
    public List<String> getFileIds() {
        return fileIds;
    }
}