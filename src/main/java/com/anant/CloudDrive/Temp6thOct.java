package com.anant.CloudDrive;

import com.anant.CloudDrive.Storage.Models.UploadPartRequest;
import com.anant.CloudDrive.Storage.StorageProvider;
import com.anant.CloudDrive.Storage.UploadRecord;

abstract class UploadTask{

    StorageProvider<UploadRecord> sp;
    String associatedWithUser;
    String UploadId;
    UploadRecord record;

    public boolean doUpload(UploadPartRequest req){
         return sp.uploadPart(record, req);
//        sp.initializeUpload(associatedWithUser, record, Upload)
         //return false;
    }

    public static class UploadTaskBuilder{

        String forUserName;
        String uploadId;
        UploadRecord record;

        public UploadTaskBuilder withUser(String userName){
            this.forUserName = userName;
            return this;
        }

        public UploadTaskBuilder UploadId(String uploadId){
            this.uploadId = uploadId;
            return this;
        }

        public UploadTaskBuilder WithRecord(UploadRecord record){
            this.record = record;
            return this;
        }

        public UploadTask build(){
           return null;
        }

    }

}

public class Temp6thOct {

    public static void main(String[] args) {
        System.out.println("hello world ");
    }

}
