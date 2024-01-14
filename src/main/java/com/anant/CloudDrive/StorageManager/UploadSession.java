package com.anant.CloudDrive.StorageManager;

import com.anant.CloudDrive.StorageManager.Uploads.UploadRecord;
import com.anant.CloudDrive.StorageManager.requests.UploadIdRequest;
import org.slf4j.Logger;
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
public class UploadSession{

    //represents multiple upload entries from a session
     private final ConcurrentHashMap<String, UploadRecord> uploadRecords = new ConcurrentHashMap<>();
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
        var uploadRecord = createRecord(freshUploadId);
        uploadRecord.initUpload(userName, uploadIdRequest);
        logger.info("Created Upload Record for User - {}, Upload Id - {}", userName, freshUploadId);
        return freshUploadId;
     }

     public UploadRecord getRecord(String uploadId){
        return uploadRecords.get(uploadId);
     }

     private UploadRecord createRecord(String uploadId){
        var uploadRecord = context.getBean(UploadRecord.class);
        this.uploadRecords.put(uploadId, uploadRecord);
        return uploadRecord;
     }

     private boolean uploadIdAlreadyExists(String uploadId){
        return uploadRecords.containsKey(uploadId);
     }
}