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
public class StorageManager{

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
        String generatedUploadID = null;
        if (verifyUserHasSpaceQuotaLeft(userName)) {
            var session = uploadSessionsHolder.getSession(sessionId);
            generatedUploadID = session.createRecord(userName);
            boolean res = storageProvider.initializeUpload(userName, session.getRecord(generatedUploadID), req);
            if (!res) {
                //cleanup
                session.removeRecord(generatedUploadID);
                throw new RuntimeException("Couldn't initalize the upload");
            }
            if(session.getRecord(generatedUploadID).getState() == UploadRecordState.ABORTED){
                // client has aborted the upload
                session.removeRecord(generatedUploadID);
                return "Upload Aborted";
            }
            session.getRecord(generatedUploadID).setState(UploadRecordState.INITIALIZED);
            return generatedUploadID;
        }
        return AccountStates.ACCOUNT_UPGRADE.getValue();
    }

    public boolean uploadPart(final UploadPartRequest_ req, final String sessionId) {
        boolean wasUploadPartSuccess = false;
        UploadRecord record = getExistingUploadRecord(req.getUploadId(), sessionId);
        // we don't have any record for the given uploadID
        if (record == null) {
            return false;
        }
        if(record.getState() == UploadRecordState.COMPLETED
               || record.getState() == UploadRecordState.ABORTED){
            return false;
        }
        // below condition means that uploadId has been generated, and we are now receiving the first chunk/part
        // after the first part we will change the record state to be "IN_PROGRESS", and upload
        // hasn't been aborted by user
        if(record.getState() == UploadRecordState.INITIALIZED
              && record.getPartsUploaded() == 0)
        {
            wasUploadPartSuccess = this.storageProvider.uploadPart(record, req);
            if(wasUploadPartSuccess){
                record.setState(UploadRecordState.IN_PROGRESS);
                record.incrementPartsUploaded();
                return wasUploadPartSuccess;
            }
        }
        // below condition means upload record has been initialized and at least first part has been uploaded
        // record state to be "IN_PROGRESS"
        if(record.getState() == UploadRecordState.IN_PROGRESS
             && record.getPartsUploaded() != 0)
        {
            wasUploadPartSuccess = this.storageProvider.uploadPart(record, req);
            if(wasUploadPartSuccess){
                record.incrementPartsUploaded();
                return wasUploadPartSuccess;
            }
        }
        return false;
    }

    public boolean cancelUpload(final String uploadID, final String userName, final String sessionID){
            boolean isCancelled = false;
            var record = getExistingRecord(sessionID, uploadID);

            //completed upload can't be cancelled dummy
            if(record.getState() == UploadRecordState.COMPLETED){
                return false;
            }
            if(record.getState() == UploadRecordState.ABORTED){
                //doing nothing when aborting already aborted record
                return true;
            }
            isCancelled = this.storageProvider.abortUpload(record);
            if(isCancelled){
                record.setState(UploadRecordState.ABORTED);
                return isCancelled;
            }
        return false;
    }

    private UploadRecord getExistingRecord(String sessionID, String uploadID){
        final var session = this.uploadSessionsHolder.getExistingSession(sessionID);
        if(session == null){
            throw new RuntimeException("No session Associated with the session ID - " + sessionID);
        }
        final var record = session.getRecord(uploadID);
        if(record == null) {
            throw new RuntimeException("No Upload Record Associated with the Upload ID - " + uploadID);
        }
        return record;
    }

    public boolean completeUpload(final String uploadId, final String sessionId) {
        UploadRecord record = getExistingUploadRecord(uploadId, sessionId);
        if(record == null){
            return false;
        }
        if(record.getState() == UploadRecordState.INITIALIZED){
            System.out.println("Invalid Record State, needs to be in progress to be completed");
            return false;
        }
        if(record.getState() == UploadRecordState.COMPLETED){
            System.out.println("Upload Has already been completed, Not calling underlying " +
                    "StorageProvider implementation");
            return true;
        }
        boolean res =  storageProvider.completeUpload(record);
        if(res){
            record.setState(UploadRecordState.COMPLETED);
            return true;
        }
        return false;
    }

    private void removeExistingRecord(String uploadId, String sessionId){
       var session = uploadSessionsHolder.getExistingSession(sessionId);
       session.removeRecord(uploadId);
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

        long storageQuotaInMB = (int) storageProvider.getStorageUsedByUser(null) / 1048576;
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
    public static class UploadSessionsHolder2{
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

//    @Component
//    public static class UploadLifeCycleEventNotifier{
//        private Map<UploadRecord,List<UploadLifeCycleEventListener>> recordListeners = new ConcurrentHashMap<>();
//
//    }

    public static interface UploadLifeCycleEventListener {
        void UploadInitialised(UploadRecord record);
        void PartUpload(UploadRecord record);
        void UploadCompleted(UploadRecord record);
    }

    @Component
    @Qualifier("userUploadSession")
    @Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public static class UploadSession2{

        //represents multiple upload entries from a session
        private final ConcurrentHashMap<String, UploadRecord> uploadRecords = new ConcurrentHashMap<>();
        private final ApplicationContext context;
        private final Logger logger;

        public UploadSession2(@Autowired ApplicationContext context, @Autowired Logger logger){
            this.context = context;
            this.logger = logger;
        }

        public String createRecord(String userName){
            String freshUploadId = UUID.randomUUID().toString();
            if(uploadIdAlreadyExists(freshUploadId)){
                throw new RuntimeException("Couldn't generate a unique uploadId");
            }
            createRecord_(freshUploadId);
            //uploadRecord.initUpload(userName, uploadIdRequest);
            logger.info("Created Upload Record for User - {}, Upload Id - {}", userName, freshUploadId);
            return freshUploadId;
        }

        public UploadRecord getRecord(String uploadId){
            return uploadRecords.get(uploadId);
        }

        private void createRecord_(String uploadId){
            UploadRecord uploadRecord =  context.getBean(UploadRecord.class);
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