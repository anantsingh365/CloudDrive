package com.anant.CloudDrive.IntegrationTest;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

import java.io.*;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class ControllerLevelTest {
//
//    @Autowired private MockMvc mvc;
//    @Autowired private WebApplicationContext context;
//    @Value("${test.usermail}") String testMail;
//    private MockHttpSession session;
//
//    //kind of a ...hack/ugly? UploadLifecycle is tied to the SessionID as an internal state for the application code.
//    //so we need the same sessionIds across the tests
//    //@BeforeEach
//    public synchronized void setMockSession(){
//           if(this.session == null){
//               session = new MockHttpSession();
//               for(int i = 0; i < 10; ++i ){
//                   session.changeSessionId();
//               }
//           }
//    }
//
////    @Test
//    public void testingHomeController(){
//        try {
//            mvc.perform(get("/user/home").session(session)
//                    .with(user(testMail)
//                    .password("1234")
//                    .roles("USER")))
//                    .andExpect(status().isOk());
//
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
//    }
//
//    public String getUploadId(){
//        setMockSession();
//        try {
//            String uploadIdPostPayload = "{ \"filename\": \"sampleFile\",\"mimetype\": \"MP3\",\"contenttype\": \"MP3\"}";
//            var uploadId= mvc.perform(post("/user/uploadId")
//                            .session(this.session)
//                            .contentType(MediaType.APPLICATION_JSON)
//                            .content(uploadIdPostPayload)
//                            .with(user(testMail)
//                                    .password("1234")
//                                    .roles("USER")))
//                            .andExpect(status().isOk())
//                    .andReturn().getResponse().getContentAsString();
//            return uploadId;
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
//    }
//
//    @Test
//    public void uploadParts(){
//       setMockSession();
//        File sampleFile  = new File("src/test/resources/SampleUploadFile");
//        FileInputStream ins;
//        try {
//            ins = new FileInputStream(sampleFile);
//            HttpHeaders httpHeaders = new HttpHeaders();
//            //httpHeaders.add("content-length", String.valueOf(sampleFile.length()));
//            httpHeaders.add("user-id", getUploadId());
//
//            var response = mvc.perform(post("/user/uploadFile").content(ins.readAllBytes()).session(this.session).headers(httpHeaders)).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
//            ins.close();
//          //  System.out.println(response);
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
//
////        var uploadId= mvc.perform(post("/user/uploadId")
////                        .contentType(MediaType.MULTIPART_FORM_DATA)
////                        .content()
////                        .with(user(testMail)
////                                .password("1234")
////                                .roles("USER")))
////                .andExpect(status().isOk())
////                .andReturn()
////                .getResponse()
////                .getContentAsString();
//    }
//
////
////   @Test
////   public void performFileUpload() {
////       // var uploadId = this.getUploadId();
////
////   }

}
