package com.anant.CloudDrive.IntegrationTest;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.security.test.context.support.WithMockUser;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class E2EControllerTest {

   @LocalServerPort
   private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    @WithMockUser(username = "testUser@mail.com", password = "1234")
    void greetingShouldReturnDefaultMessage() throws Exception {
        var res = this.restTemplate.getForObject("http://localhost:" + port + "/user/home", String.class);
    }

}
