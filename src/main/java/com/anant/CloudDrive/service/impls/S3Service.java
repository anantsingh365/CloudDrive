package com.anant.CloudDrive.service.impls;

import com.anant.CloudDrive.Utils.CommonUtils;
import com.anant.CloudDrive.requests.UploadRequest;
import com.anant.CloudDrive.s3.S3Operations;
import com.anant.CloudDrive.s3.UserUploads.UploadEntry;
import com.anant.CloudDrive.s3.UserUploads.*;

import com.anant.CloudDrive.service.StorageService;
import com.anant.CloudDrive.service.UserFileMetaData;
import org.slf4j.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.request.RequestContextHolder;

import java.io.InputStream;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import static com.anant.CloudDrive.Utils.CommonUtils.getUserData;

@Service
public class S3Service implements StorageService {

    private final Logger logger;
    private final String bucketName;
    private final UploadSessionsHolder uploadSessionsHolder;
    private final S3Operations s3Operations;
    @Autowired WebApplicationContext context;

    private final ConcurrentHashMap<String, List<UserFileMetaData>> savedFileListing = new ConcurrentHashMap<>();

    public S3Service(@Value("${s3.bucketName}") String bucketName,
                     @Autowired Logger logger,
                     @Autowired UploadSessionsHolder uploadSessionsHolder,
                     @Autowired S3Operations s3Operations)
    {
        this.bucketName = bucketName;
        this.logger = logger;
        this.uploadSessionsHolder = uploadSessionsHolder;
        this.s3Operations = s3Operations;
    }

    @Override
    public String getUploadId(String fileName){
        return this.getUploadSession().registerUploadId(fileName);
    }
    @Override
    public boolean upload(UploadRequest req){
        var session = this.getUploadSession();
        var entry = session.getEntry(req.getUploadId());
        return entry != null && s3Operations.uploadFile(entry, req);
    }

    @Override
    public boolean completeUpload(String uploadId){
        var entry = this.getUserEntry(uploadId);
        return entry != null && s3Operations.completeUserUpload(entry);
    }

    @Override
    public Resource download(String key){
        InputStream s3ObjectInputStream= s3Operations.getS3ObjectInputStream(key);
        return new InputStreamResource(s3ObjectInputStream);
    }

    @Override
    public List<UserFileMetaData> getUserObjectsMetaData(){
        //get objects for user with username as prefix
        String userName = getUserData(CommonUtils.signedInUser.GET_USERNAME);
       // if(savedFileListing.get(userName) == null){
            System.out.println("Generating new File Listing");
            var fileListing = s3Operations.getUserObjectsMetaData(userName);
           // savedFileListing.put(Objects.requireNonNull(userName), fileListing);
            return fileListing;
     //   }
      //  System.out.println("Using Saved File Listing");
      //  return s3Operations.getUserObjectListing(userName);
    }

    @Override
    public boolean deleteUserFile(String key){
        boolean result = s3Operations.deleteObject(key);
       // if(result){
        //    System.out.println("Object deleted, removing saved fileListing");
        //    savedFileListing.remove(Objects.requireNonNull(getUserData(signedInUser.GET_USERNAME)));
        //}
        return result;
    }

    @Override
    public boolean renameFile(int id)   {
        return false;
    }

    @Override
    public long getUserStorageQuota() {
        var userObjectListing = getUserObjectsMetaData();
        long sum=0;
        for(UserFileMetaData file: userObjectListing){
            sum += file.getSize();
        }
        return sum;
    }

    private UploadEntry getUserEntry(String uploadId){
        var session = uploadSessionsHolder.getExistingSession(getUserData(CommonUtils.signedInUser.GET_SESSIONID));
        return session != null ? session.getEntry(uploadId) : null;
    }

    private UploadSession getUploadSession(){
        return uploadSessionsHolder.getSession(getUserData(CommonUtils.signedInUser.GET_SESSIONID));
    }
}
