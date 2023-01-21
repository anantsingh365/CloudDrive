package com.anant.CloudDrive.s3;

import com.amazonaws.SdkClientException;
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

    @Autowired
    private AmazonS3 s3Client;

    @Value("${s3.bucketName}")
    private String bucketName;

    private final List<PartETag> partETags = new ArrayList<>();

    private static final long PART_SIZE = 14 * 1024 * 1024;

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
            InitiateMultipartUploadRequest initRequest = new InitiateMultipartUploadRequest(bucketName, userSpecificKeyName);
            var initResponse = s3Client.initiateMultipartUpload(initRequest);
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

        if( partSize == -1){
            partSize = (int) PART_SIZE;
          }
//        else
//           uploadPartRequest = setLastPart(uploadPartRequest);
//        }
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
                System.out.println("Uploading a part for key " + userUploadKeyName);
                ++partNumber;

               // System.out.println("Upload aborted");
            } catch (SdkClientException e) {
                e.printStackTrace();
            }
    }
    public UploadPartRequest setLastPart(UploadPartRequest req){
        return req.withLastPart(true);
    }

      public void completeUserUpload(){
        // Complete the multipart upload.
        var compRequest = new CompleteMultipartUploadRequest(bucketName, userUploadKeyName,
                initResponse.getUploadId(), partETags);
        var result = s3Client.completeMultipartUpload(compRequest);
          System.out.println("upload for keyName "+result.getKey()+" complete");
    }
}