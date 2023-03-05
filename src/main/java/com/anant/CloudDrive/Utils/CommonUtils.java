package com.anant.CloudDrive.Utils;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestContextHolder;

public class CommonUtils {

    public static String getUserData(signedInUser requestedData){
        switch (requestedData){
            case GET_SESSIONID -> {
                var sessionId = RequestContextHolder.currentRequestAttributes().getSessionId();
                System.out.println("Session id from requestContextHolder is - " + sessionId);
                return sessionId;
            }
            case GET_USERNAME -> {
                return SecurityContextHolder.getContext().getAuthentication().getName();
            }
            case GET_AUTHORITIES -> {
                return SecurityContextHolder.getContext().getAuthentication().getAuthorities().toString();
            }
        }
        return null;
    }
    public enum signedInUser {
        GET_SESSIONID,
        GET_USERNAME,
        GET_AUTHORITIES
    }

}
