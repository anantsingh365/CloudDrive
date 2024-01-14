package com.anant.CloudDrive.UploadManager;

import com.anant.CloudDrive.UploadManager.Uploads.UploadEntry;
import com.anant.CloudDrive.UploadManager.requests.UploadPartRequest_;
import org.springframework.core.io.Resource;

import java.util.List;

public interface BaseStorageProvider{
    Resource download(String fileName);
    boolean deleteFile(String fileName);
    boolean renameFile(String oldFileName, String newFileName);
    long getStorageUsedByUser();
    boolean uploadPart(UploadEntry entry, UploadPartRequest_ req);
    boolean completeUpload(UploadEntry entry);
    List<UserFileMetaData> getUserObjectsMetaData(String userName);
}
