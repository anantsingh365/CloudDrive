package com.anant.CloudDrive.StorageServiceTest;

import com.anant.CloudDrive.Storage.*;
import com.anant.CloudDrive.Storage.Models.UploadIdRequest;
import com.anant.CloudDrive.Storage.Models.UploadPartRequest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
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
    public void GetNewUploadIdFailure() {
        when(subscriptionService.getTier(anyString())).thenReturn("100"); // 100 mb
        when(storageProvider.getStorageUsedByUser(any(String.class))).thenReturn(150000000L);//roughly 150 mb from bytes to mb
        String uploadId = manager.getUploadId(new UploadIdRequest("testFile", "audio/Flac"), "0987654321", "AnantSingh");
        System.out.println("Success Upload ID creation Test - " + uploadId);
        Assertions.assertEquals("Account Upgrade", uploadId);
    }

    //below tests pretty much covers entire upload lifecycle in its entirety
    @Test
    public void completeUploadTest(){
        when(subscriptionService.getTier(anyString())).thenReturn("200"); // 200 mb
        when(storageProvider.initializeUpload(anyString(), any(UploadRecord.class), any(UploadIdRequest.class))).thenReturn(true);
        when(storageProvider.uploadPart(any(UploadRecord.class), any(UploadPartRequest.class))).thenReturn(true);
        when(storageProvider.completeUpload(any(UploadRecord.class))).thenReturn(true);
        String uploadId = manager.getUploadId(new UploadIdRequest("testFile", "audio/Flac"), "0987654321", "AnantSingh");

       // doing multipart upload
        UploadPartRequest req = new UploadPartRequest(null, uploadId, 0L);
        boolean partUploadRes = manager.uploadPart(req, "0987654321");
        UploadPartRequest req2 = new UploadPartRequest(null, uploadId, 0L);
        manager.uploadPart(req, "0987654321");
        UploadPartRequest req3 = new UploadPartRequest(null, uploadId, 0L);
        manager.uploadPart(req, "0987654321");

        boolean completeUploadResult = manager.completeUpload(uploadId, "0987654321");
        Assertions.assertTrue(completeUploadResult);
        Assertions.assertTrue(partUploadRes);
    }

    @Test
    public void sending(){}

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
        when(storageProvider.uploadPart(any(UploadRecord.class), any(UploadPartRequest.class))).thenReturn(true);
        when(storageProvider.completeUpload(any(UploadRecord.class))).thenReturn(true);
        String uploadId = manager.getUploadId(new UploadIdRequest("testFile", "audio/Flac"), "0987654321", "AnantSingh");

        StorageManager.UploadSession2 session1 = holder.getExistingSession("0987654321");
        UploadRecordState state =  session1.getRecord(uploadId).getState();

        //fetching uploadID will cause the state to change from null to INITIALIZED
        Assertions.assertEquals(state, UploadRecordState.INITIALIZED);

        ////////////////// UPLOADING A PART //////////////////////////////////////////////////
        UploadPartRequest req = new UploadPartRequest(null, uploadId, 0L);
        manager.uploadPart(req, "0987654321");
        /////////////////////////////////////////////////////////////////////////////////////

        StorageManager.UploadSession2 session2 = holder.getExistingSession("0987654321");
        UploadRecordState state2 =  session2.getRecord(uploadId).getState();

        //After first chunk is successfully uploaded, upload state should be "IN_PROGRESS"
        Assertions.assertEquals(state2, UploadRecordState.IN_PROGRESS);

        //////////////////// UPLOADING A PART //////////////////////////////////////////////////
        UploadPartRequest req2 = new UploadPartRequest(null, uploadId, 0L);
        manager.uploadPart(req, "0987654321");
        ////////////////////////////////////////////////////////////////////////////////////////

        //////////////////// UPLOADING A PART /////////////////////////////////////////////////
        UploadPartRequest req3 = new UploadPartRequest(null, uploadId, 0L);
        manager.uploadPart(req, "0987654321");
        ///////////////////////////////////////////////////////////////////////////////////////

        boolean completeUploadResult = manager.completeUpload(uploadId, "0987654321");

        StorageManager.UploadSession2 session3= holder.getExistingSession("0987654321");
        UploadRecordState state3 =  session3.getRecord(uploadId).getState();

        // final LifeCycle State for a Upload
        Assertions.assertEquals(state3, UploadRecordState.COMPLETED);
       // verify(storageProvider.completeUpload()).
        Assertions.assertTrue(completeUploadResult);
    }

    @Test
    public void ValidateAllTheStorageProviderMethodsAreBeingCalledByTheStorageManager(@Autowired StorageManager.UploadSessionsHolder2 holder)
    {
        when(subscriptionService.getTier(anyString())).thenReturn("200"); // 200 mb
        when(storageProvider.initializeUpload(anyString(), any(UploadRecord.class), any(UploadIdRequest.class))).thenReturn(true);
        when(storageProvider.uploadPart(any(UploadRecord.class), any(UploadPartRequest.class))).thenReturn(true);
        when(storageProvider.completeUpload(any(UploadRecord.class))).thenReturn(true);
        String uploadId = manager.getUploadId(new UploadIdRequest("testFile", "audio/Flac"), "0987654321", "AnantSingh");

        ////////////////// UPLOADING PART 1 //////////////////////////////////////////////////
        UploadPartRequest req = new UploadPartRequest(null, uploadId, 0L);
        manager.uploadPart(req, "0987654321");
        /////////////////////////////////////////////////////////////////////////////////////

        //////////////////// UPLOADING PART 2 //////////////////////////////////////////////////
        UploadPartRequest req2 = new UploadPartRequest(null, uploadId, 0L);
        manager.uploadPart(req, "0987654321");
        ////////////////////////////////////////////////////////////////////////////////////////

        //////////////////// UPLOADING PART 3 //////////////////////////////////////////////////
        UploadPartRequest req3 = new UploadPartRequest(null, uploadId, 0L);
        manager.uploadPart(req, "0987654321");
        ///////////////////////////////////////////////////////////////////////////////////////

        ////////////////////////////COMPLETE THE UPLOAD////////////////////////////////////////
        manager.completeUpload(uploadId, "0987654321");

        verify(storageProvider, atMost(1)).initializeUpload(any(String.class), any(UploadRecord.class), any(UploadIdRequest.class));
        verify(storageProvider, atLeastOnce()).uploadPart(any(UploadRecord.class), any(UploadPartRequest.class));
        verify(storageProvider, atMost(1)).completeUpload(any(UploadRecord.class));
    }
}