package com.anant.CloudDrive.MultipleUsersUploadManagementTest;

import com.anant.CloudDrive.StorageManager.UploadSession;
import com.anant.CloudDrive.StorageManager.UploadRecord;
import com.anant.CloudDrive.StorageManager.Models.UploadIdRequest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Assertions;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;

import org.springframework.context.ApplicationContext;

import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;

public class UploadSessionUnitTest {

    private final List<UploadRecord> UploadRecordsMocks = List.of(mock(UploadRecord.class),
            mock(UploadRecord.class)
            , mock(UploadRecord.class));

    private UploadSession session;

    @BeforeEach
    public void setup() {
        ApplicationContext context = mock(ApplicationContext.class);
        Logger logger = mock(Logger.class);
        session = new UploadSession(context, logger);
        when(context.getBean(UploadRecord.class)).thenReturn(UploadRecordsMocks.get(0), UploadRecordsMocks.get(1), UploadRecordsMocks.get(2));
    }

    @Test
    public void CorrectUploadRecordsAreBeingReturned() throws Exception {
        String uploadId1 = session.createRecord("testUser1", new UploadIdRequest("testFile1", "mp4"));
        String uploadId2 = session.createRecord("testUser2", new UploadIdRequest("testFile2", "mp4"));
        String uploadId3 = session.createRecord("testUser3", new UploadIdRequest("testFile3", "mp4"));

        //for every unique upload ID we have a corresponding upload record, this test is basically to make sure
        // that upload ID is getting generated and correct upload record associated with upload ID is being returned;
        var _record1 = session.getRecord(uploadId1);
        Assertions.assertEquals(UploadRecordsMocks.get(0),_record1);

        var _record2 = session.getRecord(uploadId2);
        Assertions.assertEquals(UploadRecordsMocks.get(1),_record2);

        var _record3 = session.getRecord(uploadId3);
        Assertions.assertEquals(UploadRecordsMocks.get(2), _record3);
    }

    @Test
    public void NullIsBeingReturnedForInvalidUploadId(){
        String invalidUploadId = UUID.randomUUID().toString();
        var invalidRecord = session.getRecord(invalidUploadId);
        Assertions.assertNull(invalidRecord);
    }
}