package com.anant.CloudDrive.controller.Responses.Upload;

import com.anant.CloudDrive.Utils.CommonUtils;
import com.anant.CloudDrive.controller.Responses.AbstractFileOperationsResponse;

public class UploadIdGeneratedResponse extends AbstractFileOperationsResponse {

    private String createdUploadId;

    public UploadIdGeneratedResponse(boolean isSuccess, String description, String createdUploadId) {
        super("Generate Upload ID", isSuccess, CommonUtils.getUserData(CommonUtils.signedInUser.GET_USERNAME), description);
        this.createdUploadId = createdUploadId;
    }
    public String getCreatedUploadId(){
        return this.createdUploadId;
    }

    public void setCreatedUploadId(String uploadId){
        this.createdUploadId = uploadId;
    }
}
