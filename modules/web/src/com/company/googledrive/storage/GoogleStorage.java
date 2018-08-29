package com.company.googledrive.storage;

import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import org.springframework.stereotype.Component;

import java.io.FileDescriptor;
import java.util.List;

@Component
public class GoogleStorage extends GoogleDriveFileStorageWithoutInterface {

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

}


