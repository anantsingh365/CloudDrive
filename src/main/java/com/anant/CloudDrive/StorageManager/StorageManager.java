package com.anant.CloudDrive.StorageManager;

import com.anant.CloudDrive.StorageManager.Models.UserFileMetaData;
import com.anant.CloudDrive.StorageManager.Models.UploadIdRequest;
import com.anant.CloudDrive.StorageManager.Models.UploadPartRequest_;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class StorageManager {

    private final ApplicationContext context;

    private final StorageProvider storageProvider;
    private final SubscriptionService subscriptionService;
    private final UploadSessionsHolder2 uploadSessionsHolder;
    private final LocalStorageVideoStreamService videoStreamService;

    public StorageManager(@Autowired ApplicationContext context,
                          @Autowired StorageProvider storageProvider,
                          @Autowired SubscriptionService subscriptionService,
                          @Autowired UploadSessionsHolder2 uploadSessionsHolder, @Autowired LocalStorageVideoStreamService videoStreamService) {
        this.context = context;
        this.storageProvider = storageProvider;
        this.subscriptionService = subscriptionService;
        this.uploadSessionsHolder = uploadSessionsHolder;
        this.videoStreamService = videoStreamService;
    }

    public String getUploadId(final UploadIdRequest req, final String sessionId, final String userName) {
        if (verifyUserHasSpaceQuotaLeft(userName)) {
            var session = uploadSessionsHolder.getSession(sessionId);
            String newUploadId = session.createRecord(userName, req);
            boolean res = storageProvider.initializeUpload(userName, session.getRecord(newUploadId), req);
            if (!res) {
                //cleanup
                session.removeRecord(newUploadId);
                throw new RuntimeException("Couldn't initalize the upload");
            }
            session.getRecord(newUploadId).setState(UploadRecordState.INITIALISED);
            return newUploadId;
        }
        return AccountStates.ACCOUNT_UPGRADE.getValue();
    }

    public boolean uploadPart(final UploadPartRequest_ req, final String sessionId) {
        UploadRecord record = getExistingUploadRecord(req.getUploadId(), sessionId);
        // we don't have any record for the given uploadID
        if (record == null) {
            return false;
        }

        if(record.getState() == UploadRecordState.COMPLETED){
            return false;
        }

        // below condition means that uploadId has been generated, and we are now receiving the first chunk/part
        // after the first part we will change the record state to be "IN_PROGRESS"
        if(record.getState() == UploadRecordState.INITIALISED && record.getPartsUploaded() == 0){
            boolean res = this.storageProvider.uploadPart(record, req);
            if(res){
                record.setState(UploadRecordState.IN_PROGRESS);
                record.incrementPartsUploaded();
                return true;
            }
        }

        // below condition means upload record has been initialized and at least first part has been uploaded
        // record state to be "IN_PROGRESS"
        if(record.getState() == UploadRecordState.IN_PROGRESS && record.getPartsUploaded() != 0){
            boolean res = this.storageProvider.uploadPart(record, req);
            if(res){
                record.incrementPartsUploaded();
                return true;
            }
        }

        return false;
    }

    public boolean completeUpload(final String uploadId, final String sessionId) {
        UploadRecord record = getExistingUploadRecord(uploadId, sessionId);
        if(record == null){
            return false;
        }
        if(record.getState() == UploadRecordState.INITIALISED || record.getState() == UploadRecordState.NOT_INITIALISED){
            System.out.println("Invalid Upload Record State, state either Not initialised or initialised" +
                    " but needs to be in In_Progress only to be eligible for completion");
            return false;
        }
        if(record.getState() == UploadRecordState.COMPLETED){
            System.out.println("Upload Has already been completed, Not calling underlying " +
                    "StorageProvider implementation");
            return true;
        }
        return storageProvider.completeUpload(record);
    }

    private UploadRecord getExistingUploadRecord(final String uploadId, final String sessionId) {
        var session = uploadSessionsHolder.getExistingSession(sessionId);
        if (session == null) {
            return null;
        }
        return session.getRecord(uploadId);
    }

    public Resource download(final String fileName) {
        return storageProvider.download(fileName);
    }

    public List<UserFileMetaData> getUserObjectsMetaData(String userName) {
        return storageProvider.getUserObjectsMetaData(userName);
    }

    public boolean deleteUserFile(final String fileName) {
        return storageProvider.deleteFile(fileName);
    }

    public boolean renameFile(final String originalName, final String newName) {
        return storageProvider.renameFile(originalName, newName);
    }

    public long getStorageUsedByUser(String userName) {
        var userObjectListing = getUserObjectsMetaData(userName);
        long sum = 0;
        for (UserFileMetaData file : userObjectListing) {
            sum += file.getSize();
        }
        return sum;
    }

    public ResponseEntity<byte[]> getBlob(String fileName, String range, String contentType) {
        return videoStreamService.getBlob(fileName, range, contentType);
    }

    private boolean verifyUserHasSpaceQuotaLeft(String userName) {
        String storageTier = subscriptionService.getTier(userName);
        int storageTierInt = Integer.parseInt(storageTier);

        long storageQuotaInMB = (int) storageProvider.getStorageUsedByUser() / 1048576;
        if (storageQuotaInMB < storageTierInt) {
            System.out.println("User has space quota left");
            return true;
        }
        System.out.println("User has exhausted the space quota");
        return false;
    }

    public enum AccountStates {
        ACCOUNT_UPGRADE("Account Upgrade"), ACCOUNT_BLOCKED("Account Blocked");
        private final String value;

        AccountStates(String value) {
            this.value = value;
        }

        public String getValue() {
            return this.value;
        }

        @Override
        public String toString() {
            return this.getValue();
        }
    }

    @Component
    private static class UploadSessionsHolder2{
        private final ApplicationContext context;
        private final Logger logger;
        private final ConcurrentHashMap<String, UploadSession2> sessions = new ConcurrentHashMap<>();

        // one session ID --has---> one upload Session --has---> multiple Upload Records
        public UploadSessionsHolder2(@Autowired ApplicationContext context,@Autowired Logger logger) {
            this.context = context;
            this.logger = logger;
        }

        public UploadSession2 getSession(String sessionId){
            var userSession = getExistingSession(sessionId);
            if(userSession == null){
                return createNewSession(sessionId);
            }
            return userSession;
        }

        public UploadSession2 getExistingSession(String sessionId){
            return sessions.get(sessionId);
        }

        private UploadSession2 createNewSession(String userName){
            var uploadSession = context.getBean(UploadSession2.class);
            sessions.put(userName, uploadSession);
            return uploadSession;
        }
    }

    @Component
    @Qualifier("userUploadSession")
    @Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    private static class UploadSession2{

        //represents multiple upload entries from a session
        private final ConcurrentHashMap<String, UploadRecord> uploadRecords = new ConcurrentHashMap<>();
        private final ApplicationContext context;
        private final Logger logger;

        public UploadSession2(@Autowired ApplicationContext context, @Autowired Logger logger){
            this.context = context;
            this.logger = logger;
        }

        public String createRecord(String userName, UploadIdRequest uploadIdRequest){
            String freshUploadId = UUID.randomUUID().toString();
            if(uploadIdAlreadyExists(freshUploadId)){
                throw new RuntimeException("Couldn't generate a unique uploadId");
            }
            createRecord(freshUploadId);
            //uploadRecord.initUpload(userName, uploadIdRequest);
            logger.info("Created Upload Record for User - {}, Upload Id - {}", userName, freshUploadId);
            return freshUploadId;
        }

        public UploadRecord getRecord(String uploadId){
            return uploadRecords.get(uploadId);
        }

        private void createRecord(String uploadId){
            var uploadRecord = context.getBean(UploadRecord.class);
            this.uploadRecords.put(uploadId, uploadRecord);
        }

        public void removeRecord(String uploadId){
            this.uploadRecords.remove(uploadId);
        }

        private boolean uploadIdAlreadyExists(String uploadId){
            return uploadRecords.containsKey(uploadId);
        }
    }
}