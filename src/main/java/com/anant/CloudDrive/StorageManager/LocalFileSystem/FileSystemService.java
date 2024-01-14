package com.anant.CloudDrive.StorageManager.LocalFileSystem;

import com.anant.CloudDrive.StorageManager.BaseStorageProvider;
import com.anant.CloudDrive.StorageManager.Uploads.UploadEntry;
import com.anant.CloudDrive.StorageManager.requests.UploadPartRequest_;
import com.anant.CloudDrive.StorageManager.UserFileMetaData;

import org.springframework.context.annotation.Profile;
import org.springframework.core.io.Resource;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Profile("local")
public class FileSystemService implements BaseStorageProvider {

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
    public boolean uploadPart(UploadEntry entry, UploadPartRequest_ req) {
        return false;
    }

    @Override
    public boolean completeUpload(UploadEntry entry) {
        return false;
    }

    @Override
    public List<UserFileMetaData> getUserObjectsMetaData(String userName) {
        return null;
    }
}
