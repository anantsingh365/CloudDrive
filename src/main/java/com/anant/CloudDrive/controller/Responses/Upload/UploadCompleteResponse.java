package com.anant.CloudDrive.controller.Responses.Upload;

import com.anant.CloudDrive.Utils.CommonUtils;
import com.anant.CloudDrive.controller.Responses.AbstractFileOperationsResponse;

public class UploadCompleteResponse extends AbstractFileOperationsResponse {

    private String uploadId;

    public UploadCompleteResponse(boolean isSuccess, String description, String uploadId) {
        super("Complete-Upload", isSuccess, CommonUtils.getUserData(CommonUtils.signedInUser.GET_USERNAME), description);
        this.uploadId = uploadId;
    }
    public String getUploadId(){
       return this.uploadId;
    }

    public void setUploadId(String uploadId){
        this.uploadId = uploadId;
    }
}
