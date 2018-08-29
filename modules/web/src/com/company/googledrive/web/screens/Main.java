package com.company.googledrive.web.screens;

import com.company.googledrive.entity.GoogleDriveFileEntity;
import com.company.googledrive.storage.GoogleStorage;
import com.google.api.client.http.FileContent;
import com.google.api.services.drive.model.File;
import com.haulmont.cuba.core.entity.FileDescriptor;
import com.haulmont.cuba.core.global.FileStorageException;
import com.haulmont.cuba.core.global.Metadata;
import com.haulmont.cuba.gui.components.AbstractWindow;
import com.haulmont.cuba.gui.components.FileMultiUploadField;
import com.haulmont.cuba.gui.data.DataSupplier;
import com.haulmont.cuba.gui.data.HierarchicalDatasource;
import com.haulmont.cuba.gui.upload.FileUploadingAPI;

import javax.inject.Inject;
import java.util.*;

public class Main extends AbstractWindow {

    @Inject
    FileMultiUploadField upload;

    @Inject
    HierarchicalDatasource<GoogleDriveFileEntity, UUID> googleDriveDs;

    @Inject
    GoogleStorage googleStorage;

    @Inject
    private Metadata metadata;

    @Inject
    private FileUploadingAPI fileUploadingAPI;

    @Inject
    private DataSupplier dataSupplier;

    @Override
    public void init(Map<String, Object> params) {
        super.init(params);
        List<File> files = googleStorage.getFiles();
        if (files == null) {
            return;
        }
        HashMap<String, File> fileById = new HashMap<>();
        HashMap<String, GoogleDriveFileEntity> includedEntityById = new HashMap<>();
        files.forEach(file -> fileById.put(file.getId(), file));
        for (File file :
                files) {
            String fileId = file.getId();
            if (includedEntityById.containsKey(fileId)) {
                continue;
            }
            GoogleDriveFileEntity entity = createEntity(file);
            googleDriveDs.includeItem(entity);
            includedEntityById.put(fileId, entity);
            recursiveIncludeParent(entity, fileById, includedEntityById);
            GoogleDriveFileEntity parentEntity = includedEntityById.get(entity.getParents());
            entity.setParent(parentEntity);

        }


        upload.addQueueUploadCompleteListener(() -> {
            for (Map.Entry<UUID, String> entry : upload.getUploadsMap().entrySet()) {
                UUID fileId = entry.getKey();
                String fileName = entry.getValue();
                FileDescriptor fd = fileUploadingAPI.getFileDescriptor(fileId, fileName);

                // save file to FileStorage
                // FileContent fileContent = new FileContent(null, fd);
                try {
                    fileUploadingAPI.putFileIntoStorage(fileId, fd);
                } catch (FileStorageException e) {
                    throw new RuntimeException("Error saving file to FileStorage", e);
                }
                // save file descriptor to database
                dataSupplier.commit(fd);
            }
            showNotification("Uploaded files: " + upload.getUploadsMap().values(), NotificationType.HUMANIZED);
            upload.clearUploads();
        });

        upload.addFileUploadErrorListener(event ->
                showNotification("File upload error", NotificationType.HUMANIZED));
    }

    private void recursiveIncludeParent(GoogleDriveFileEntity entity,
                                        HashMap<String, File> fileById,
                                        HashMap<String, GoogleDriveFileEntity> includedFileIds) {
        String parentId = entity.getParents();
        if (parentId != null && !includedFileIds.containsKey(parentId)) {
            File parentFile = fileById.get(parentId);
            if (parentFile != null) {
                GoogleDriveFileEntity parentEntity = createEntity(parentFile);
                googleDriveDs.includeItem(parentEntity);
                includedFileIds.put(parentId, parentEntity);
                recursiveIncludeParent(parentEntity, fileById, includedFileIds);
            }
        }
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
}