package com.anant.CloudDrive.StorageProviders.Uploads;

import com.anant.CloudDrive.StorageProviders.requests.UploadIdRequest;
import com.anant.CloudDrive.StorageProviders.requests.UploadPartRequest_;

public interface UploadEntry {
    void setUploadKeyName(String userName, UploadIdRequest uploadIdRequest);

    boolean uploadPart(UploadPartRequest_ uploadPartRequest);

    boolean completeUserUpload();
}
