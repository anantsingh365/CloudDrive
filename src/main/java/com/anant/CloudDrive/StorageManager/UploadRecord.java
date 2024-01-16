package com.anant.CloudDrive.StorageManager;


//abstract class to encapsulate the state of an individual upload lifecycle
// state will be updated from
// -----> "NOT INITIALIZED (no uploadID)"
// -----> "INITIALIZED (uploadID generated)"
// -----> "IN PROGRESS (after first part has been uploaded)"
// -----> "COMPLETED (all parts have been uploaded and upload complete call has been triggered on storage Providers)"
public abstract class UploadRecord {

    private UploadRecordState state = UploadRecordState.NOT_INITIALISED;

    //variable ideally to be used by storageManager to keep track of state of the upload Record
    private int partsUploaded = 0;

    public int getPartsUploaded(){
       return this.partsUploaded;
    }
    public void incrementPartsUploaded(){
        ++this.partsUploaded;
    }

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