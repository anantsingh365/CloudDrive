package com.anant.CloudDrive.s3;

import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@PropertySource("classpath:S3Credentials.properties")
public class S3MultiPartUpload {

    private final List<PartETag> partETags = new ArrayList<>();
    @Autowired private AmazonS3 s3Client;
    @Value("${s3.bucketName}") private String bucketName;
    @Autowired private Logger logger;
    private int partNumber = 1;
    private InitiateMultipartUploadResult initResponse;
    private boolean isUploadInitiated = false;
    private boolean isUploadCompleted = false;
    private String userUploadKeyName;

    public S3MultiPartUpload() {
    }

    public void setUploadKeyName(String userName, String keyName) {
        this.userUploadKeyName = getUserNamePrefixForKeyName(userName, keyName);
    }

    private void initiateUploadForKeyName(String userSpecificKeyName) {
        var initRequest = new InitiateMultipartUploadRequest(bucketName, userSpecificKeyName);
        initResponse = s3Client.initiateMultipartUpload(initRequest);
        isUploadInitiated = true;
    }

    private String getUserNamePrefixForKeyName(String username, String keyName) {
        return userUploadKeyName = username + "/" + keyName;
    }

    public boolean upload(InputStream ins, long partSize) {

        if (!isUploadInitiated && !isUploadCompleted) {
            initiateUploadForKeyName(userUploadKeyName);
        }
        if (isUploadCompleted) {
            throw new IllegalStateException("Upload has already been completed");
        }

        try {
            //UploadPartRequest uploadRequest = new UploadPartRequest()
            var uploadPartRequest = new UploadPartRequest()
                    .withBucketName(bucketName)
                    .withKey(userUploadKeyName)
                    .withUploadId(initResponse.getUploadId())
                    .withLastPart(true)
                    .withPartNumber(partNumber)
                    .withInputStream(ins)
                    .withPartSize(partSize);

            // Upload the part and add the response's ETag to our list.
            var uploadResult = s3Client.uploadPart(uploadPartRequest);
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
        var compRequest = new CompleteMultipartUploadRequest(bucketName, userUploadKeyName,
                initResponse.getUploadId(), partETags);

        CompleteMultipartUploadResult result;
        try {
            result = s3Client.completeMultipartUpload(compRequest);
        } catch (Exception e) {
            logger.info("upload for keyName {} failee", userUploadKeyName);
            e.printStackTrace();
            return false;
        }
        logger.info("upload for keyName {} complete", result.getKey());
        isUploadCompleted = true;
        return true;
    }
}