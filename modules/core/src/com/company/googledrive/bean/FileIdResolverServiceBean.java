package com.company.googledrive.bean;
import com.company.googledrive.service.FileIdResolverService;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

@Service(FileIdResolverService.NAME)
public class FileIdResolverServiceBean implements FileIdResolverService {

    @Inject
    GoogleDriveFileStorage fileStorage;

    @Override
    public String getFileId(String googleFileId) throws Exception {
        Drive drive = fileStorage.getDrive();
        File executed = drive.files()
                .get(googleFileId)
                .setFields("id, name")
                .execute();
        return executed
                .getName();
    }
}
