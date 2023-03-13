package com.anant.CloudDrive.LocalFileSystem;

import com.anant.CloudDrive.Utils.CommonUtils;
import com.anant.CloudDrive.s3.UserUploads.S3UploadEntry;
import com.anant.CloudDrive.service.StorageService;
import com.anant.CloudDrive.service.Uploads.UploadEntry;
import com.anant.CloudDrive.service.Uploads.UploadSession;
import com.anant.CloudDrive.service.Uploads.UploadSessionsHolder;
import com.anant.CloudDrive.service.UserFileMetaData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;

import com.anant.CloudDrive.service.Uploads.requests.*;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.anant.CloudDrive.Utils.CommonUtils.getUserData;

@Service
@Profile("local")
public class FileSystemService implements StorageService {

    private final UploadSessionsHolder uploadSessionsHolder;
    private final FileSystemOperations fileSystemOperations;

    public FileSystemService(@Autowired UploadSessionsHolder uploadSessionsHolder,
                             @Autowired FileSystemOperations fileSystemOperations) {
        this.uploadSessionsHolder = uploadSessionsHolder;
        this.fileSystemOperations = fileSystemOperations;
    }

    @Override
    public String getUploadId(UploadIdRequest request) {

        return this.getUploadSession().registerUploadId(request);
        //testing
        //return "abcd123";
    }

    @Override
    public boolean uploadPart(UploadPartRequest req) {
        //testing

        var session = this.getExistingSession(CommonUtils.getUserData(CommonUtils.signedInUser.GET_SESSIONID));
        var entry =  session.getEntry(req.getUploadId());
        return entry != null && fileSystemOperations.uploadFile(req);
    }

    @Override
    public boolean completeUpload(String uploadId) {
        return true;
    }

    @Override
    public Resource download(String key) {
        return null;
    }

    @Override
    public List<UserFileMetaData> getUserObjectsMetaData() {
         List<UserFileMetaData> fileMetaData = new ArrayList<UserFileMetaData>();
         fileMetaData.add(new UserFileMetaData("testFile", 1000000L, new Date(), "test/type"));
         return fileMetaData;
    }

    @Override
    public boolean deleteUserFile(String key) {
        return true;
    }

    @Override
    public boolean renameFile(String originalName, String newName) {
        return true;
    }

    @Override
    public long getStorageUsedByUser() {
        return 0;
    }

    @Override
    public ResponseEntity<byte[]> getFileBytes(String fileName, String range, String contentType) {
        return null;
    }

    private UploadEntry getUserEntry(String uploadId){
        var session = uploadSessionsHolder.getExistingSession(getUserData(CommonUtils.signedInUser.GET_SESSIONID));
        return session != null ? session.getEntry(uploadId) : null;
    }

    private UploadSession getUploadSession(){
        return uploadSessionsHolder.getSession(getUserData(CommonUtils.signedInUser.GET_SESSIONID));
    }
    private UploadSession getExistingSession(String sessionId){
        return uploadSessionsHolder.getExistingSession(sessionId);
    }
}
