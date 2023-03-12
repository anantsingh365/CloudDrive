package com.anant.CloudDrive.service;

import com.anant.CloudDrive.requests.UploadIdRequest;
import com.anant.CloudDrive.requests.UploadPartRequest;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface StorageService {

     String getUploadId(UploadIdRequest request);

     boolean upload(UploadPartRequest req);

     boolean completeUpload(String uploadId);

     Resource download(String key);

     List<UserFileMetaData> getUserObjectsMetaData();

     boolean deleteUserFile(String key);

     boolean renameFile(int id);

     long getStorageUsedByUser();

     ResponseEntity<byte[]> getFileBytes(String fileName, String range, String contentType);
}
