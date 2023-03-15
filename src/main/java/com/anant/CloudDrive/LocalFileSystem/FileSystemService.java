package com.anant.CloudDrive.LocalFileSystem;

import com.anant.CloudDrive.Utils.CommonUtils;
import com.anant.CloudDrive.s3.UserUploads.S3UploadEntry;
import com.anant.CloudDrive.service.StorageService;
import com.anant.CloudDrive.service.SubscriptionService;
import com.anant.CloudDrive.service.Uploads.UploadEntry;
import com.anant.CloudDrive.service.Uploads.UploadSession;
import com.anant.CloudDrive.service.Uploads.UploadSessionsHolder;
import com.anant.CloudDrive.service.UserFileMetaData;
import jakarta.transaction.NotSupportedException;
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
public class FileSystemService extends StorageService {

    private final FileSystemOperations fileSystemOperations;

    public FileSystemService(@Autowired UploadSessionsHolder uploadSessionsHolder, @Autowired SubscriptionService subscriptionService, @Autowired FileSystemOperations fileSystemOperations) {
        super(uploadSessionsHolder, subscriptionService);
        this.fileSystemOperations = fileSystemOperations;
    }


    @Override
    public boolean uploadPart(UploadPartRequest req) {
        //testing
        var entry = super.getExistingUserEntry(req.getUploadId());
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
}
