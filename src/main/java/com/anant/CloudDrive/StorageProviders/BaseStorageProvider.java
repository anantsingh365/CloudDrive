package com.anant.CloudDrive.StorageProviders;

import com.anant.CloudDrive.StorageProviders.Uploads.UploadEntry;
import com.anant.CloudDrive.StorageProviders.requests.UploadPartRequest;
import org.springframework.core.io.Resource;

import java.util.List;

public interface BaseStorageProvider{
    Resource download(String fileName);
    boolean deleteFile(String fileName);
    boolean renameFile(String oldFileName, String newFileName);
    long getStorageUsedByUser();
    boolean uploadPart(UploadEntry entry, UploadPartRequest req);
    boolean completeUpload(UploadEntry entry);
    List<UserFileMetaData> getUserObjectsMetaData(String userName);
}
