package com.anant.CloudDrive;

import com.anant.CloudDrive.S3.S3Download;
import com.sun.tools.jconsole.JConsoleContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class ConsoleRunner implements CommandLineRunner {

    @Autowired
    public S3Download s3Download;

    @Override
    public void run(String... args) throws Exception {
        System.out.println(s3Download.hashCode() + " " + "inside the console runner");

    }
}
