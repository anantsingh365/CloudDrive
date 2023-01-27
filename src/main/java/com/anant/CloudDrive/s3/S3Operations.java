package com.anant.CloudDrive.s3;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.S3Object;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.io.InputStream;
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

    private String getUserNameFolderPrefix(){
        return getLoggedInUserName() + "/";
    }
    private String getLoggedInUserName() {
        return SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();
    }
    public void downloadFile(){
        S3Object o = s3Client.getObject(bucketName, getUserNameFolderPrefix());
    }
    public List<String> getUserFileListing(){

        // forward slash "/" is used to represent as folder in s3, there is no concept
        // of actual folders in s3. each user has a folder named after their username + "/".
        return s3Client.listObjectsV2(bucketName, getLoggedInUserName()+"/")
                .getObjectSummaries().stream()
                    .map(
                        elem -> elem.getKey()
                                .substring((getLoggedInUserName()+"/")
                                .length())
                        )
                .toList();
    }
    public InputStream getS3ObjectInputStream(String keyName){
        return s3Client
                .getObject(bucketName, keyName)
                .getObjectContent();
    }
}
