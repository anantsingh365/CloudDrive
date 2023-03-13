package com.anant.CloudDrive.s3;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.anant.CloudDrive.Utils.CommonUtils;

import com.anant.CloudDrive.s3.UserUploads.S3UploadEntry;
import com.anant.CloudDrive.service.Uploads.requests.*;
import com.anant.CloudDrive.service.*;
import com.anant.CloudDrive.service.Uploads.UploadSession;
import com.anant.CloudDrive.service.Uploads.UploadSessionsHolder;
import org.slf4j.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import static com.anant.CloudDrive.Utils.CommonUtils.getUserData;

@Service
public class S3Service implements StorageService{

    private final Logger logger;
    private final String bucketName;
    private final UploadSessionsHolder uploadSessionsHolder;
    private final S3Operations s3Operations;
    private final SubscriptionService subscriptionService;
    @Autowired private AmazonS3 s3;
    @Autowired private LocalStorageVideoStreamService videoStreamService;

    private final ConcurrentHashMap<String, List<UserFileMetaData>> savedFileListing = new ConcurrentHashMap<>();

    public S3Service(@Value("${s3.bucketName}") String bucketName,
                     @Autowired Logger logger,
                     @Autowired UploadSessionsHolder uploadSessionsHolder,
                     @Autowired S3Operations s3Operations,
                     @Autowired SubscriptionService subscriptionService)
    {
        this.bucketName = bucketName;
        this.logger = logger;
        this.uploadSessionsHolder = uploadSessionsHolder;
        this.s3Operations = s3Operations;
        this.subscriptionService = subscriptionService;
    }

    @Override
    public String getUploadId(UploadIdRequest uploadIdRequest){
        if(validateUploadRequestTier()){
            return this.getUploadSession().registerUploadId(uploadIdRequest);
        }
        return AccountStates.ACCOUNT_UPGRADE.getValue();
    }
    @Override
    public boolean uploadPart(UploadPartRequest req){
       // if(validateUploadRequestTier()){
            var session = this.getUploadSession();
            var entry = (S3UploadEntry) session.getEntry(req.getUploadId());
            return entry != null && s3Operations.uploadFile(entry, req);
        //}
       // return false;
    }

    @Override
    public boolean completeUpload(String uploadId){
        var entry = this.getUserEntry(uploadId);
        return entry != null && s3Operations.completeUserUpload(entry);
    }

    @Override
    public Resource download(String key){
        S3ObjectInputStream s3ObjectInputStream = s3Operations.getS3ObjectInputStream(key);
        return new InputStreamResource(s3ObjectInputStream);
    }

    @Override
    public List<UserFileMetaData> getUserObjectsMetaData(){
        //get objects for user with username as prefix
        String userName = getUserData(CommonUtils.signedInUser.GET_USERNAME);
       // if(savedFileListing.get(userName) == null){
            System.out.println("Generating new File Listing");
            var fileListing = s3Operations.getUserObjectsMetaData(userName);
           // savedFileListing.put(Objects.requireNonNull(userName), fileListing);
            return fileListing;
     //   }
      //  System.out.println("Using Saved File Listing");
      //  return s3Operations.getUserObjectListing(userName);
    }

    @Override
    public boolean deleteUserFile(String key){
        boolean result = s3Operations.deleteObject(key);
       // if(result){
        //    System.out.println("Object deleted, removing saved fileListing");
        //    savedFileListing.remove(Objects.requireNonNull(getUserData(signedInUser.GET_USERNAME)));
        //}
        return result;
    }

    @Override
    public boolean renameFile(String originalFileName, String newFileName){
        return s3Operations.renameFile(originalFileName, newFileName);
    }

    @Override
    public long getStorageUsedByUser() {
        var userObjectListing = getUserObjectsMetaData();
        long sum=0;
        for(UserFileMetaData file: userObjectListing){
            sum += file.getSize();
        }
        return sum;
    }

    @Override
    public ResponseEntity<byte[]> getFileBytes(String key_name, String range, String contentType){
        return videoStreamService.prepareContent(key_name, range, contentType);
//        try {
//            return s3Operations.getRangedS3ObjectInputStream(key_name,start,end).readAllBytes();
//        } catch (IOException e) {
//            e.printStackTrace();
//            throw new RuntimeException("Couldn't Read Ranged S3 Input Stream");
//        }
    }

    private S3UploadEntry getUserEntry(String uploadId){
        var session = uploadSessionsHolder.getExistingSession(getUserData(CommonUtils.signedInUser.GET_SESSIONID));
        return session != null ? (S3UploadEntry) session.getEntry(uploadId) : null;
    }

    private UploadSession getUploadSession(){
        return uploadSessionsHolder.getSession(getUserData(CommonUtils.signedInUser.GET_SESSIONID));
    }

    private boolean validateUploadRequestTier(){
        String storageTier = subscriptionService.getTier(getUserData(CommonUtils.signedInUser.GET_USERNAME));
        int storageTierInMB = Integer.parseInt(storageTier);

        // to get MB from bytes divide by 1024*1024 i.e, (1048576)
        long storageQuotaInMB = (int) getStorageUsedByUser()/1048576;
        if(storageQuotaInMB < storageTierInMB){
            System.out.println("User Upload has valid tier");
            return true;
        }
        System.out.println("User Upload has invalid tier");
        return false;
    }

    private enum AccountStates{
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