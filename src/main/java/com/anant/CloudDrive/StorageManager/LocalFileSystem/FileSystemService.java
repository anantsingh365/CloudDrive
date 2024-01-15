package com.anant.CloudDrive.StorageManager.LocalFileSystem;

import com.anant.CloudDrive.StorageManager.StorageProvider;
import com.anant.CloudDrive.StorageManager.UploadRecord;
import com.anant.CloudDrive.StorageManager.Models.UploadIdRequest;
import com.anant.CloudDrive.StorageManager.Models.UploadPartRequest_;
import com.anant.CloudDrive.StorageManager.Models.UserFileMetaData;

import org.springframework.context.annotation.Profile;
import org.springframework.core.io.Resource;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Profile("local")
public class FileSystemService implements StorageProvider {

    @Override
    public Resource download(String fileName) {
        return null;
    }

    @Override
    public boolean deleteFile(String fileName) {
        return false;
    }

    @Override
    public boolean renameFile(String oldFileName, String newFileName) {
        return false;
    }

    @Override
    public long getStorageUsedByUser() {
        return 0;
    }

    @Override
    public boolean initializeUpload(String userName, UploadRecord record, UploadIdRequest req) {
        return false;
    }

    @Override
    public boolean uploadPart(UploadRecord entry, UploadPartRequest_ req) {
        return false;
    }

    @Override
    public boolean completeUpload(UploadRecord entry) {
        return false;
    }

    @Override
    public List<UserFileMetaData> getUserObjectsMetaData(String userName) {
        return null;
    }
}
