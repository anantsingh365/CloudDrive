package com.anant.CloudDrive.controller.Responses;

public abstract class AbstractFileOperationsResponse {

    private String Operation;
    private boolean isSuccess;
    private String userName;
    private String Description;

    public AbstractFileOperationsResponse(String operation, boolean isSuccess, String userName,  String description) {
        Operation = operation;
        this.isSuccess = isSuccess;
        this.userName = userName;
        Description = description;
    }

    public String getOperation() {
        return Operation;
    }

    public void setOperation(String operation) {
        Operation = operation;
    }

    public boolean getisSuccess(){
       return isSuccess;
    }

    public void setisSuccess(boolean success) {
        isSuccess = success;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getDescription() {
        return Description;
    }

    public void setDescription(String description) {
        Description = description;
    }
}
