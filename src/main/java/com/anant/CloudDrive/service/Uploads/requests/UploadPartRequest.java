package com.anant.CloudDrive.service.Uploads.requests;

import java.io.InputStream;

public class UploadPartRequest {

    private final InputStream ins;
    private final String uploadId;
    private final long contentLength;

    public UploadPartRequest(InputStream ins, String uploadId, long contentLength) {
        this.ins = ins;
        this.uploadId = uploadId;
        this.contentLength = contentLength;
    }

    public InputStream getInputStream(){
        return ins;
    }
    public String getUploadId(){
        return uploadId;
    }
    public long getContentLength(){
        return contentLength;
    }

}
