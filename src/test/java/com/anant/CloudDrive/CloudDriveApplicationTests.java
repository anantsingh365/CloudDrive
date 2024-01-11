package com.anant.CloudDrive;

import com.anant.CloudDrive.StorageProviders.StorageService;
import com.anant.CloudDrive.StorageProviders.Uploads.UploadEntry;
import com.anant.CloudDrive.StorageProviders.Uploads.UploadSession;
import com.anant.CloudDrive.StorageProviders.requests.UploadIdRequest;
import com.anant.CloudDrive.StorageProviders.s3.S3UploadEntry;
import com.anant.CloudDrive.controller.Home;
import com.anant.CloudDrive.repository.UserRepository;
import com.anant.CloudDrive.security.UserService;
import com.anant.CloudDrive.service.SubscriptionService;

import org.junit.BeforeClass;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Assertions;

import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

import javax.sql.DataSource;

import java.util.UUID;

import static org.mockito.Mockito.when;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

//@SpringBootTest
@SpringBootTest
@AutoConfigureMockMvc
public class CloudDriveApplicationTests {

//	@Mock
//	private WebApplicationContext Webcontext;
	@Autowired
	private MockMvc mvc;

	@Autowired
	ApplicationContext context;

	@MockBean
	public Logger logger;

	@Autowired
	UploadSession session;

	@MockBean
	public Home home;

//	@MockBean
//	public UploadEntry entry;

//	@MockBean
//	public StorageService service;
//
//	@MockBean
//	public  UserService userService;
//
//	@MockBean
//	public  UserRepository userRepo;
//
//	@MockBean
//	public  SubscriptionService subscriptionService;

	@BeforeEach
	public void setupStorageServiceMock(){
		//creating three entries so that when upload Id is generated we can validate that correct entries are
		// returned being returned by upload session.
		//when(context.getBean(UploadEntry.class)).thenReturn(new S3UploadEntry("first", null, null));
	}

	@Test
	@WithMockUser(roles = {"USER"})
	public void testAEndPoint() throws Exception {
//
//		//UploadSession session = context.getBean(UploadSession.class);
//		String uploadId1 = session.registerUploadId(new UploadIdRequest("testFile1", "mp4"));
//		//UploadSession session2 = context.getBean(UploadSession.class);
//		String uploadId2 = session.registerUploadId(new UploadIdRequest("testFile2", "mp4"));
//		var _entry1 =  session.getEntry(uploadId1);
//
//		Assertions.assertNotNull(_entry1);
//
//		String invalidUploadId = UUID.randomUUID().toString();
//		var invalidEntry = session.getEntry(invalidUploadId);
//
//		Assertions.assertNull(invalidEntry);
	}
}