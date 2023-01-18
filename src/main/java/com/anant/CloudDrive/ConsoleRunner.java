package com.anant.CloudDrive;

import com.amazonaws.services.s3.AmazonS3;
import com.anant.CloudDrive.s3.S3Download;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class ConsoleRunner implements CommandLineRunner {

    @Autowired
    public S3Download s3Download;

    @Autowired
    public AmazonS3 s3client;

    @Override
    public void run(String... args) throws Exception {
//        Arrays.stream(args).forEach(System.out::println);
        s3client.listBuckets().forEach(System.out::println);
    }

}
