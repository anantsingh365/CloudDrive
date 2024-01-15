package com.anant.CloudDrive.StorageManager;

import com.anant.CloudDrive.StorageManager.Models.UserFileMetaData;
import com.anant.CloudDrive.StorageManager.Models.UploadIdRequest;
import com.anant.CloudDrive.StorageManager.Models.UploadPartRequest_;
import org.springframework.core.io.Resource;

import java.util.List;

public interface StorageProvider {
    Resource download(String fileName);
    boolean deleteFile(String fileName);
    boolean renameFile(String oldFileName, String newFileName);

    long getStorageUsedByUser();

    boolean initializeUpload(String userName, UploadRecord record, UploadIdRequest req);
    boolean uploadPart(UploadRecord record, UploadPartRequest_ req);
    boolean completeUpload(UploadRecord record);

    List<UserFileMetaData> getUserObjectsMetaData(String userName);
}
