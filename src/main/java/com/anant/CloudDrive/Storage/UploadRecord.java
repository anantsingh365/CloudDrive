package com.anant.CloudDrive.Storage;

//abstract class to encapsulate the state of an individual upload lifecycle
// implementations will ideally put variables
// relevant to the concrete storageProvider implementations
// state will be updated from
// -----> "INITIALIZED (uploadID generated, first part not yet uploaded)"
// -----> "IN PROGRESS (after first part has been uploaded)"
// -----> "COMPLETED (all parts have been uploaded and upload complete call has been triggered on storage Providers and returned success)"
public abstract class UploadRecord{

    private UploadRecordState state = UploadRecordState.NOT_CREATED;

    //variable ideally to be used by storageManager to keep track of state of the upload Record
    private int partsUploaded = 0;

    protected int getPartsUploaded(){
       return this.partsUploaded;
    }

    protected void incrementPartsUploaded(){
        ++this.partsUploaded;
    }

    protected void setState(UploadRecordState state){
        if(state == UploadRecordState.NOT_CREATED){
            throw new IllegalStateException("NOT_CREATED is the default implicit state, not supposed to be set explicitly");
        }
       this.state = state;
    }

    protected UploadRecordState getState(){
       return this.state;
    }
}
