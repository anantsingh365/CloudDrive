package com.anant.CloudDrive.Storage.LocalFileSystem;

import com.anant.CloudDrive.Storage.UploadRecord;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
@Profile("local")
public class LocalUploadRecord extends UploadRecord {

    private String uploadId;
    private String uploadName;

    public String getUploadId() {
        return uploadId;
    }

    public void setUploadId(String uploadId) {
        this.uploadId = uploadId;
    }

    public String getUploadName() {
        return uploadName;
    }

    public void setUploadName(String uploadName) {
        this.uploadName = uploadName;
    }
}
