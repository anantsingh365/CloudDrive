package com.anant.CloudDrive.IntegrationTest;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;

public class APITests {

    public static void main(String[] args) {
        String Cookie = "";
        String redirectedLocation = "";
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = null;

        try {
            request = HttpRequest.newBuilder()
                    .uri(new URI("http://localhost:9500/login"))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString("username=test@mail.com&password=7611"))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
           // System.out.println(response.statusCode());
           // System.out.println(response.body());

            for (Map.Entry<String, List<String>> s : response.headers().map().entrySet()) {
                if (s.getKey().equals("location")) {
                 //   System.out.println("Redirected location is - " + s.getValue().get(0));
                    redirectedLocation = s.getValue().get(0);
                }
            }
            //set the cookie
            Cookie = response.headers().firstValue("Set-Cookie").orElse("");

            // hit the secured endpoint
            HttpRequest protectedRequest = HttpRequest.newBuilder()
                    .uri(new URI("http://localhost:9500/user/home"))
                    .header("Cookie", Cookie)
                    .build();

            HttpResponse<String> protectedResponse = client.send(protectedRequest, HttpResponse.BodyHandlers.ofString());
         //   System.out.println(protectedResponse.body());

        } catch (URISyntaxException | IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
