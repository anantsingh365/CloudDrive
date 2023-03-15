package com.anant.CloudDrive.StorageProviders;

import java.util.Date;

public class UserFileMetaData {

    private final String name;
    private final Long size;
    private final Date lastModified;
    private final String contentType;


    public UserFileMetaData(String name, Long size, Date lastModified, String contentType) {
        this.name = name;
        this.size = size;
        this.lastModified = lastModified;
        this.contentType = contentType;
    }

    public String getName(){
        return name;
    }
    public Long getSize(){
        return size;
    }
    public Date getLastModified(){
       return lastModified;
    }
    public String getContentType(){
        return this.contentType;
    }
}
