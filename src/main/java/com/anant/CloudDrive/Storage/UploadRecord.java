package com.anant.CloudDrive.Storage;

import com.amazonaws.services.s3.transfer.Upload;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static com.anant.CloudDrive.Storage.UploadRecord.UploadState.NOT_CREATED;

//abstract class to encapsulate the state of an individual upload lifecycle
// implementations will ideally put variables
// relevant to the concrete storageProvider implementations
// state will be updated from
// -----> "INITIALIZED (uploadID generated, first part not yet uploaded)"
// -----> "IN PROGRESS (after first part has been uploaded)"
// -----> "COMPLETED (all parts have been uploaded and upload complete call has been triggered on storage Providers and returned success)"
public abstract class UploadRecord{

    private String associatedWithUser = null;
    private String uploadId = null;
    private UploadState state = NOT_CREATED;

    //variable ideally to be used by storageManager to keep track of the number of parts Uploaded
    private int partsUploaded = 0;

    protected int getPartsUploaded(){
       return this.partsUploaded;
    }

    protected void incrementPartsUploaded(){
        ++this.partsUploaded;
    }

    protected void setState(UploadState state){
        if(state == UploadState.NOT_CREATED){
            throw new IllegalStateException("NOT_CREATED is the default implicit state, not supposed to be set explicitly");
        }
       this.state = state;
    }

    protected boolean verifyStateTransitionForRecord(UploadState state){
        return false;
    }

    public UploadState getState(){
       return this.state;
    }
    public void setUploadId(String uploadId){
        this.uploadId = uploadId;
    }

    public String getUploadId(){
       return this.uploadId;
    }
    public void setAssociatedWithUser(String userName){
       this.associatedWithUser = userName;
    }
    public String getAssociatedWithUser(){
        return this.associatedWithUser;
    }

    public enum UploadState{
        NOT_CREATED, INITIALIZED, IN_PROGRESS, ABORTED, COMPLETED
    }
}
