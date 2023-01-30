package com.anant.CloudDrive.s3.UserUploads;

import org.slf4j.Logger;
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

    public  UploadSession getSession(String sessionId){
        var userSession = getExistingSession(sessionId);
        //if there is no active user session present, create new
        //this will be created only once per sessionId
        if(userSession == null){
            return createNewSession(sessionId);
        }
        //each time a new upload id has to be generated
        return userSession;
    }

    public UploadSession getExistingSession(String session){
        return sessions.get(session);
    }
    private UploadSession createNewSession(String userName){
        var uploadSession = context.getBean(UploadSession.class);
        sessions.put(userName, uploadSession);
        return uploadSession;
    }
}