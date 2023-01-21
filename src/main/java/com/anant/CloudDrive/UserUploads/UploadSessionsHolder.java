package com.anant.CloudDrive.UserUploads;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

@Component
public class UploadSessionsHolder {

    @Autowired private ApplicationContext context;
    @Autowired private Logger logger;

    //stores current upload sessions for users
    private final ConcurrentHashMap<String, UploadSession> sessions = new ConcurrentHashMap<>();

    public UploadSessionsHolder(){}

    public  UploadSession getSession(String userName, String keyName){
        var userSession = getExistingSession(userName);

        //if there is no active user session present, create new
        //this will be created only once per user
        if(userSession == null){
            return createNewSession(userName, context.getBean(UploadSession.class));
        }
        //each time a new upload id has to be generated
        return userSession;
    }

    public UploadSession getExistingSession(String userName){
        return sessions.get(userName);
    }
    private UploadSession createNewSession(String userName, UploadSession uploadSession){
        var newSession = context.getBean(UploadSession.class);
        sessions.put(userName, newSession);
        return newSession;
    }
}