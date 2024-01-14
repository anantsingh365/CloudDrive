package com.anant.CloudDrive.StorageProviders;

import com.anant.CloudDrive.StorageProviders.Uploads.UploadEntry;
import com.anant.CloudDrive.Utils.CommonUtils;
import com.anant.CloudDrive.UploadManager.UploadSession;
import com.anant.CloudDrive.UploadManager.UploadSessionsHolder;
import com.anant.CloudDrive.StorageProviders.requests.*;

import com.anant.CloudDrive.service.SubscriptionService;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static com.anant.CloudDrive.Utils.CommonUtils.getUserData;

public abstract class StorageService {

     private final UploadSessionsHolder uploadSessionsHolder;
     private final SubscriptionService subscriptionService;

     public StorageService(UploadSessionsHolder uploadSessionsHolder, SubscriptionService subscriptionService) {
          this.uploadSessionsHolder = uploadSessionsHolder;
          this.subscriptionService = subscriptionService;
     }
     abstract public boolean uploadPart(com.anant.CloudDrive.StorageProviders.requests.UploadPartRequest req);

     abstract public boolean completeUpload(String uploadId);

     abstract public Resource download(String key);

     abstract public List<UserFileMetaData> getUserObjectsMetaData();

     abstract public boolean deleteUserFile(String key);

     abstract public boolean renameFile(String originalName, String newName);

     abstract public long getStorageUsedByUser();

     abstract public ResponseEntity<byte[]> getFileBytes(String fileName, String range, String contentType);

     public String getUploadId(UploadIdRequest uploadIdRequest){
          if(validateUploadRequestTier()){
               return this.getUploadSession().registerUploadId(CommonUtils.getUserData(CommonUtils.signedInUser.GET_USERNAME), uploadIdRequest);
          }
          return StorageService.AccountStates.ACCOUNT_UPGRADE.getValue();
     }

     public UploadEntry getExistingUserEntry(String uploadId){
          var session = uploadSessionsHolder.getExistingSession(getUserData(CommonUtils.signedInUser.GET_SESSIONID));
          return session != null ? session.getPart(uploadId) : null;
     }

     private  UploadSession getUploadSession(){
          return uploadSessionsHolder.getSession(getUserData(CommonUtils.signedInUser.GET_SESSIONID));
     }

     public boolean validateUploadRequestTier(){
          String storageTier = subscriptionService.getTier(getUserData(CommonUtils.signedInUser.GET_USERNAME));
          int storageTierInMB = Integer.parseInt(storageTier);

          long storageQuotaInMB = (int) getStorageUsedByUser()/1048576;
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
