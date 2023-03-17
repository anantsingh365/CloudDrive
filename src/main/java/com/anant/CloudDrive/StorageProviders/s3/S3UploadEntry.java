package com.anant.CloudDrive.StorageProviders.s3;

import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import com.anant.CloudDrive.StorageProviders.Uploads.requests.*;
import com.anant.CloudDrive.StorageProviders.Uploads.UploadEntry;
import com.anant.CloudDrive.StorageProviders.Uploads.requests.UploadPartRequest;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@PropertySource("classpath:S3Credentials.properties")
@Qualifier("S3UploadEntry")
@Profile("s3")
public class S3UploadEntry implements UploadEntry {

    private final List<PartETag> partETags = new ArrayList<>();
    private final AmazonS3 s3Client;
    private final String bucketName ;
    private final Logger logger;

    private int partNumber = 1;
    private InitiateMultipartUploadResult initResponse;
    private boolean isUploadInitiated = false;
    private boolean isUploadCompleted = false;
    private String userUploadKeyName;
    private String contentType;

    public S3UploadEntry(@Value("${s3.bucketName}") String bucketName,
                         @Autowired AmazonS3 s3Client,
                         @Autowired Logger logger)
    {
        this.bucketName = bucketName;
        this.s3Client = s3Client;
        this.logger = logger;
    }


    public void setUploadKeyName(String userName, UploadIdRequest uploadIdRequest) {
        this.userUploadKeyName = getUserNamePrefixForKeyName(userName, uploadIdRequest.getFileName());
        this.contentType = uploadIdRequest.getContentType();
    }

    private void initiateUploadForKeyName(String userSpecificKeyName, String contentType) {
        var initRequest = new InitiateMultipartUploadRequest(bucketName, userSpecificKeyName);
        ObjectMetadata metadata = new ObjectMetadata();
        this.contentType = contentType;
        metadata.setContentType(contentType);
        initRequest.setObjectMetadata(metadata);
        initResponse = s3Client.initiateMultipartUpload(initRequest);
        isUploadInitiated = true;
    }

    private String getUserNamePrefixForKeyName(String username, String keyName) {
        return userUploadKeyName = username + "/" + keyName;
    }

    public boolean uploadPart(UploadPartRequest uploadPartRequest) {
        InputStream ins = uploadPartRequest.getInputStream();
        long partSize = uploadPartRequest.getContentLength();

        if (!isUploadInitiated && !isUploadCompleted) {
            initiateUploadForKeyName(userUploadKeyName, contentType);
        }
        if (isUploadCompleted) {
            throw new IllegalStateException("Upload has already been completed");
        }

        try {
            //UploadPartRequest uploadRequest = new UploadPartRequest()
            var S3uploadPartRequest = new com.amazonaws.services.s3.model.UploadPartRequest()
                    .withBucketName(bucketName)
                    .withKey(userUploadKeyName)
                    .withUploadId(initResponse.getUploadId())
                    .withLastPart(true)
                    .withPartNumber(partNumber)
                    .withInputStream(ins)
                    .withPartSize(partSize);

            // Upload the part and add the response's ETag to our list.
            var uploadResult = s3Client.uploadPart(S3uploadPartRequest);
            partETags.add(uploadResult.getPartETag());
            logger.info("Uploading a part for key {} ", userUploadKeyName);
            ++partNumber;
            return true;

        } catch (SdkClientException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean completeUserUpload() {
        // Complete the multipart upload.
        if(!isUploadInitiated){
            throw new IllegalStateException("upload for keyname "
                    + userUploadKeyName + " hasn't initiated.");
        }
        if(isUploadCompleted){
            logger.info("Upload for keyName {} already completed", userUploadKeyName);
            return true;
        }
        var compRequest = new CompleteMultipartUploadRequest(bucketName, userUploadKeyName,
                initResponse.getUploadId(), partETags);

        CompleteMultipartUploadResult result;
        try {
            result = s3Client.completeMultipartUpload(compRequest);
        } catch (Exception e) {
            logger.info("upload for keyName {} failed", userUploadKeyName);
            e.printStackTrace();
            return false;
        }
        logger.info("upload for keyName {} complete", result.getKey());
        isUploadCompleted = true;
        return true;
    }
}