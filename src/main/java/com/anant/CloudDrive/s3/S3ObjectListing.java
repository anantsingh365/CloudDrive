package com.anant.CloudDrive.s3;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.ListBucketsRequest;
import com.amazonaws.services.s3.model.ListObjectsV2Result;
import com.amazonaws.services.s3.model.ObjectListing;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class S3ObjectListing {

    private final AmazonS3 s3Client;
    private final Logger logger;
    private final String bucketName;

    public S3ObjectListing(@Value("{$s3.bucketName}") String bucketName,
                           @Autowired AmazonS3 s3Client,
                           @Autowired Logger logger)
    {
        this.bucketName = bucketName;
        this.s3Client = s3Client;
        this.logger = logger;
    }
    private String setUserNameFolderPrefix(){

        return SecurityContextHolder.getContext().getAuthentication().getName() + "/";
    }
    public ListObjectsV2Result getUserFiles(){
        ListObjectsV2Result list = s3Client.listObjectsV2(bucketName, setUserNameFolderPrefix());
        list.getObjectSummaries().forEach(System.out::println);
        return list;
    }
}
