package com.company.googledrive.storage;

import com.company.googledrive.entity.ExtFileDescriptor;
import com.company.googledrive.service.UploadHelperService;
import com.haulmont.cuba.core.entity.FileDescriptor;
import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.core.global.FileStorageException;
import com.haulmont.cuba.gui.upload.FileUploading;

import java.util.UUID;

/**
 * @author Antonlomako. created on 12.03.2019.
 */
public class ExtFileUploading extends FileUploading {

    @Override
    public void putFileIntoStorage(UUID fileId, FileDescriptor fileDescr) throws FileStorageException {
        super.putFileIntoStorage(fileId,fileDescr);

        if(fileDescr instanceof ExtFileDescriptor) {
            ExtFileDescriptor extFileDescriptor = (ExtFileDescriptor) fileDescr;
            String googleFileId = AppBeans.get(UploadHelperService.class).getGoogleFileId(fileDescr.getName());

            if (googleFileId != null) {
                extFileDescriptor.setGdriveId(googleFileId);
            }
        }
    }
}
