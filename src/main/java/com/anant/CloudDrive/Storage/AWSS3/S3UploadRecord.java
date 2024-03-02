package com.anant.CloudDrive.Storage.AWSS3;

import com.amazonaws.services.s3.model.InitiateMultipartUploadResult;
import com.amazonaws.services.s3.model.PartETag;
import com.anant.CloudDrive.Storage.UploadRecord;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Profile("s3")
public class S3UploadRecord extends UploadRecord {
    protected final List<PartETag> partETags = new ArrayList<>();
    protected int partNumber = 1;
    protected InitiateMultipartUploadResult initResponse;
    protected boolean isUploadInitiated = false;
    protected boolean isUploadCompleted = false;
    protected String userUploadKeyName;
    protected String contentType;
}
