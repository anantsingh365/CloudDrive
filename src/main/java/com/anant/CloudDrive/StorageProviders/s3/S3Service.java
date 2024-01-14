package com.anant.CloudDrive.StorageProviders.s3;

import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.anant.CloudDrive.StorageProviders.LocalStorageVideoStreamService;
import com.anant.CloudDrive.StorageProviders.StorageService;
import com.anant.CloudDrive.StorageProviders.UserFileMetaData;
import com.anant.CloudDrive.Utils.CommonUtils;

import com.anant.CloudDrive.StorageProviders.requests.*;
import com.anant.CloudDrive.service.*;
import com.anant.CloudDrive.UploadManager.UploadSessionsHolder;
import org.slf4j.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import static com.anant.CloudDrive.Utils.CommonUtils.getUserData;

@Service
@Profile("s3")
public class S3Service extends StorageService {

    private final Logger logger;
    private final String bucketName;
    private final UploadSessionsHolder uploadSessionsHolder;
    private final S3Operations s3Operations;
    private final SubscriptionService subscriptionService;
    @Autowired private LocalStorageVideoStreamService videoStreamService;

    private final ConcurrentHashMap<String, List<UserFileMetaData>> savedFileListing = new ConcurrentHashMap<>();

    public S3Service(@Value("${s3.bucketName}") String bucketName,
                     @Autowired Logger logger,
                     @Autowired UploadSessionsHolder uploadSessionsHolder,
                     @Autowired S3Operations s3Operations,
                     @Autowired SubscriptionService subscriptionService)
    {
        super(uploadSessionsHolder,subscriptionService);
        this.bucketName = bucketName;
        this.logger = logger;
        this.uploadSessionsHolder = uploadSessionsHolder;
        this.s3Operations = s3Operations;
        this.subscriptionService = subscriptionService;
    }

    @Override
    public boolean uploadPart(UploadPartRequest_ req){
       // if(super.validateUploadRequestTier()){
            var entry = (S3UploadEntry) super.getExistingUserEntry(req.getUploadId());
            return entry != null && s3Operations.uploadFile(entry, req);
      //  }
       // return false;
    }

    @Override
    public boolean completeUpload(String uploadId){
        var entry = super.getExistingUserEntry(uploadId);
        return entry != null && s3Operations.completeUserUpload((S3UploadEntry) entry);
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

    public boolean deleteAllUserFile(){
        String userName = "tempUser";
        var userFilesMetaData = getUserObjectsMetaData();
        boolean res;
        for (UserFileMetaData userFileMetaData : userFilesMetaData) {
            res = this.deleteUserFile(userFileMetaData.getName());
            if(!res){
                System.out.println("couldn't delete some or all files");
            }
        }
        return true;
    }

    @Override
    public boolean renameFile(String originalFileName, String newFileName){
        return s3Operations.renameFile(originalFileName, CommonUtils.getUserData(CommonUtils.signedInUser.GET_USERNAME) +"/" + newFileName);
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
       // String temp = "this is Hello  accidentally  ";
//        try {
//            return s3Operations.getRangedS3ObjectInputStream(key_name,start,end).readAllBytes();
//        } catch (IOException e) {
//            e.printStackTrace();
//            throw new RuntimeException("Couldn't Read Ranged S3 Input Stream");
//        }
    }
}