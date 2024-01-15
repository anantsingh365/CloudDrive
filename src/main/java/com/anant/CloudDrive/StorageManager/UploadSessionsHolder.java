package com.anant.CloudDrive.StorageManager;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

@Component
public class UploadSessionsHolder {
     private final ApplicationContext context;
     private final Logger logger;
     private final ConcurrentHashMap<String, UploadSession> sessions = new ConcurrentHashMap<>();

     // one session ID --has---> one upload Session --has---> multiple Upload Records
    public UploadSessionsHolder(@Autowired ApplicationContext context, @Autowired Logger logger){
        this.context = context;
        this.logger = logger;
    }

    public UploadSession getSession(String sessionId){
        var userSession = getExistingSession(sessionId);
        if(userSession == null){
            return createNewSession(sessionId);
        }
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
}