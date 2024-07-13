package com.anant.CloudDrive.controller.Responses.Upload;

public class UploadPartSuccessResponse {

    private String Operation;
    private String userName;
    private String uploadID;
    private String Description;
    private boolean isSuccess;

    public UploadPartSuccessResponse(String operation, boolean isSuccess, String userName, String uploadID, String description) {
        this.Operation = operation;
        this.isSuccess = isSuccess;
        this.userName = userName;
        this.uploadID = uploadID;
        this.Description = description;
    }

    public String getOperation() {
        return Operation;
    }

    public void setOperation(String operation) {
        Operation = operation;
    }

    public boolean isSuccess() {
        return isSuccess;
    }

    public void setSuccess(boolean success) {
        isSuccess = success;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUploadID() {
        return uploadID;
    }

    public void setUploadID(String uploadID) {
        this.uploadID = uploadID;
    }

    public String getDescription() {
        return Description;
    }

    public void setDescription(String description) {
        Description = description;
    }

}
