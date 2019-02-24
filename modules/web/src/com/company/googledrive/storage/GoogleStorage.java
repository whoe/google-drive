package com.company.googledrive.storage;

import com.company.googledrive.config.GoogleDriveConfig;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

@Component
public class GoogleStorage {

    protected static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

    @Inject
    protected GoogleDriveConfig config;

    public List<File> getFiles() {
        List<File> files = null;
        try {
            Drive service = getDrive();
            files = service.files().list()
                    .setFields("nextPageToken, files(id, name, parents, mimeType)")
                    .execute()
                    .getFiles();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return files;
    }

    public List<File> getFiles(List<String> fileIds) {
        List<File> files = new ArrayList<>();
        try {
            Drive service = getDrive();
            fileIds.forEach(fileId->{
                try {
                    File file = service.files().get(fileId)
                            .setFields("id, name, parents, mimeType")
                            .execute();
                    files.add(file);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }

        return files;
    }

    public InputStream getInputStreamForFile(String fileId) throws Exception {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        getDrive().files().get(fileId)
                .executeMediaAndDownloadTo(outputStream);

        return new ByteArrayInputStream(outputStream.toByteArray());
    }

    /**
     * Get google drive link
     *
     * @return google drive link
     * @throws Exception in case of any unexpected problems
     */
    protected Drive getDrive() throws Exception {
        NetHttpTransport transport = GoogleNetHttpTransport.newTrustedTransport();
        return new Drive.Builder(transport, JSON_FACTORY, null)
                .setHttpRequestInitializer(getCredentials(transport))
                .setApplicationName(config.getApplicationName())
                .build();
    }

    /**
     * Prepare and get google credentials
     *
     * @param transport communication transport object
     * @return google credentials
     * @throws Exception in case of any unexpected problems
     */
    protected Credential getCredentials(NetHttpTransport transport) throws Exception {
        Credential credential;
        try (StringReader reader = new StringReader(config.getClientOAuthSecretJson())) {
            GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, reader);
            credential = new GoogleCredential.Builder()
                    .setTransport(transport)
                    .setJsonFactory(JSON_FACTORY)
                    .setClientSecrets(clientSecrets)
                    .build();
            credential.setRefreshToken(config.getRefreshToken());
        }
        return credential;
    }

}


