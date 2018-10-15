package com.company.googledrive.service;

public interface FileIdResolverService {
    String NAME = "googledrive_FileIdResolverService";

    String getFileId(String googleFileId) throws Exception;
}
