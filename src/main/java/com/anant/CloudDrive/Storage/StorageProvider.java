package com.anant.CloudDrive.Storage;

import com.anant.CloudDrive.Storage.Models.UserFileMetaData;
import com.anant.CloudDrive.Storage.Models.UploadIdRequest;
import com.anant.CloudDrive.Storage.Models.UploadPartRequest;
import org.springframework.core.io.Resource;

import java.util.List;

public interface StorageProvider<T extends UploadRecord> {
    Resource download(String fileName);
    boolean deleteFile(String fileName);
    boolean renameFile(String oldFileName, String newFileName);

    long getStorageUsedByUser(String userName);

    boolean initializeUpload(String userName, T record, UploadIdRequest req);
    boolean uploadPart(T record, UploadPartRequest req);
    boolean abortUpload(T record);
    boolean completeUpload(T record);

    List<UserFileMetaData> getUserObjectsMetaData(String userName);
}