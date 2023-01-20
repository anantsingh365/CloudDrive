package com.anant.CloudDrive.UserUploads;

import com.anant.CloudDrive.s3.S3MultiPartUpload;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.UUID;

import java.util.concurrent.ConcurrentHashMap;

@Component
@Qualifier("userUploadSession")
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class UserUploadSession {

    @Autowired
    private  ApplicationContext context;

    //represents multiple upload entries from a user
    private final ConcurrentHashMap<String, S3MultiPartUpload> uploadEntries= new ConcurrentHashMap<>();

    public String getUploadId(String userName, String keyName){
        String uploadId = UUID.randomUUID().toString();
        if(uploadEntries.containsKey(uploadId)){
            throw new RuntimeException("Couldn't generate a unique uploadId");
        }
        //for every ask same entry will be used.
        createEntry(uploadId).setUploadKeyName(userName, keyName);
        return uploadId;
     }
     public S3MultiPartUpload getUploadEntry(String uploadId){
         return uploadEntries.get(uploadId);
     }

     private S3MultiPartUpload createEntry(String uploadId){
        S3MultiPartUpload s3MultiPartUpload = context.getBean(S3MultiPartUpload.class);
        this.uploadEntries.put(uploadId, s3MultiPartUpload);
        return s3MultiPartUpload;
     }
}