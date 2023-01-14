package com.anant.CloudDrive;

import com.amazonaws.auth.PropertiesFileCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class CloudDriveApplication {

	public static void main(String[] args) {
		SpringApplication.run(CloudDriveApplication.class, args);
	}

	@Bean
	public AmazonS3 getS3Client(){
		return AmazonS3ClientBuilder
				.standard()
				.withCredentials(new PropertiesFileCredentialsProvider("src/main/resources/S3Credentials.properties"))
				.withRegion(Regions.AP_SOUTH_1)
				.build();
	}
}