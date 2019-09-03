package com.company.googledrive.service;

import org.springframework.stereotype.Service;

import java.util.*;


import java.util.concurrent.ConcurrentHashMap;

@Service(UploadHelperService.NAME)
public class UploadHelperServiceBean implements UploadHelperService {

    protected Map<String,String> fileIdMap=new ConcurrentHashMap<>();
    protected List<String> itemsOrder=new ArrayList<>();

    protected int mapSizeLimit=1000;
    protected int valueToDelete=100;

    @Override
    public void addJustUploadedFile(String fileName, String createdFileId) {
        checkMapSize();
        fileIdMap.put(fileName,createdFileId);
        itemsOrder.add(fileName);
    }

    @Override
    public String getGoogleFileId(String fileName) {
        return fileIdMap.get(fileName);
    }

    private void checkMapSize(){
        if(fileIdMap.size()>mapSizeLimit){
            List<String> itemsToRemove=new ArrayList<>(itemsOrder.subList(0,100));

            itemsToRemove.forEach(item->{
                fileIdMap.remove(item);
            });
            itemsOrder.removeAll(itemsToRemove);
        }
    }
}