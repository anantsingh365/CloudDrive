package com.anant.CloudDrive.StorageManager.LocalFileSystem;


import com.anant.CloudDrive.StorageManager.UploadRecord;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Profile("local")
public class FileSystemUploadPartRecord extends UploadRecord {

}
