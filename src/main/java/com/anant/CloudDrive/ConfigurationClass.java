package com.anant.CloudDrive;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackages = "com.anant.CloudDrive")
public class ConfigurationClass {
    static{
        System.out.println("Custom Configuration class");
    }

}
