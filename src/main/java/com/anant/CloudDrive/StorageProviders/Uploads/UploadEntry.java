package com.anant.CloudDrive.StorageProviders.Uploads;

import com.anant.CloudDrive.StorageProviders.requests.UploadIdRequest;
import com.anant.CloudDrive.StorageProviders.requests.UploadPartRequest;

public interface UploadEntry {
    void setUploadKeyName(String userName, UploadIdRequest uploadIdRequest);

    boolean uploadPart(UploadPartRequest uploadPartRequest);

    boolean completeUserUpload();
}
