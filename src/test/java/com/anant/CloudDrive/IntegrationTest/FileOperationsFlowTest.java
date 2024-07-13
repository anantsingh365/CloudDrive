package com.anant.CloudDrive.IntegrationTest;

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
public class FileOperationsFlowTest {

    @Value("${test.usermail}") String testMail;
    @Autowired StorageManager manager;
    @Autowired StorageManager.UploadSessionsHolder2 holder;
    private final String sampleFileToUploadPath = "src/test/resources/SampleUploadFile";

    private List<UploadPartRequest> getUploadPartRequests(final String uploadId) {
        final File sampleUploadFile = new File(sampleFileToUploadPath);
        try {
            //same size for test convenience
            final long fileSize = sampleUploadFile.length();

            //list of 3 Upload parts request
            return List.of(new UploadPartRequest(new FileInputStream(sampleUploadFile), uploadId, fileSize)
                            ,new UploadPartRequest(new FileInputStream(sampleUploadFile), uploadId, fileSize)
                            ,new UploadPartRequest(new FileInputStream(sampleUploadFile), uploadId, fileSize));


        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void GetNewUploadIdFailure() {
        final String uploadId = manager.getUploadId(new UploadIdRequest("testFile", "audio/Flac"), "0987654321", testMail);
        System.out.println(uploadId);
        Assertions.assertNotNull(uploadId);
    }

    //below tests pretty much covers entire upload lifecycle in its entirety
    @Test
    public void completeUploadTest() {

    }

    @Test
    public void completeUploadTestShouldFailWhenPartUploadStepSkipped() {
        final String uploadId = manager.getUploadId(new UploadIdRequest("testFile", "audio/Flac"), "0987654321", "AnantSingh");
        final boolean completeUploadResult = manager.completeUpload(uploadId, "0987654321");
        Assertions.assertFalse(completeUploadResult);
    }

    // from "INITIALIZED" -----> "IN_PROGRESS" ------> "COMPLETED"
    @Test
    public void Validate_Upload_With_Life_Cycle_State_Transitions() {

        final String uploadId = manager.getUploadId(new UploadIdRequest("__testFile", "audio/Flac"), "0987654321", testMail);
        final var requests = getUploadPartRequests(uploadId);

        final StorageManager.UploadSession2 session1 = holder.getExistingSession("0987654321");
        final UploadRecordState state = session1.getRecord(uploadId).getState();

        //fetching uploadID will cause the state to change from null to INITIALIZED
        Assertions.assertEquals(UploadRecordState.INITIALIZED, state);

        try {
            ////////////////// UPLOADING A PART //////////////////////////////////////////////////
            boolean res1 = manager.uploadPart(requests.get(0), "0987654321");
            requests.get(0).getInputStream().close();

            Assertions.assertTrue(res1);

            final StorageManager.UploadSession2 session2 = holder.getExistingSession("0987654321");
            final UploadRecordState state2 = session2.getRecord(uploadId).getState();

            //After first chunk is successfully uploaded, upload state should be "IN_PROGRESS"
            Assertions.assertEquals(state2, UploadRecordState.IN_PROGRESS);

            boolean res2 = manager.uploadPart(requests.get(1), "0987654321");
            boolean res3 = manager.uploadPart(requests.get(2), "0987654321");
            Assertions.assertTrue(res2);
            Assertions.assertTrue(res3);

            boolean completeUploadResult = manager.completeUpload(uploadId, "0987654321");
            Assertions.assertTrue(completeUploadResult);

            final StorageManager.UploadSession2 session3 = holder.getExistingSession("0987654321");
            final UploadRecordState state3 = session3.getRecord(uploadId).getState();
            Assertions.assertEquals(UploadRecordState.COMPLETED, state3);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void Validate_All_The_StorageProvider_Methods_Are_Being_Called_ByTheStorage_Manager_For_A_Successful_Upload(@Autowired StorageManager.UploadSessionsHolder2 holder) {

    }
}