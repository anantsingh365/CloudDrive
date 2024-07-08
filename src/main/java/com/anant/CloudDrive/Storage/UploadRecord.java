package com.anant.CloudDrive.Storage;

import com.amazonaws.services.s3.transfer.Upload;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

//abstract class to encapsulate the state of an individual upload lifecycle
// implementations will ideally put variables
// relevant to the concrete storageProvider implementations
// state will be updated from
// -----> "INITIALIZED (uploadID generated, first part not yet uploaded)"
// -----> "IN PROGRESS (after first part has been uploaded)"
// -----> "COMPLETED (all parts have been uploaded and upload complete call has been triggered on storage Providers and returned success)"
public abstract class UploadRecord{

    private UploadRecordState state = UploadRecordState.NOT_CREATED;
    private final LinkedHashMap<UploadRecordState, Boolean> stateTransitionMap = new LinkedHashMap<>();
    private final UploadRecordState notCreatedState = UploadRecordState.NOT_CREATED;
    private final UploadRecordState InitState = UploadRecordState.INITIALIZED;
    private final UploadRecordState InProgressState = UploadRecordState.IN_PROGRESS;
    private final UploadRecordState CompletedState = UploadRecordState.COMPLETED;

    {
        this.stateTransitionMap.put(this.notCreatedState, true);
        this.stateTransitionMap.put(this.InitState, false);
        this.stateTransitionMap.put(this.InProgressState, false);
        this.stateTransitionMap.put(this.CompletedState, false);
    }

    //variable ideally to be used by storageManager to keep track of the number of parts Uploaded
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

    protected boolean verifyStateTransitionForRecord(UploadRecordState state){
        return false;
    }

    public UploadRecordState getState(){
       return this.state;
    }
}
