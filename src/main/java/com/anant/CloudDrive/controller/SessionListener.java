package com.anant.CloudDrive.controller;


import jakarta.servlet.http.HttpSessionEvent;
import jakarta.servlet.http.HttpSessionListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SessionListener implements HttpSessionListener {
//
//    @Autowired
//    UploadSessionsHolder uploadSessionsHolder;
//
//    @Override
//    public void sessionCreated(HttpSessionEvent se) {
//        System.out.println("A Session Created");
//    }
//
//    @Override
//    public void sessionDestroyed(HttpSessionEvent se) {
//        String sessionId = se.getSession().getId();
//        //uploadSessionsHolder.removeUploadSession(sessionId);
//        System.out.println("Upload Session - " + sessionId + " removed");
//        System.out.println("Active Upload Sessions");
////        Iterator<String> iter = uploadSessionsHolder.getActiveSessionIds().asIterator();
////        while(iter.hasNext()){
////            System.out.println(iter.next());
////        }
//    }
}