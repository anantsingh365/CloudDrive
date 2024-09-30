package com.anant.CloudDrive.Storage.Models;

import java.io.IOException;
import java.io.InputStream;

public class UploadPartRequest {

    private final InputStream ins;
    private final String uploadId;
    private final long contentLength;

    public UploadPartRequest(InputStream ins, String uploadId, long contentLength) {
        try {
            boolean a = ins.available() == 0;
            if(ins == null  || contentLength == 0 || uploadId == null || uploadId.isEmpty()){
                throw new IllegalStateException("InputStream/ContentLength/uploadId either null/zero/empty/not-Available");
            }
            this.ins = ins;
            this.uploadId = uploadId;
            this.contentLength = contentLength;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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
