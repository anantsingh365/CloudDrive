package com.anant.CloudDrive.LocalFileSystem.UserUploads;

import com.anant.CloudDrive.service.Uploads.UploadEntry;
import com.anant.CloudDrive.service.Uploads.requests.*;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Profile("local")
public class FileSystemUploadEntry implements UploadEntry {
    @Override
    public void setUploadKeyName(String userName, UploadIdRequest uploadIdRequest) {
        return;
    }

    @Override
    public boolean uploadPart(UploadPartRequest uploadPartRequest) {
        return true;
    }

    @Override
    public boolean completeUserUpload() {
        //testing
        return true;
    }
}
