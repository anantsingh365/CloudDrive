package com.anant.CloudDrive.s3;

import com.amazonaws.SdkClientException;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
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

    @Value("${s3.bucketName}")
    private String bucketName;

    @Autowired
    private AmazonS3 s3Client;

    private List<PartETag> partETags = new ArrayList<>();

    private int partNumber = 1;
    private InitiateMultipartUploadRequest initRequest;
    private InitiateMultipartUploadResult initResponse;

    private boolean isUploadInitiated = false;

    private String userUploadKeyName;


    public S3MultiPartUpload(){}

    public void initiateUploadForKeyName(String userName, String keyName){
        if(!isUploadInitiated){
            // Initiate the multipart upload.
            initRequest = new InitiateMultipartUploadRequest(bucketName, getUserNamePrefixForKeyName(userName, keyName));
            initResponse = s3Client.initiateMultipartUpload(initRequest);
        }else{
            throw new IllegalStateException("Upload has already been initiated for keyName "+ keyName);
        }
    }
    private String getUserNamePrefixForKeyName(String username, String keyName){
        return userUploadKeyName = username +"/" + keyName;
    }

    public void upload(String keyName, InputStream ins){
        Regions clientRegion = Regions.AP_SOUTH_1;
        long partSize = 14 * 1024 * 1024; // Set part size to 14 MB.

        try {
                // Create the request to upload a part.
                UploadPartRequest uploadRequest = new UploadPartRequest()
                        .withBucketName(bucketName)
                        .withKey(userUploadKeyName)
                        .withUploadId(initResponse.getUploadId())
                        .withPartNumber(partNumber)
//                        .withFileOffset(filePosition)
//                        .withFile(file)
                        .withInputStream(ins)
                        .withPartSize(partSize);

                // Upload the part and add the response's ETag to our list.
                UploadPartResult uploadResult = s3Client.uploadPart(uploadRequest);
                partETags.add(uploadResult.getPartETag());
                System.out.println("Only 1 time");
                ++partNumber;

            System.out.println("Upload aborted");
        } catch (SdkClientException e) {
            e.printStackTrace();
        }
    }

    public void completeUserUpload(){
        // Complete the multipart upload.
        CompleteMultipartUploadRequest compRequest = new CompleteMultipartUploadRequest(bucketName, userUploadKeyName,
                initResponse.getUploadId(), partETags);
    }
}
