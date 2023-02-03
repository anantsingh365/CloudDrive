package com.anant.CloudDrive.service;

import java.util.Date;

public class UserFileMetaData {

    private final String name;
    private final Long size;
    private final Date lastModified;

    public UserFileMetaData(String name, Long size, Date lastModified) {
        this.name = name;
        this.size = size;
        this.lastModified = lastModified;
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
}
