package com.anant.CloudDrive.s3;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.S3Object;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;

class S3FileDownload {

    private final AmazonS3 s3Client;
    private final Logger logger;
    private final String bucketName;

    public S3FileDownload(@Autowired  AmazonS3 s3Client,
                          @Autowired Logger logger,
                          @Value("${s3.bucketName}") String bucketName)
    {
        this.s3Client = s3Client;
        this.logger = logger;
        this.bucketName = bucketName;
    }
    private String setUserNameFolderPrefix(){
        return SecurityContextHolder.getContext().getAuthentication().getName() + "/";
    }
    public void downloadFile(){
        S3Object o = s3Client.getObject(bucketName, setUserNameFolderPrefix());
    }
}
