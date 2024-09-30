package com.anant.CloudDrive.Storage.Models;

public class UploadIdRequest {

    private final String fileName;
    private final String contentType;

    public UploadIdRequest(String fileName, String contentType){
       // if(fileName == null || fileName.isEmpty() || contentType == null || contentType.isEmpty()){
       //     throw new IllegalStateException("FileName or Content type cannot be null/empty");
       // }
        this.fileName = fileName;
        this.contentType = contentType;
    }

    public String getFileName() {
        return fileName;
    }

    public String getContentType(){
        return contentType;
    }

    public boolean isRequestValid(){
        return this.fileName != null && this.contentType != null;
    }
}
