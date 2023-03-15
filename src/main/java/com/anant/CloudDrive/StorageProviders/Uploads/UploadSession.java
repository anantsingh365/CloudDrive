package com.anant.CloudDrive.StorageProviders.Uploads;

import com.anant.CloudDrive.StorageProviders.Uploads.requests.*;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.UUID;

@Component
@Qualifier("userUploadSession")
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class UploadSession{

     private  final ApplicationContext context;
     private final Logger logger;

    public UploadSession(@Autowired ApplicationContext context, @Autowired Logger logger){
        this.context = context;
        this.logger = logger;
    }

    //represents multiple upload entries from a user
    private final HashMap<String, UploadEntry> uploadEntries= new HashMap<>();

    public String registerUploadId(UploadIdRequest uploadIdRequest){
        String freshUploadId = UUID.randomUUID().toString();
        if(uploadIdAlreadyExists(freshUploadId)){
            throw new RuntimeException("Couldn't generate a unique uploadId");
        }
        //for every ask same entry will be used.
        createEntry(freshUploadId).setUploadKeyName(getLoggedInUserName(), uploadIdRequest);
        logger.info("Created Upload Entry for User - {}, Upload Id - {}", getLoggedInUserName(), freshUploadId);
        return freshUploadId;
     }
     public UploadEntry getEntry(String uploadId){
         return uploadEntries.get(uploadId);
     }
     private String getLoggedInUserName(){
        return SecurityContextHolder.getContext().getAuthentication().getName();
     }
     private UploadEntry createEntry(String uploadId){
        var s3MultiPartUpload = context.getBean(UploadEntry.class);
        //context.getBean(UploadEntry.class);
        this.uploadEntries.put(uploadId, s3MultiPartUpload);
        return s3MultiPartUpload;
     }
     private boolean uploadIdAlreadyExists(String uploadId){
        return uploadEntries.containsKey(uploadId);
     }
}