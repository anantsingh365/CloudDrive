package com.anant.CloudDrive.Storage.LocalFileSystem;

import com.anant.CloudDrive.Storage.Models.UploadIdRequest;
import com.anant.CloudDrive.Storage.Models.UploadPartRequest;
import com.anant.CloudDrive.Storage.StorageProvider;
import com.anant.CloudDrive.Storage.Models.UserFileMetaData;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Repository;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

@Repository
@PropertySource("classpath:LocalStorageProperties.properties")
@Profile("local")
public class LocalFileSystemAI implements StorageProvider<LocalUploadRecord> {

    @Value("${local.storagePath}")
    private String storagePath;

    @Override
    public Resource download(String fileName) {
        // Implement download logic
        return null;
    }

    @Override
    public boolean uploadPart(LocalUploadRecord record, UploadPartRequest req) {
        return processPartUploadPriv(record, req);
    }

    @Override
    public boolean abortUpload(LocalUploadRecord record) {
        try {
            Path uploadFolder = Path.of(storagePath, record.getUploadId());

            // Delete individual part files
            Files.walk(uploadFolder)
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);

            // Delete the upload folder
            Files.deleteIfExists(uploadFolder);

            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean deleteFile(String fileName) {
        // Implement delete file logic
        return false;
    }

    @Override
    public boolean renameFile(String oldFileName, String newFileName) {
        // Implement rename file logic
        return false;
    }

    @Override
    public long getStorageUsedByUser(String userName) {
        return 0;
    }

    @Override
    public boolean initializeUpload(String userName, LocalUploadRecord record, UploadIdRequest req) {
        return initializeUploadPriv(userName, record, req);
    }

    @Override
    public boolean completeUpload(LocalUploadRecord record) {
        return completeUploadPriv(record);
    }

    @Override
    public List<UserFileMetaData> getUserObjectsMetaData(String userName) {
        // Implement user objects metadata retrieval logic
        return Collections.emptyList();
    }

    private boolean initializeUploadPriv(String userName, LocalUploadRecord record, UploadIdRequest req) {
        String uploadId = record.getUploadId();
        String uploadName = req.getFileName();

        record.setUploadId(uploadId);
        record.setUploadName(uploadName);

        return initiateUploadForKeyName(userName + "/" + uploadId, req.getContentType(), record);
    }

    private boolean processPartUploadPriv(LocalUploadRecord record, UploadPartRequest uploadPartRequest) {
        InputStream ins = uploadPartRequest.getInputStream();
        long partSize = uploadPartRequest.getContentLength();

//        int partNumber = record.getPartsUploaded() + 1;
 //       String chunkFileName = String.valueOf(partNumber);

        // Implement part upload logic
        return false;
    }

    private boolean initiateUploadForKeyName(String userSpecificKeyName, String contentType, LocalUploadRecord record) {
        // Implement initiate upload logic
        return false;
    }

    private boolean completeUploadPriv(LocalUploadRecord record) {
        try {
            Path uploadFolder = Path.of(storagePath, record.getUploadId());
            List<Path> partFiles = Files.walk(uploadFolder)
                    .filter(path -> !Files.isDirectory(path))
                    .sorted(Comparator.comparingInt(path -> Integer.parseInt(path.getFileName().toString())))
                    .collect(Collectors.toList());

            Path targetFilePath = Path.of(storagePath, record.getUploadId() + "_" + record.getUploadName() + ".complete");

            // Concatenate all parts to create the complete file
            try (OutputStream outputStream = Files.newOutputStream(targetFilePath, StandardOpenOption.CREATE)) {
                for (Path partFile : partFiles) {
                    Files.copy(partFile, outputStream);
                }
            }

            // Delete individual part files
            Files.walk(uploadFolder)
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);

            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}
