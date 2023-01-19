package com.anant.CloudDrive.UserUploads;

import com.anant.CloudDrive.GetApplicationContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

@Component
public class UserUploadSessions {

    @Autowired
    private  GetApplicationContext context;

    private final ConcurrentHashMap<String, UserUploadSession> sessions = new ConcurrentHashMap<>();

    public UserUploadSessions(){}

    public  String getUploadId(String userName, String keyName){
        var userUploadSession = getUserUploadSession(userName);
        if(userUploadSession == null){
             insertUserUploadSession(userName, context.getApplicationContext().getBean(UserUploadSession.class));
             UserUploadSession newUserUploadSession = getUserUploadSession(userName);
             return newUserUploadSession.getUserSpecificUploadId(userName, keyName);
        }
        return userUploadSession.getUserSpecificUploadId(userName, keyName);
    }

    private  UserUploadSession getUserUploadSession(String userName){
        return sessions.get(userName);
    }

    private void insertUserUploadSession(String userName, UserUploadSession userUploadSession){
        sessions.put(userName, userUploadSession);
    }
}