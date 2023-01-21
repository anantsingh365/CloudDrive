package com.anant.CloudDrive.s3;

import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    @Autowired private AmazonS3 s3Client;
    @Value("${s3.bucketName}") private String bucketName;

    @Autowired private Logger logger;
    private final List<PartETag> partETags = new ArrayList<>();
    private int partNumber = 1;
    private InitiateMultipartUploadResult initResponse;
    private boolean isUploadInitiated = false;
    private String userUploadKeyName;

    public S3MultiPartUpload(){}

    public void setUploadKeyName(String userName, String keyName){
            this.userUploadKeyName = getUserNamePrefixForKeyName(userName, keyName);
    }

    private void initiateUploadForKeyName(String userSpecificKeyName){
        if(!isUploadInitiated){
            // Initiate the multipart upload.
            var initRequest = new InitiateMultipartUploadRequest(bucketName, userSpecificKeyName);
            initResponse = s3Client.initiateMultipartUpload(initRequest);
            isUploadInitiated = true;
        }else{
            throw new IllegalStateException("Upload has already been initiated for keyName "+ userSpecificKeyName);
        }
    }

    private String getUserNamePrefixForKeyName(String username, String keyName){
        return userUploadKeyName = username +"/" + keyName;
    }

    public void upload(InputStream ins, long partSize){
        var uploadPartRequest = new UploadPartRequest();

        if(!isUploadInitiated) {
            initiateUploadForKeyName(userUploadKeyName);
        }

            try {
                //UploadPartRequest uploadRequest = new UploadPartRequest()
                        uploadPartRequest
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

            } catch (SdkClientException e) {
                e.printStackTrace();
            }
    }

      public void completeUserUpload(){
        // Complete the multipart upload.
        var compRequest = new CompleteMultipartUploadRequest(bucketName, userUploadKeyName,
                initResponse.getUploadId(), partETags);
        var result = s3Client.completeMultipartUpload(compRequest);
        logger.info("upload for keyName {} complete", result.getKey());
    }
}