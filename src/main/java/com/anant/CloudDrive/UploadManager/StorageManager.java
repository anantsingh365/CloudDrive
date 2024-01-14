package com.anant.CloudDrive.UploadManager;

import com.anant.CloudDrive.StorageProviders.BaseStorageProvider;
import com.anant.CloudDrive.StorageProviders.LocalStorageVideoStreamService;
import com.anant.CloudDrive.StorageProviders.StorageService;
import com.anant.CloudDrive.StorageProviders.Uploads.UploadEntry;
import com.anant.CloudDrive.StorageProviders.UserFileMetaData;
import com.anant.CloudDrive.StorageProviders.requests.UploadIdRequest;
import com.anant.CloudDrive.StorageProviders.requests.UploadPartRequest;
import com.anant.CloudDrive.Utils.CommonUtils;
import com.anant.CloudDrive.service.SubscriptionService;
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
        return StorageService.AccountStates.ACCOUNT_UPGRADE.getValue();
    }

    public boolean uploadPart(final UploadPartRequest req){
        UploadEntry entry = getExistingUserEntry(req.getUploadId());
        if(entry == null){
           return false;
        }
        return this.storageProvider.uploadPart(entry, req);
    }

    public boolean completeUpload(final String uploadId){
        UploadEntry entry = getExistingUserEntry(uploadId);
        return storageProvider.completeUpload(entry);
    }

    private UploadEntry getExistingUserEntry(final String uploadId ){
       UploadSession session = uploadSessionsHolder.getExistingSession(getUserData(CommonUtils.signedInUser.GET_SESSIONID));
       if(session == null){
           return null;
       }
       return session.getPart(uploadId);
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
       return videoStreamService.prepareContent(fileName, range, contentType);
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
}
