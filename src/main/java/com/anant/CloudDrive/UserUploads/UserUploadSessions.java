package com.anant.CloudDrive.UserUploads;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

@Component
public class UserUploadSessions {

    @Autowired
    private ApplicationContext context;

    //stores current upload sessions for users
    private final ConcurrentHashMap<String, UserUploadSession> sessions = new ConcurrentHashMap<>();

    public UserUploadSessions(){}

    public  String getUploadId(String userName, String keyName){
        var userSession = getExistingSession(userName);

        //there is no active user session present, create new
        //this will be created only once per user
        if(userSession == null){
             var newSession = createNewSession(userName, context.getBean(UserUploadSession.class));
             return newSession.getUploadId(userName, keyName);
        }
        //each time a new upload id has to be generated
        return userSession.getUploadId(userName, keyName);
    }

    public UserUploadSession getUserSession(String userName){
        return sessions.get(userName);
    }

    private  UserUploadSession getExistingSession(String userName){
        return sessions.get(userName);
    }

    private UserUploadSession createNewSession(String userName, UserUploadSession userUploadSession){
        var newSession = context.getBean(UserUploadSession.class);
        sessions.put(userName, newSession);
        return newSession;
    }
}