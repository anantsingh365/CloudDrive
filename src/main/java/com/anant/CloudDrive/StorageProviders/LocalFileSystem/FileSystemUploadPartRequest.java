package com.anant.CloudDrive.StorageProviders.LocalFileSystem;

import com.anant.CloudDrive.StorageProviders.Uploads.UploadEntry;
import com.anant.CloudDrive.StorageProviders.requests.*;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Profile("local")
public class FileSystemUploadPartRequest implements UploadEntry {
    @Override
    public void setUploadKeyName(String userName, UploadIdRequest uploadIdRequest) {
        return;
    }

    @Override
    public boolean uploadPart(com.anant.CloudDrive.StorageProviders.requests.UploadPartRequest uploadPartRequest) {
        return true;
    }

    @Override
    public boolean completeUserUpload() {
        //testing
        return true;
    }
}
