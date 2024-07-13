package com.anant.CloudDrive.controller.Responses;

public class UploadCompleteResponse {

    private String Operation;
    private boolean isSuccess;
    private String userName;
    private String uploadID;
    private String Description;

    public UploadCompleteResponse(String operation, boolean isSuccess, String userName, String uploadID, String Description) {
        this.Operation = operation;
        this.isSuccess = isSuccess;
        this.userName = userName;
        this.uploadID = uploadID;
        this.Description = Description;
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

}
