package com.anant.CloudDrive.StorageManager;

import com.anant.CloudDrive.StorageManager.Uploads.UploadRecord;
import com.anant.CloudDrive.StorageManager.requests.UploadIdRequest;
import com.anant.CloudDrive.StorageManager.requests.UploadPartRequest_;
import com.anant.CloudDrive.Utils.CommonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.anant.CloudDrive.Utils.CommonUtils.getUserData;

@Service
public class StorageManager {

    final ApplicationContext context;

    private final BaseStorageProvider storageProvider;
    private final SubscriptionService subscriptionService;
    private final UploadSessionsHolder uploadSessionsHolder;
    private final LocalStorageVideoStreamService videoStreamService;

    StorageManager(@Autowired ApplicationContext context,
                   @Autowired BaseStorageProvider storageProvider,
                   @Autowired SubscriptionService subscriptionService,
                   @Autowired UploadSessionsHolder uploadSessionsHolder, LocalStorageVideoStreamService videoStreamService)
    {
        this.context = context;
        this.storageProvider = storageProvider;
        this.subscriptionService = subscriptionService;
        this.uploadSessionsHolder= uploadSessionsHolder;
        this.videoStreamService = videoStreamService;
    }

    public String getUploadId(final UploadIdRequest req, final String sessionId, final String userName){
        if(verifyUserHasSpaceQuotaLeft()){
            //TO DO - refactor upload session to accept newUploadRequestClass
            return this.uploadSessionsHolder.getSession(sessionId).registerUploadId(userName, req);
        }
        return AccountStates.ACCOUNT_UPGRADE.getValue();
    }

    public boolean uploadPart(final UploadPartRequest_ req){
        UploadRecord record = getExistingUploadRecord(req.getUploadId());
        if(record == null){
           return false;
        }
        return this.storageProvider.uploadPart(record, req);
    }

    public boolean completeUpload(final String uploadId){
        UploadRecord entry = getExistingUploadRecord(uploadId);
        return storageProvider.completeUpload(entry);
    }

    private UploadRecord getExistingUploadRecord(final String uploadId ){
       UploadSession session = uploadSessionsHolder.getExistingSession(getUserData(CommonUtils.signedInUser.GET_SESSIONID));
       if(session == null){
           return null;
       }
       return session.getRecord(uploadId);
    }

    public Resource download(final String fileName){
        return storageProvider.download(fileName);
    }

    public List<UserFileMetaData> getUserObjectsMetaData(String userName){
        return storageProvider.getUserObjectsMetaData(userName);
    }

    public boolean deleteUserFile(final String fileName){
        return storageProvider.deleteFile(fileName);
    }

    public boolean renameFile(final String originalName, final String newName){
        return storageProvider.renameFile(originalName, newName);
    }

    public long getStorageUsedByUser(String userName){
        var userObjectListing = getUserObjectsMetaData(userName);
        long sum=0;
        for(UserFileMetaData file: userObjectListing){
            sum += file.getSize();
        }
        return sum;
    }

    public ResponseEntity<byte[]> getBlob(String fileName, String range, String contentType){
       return videoStreamService.getBlob(fileName, range, contentType);
    }

    private boolean verifyUserHasSpaceQuotaLeft(){
        String storageTier = subscriptionService.getTier(getUserData(CommonUtils.signedInUser.GET_USERNAME));
        int storageTierInMB = Integer.parseInt(storageTier);

        long storageQuotaInMB = (int) storageProvider.getStorageUsedByUser()/1048576;
        if(storageQuotaInMB < storageTierInMB){
            System.out.println("User Upload has valid tier");
            return true;
        }
        System.out.println("User Upload has invalid tier");
        return false;
    }

    public enum AccountStates{
        ACCOUNT_UPGRADE("Account Upgrade"), ACCOUNT_BLOCKED("Account Blocked");
        private final String value;

        AccountStates(String value){
            this.value = value;
        }
        public String getValue(){
            return this.value;
        }
        @Override
        public String toString() {
            return this.getValue();
        }
    }
}