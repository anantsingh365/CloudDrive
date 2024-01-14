package com.anant.CloudDrive.UploadManager.AWSS3;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CopyObjectRequest;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.anant.CloudDrive.StorageProviders.BaseStorageProvider;
import com.anant.CloudDrive.StorageProviders.Uploads.UploadEntry;
import com.anant.CloudDrive.StorageProviders.UserFileMetaData;
import com.anant.CloudDrive.StorageProviders.requests.UploadPartRequest;
import com.anant.CloudDrive.Utils.CommonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
@Qualifier("s3")
public class S3StorageProvider implements BaseStorageProvider {

    private final AmazonS3 s3Client;
    private final String bucketName;

    public S3StorageProvider(@Autowired AmazonS3 s3Client,@Autowired String bucketName) {
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
    public boolean uploadPart(UploadEntry entry, UploadPartRequest req) {
        boolean res = entry.uploadPart(req);
        return res;
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
    public boolean completeUpload(UploadEntry entry) {
       return entry.completeUserUpload();
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
                        //getContentType(x.getKey()))
                ));
        return list;
        //   }
    }
}
