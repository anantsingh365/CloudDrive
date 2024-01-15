package com.anant.CloudDrive.StorageManager.AWSS3;

import com.amazonaws.services.s3.model.InitiateMultipartUploadResult;
import com.amazonaws.services.s3.model.PartETag;
import com.anant.CloudDrive.StorageManager.UploadRecord;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@PropertySource("classpath:S3Credentials.properties")
@Qualifier("S3UploadEntry")
@Profile("s3")
public class S3UploadRecord implements UploadRecord {
    protected final List<PartETag> partETags = new ArrayList<>();
    protected int partNumber = 1;
    protected InitiateMultipartUploadResult initResponse;
    protected boolean isUploadInitiated = false;
    protected boolean isUploadCompleted = false;
    protected String userUploadKeyName;
    protected String contentType;
}
