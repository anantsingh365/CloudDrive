package com.anant.CloudDrive.StorageServiceTest;

import com.anant.CloudDrive.StorageManager.*;
import com.anant.CloudDrive.StorageManager.Models.UploadIdRequest;
import com.anant.CloudDrive.StorageManager.Models.UploadPartRequest_;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@SpringBootTest
public class StorageManagerTest2 {

    @MockBean
    SubscriptionService subscriptionService;

    @MockBean
    StorageProvider storageProvider;

    @Autowired
    StorageManager manager;

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

    @Test
    public void uploadPartTest() {
        when(subscriptionService.getTier(anyString())).thenReturn("200"); // 200 mb
        when(storageProvider.initializeUpload(anyString(), any(UploadRecord.class), any(UploadIdRequest.class))).thenReturn(true);
        when(storageProvider.uploadPart(any(UploadRecord.class), any(UploadPartRequest_.class))).thenReturn(true);
        String uploadId = manager.getUploadId(new UploadIdRequest("testFile", "audio/Flac"), "0987654321", "AnantSingh");
        UploadPartRequest_ req = new UploadPartRequest_(null, uploadId, 0L);
        boolean uploadPartRes = manager.uploadPart(req, "0987654321");
        ArgumentCaptor<UploadRecord> argumentCaptor = ArgumentCaptor.forClass(UploadRecord.class);
        ArgumentCaptor<UploadPartRequest_> argumentCaptor2 = ArgumentCaptor.forClass(UploadPartRequest_.class);
        verify(storageProvider).uploadPart(argumentCaptor.capture(), argumentCaptor2.capture());
        Assertions.assertTrue(uploadPartRes);
    }

    //below tests pretty much covers entire upload lifecycle in its entirety
    @Test
    public void completeUploadTest(){
        when(subscriptionService.getTier(anyString())).thenReturn("200"); // 200 mb
        when(storageProvider.initializeUpload(anyString(), any(UploadRecord.class), any(UploadIdRequest.class))).thenReturn(true);
        when(storageProvider.uploadPart(any(UploadRecord.class), any(UploadPartRequest_.class))).thenReturn(true);
        when(storageProvider.completeUpload(any(UploadRecord.class))).thenReturn(true);
        String uploadId = manager.getUploadId(new UploadIdRequest("testFile", "audio/Flac"), "0987654321", "AnantSingh");

       // doing multipart upload
        UploadPartRequest_ req = new UploadPartRequest_(null, uploadId, 0L);
        manager.uploadPart(req, "0987654321");
        UploadPartRequest_ req2 = new UploadPartRequest_(null, uploadId, 0L);
        manager.uploadPart(req, "0987654321");
        UploadPartRequest_ req3 = new UploadPartRequest_(null, uploadId, 0L);
        manager.uploadPart(req, "0987654321");

        boolean completeUploadResult = manager.completeUpload(uploadId, "0987654321");
        Assertions.assertTrue(completeUploadResult);
    }

    @Test
    public void completeUploadTestShouldFailWhenPartUploadStepSkipped(){
        when(subscriptionService.getTier(anyString())).thenReturn("200"); // 200 mb
        when(storageProvider.initializeUpload(anyString(), any(UploadRecord.class), any(UploadIdRequest.class))).thenReturn(true);
        when(storageProvider.completeUpload(any(UploadRecord.class))).thenReturn(true);
        String uploadId = manager.getUploadId(new UploadIdRequest("testFile", "audio/Flac"), "0987654321", "AnantSingh");

        boolean completeUploadResult = manager.completeUpload(uploadId, "0987654321");
        Assertions.assertFalse(completeUploadResult);
    }

    // from "INITIALIZED" -----> "IN_PROGRESS" ------> "COMPLETED"
    @Test
    public void ValidateUploadRecordLifeCycleStateTransitions(@Autowired StorageManager.UploadSessionsHolder2 holder){
        when(subscriptionService.getTier(anyString())).thenReturn("200"); // 200 mb
        when(storageProvider.initializeUpload(anyString(), any(UploadRecord.class), any(UploadIdRequest.class))).thenReturn(true);
        when(storageProvider.uploadPart(any(UploadRecord.class), any(UploadPartRequest_.class))).thenReturn(true);
        when(storageProvider.completeUpload(any(UploadRecord.class))).thenReturn(true);
        String uploadId = manager.getUploadId(new UploadIdRequest("testFile", "audio/Flac"), "0987654321", "AnantSingh");

        StorageManager.UploadSession2 session1 = holder.getExistingSession("0987654321");
        UploadRecordState state =  session1.getRecord(uploadId).getState();

        //fetching uploadID will cause the state to change from null to INITIALIZED
        Assertions.assertEquals(state, UploadRecordState.INITIALIZED);

        ////////////////// UPLOADING A PART //////////////////////////////////////////////////
        UploadPartRequest_ req = new UploadPartRequest_(null, uploadId, 0L);
        manager.uploadPart(req, "0987654321");
        /////////////////////////////////////////////////////////////////////////////////////

        StorageManager.UploadSession2 session2 = holder.getExistingSession("0987654321");
        UploadRecordState state2 =  session2.getRecord(uploadId).getState();

        Assertions.assertEquals(state2, UploadRecordState.IN_PROGRESS);

        //////////////////// UPLOADING A PART //////////////////////////////////////////////////
        UploadPartRequest_ req2 = new UploadPartRequest_(null, uploadId, 0L);
        manager.uploadPart(req, "0987654321");
        ////////////////////////////////////////////////////////////////////////////////////////

        //////////////////// UPLOADING A PART //////////////////////////////////////////////////
        UploadPartRequest_ req3 = new UploadPartRequest_(null, uploadId, 0L);
        manager.uploadPart(req, "0987654321");
        ///////////////////////////////////////////////////////////////////////////////////////

        boolean completeUploadResult = manager.completeUpload(uploadId, "0987654321");

        StorageManager.UploadSession2 session3= holder.getExistingSession("0987654321");
        UploadRecordState state3 =  session3.getRecord(uploadId).getState();

        // final LifeCycle State for a Upload
        Assertions.assertEquals(state3, UploadRecordState.COMPLETED);

        Assertions.assertTrue(completeUploadResult);
    }
}