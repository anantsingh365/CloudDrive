package com.anant.CloudDrive.E2ETest;

import com.anant.CloudDrive.Storage.*;
import com.anant.CloudDrive.Storage.Models.UploadIdRequest;
import com.anant.CloudDrive.Storage.Models.UploadPartRequest;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

@SpringBootTest
@Tag("FunctionalFlow")
public class StorageManagerTest {

    @Autowired StorageManager manager;
    @Autowired StorageManager.UploadSessionsHolder2 holder;
    @Value("${test.usermail}") String testMail;
    private final String sampleFileToUploadPath = "src/test/resources/SampleUploadFile";

    @BeforeEach
    public void setUpUser(){
    }

    public List<UploadPartRequest> getUploadPartRequests(String uploadId){
        File sampleUploadFile = new File(sampleFileToUploadPath);
        try {
            //same size for test convenience
            long fileSize = sampleUploadFile.length();

            //list of 3 Upload parts request
            return List.of(new UploadPartRequest(new FileInputStream(sampleUploadFile), uploadId, fileSize),
                    new UploadPartRequest(new FileInputStream(sampleUploadFile), uploadId, fileSize),
                    new UploadPartRequest(new FileInputStream(sampleUploadFile), uploadId, fileSize));

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void GetNewUploadIdFailure() {
        String uploadId = manager.getUploadId(new UploadIdRequest("testFile", "audio/Flac"), "0987654321", testMail);
        System.out.println(uploadId);
        Assertions.assertNotNull(uploadId);
    }

    //below tests pretty much covers entire upload lifecycle in its entirety
    @Test
    public void completeUploadTest(){
        String uploadId = manager.getUploadId(new UploadIdRequest("testFile", "audio/Flac"), "0987654321", testMail);
       //doing multipart upload
        var requests = getUploadPartRequests(uploadId);
        try {
            var uploadRes1 = manager.uploadPart(requests.get(0), "0987654321");
            requests.get(0).getInputStream().close();
            Assertions.assertTrue(uploadRes1);

            //uploading part 2
            var uploadRes2 = manager.uploadPart(requests.get(1), "0987654321");
            requests.get(1).getInputStream().close();
            Assertions.assertTrue(uploadRes2);

            //uploading part 3
            var uploadRes3 = manager.uploadPart(requests.get(2), "0987654321");
            requests.get(2).getInputStream().close();
            Assertions.assertTrue(uploadRes3);

            var completeUploadResult = manager.completeUpload(uploadId, "0987654321");
            Assertions.assertTrue(completeUploadResult);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void completeUploadTestShouldFailWhenPartUploadStepSkipped(){
        String uploadId = manager.getUploadId(new UploadIdRequest("testFile", "audio/Flac"), "0987654321", "AnantSingh");
        boolean completeUploadResult = manager.completeUpload(uploadId, "0987654321");
        Assertions.assertFalse(completeUploadResult);
    }

    // from "INITIALIZED" -----> "IN_PROGRESS" ------> "COMPLETED"
    @Test
    public void ValidateUploadRecordLifeCycleStateTransitions(){
        String uploadId = manager.getUploadId(new UploadIdRequest("__testFile", "audio/Flac"), "0987654321", testMail);
        var requests = getUploadPartRequests(uploadId);

        StorageManager.UploadSession2 session1 = holder.getExistingSession("0987654321");
        UploadRecordState state =  session1.getRecord(uploadId).getState();

        //fetching uploadID will cause the state to change from null to INITIALIZED
        Assertions.assertEquals(state, UploadRecordState.INITIALIZED);

        ////////////////// UPLOADING A PART //////////////////////////////////////////////////
        manager.uploadPart(requests.get(0), "0987654321");

        StorageManager.UploadSession2 session2 = holder.getExistingSession("0987654321");
        UploadRecordState state2 =  session2.getRecord(uploadId).getState();

        //After first chunk is successfully uploaded, upload state should be "IN_PROGRESS"
        Assertions.assertEquals(state2, UploadRecordState.IN_PROGRESS);

        manager.uploadPart(requests.get(1), "0987654321");
        manager.uploadPart(requests.get(2), "0987654321");

        boolean completeUploadResult = manager.completeUpload(uploadId, "0987654321");

        StorageManager.UploadSession2 session3= holder.getExistingSession("0987654321");
        UploadRecordState state3 =  session3.getRecord(uploadId).getState();

        // final LifeCycle State for a Upload
        Assertions.assertEquals(state3, UploadRecordState.COMPLETED);
        Assertions.assertTrue(completeUploadResult);
    }

    @Test
    public void Validate_All_The_StorageProvider_Methods_Are_Being_Called_ByTheStorage_Manager_For_A_Successful_Upload(@Autowired StorageManager.UploadSessionsHolder2 holder)
    {
        String uploadId = manager.getUploadId(new UploadIdRequest("testFile", "audio/Flac"), "0987654321", "AnantSingh");
        var requests = getUploadPartRequests(uploadId);

        ////////////////// UPLOADING PARTS //////////////////////////////////////////////////
        manager.uploadPart(requests.get(0), "0987654321");
        manager.uploadPart(requests.get(1), "0987654321");
        manager.uploadPart(requests.get(2), "0987654321");

        ////////////////////////////COMPLETE THE UPLOAD////////////////////////////////////////
        manager.completeUpload(uploadId, "0987654321");
    }
}