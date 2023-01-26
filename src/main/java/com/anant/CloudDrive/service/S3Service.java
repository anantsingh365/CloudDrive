package com.anant.CloudDrive.service;

import com.amazonaws.services.s3.AmazonS3;
import com.anant.CloudDrive.requests.UploadRequest;
import com.anant.CloudDrive.s3.S3MultiPartUpload;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import com.anant.CloudDrive.UserUploads.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class S3Service {

    private final AmazonS3 s3Client;
    private final Logger logger;
    private final String bucketName;
    private final UploadSessionsHolder uploadSessionsHolder;

    public S3Service(@Value("{$s3.bucketName}") String bucketName,
                     @Autowired AmazonS3 s3Client,
                     @Autowired Logger logger,
                     @Autowired UploadSessionsHolder uploadSessionsHolder)
    {
        this.bucketName = bucketName;
        this.s3Client = s3Client;
        this.logger = logger;
        this.uploadSessionsHolder = uploadSessionsHolder;
    }

    public void getUploadId(){


    }

    public boolean upload(UploadRequest req){
        var session = uploadSessionsHolder.getSession(getLoggedInUserName());
        var entry = session.getEntry(req.getUploadId());
        if(entry == null){
            return false;
        }
        entry.upload(req);
        return true;
    }

    public boolean completeUpload(String uploadId){
        var entry = getUserEntry(uploadId);
        return entry != null && entry.completeUserUpload();
    }

    public void download(){


    }

    public void getUserListing(){


    }

    public void getObjectMetadata(){


    }

    public void deleteUserfile(){


    }

    public void renameFile(){


    }
    private String getLoggedInUserName(){
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }
    private S3MultiPartUpload getUserEntry(String uploadId){
        var session = uploadSessionsHolder.getExistingSession(getLoggedInUserName());
        return session != null ? session.getEntry(uploadId) : null;
    }
}
