package com.company.googledrive.service;



public interface UploadHelperService {
    String NAME = "googledrive_UploadHelperService";

    void addJustUploadedFile(String fileName, String createdFileId);
    String getGoogleFileId(String fileName);
    
}