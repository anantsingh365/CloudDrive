package com.anant.CloudDrive.Storage.LocalFileSystem;

import com.anant.CloudDrive.Storage.StorageProvider;
import com.anant.CloudDrive.Storage.Models.UploadIdRequest;
import com.anant.CloudDrive.Storage.Models.UploadPartRequest;
import com.anant.CloudDrive.Storage.Models.UserFileMetaData;

import org.springframework.context.annotation.Profile;
import org.springframework.core.io.Resource;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Profile("local")
public class FileSystemService implements StorageProvider<FileSystemUploadPartRecord> {

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
    public long getStorageUsedByUser(String userName) {
        return 0;
    }

    @Override
    public boolean initializeUpload(String userName, FileSystemUploadPartRecord record, UploadIdRequest req) {
        return false;
    }

    @Override
    public boolean uploadPart(FileSystemUploadPartRecord entry, UploadPartRequest req) {
        return false;
    }

    @Override
    public boolean abortUpload(FileSystemUploadPartRecord record) {
        return false;
    }

    @Override
    public boolean completeUpload(FileSystemUploadPartRecord entry) {
        return false;
    }

    @Override
    public List<UserFileMetaData> getUserObjectsMetaData(String userName) {
        return null;
    }
}
