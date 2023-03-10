package com.anant.CloudDrive.service.Uploads;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.Enumeration;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class UploadSessionsHolder {

     private final ApplicationContext context;
     private final Logger logger;

    public UploadSessionsHolder(@Autowired ApplicationContext context, @Autowired Logger logger){
        this.context = context;
        this.logger = logger;
    }

    //stores current upload sessions for users
    private final ConcurrentHashMap<String, UploadSession> sessions = new ConcurrentHashMap<>();

    public UploadSession getSession(String sessionId){
        var userSession = getExistingSession(sessionId);
        //if there is no active user session present, create new
        //this will be created only once per sessionId
        if(userSession == null){
            return createNewSession(sessionId);
        }
        //each time a new upload id has to be generated
        return userSession;
    }

    public UploadSession getExistingSession(String sessionId){
        return sessions.get(sessionId);
    }
    private UploadSession createNewSession(String userName){
        var uploadSession = context.getBean(UploadSession.class);
        sessions.put(userName, uploadSession);
        return uploadSession;
    }
    public void removeUploadSession(String sessionId){
        sessions.remove(sessionId);
    }

    public Enumeration<String> getActiveSessionIds(){
        return sessions.keys();
    }
}