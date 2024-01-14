package com.anant.CloudDrive.UploadManager.LocalFileSystem;

import com.anant.CloudDrive.UploadManager.Uploads.UploadEntry;

import com.anant.CloudDrive.UploadManager.requests.UploadIdRequest;
import com.anant.CloudDrive.UploadManager.requests.UploadPartRequest_;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Profile("local")
public class FileSystemUploadPartEntry implements UploadEntry {
    @Override
    public void setUploadKeyName(String userName, UploadIdRequest uploadIdRequest) {
        return;
    }

    @Override
    public boolean uploadPart(UploadPartRequest_ uploadPartRequest) {
        return true;
    }

    @Override
    public boolean completeUserUpload() {
        //testing
        return true;
    }
}
