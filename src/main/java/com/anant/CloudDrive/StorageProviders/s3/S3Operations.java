package com.anant.CloudDrive.StorageProviders.s3;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;

import com.anant.CloudDrive.StorageProviders.requests.UploadPartRequest_;
import com.anant.CloudDrive.StorageProviders.UserFileMetaData;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@Profile("s3")
public class S3Operations {

    private final AmazonS3 s3Client;
    private final Logger logger;
    private final String bucketName;

    protected S3Operations(@Autowired AmazonS3 s3Client,
                          @Autowired Logger logger,
                          @Value("${s3.bucketName}") String bucketName)
    {
        this.s3Client = s3Client;
        this.logger = logger;
        this.bucketName = bucketName;
    }

    protected void downloadFile(String keyName){
        S3Object o = s3Client.getObject(bucketName, keyName + "/");
    }

    protected List<UserFileMetaData> getUserObjectsMetaData(String key){

        // forward slash "/" is used to represent as folder in s3, there is no concept
        // of actual folders in s3. each user has a folder named after their username + "/".

        List<UserFileMetaData> list = new ArrayList<>();

        s3Client.listObjectsV2(bucketName, key+"/")
                .getObjectSummaries().forEach(x -> list.add(
                                    new UserFileMetaData(x.getKey(),
                                        x.getSize(),
                                        x.getLastModified(),
                                        s3Client.getObjectMetadata(bucketName, x.getKey()).getContentType())
                                        //getContentType(x.getKey()))
                                    ));
          return list;
    }
    protected S3ObjectInputStream getS3ObjectInputStream(String keyName){
        return s3Client
                .getObject(bucketName, keyName)
                .getObjectContent();
    }

    protected boolean uploadFile(S3UploadEntry entry, UploadPartRequest_ req){
        return entry.uploadPart(req);
    }

    protected boolean deleteObject(String keyName){
        //DeleteObjectRequest del = new DeleteObjectRequest();
        try{
            s3Client.deleteObject(bucketName, keyName);
            return true;
        }catch(AmazonServiceException e){
            System.err.println(e.getErrorMessage());
            return false;
        }
    }

    protected void s3Objects(){
        var filteredList = s3Client.listObjectsV2(null,null).getObjectSummaries().stream().filter( x -> {
            var key = x.getKey();
            var metaData = s3Client.getObjectMetadata(bucketName, key);
            return metaData.getContentType().equals("media");
        }).toList();
    }

    protected boolean completeUserUpload(S3UploadEntry entry){
        return entry.completeUserUpload();
    }

    // in s3 you have to copy a object with new name and delete the original to rename it.
    protected boolean renameFile(String originalKeyName, String newKeyName){
        CopyObjectRequest copyObjRequest = new CopyObjectRequest(bucketName,
                originalKeyName, bucketName, newKeyName);
        try{
            var result = s3Client.copyObject(copyObjRequest);
            s3Client.deleteObject(new DeleteObjectRequest(bucketName, originalKeyName));
            return true;
        }catch(Exception e){
            e.printStackTrace();
            return false;
        }
    }
}
