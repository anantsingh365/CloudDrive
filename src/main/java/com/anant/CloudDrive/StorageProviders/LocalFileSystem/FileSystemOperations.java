package com.anant.CloudDrive.StorageProviders.LocalFileSystem;

import com.anant.CloudDrive.StorageProviders.requests.*;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;

@Component
@Profile("local")
public class FileSystemOperations {

    protected boolean uploadFile(UploadPartRequest_ req){
        var ins = req.getInputStream();
        File file = new File("tempUserData");
        try {
           // Thread.sleep(500);
            BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(file, true));
            ins.transferTo(out);
            out.close();
            // ins.close();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
}
