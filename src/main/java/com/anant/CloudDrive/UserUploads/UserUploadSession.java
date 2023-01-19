package com.anant.CloudDrive.UserUploads;

import com.amazonaws.services.mgn.model.Application;
import com.anant.CloudDrive.s3.S3MultiPartUpload;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.thymeleaf.context.IContext;

import java.util.UUID;

import java.util.concurrent.ConcurrentHashMap;

@Component
@Qualifier("userUploadSession")
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class UserUploadSession {

    @Autowired
    private  ApplicationContext context;

    private final ConcurrentHashMap<String, S3MultiPartUpload> uploadEntries= new ConcurrentHashMap<>();

    public String getUserSpecificUploadId(String userName, String keyName){
        String uploadId = UUID.randomUUID().toString();
        if(uploadEntries.containsKey(uploadId)){
            return null;
        }
        uploadEntries.put(uploadId, context.getBean(S3MultiPartUpload.class));
        S3MultiPartUpload multiPartUploadEntry = uploadEntries.get(uploadId);
        multiPartUploadEntry.initiateUploadForKeyName(userName, keyName);
        return uploadId;
     }
}