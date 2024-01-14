package com.anant.CloudDrive.UploadManager;

public class CreateNewUploadRequest {
    private final String fileName;
    private final String contentType;

    public CreateNewUploadRequest(String fileName, String contentType, String sessionId){
        this.fileName = fileName;
        this.contentType = contentType;
    }

    public String getFileName() { return fileName; }
    public String getContentType(){ return contentType; }
    public boolean isRequestValid() {
        return this.fileName != null && this.contentType != null;
    }
}
