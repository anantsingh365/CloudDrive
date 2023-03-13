package com.anant.CloudDrive.service;

import com.anant.CloudDrive.service.Uploads.requests.*;

import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface StorageService {

     String getUploadId(UploadIdRequest request);

     boolean uploadPart(UploadPartRequest req);

     boolean completeUpload(String uploadId);

     Resource download(String key);

     List<UserFileMetaData> getUserObjectsMetaData();

     boolean deleteUserFile(String key);

     boolean renameFile(String originalName, String newName);

     long getStorageUsedByUser();

     ResponseEntity<byte[]> getFileBytes(String fileName, String range, String contentType);
}
