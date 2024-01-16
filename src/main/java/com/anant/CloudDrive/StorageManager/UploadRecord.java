package com.anant.CloudDrive.StorageManager;


public abstract class UploadRecord {

    private UploadRecordState state = UploadRecordState.NOT_INITIALISED;

    public void setState(UploadRecordState state){
       this.state = state;
    }
    public UploadRecordState getState(){
       return this.state;
    }
}
enum UploadRecordState {
    NOT_INITIALISED, INITIALISED, IN_PROGRESS, COMPLETED
}