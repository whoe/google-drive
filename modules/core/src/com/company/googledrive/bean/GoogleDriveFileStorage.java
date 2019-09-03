package com.company.googledrive.bean;

import com.company.googledrive.entity.ExtFileDescriptor;
import com.company.googledrive.service.UploadHelperService;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.company.googledrive.config.GoogleDriveConfig;
import com.haulmont.cuba.core.entity.FileDescriptor;
import com.haulmont.cuba.core.global.DataManager;
import com.haulmont.cuba.core.global.FileStorageException;
import com.haulmont.cuba.core.app.FileStorageAPI;
import org.apache.commons.lang.BooleanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.*;

/**
 * File storage implementation based on Google Drive
 *
 * @author adiatullin
 */
public class GoogleDriveFileStorage implements FileStorageAPI {

    private static final Logger log = LoggerFactory.getLogger(GoogleDriveFileStorage.class);

    protected static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

    @Inject
    protected GoogleDriveConfig config;

    @Inject
    protected UploadHelperService uploadHelperService;

    @Inject
    private GoogleDriveFileWorker googleDriveWorker;

    @Inject
    private DefaultFileWorker defaultFileWorker;




    @Override
    public long saveStream(FileDescriptor fileDescr, InputStream inputStream) throws FileStorageException {
        if(fileDescr instanceof ExtFileDescriptor){
            ExtFileDescriptor extFileDescriptor= (ExtFileDescriptor) fileDescr;
            if(BooleanUtils.isTrue(extFileDescriptor.getUseDefaultFileApi())){
                return defaultFileWorker.saveStream(fileDescr,inputStream);
            }
            else{
                return googleDriveWorker.saveStream(fileDescr,inputStream);
            }
        }
        else{
            return defaultFileWorker.saveStream(fileDescr,inputStream);
        }
    }

    @Override
    public void saveFile(FileDescriptor fileDescr, byte[] data) throws FileStorageException {
        if(fileDescr instanceof ExtFileDescriptor){
            ExtFileDescriptor extFileDescriptor= (ExtFileDescriptor) fileDescr;
            if(BooleanUtils.isTrue(extFileDescriptor.getUseDefaultFileApi())){
                defaultFileWorker.saveFile(fileDescr,data);
            }
            else{
                googleDriveWorker.saveFile(fileDescr,data);
            }
        }
        else{
            defaultFileWorker.saveFile(fileDescr,data);
        }
    }

    @Override
    public void removeFile(FileDescriptor fileDescr) throws FileStorageException {
        if(fileDescr instanceof ExtFileDescriptor){
            ExtFileDescriptor extFileDescriptor= (ExtFileDescriptor) fileDescr;
            if(BooleanUtils.isTrue(extFileDescriptor.getUseDefaultFileApi())){
                defaultFileWorker.removeFile(fileDescr);
            }
            else{
                googleDriveWorker.removeFile(fileDescr);
            }
        }
        else{
            defaultFileWorker.removeFile(fileDescr);
        }
    }

    @Override
    public InputStream openStream(FileDescriptor fileDescr) throws FileStorageException {
        if(fileDescr instanceof ExtFileDescriptor){
            ExtFileDescriptor extFileDescriptor= (ExtFileDescriptor) fileDescr;
            if(BooleanUtils.isTrue(extFileDescriptor.getUseDefaultFileApi())){
                return defaultFileWorker.openStream(fileDescr);
            }
            else{
                return googleDriveWorker.openStream(fileDescr);
            }
        }
        else{
            return defaultFileWorker.openStream(fileDescr);
        }
    }

    @Override
    public byte[] loadFile(FileDescriptor fileDescr) throws FileStorageException {
        if(fileDescr instanceof ExtFileDescriptor){
            ExtFileDescriptor extFileDescriptor= (ExtFileDescriptor) fileDescr;
            if(BooleanUtils.isTrue(extFileDescriptor.getUseDefaultFileApi())){
                return defaultFileWorker.loadFile(fileDescr);
            }
            else{
                return googleDriveWorker.loadFile(fileDescr);
            }
        }
        else{
            return defaultFileWorker.loadFile(fileDescr);
        }
    }

    @Override
    public boolean fileExists(FileDescriptor fileDescr) throws FileStorageException {
        if(fileDescr instanceof ExtFileDescriptor){
            ExtFileDescriptor extFileDescriptor= (ExtFileDescriptor) fileDescr;
            if(BooleanUtils.isTrue(extFileDescriptor.getUseDefaultFileApi())){
                return defaultFileWorker.fileExists(fileDescr);
            }
            else{
                return googleDriveWorker.fileExists(fileDescr);
            }
        }
        else{
            return defaultFileWorker.fileExists(fileDescr);
        }
    }
}




