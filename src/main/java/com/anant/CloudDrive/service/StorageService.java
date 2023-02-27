package com.anant.CloudDrive.service;

import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.anant.CloudDrive.requests.UploadRequest;
import org.springframework.core.io.Resource;

import java.util.List;

public interface StorageService {

     String getUploadId(String fileName);

     boolean upload(UploadRequest req);

     boolean completeUpload(String uploadId);

     Resource download(String key);

     List<UserFileMetaData> getUserObjectsMetaData();

     boolean deleteUserFile(String key);

     boolean renameFile(int id);

     long getStorageUsedByUser();
}
