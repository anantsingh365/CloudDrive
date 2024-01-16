package com.anant.CloudDrive.StorageManager.AWSS3;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;

import com.anant.CloudDrive.StorageManager.StorageProvider;
import com.anant.CloudDrive.StorageManager.UploadRecord;
import com.anant.CloudDrive.StorageManager.Models.UserFileMetaData;
import com.anant.CloudDrive.StorageManager.Models.UploadIdRequest;
import com.anant.CloudDrive.StorageManager.Models.UploadPartRequest_;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Repository;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Repository
@Qualifier("s3")
@Profile("s3")
public class S3StorageProvider implements StorageProvider<S3UploadRecord> {

    private final AmazonS3 s3Client;
    private final String bucketName;

    public S3StorageProvider(@Autowired AmazonS3 s3Client, @Value("${s3.bucketName}") String bucketName) {
        this.s3Client = s3Client;
        this.bucketName = bucketName;
    }

    @Override
    public Resource download(String fileName) {
        final String keyName = fileName;
        return new InputStreamResource(s3Client
                .getObject(bucketName, keyName)
                .getObjectContent());
    }

    @Override
    public boolean uploadPart(S3UploadRecord record, UploadPartRequest_ req) {
        //boolean res = entry.uploadPart(req);
        return this.processPartUploadPriv(record, req);
    }

    @Override
    public boolean deleteFile(String fileName) {
        try{
            s3Client.deleteObject(bucketName,fileName);
            return true;
        }catch(AmazonServiceException e){
            System.err.println(e.getErrorMessage());
            return false;
        }
    }

    @Override
    public boolean renameFile(String oldFileName, String newFileName) {
        CopyObjectRequest copyObjRequest = new CopyObjectRequest(bucketName,
                oldFileName, bucketName, newFileName);
        try{
            var result = s3Client.copyObject(copyObjRequest);
            s3Client.deleteObject(new DeleteObjectRequest(bucketName, oldFileName));
            return true;
        }catch(Exception e){
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public long getStorageUsedByUser() {
        return 0;
    }

    @Override
    public boolean initializeUpload(String userName, S3UploadRecord record, UploadIdRequest req) {
        return this.initializeUploadPriv(userName, record, req);
        //record.initUpload(userName, req);
    }

    @Override
    public boolean completeUpload(S3UploadRecord record) {
        return completeUploadPriv(record);
    }

    @Override
    public List<UserFileMetaData> getUserObjectsMetaData(String userName) {
        // if(savedFileListing.get(userName) == null){
        System.out.println("Generating new File Listing");
        List<UserFileMetaData> list = new ArrayList<>();
        s3Client.listObjectsV2(bucketName, userName+"/")
                .getObjectSummaries().forEach(x -> list.add(
                        new UserFileMetaData(x.getKey(),
                                x.getSize(),
                                x.getLastModified(),
                                s3Client.getObjectMetadata(bucketName, x.getKey()).getContentType())
                ));
        return list;
    }

    private boolean initializeUploadPriv(String userName, S3UploadRecord record, UploadIdRequest req){
        return initiateUploadForKeyName(userName + "/" + req.getFileName(), req.getContentType(), record);
    }

    private boolean processPartUploadPriv(S3UploadRecord record, UploadPartRequest_ uploadPartRequest){
        InputStream ins = uploadPartRequest.getInputStream();
        long partSize = uploadPartRequest.getContentLength();
        List<PartETag> partETags = record.partETags;

        if (record.isUploadCompleted) {
            throw new IllegalStateException("Upload has already been completed");
        }

        try {
            //UploadPartRequest uploadRequest = new UploadPartRequest()
            var S3uploadPartRequest = new UploadPartRequest()
                    .withBucketName(bucketName)
                    .withKey(record.userUploadKeyName)
                    .withUploadId(record.initResponse.getUploadId())
                    .withLastPart(true)
                    .withPartNumber(record.partNumber)
                    .withInputStream(ins)
                    .withPartSize(partSize);

            // Upload the part and add the response's ETag to our list.
            int a = 10;
            var uploadResult = s3Client.uploadPart(S3uploadPartRequest);
            partETags.add(uploadResult.getPartETag());
            //logger.info("Uploading a part for key {} ", USER_UPLOAD_KEYNAME);
            System.out.println("Uploading a part for key userName -  " +  record.userUploadKeyName);
            ++record.partNumber;
            return true;

        } catch (SdkClientException e) {
            e.printStackTrace();
            return false;
        }
    }
    private boolean initiateUploadForKeyName(final String userSpecificKeyName, final String contentType, final S3UploadRecord record) {
        final var initRequest = new InitiateMultipartUploadRequest(bucketName, userSpecificKeyName);
        final ObjectMetadata metadata = new ObjectMetadata();
        record.contentType = contentType;
        metadata.setContentType(contentType);
        initRequest.setObjectMetadata(metadata);
        try{
            record.initResponse = s3Client.initiateMultipartUpload(initRequest);
            record.isUploadInitiated = true;
            record.userUploadKeyName = userSpecificKeyName;
            return true;
        }catch(Exception e){
            e.printStackTrace();
            return false;
        }
    }

    private boolean completeUploadPriv(S3UploadRecord record){
        if(!record.isUploadInitiated){
            throw new IllegalStateException("upload for keyname "
                    + record.userUploadKeyName + " hasn't initiated.");
        }
        if(record.isUploadCompleted){
            System.out.println("Upload for keyName {} already completed" + record.userUploadKeyName);
            return true;
        }
        var compRequest = new CompleteMultipartUploadRequest(bucketName, record.userUploadKeyName,
          record.initResponse.getUploadId(), record.partETags);

        CompleteMultipartUploadResult result;
        try {
            result = s3Client.completeMultipartUpload(compRequest);
        } catch (Exception e) {
            System.out.println("upload for keyName {} failed" + record.userUploadKeyName);
            e.printStackTrace();
            return false;
        }
        System.out.println("upload for keyName {} complete" + result.getKey());
        record.isUploadCompleted = true;
        return true;
    }
}