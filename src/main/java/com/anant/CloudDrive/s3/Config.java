package com.anant.CloudDrive.s3;

import com.amazonaws.auth.PropertiesFileCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@ComponentScan("com.anant.CloudDrive")
public class Config {

    @Bean
    public AmazonS3 getS3Client(){
        return AmazonS3ClientBuilder
                .standard()
                .withCredentials(new PropertiesFileCredentialsProvider("src/main/resources/S3Credentials.properties"))
                .withRegion(Regions.AP_SOUTH_1)
                .build();
    }
}
