package com.anant.CloudDrive.StorageManager.Uploads;

import com.anant.CloudDrive.StorageManager.requests.UploadIdRequest;
import com.anant.CloudDrive.StorageManager.requests.UploadPartRequest_;

public interface UploadRecord {
    void initUpload(String userName, UploadIdRequest uploadIdRequest);
    boolean uploadPart(UploadPartRequest_ uploadPartRequest);
    boolean completeUserUpload();
}