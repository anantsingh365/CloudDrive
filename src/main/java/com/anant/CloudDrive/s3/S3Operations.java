package com.anant.CloudDrive.s3;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import com.anant.CloudDrive.requests.UploadRequest;
import com.anant.CloudDrive.s3.UserUploads.UploadEntry;
import com.anant.CloudDrive.service.UserFileMetaData;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Component
public class S3Operations {

    private final AmazonS3 s3Client;
    private final Logger logger;
    private final String bucketName;

    public S3Operations(@Autowired AmazonS3 s3Client,
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

    protected  List<UserFileMetaData> getUserObjectsMetaData(String key){

        // forward slash "/" is used to represent as folder in s3, there is no concept
        // of actual folders in s3. each user has a folder named after their username + "/".

        List<UserFileMetaData> list = new ArrayList<>();
        s3Client.listObjectsV2(bucketName, key+"/")
                .getObjectSummaries().forEach(x -> list.add(new UserFileMetaData(x.getKey(), x.getSize(), x.getLastModified())));
          return list;
    }

    protected S3ObjectInputStream getS3ObjectInputStream(String keyName){
        return s3Client
                .getObject(bucketName, keyName)
                .getObjectContent();
    }

    protected S3ObjectInputStream getRangedS3ObjectInputStream(String keyName, long start, long end){
        GetObjectRequest getObjectRequest = new GetObjectRequest(bucketName, keyName).withRange(start,end);
        S3Object object = s3Client.getObject(getObjectRequest);
        return object.getObjectContent();
    }

    protected boolean uploadFile(UploadEntry entry, UploadRequest req){
        return entry.upload(req);
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

    protected boolean completeUserUpload(UploadEntry entry){
        return entry.completeUserUpload();
    }

    protected boolean renameFile(){
        return false;
    }
}
