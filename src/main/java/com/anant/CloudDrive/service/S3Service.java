package com.anant.CloudDrive.service;

import com.anant.CloudDrive.requests.UploadRequest;
import com.anant.CloudDrive.s3.S3Operations;
import com.anant.CloudDrive.s3.UserUploads.UploadEntry;
import com.anant.CloudDrive.s3.UserUploads.*;

import org.slf4j.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.List;

@Service
public class S3Service implements StorageService {

    private final Logger logger;
    private final String bucketName;
    private final UploadSessionsHolder uploadSessionsHolder;
    private final S3Operations s3Operations;

    public S3Service(@Value("${s3.bucketName}") String bucketName,
                     @Autowired Logger logger,
                     @Autowired UploadSessionsHolder uploadSessionsHolder,
                     @Autowired S3Operations s3Operations)
    {
        this.bucketName = bucketName;
        this.logger = logger;
        this.uploadSessionsHolder = uploadSessionsHolder;
        this.s3Operations = s3Operations;
    }

    @Override
    public String getUploadId(String fileName){
        return this.getUploadSession().registerUploadId(fileName);
    }

    @Override
    public boolean upload(UploadRequest req){
        var session = this.getUploadSession();
        var entry = session.getEntry(req.getUploadId());
        return entry != null && entry.upload(req);
    }

    @Override
    public boolean completeUpload(String uploadId){
        var entry = this.getUserEntry(uploadId);
        return entry != null && entry.completeUserUpload();
    }

    @Override
    public Resource download(String key){
        InputStream s3ObjectInputStream= s3Operations.getS3ObjectInputStream(key);
        return gets3ObjectAsResource(s3ObjectInputStream);
    }

    private Resource gets3ObjectAsResource(InputStream ins){
        return new InputStreamResource(ins);
    }

    @Override
    public List<String> getFilesListing(){
        //get objects for user with username as prefix
        return s3Operations.getUserFileListing();
    }

    @Override
    public void getObjectMetaData(int id){

    }

    @Override
    public boolean deleteUserFile(String key){
        return s3Operations.deleteObject(key);
    }

    @Override
    public boolean renameFile(int id){
        return false;
    }

    private String getLoggedInUserName(){
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    private UploadEntry getUserEntry(String uploadId){
        var session = uploadSessionsHolder.getExistingSession(getLoggedInUserName());
        return session != null ? session.getEntry(uploadId) : null;
    }

    private UploadSession getUploadSession(){
        return uploadSessionsHolder.getSession(getLoggedInUserName());
    }
}