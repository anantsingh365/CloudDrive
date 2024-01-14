package com.anant.CloudDrive.UploadManager.Uploads;

import com.anant.CloudDrive.UploadManager.requests.UploadIdRequest;
import com.anant.CloudDrive.UploadManager.requests.UploadPartRequest_;

public interface UploadEntry {
    void setUploadKeyName(String userName, UploadIdRequest uploadIdRequest);

    boolean uploadPart(UploadPartRequest_ uploadPartRequest);

    boolean completeUserUpload();
}
