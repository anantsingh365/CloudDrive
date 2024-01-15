package com.anant.CloudDrive.StorageServiceTest;

import com.anant.CloudDrive.StorageManager.*;
import com.anant.CloudDrive.StorageManager.Models.UploadIdRequest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationContext;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest
public class StorageManagerTest2 {

    @Autowired
    ApplicationContext context;

    @Autowired
    UploadSessionsHolder uploadSessionsHolderImpl;

    @MockBean
    LocalStorageVideoStreamService videoStreamService;

    @MockBean
    SubscriptionService subscriptionService;

    @MockBean StorageProvider storageProvider;

    StorageManager manager;

    @BeforeEach
    public void setup(){
       manager = context.getBean(StorageManager.class);
    }

    @Test
    public void GetNewUploadIdSuccess() {
        when(subscriptionService.getTier(anyString())).thenReturn("200"); // 200 mb
        when(storageProvider.initializeUpload(anyString(), any(UploadRecord.class), any(UploadIdRequest.class))).thenReturn(true);
        String uploadId = manager.getUploadId(new UploadIdRequest("testFile", "audio/Flac"), "0987654321", "AnantSingh");
        System.out.println("Success Upload ID creation Test - " + uploadId);
        Assertions.assertNotEquals("Account Upgrade", uploadId);
    }

    @Test
    public void GetNewUploadIdFailure() {
        when(subscriptionService.getTier(anyString())).thenReturn("100"); // 100 mb
        when(storageProvider.getStorageUsedByUser()).thenReturn(150000000L);//roughly 150 mb from bytes to mb
        String uploadId = manager.getUploadId(new UploadIdRequest("testFile", "audio/Flac"), "0987654321", "AnantSingh");
        System.out.println("Success Upload ID creation Test - " + uploadId);
        Assertions.assertEquals("Account Upgrade", uploadId);
    }
}
