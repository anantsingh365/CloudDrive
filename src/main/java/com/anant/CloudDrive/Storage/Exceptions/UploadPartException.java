package com.anant.CloudDrive.Storage.Exceptions;

public class UploadPartException extends Exception{
    public UploadPartException(String message){
       super("Cannot Process Upload Part, Reason - " + message);
    }
}
