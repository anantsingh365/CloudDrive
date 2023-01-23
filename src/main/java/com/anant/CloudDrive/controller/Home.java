package com.anant.CloudDrive.controller;

import com.anant.CloudDrive.UserUploads.UploadSession;
import com.anant.CloudDrive.UserUploads.UploadSessionsHolder;
import com.anant.CloudDrive.dto.UserDto;
import com.anant.CloudDrive.entity.User;
import com.anant.CloudDrive.s3.S3MultiPartUpload;
import com.anant.CloudDrive.service.UserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.io.*;

@Controller
@SessionAttributes("SessionClass")
public class Home {

    @Autowired private UploadSessionsHolder uploadSessionsHolder;
    @Autowired private UserService userService;
    @Autowired private UploadSession userUploadEntries;
    @Autowired private Logger logger;

    @GetMapping("/user/home")
    public String UserHome(HttpSession session){
        return "UserHome";
    }

    @GetMapping("/register")
    public String registerPage(Model model){
        UserDto user = new UserDto();
       // model.addAttribute("user", user);
        return "register";
    }

    @GetMapping("/login")
    public String loginForm(Model model) {
        model.addAttribute("SessionClass",new SessionClass());
        return "login";
    }

    @PostMapping("/register/save")
    public String registration(@Valid @ModelAttribute("user") UserDto user,
                               BindingResult result,
                               Model model){
        User existing = userService.findByEmail(user.getEmail());
        if (existing != null) {
            result.rejectValue("email", null, "There is already an account registered with that email");
        }
        if (result.hasErrors()) {
            model.addAttribute("user", user);
            return "register";
        }
        userService.saveUser(user);
        return "redirect:/register?success";
    }

    @GetMapping("/user/uploadId")
    @ResponseBody
    public ResponseEntity<String> uploadId(@RequestHeader ("filename") String fileName){
        String userName = getLoggedInUserName();
        if(fileName == null){
            return makeBadResponse("filename missing");
        }
        var uploadId = getUploadSession().getUploadId(fileName);
        return  makeOkResponse(uploadId);
    }

    @PostMapping("/user/uploadFile")
    @ResponseBody
    public  ResponseEntity<String> uploadFile(InputStream ins,
                                              @RequestHeader ("user-id") String uploadId,
                                              @RequestHeader ("content-length") String contentLength)
    {
        if( uploadId == null || contentLength == null ){
            return  makeBadResponse("Headers missing");
        }
        var entry = getUserEntry(uploadId);
        if( entry == null ) {
            return makeBadResponse("No Entry For Upload Id " + uploadId + " exists");
        }
        entry.upload(ins, Long.parseLong(contentLength));
        logger.info("upload Complete for a part");
        return makeOkResponse("dataReceived");
    }

    @PostMapping("/user/download")
    public String userDownload(){
        //to do
        return null;
    }

    @PostMapping("/user/CompleteUpload")
    @ResponseBody
    public ResponseEntity<String> completeUpload(@RequestHeader ("user-id") String uploadId){
        if(uploadId == null) {
            return makeBadResponse("UploadId Missing");
        }
        var entry = getUserEntry(uploadId);
        if(entry == null) {
            return makeBadResponse("No Entry For Upload Id " + uploadId + " exists");
        }
        entry.completeUserUpload();
        return makeOkResponse("uploadComplete for uploadId " + uploadId);
    }

    private UploadSession getUploadSession(){
        return uploadSessionsHolder.getSession(getLoggedInUserName());
    }
    private S3MultiPartUpload getUserEntry(String uploadId){
        var session = uploadSessionsHolder.getExistingSession(getLoggedInUserName());
        return session != null? session.getEntry(uploadId):null;
    }
    private String getLoggedInUserName(){
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }
    private ResponseEntity<String> makeBadResponse(String reason){
        return ResponseEntity.badRequest().body(reason);
    }
    private ResponseEntity<String> makeOkResponse(String message){
        return ResponseEntity.ok().body(message);
    }
}