package com.anant.CloudDrive.MultipleUsersUploadManagementTest;

import com.anant.CloudDrive.UploadManager.UploadSession;
import com.anant.CloudDrive.UploadManager.UploadSessionsHolder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.springframework.context.ApplicationContext;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class UploadSessionsHolderUnitTest {

    private ApplicationContext context;
    private Logger logger;

    @BeforeEach
    public void setup() {
        context = mock(ApplicationContext.class);
        logger = mock(Logger.class);
    }

    @Test
    public void getAUserUploadSession() {
        UploadSessionsHolder uploadSessionsHolder = new UploadSessionsHolder(context, logger);

        //we are going to supply multiple upload session to uploadSessionHolder class and verify whether we get
        // the first one because that will be associated with the session id "1234" and not with any other session id
        UploadSession mockSuppliedUploadSession = mock(UploadSession.class);
        UploadSession mockSuppliedUploadSession2 = mock(UploadSession.class);
        UploadSession mockSuppliedUploadSession3 = mock(UploadSession.class);

        when(context.getBean(UploadSession.class)).thenReturn(mockSuppliedUploadSession, mockSuppliedUploadSession2, mockSuppliedUploadSession3);

        var uploadSession = uploadSessionsHolder.getSession("1234");
        uploadSessionsHolder.getSession("87654");
        uploadSessionsHolder.getSession("9876543");

        Assertions.assertEquals(mockSuppliedUploadSession, uploadSession);
    }

    @Test
    public void getNullUploadSessionForInvalidSessionId() {
        UploadSessionsHolder uploadSessionsHolder = new UploadSessionsHolder(context, logger);
        var uploadSession = uploadSessionsHolder.getExistingSession("3456");
        Assertions.assertNull(uploadSession);
    }
}
