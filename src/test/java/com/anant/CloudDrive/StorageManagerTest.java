package com.anant.CloudDrive;

import com.amazonaws.services.s3.transfer.Upload;
import com.anant.CloudDrive.StorageManager.*;
import com.anant.CloudDrive.StorageManager.Models.UploadIdRequest;
import com.anant.CloudDrive.StorageManager.Models.UploadPartRequest_;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.slf4j.Logger;
import org.springframework.context.ApplicationContext;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class StorageManagerTest {

    @Test
    public void GetNewUploadIdSuccess() {

        ApplicationContext context = mock(ApplicationContext.class);
        StorageProvider storageProvider = mock(StorageProvider.class);
        SubscriptionService subscriptionService = mock(SubscriptionService.class);
       // UploadSessionsHolder uploadSessionsHolder = mock(UploadSessionsHolder.class);
        UploadSessionsHolder uploadSessionsHolderImpl = new UploadSessionsHolder(context, mock(Logger.class));
        UploadSession session = new UploadSession(context, mock(Logger.class));
        when(context.getBean(UploadSession.class)).thenReturn(session);
        when(context.getBean(UploadRecord.class)).thenReturn(new UploadRecord() {});

        LocalStorageVideoStreamService videoStreamService = mock(LocalStorageVideoStreamService.class);
        when(subscriptionService.getTier(anyString())).thenReturn("200"); // 200 mb
        when(storageProvider.initializeUpload(anyString(), any(UploadRecord.class), any(UploadIdRequest.class)))
                .thenReturn(true);

        StorageManager manager = new StorageManager(context,
                storageProvider,
                subscriptionService,
                uploadSessionsHolderImpl,
                videoStreamService);

        String uploadId = manager.getUploadId(new UploadIdRequest("testFile", "audio/Flac"), "0987654321", "AnantSingh");
        System.out.println("Success Upload ID creation Test - " + uploadId);
        Assertions.assertNotEquals("Account Upgrade", uploadId);
    }

    @Test
    public void GetNewUploadIdFailure() {

        ApplicationContext context = mock(ApplicationContext.class);
        StorageProvider storageProvider = mock(StorageProvider.class);
        SubscriptionService subscriptionService = mock(SubscriptionService.class);
        // UploadSessionsHolder uploadSessionsHolder = mock(UploadSessionsHolder.class);
        UploadSessionsHolder uploadSessionsHolderImpl = new UploadSessionsHolder(context, mock(Logger.class));
        UploadSession session = new UploadSession(context, mock(Logger.class));
        when(context.getBean(UploadSession.class)).thenReturn(session);
        when(context.getBean(UploadRecord.class)).thenReturn(new UploadRecord() {});

        LocalStorageVideoStreamService videoStreamService = mock(LocalStorageVideoStreamService.class);
        when(subscriptionService.getTier(anyString())).thenReturn("100"); // 100 mb
        when(storageProvider.getStorageUsedByUser()).thenReturn(150000000L);//roughly 150 mb from bytes to mb
        when(storageProvider.initializeUpload(anyString(), any(UploadRecord.class), any(UploadIdRequest.class)))
                .thenReturn(true);

        StorageManager manager = new StorageManager(context,
                storageProvider,
                subscriptionService,
                uploadSessionsHolderImpl,
                videoStreamService);

        String uploadId = manager.getUploadId(new UploadIdRequest("testFile", "audio/Flac"), "0987654321", "AnantSingh");
        System.out.println("Success Upload ID creation Test - " + uploadId);
        Assertions.assertEquals("Account Upgrade", uploadId);
    }

    @Test
    public void uploadPartTest(){

        ApplicationContext context = mock(ApplicationContext.class);
        StorageProvider storageProvider = mock(StorageProvider.class);
        UploadSessionsHolder uploadSessionsHolderImpl = mock(UploadSessionsHolder.class);
        UploadSession session = mock(UploadSession.class);
        when(session.getRecord(anyString())).thenReturn(new UploadRecord() {});
        when(uploadSessionsHolderImpl.getExistingSession(anyString())).thenReturn(session);
        StorageManager manager = new StorageManager(null,
                storageProvider,
                null,
                uploadSessionsHolderImpl,
                null);

        UploadPartRequest_ req = new UploadPartRequest_(null, "6765314", 0L);
        manager.uploadPart(req, "1234");
        ArgumentCaptor<UploadRecord> argumentCaptor = ArgumentCaptor.forClass(UploadRecord.class);
        ArgumentCaptor<UploadPartRequest_> argumentCaptor2 = ArgumentCaptor.forClass(UploadPartRequest_.class);
        verify(storageProvider).uploadPart(argumentCaptor.capture(),argumentCaptor2.capture());
    }
}
