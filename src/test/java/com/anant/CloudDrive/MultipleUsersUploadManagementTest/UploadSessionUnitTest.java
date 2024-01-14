package com.anant.CloudDrive.MultipleUsersUploadManagementTest;

import com.anant.CloudDrive.StorageProviders.Uploads.UploadEntry;
import com.anant.CloudDrive.UploadManager.UploadSession;
import com.anant.CloudDrive.StorageProviders.requests.UploadIdRequest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Assertions;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;

import org.springframework.context.ApplicationContext;

import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;

public class UploadSessionUnitTest {

    final List<UploadEntry> UploadEntriesMocks = List.of(mock(UploadEntry.class),
            mock(UploadEntry.class)
            , mock(UploadEntry.class));

    UploadSession session;

    @BeforeEach
    public void setup() {
        ApplicationContext context = mock(ApplicationContext.class);
        Logger logger = mock(Logger.class);
        session = new UploadSession(context, logger);
        when(context.getBean(UploadEntry.class)).thenReturn(UploadEntriesMocks.get(0), UploadEntriesMocks.get(1), UploadEntriesMocks.get(2));
    }

    @Test
    public void CorrectUploadEntriesAreBeingReturned() throws Exception {
        String uploadId1 = session.registerUploadId("testUser1", new UploadIdRequest("testFile1", "mp4"));
        String uploadId2 = session.registerUploadId("testUser2", new UploadIdRequest("testFile2", "mp4"));
        String uploadId3 = session.registerUploadId("testUser3", new UploadIdRequest("testFile3", "mp4"));

        //for every unique upload ID we have a corresponding upload Entry, this test is basically to make sure
        // that upload ID is getting generated and correct upload entry associated with upload ID is being returned;
        var _entry1 = session.getPart(uploadId1);
        Assertions.assertEquals(UploadEntriesMocks.get(0),_entry1);

        var _entry2 = session.getPart(uploadId2);
        Assertions.assertEquals(UploadEntriesMocks.get(1),_entry2);

        var _entry3 = session.getPart(uploadId3);
        Assertions.assertEquals(UploadEntriesMocks.get(2), _entry3);
    }

    @Test
    public void NullIsBeingReturnedForInvalidUploadId(){
        String invalidUploadId = UUID.randomUUID().toString();
        var invalidEntry = session.getPart(invalidUploadId);
        Assertions.assertNull(invalidEntry);
    }
}