package com.anant.CloudDrive.service.Uploads;

import com.anant.CloudDrive.service.Uploads.requests.*;

public interface UploadEntry {

    void setUploadKeyName(String userName, UploadIdRequest uploadIdRequest);

    boolean uploadPart(UploadPartRequest uploadPartRequest);

    boolean completeUserUpload();

}
