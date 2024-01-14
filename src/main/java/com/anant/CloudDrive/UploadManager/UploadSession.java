package com.anant.CloudDrive.UploadManager;

import com.anant.CloudDrive.StorageProviders.Uploads.UploadEntry;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import com.anant.CloudDrive.StorageProviders.requests.*;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Qualifier("userUploadSession")
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class UploadSession{

    //represents multiple upload entries from a session
     private final ConcurrentHashMap<String, UploadEntry> uploadEntries= new ConcurrentHashMap<>();
     private  final ApplicationContext context;
     private final Logger logger;

    public UploadSession(@Autowired ApplicationContext context, @Autowired Logger logger){
        this.context = context;
        this.logger = logger;
    }

    public String registerUploadId(String userName, UploadIdRequest uploadIdRequest){
        String freshUploadId = UUID.randomUUID().toString();
        if(uploadIdAlreadyExists(freshUploadId)){
            throw new RuntimeException("Couldn't generate a unique uploadId");
        }
        //for every ask same entry will be used.
        createEntry(freshUploadId).setUploadKeyName(userName, uploadIdRequest);
        logger.info("Created Upload Entry for User - {}, Upload Id - {}", userName, freshUploadId);
        return freshUploadId;
     }

     public UploadEntry getPart(String uploadId){
        return uploadEntries.get(uploadId);
     }

     private UploadEntry createEntry(String uploadId){
        var uploadEntry = context.getBean(UploadEntry.class);
        this.uploadEntries.put(uploadId, uploadEntry);
        return uploadEntry;
     }

     private boolean uploadIdAlreadyExists(String uploadId){
        return uploadEntries.containsKey(uploadId);
     }
}