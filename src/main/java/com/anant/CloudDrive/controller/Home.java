package com.anant.CloudDrive.controller;

import com.anant.CloudDrive.UserUploads.UploadSession;
import com.anant.CloudDrive.UserUploads.UploadSessionsHolder;
import com.anant.CloudDrive.dto.UserDto;
import com.anant.CloudDrive.entity.User;
import com.anant.CloudDrive.requests.UploadRequest;
import com.anant.CloudDrive.s3.S3MultiPartUpload;
import com.anant.CloudDrive.service.S3Service;
import com.anant.CloudDrive.service.UserService;

import jakarta.servlet.http.HttpServletResponse;
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
    @Autowired S3Service s3Service;

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
        return fileName == null ? returnBadResponse("filname missing") : returnOkResponse(s3Service.getUploadId(fileName));
    }

    @PostMapping("/user/uploadFile")
    @ResponseBody
    public  ResponseEntity<String> uploadFile(InputStream ins,
                                              @RequestHeader ("user-id") String uploadId,
                                              @RequestHeader ("content-length") String contentLength)
    {
        if( uploadId == null || contentLength == null ){
            return  returnBadResponse("Headers missing");
        }
        var req = new UploadRequest(ins, uploadId, Long.parseLong(contentLength));
        return  s3Service.upload(req) ? returnOkResponse("upload Complete for a part") : returnInternalServerError();
    }

    @PostMapping("/user/download")
    public String userDownload(HttpServletResponse res){
        //to do
        return null;
    }

    @PostMapping("/user/CompleteUpload")
    @ResponseBody
    public ResponseEntity<String> completeUpload(@RequestHeader ("user-id") String uploadId){
        if(uploadId == null) {
            return returnBadResponse("UploadId Missing");
        }
        return s3Service.completeUpload(uploadId) ? returnOkResponse("uploadComplete for uploadId " + uploadId) : returnBadResponse("couldn't " +
                "complete upload for upload id - " + uploadId);
    }
    private ResponseEntity<String> returnBadResponse(String reason){
        return ResponseEntity.badRequest().body(reason);
    }
    private ResponseEntity<String> returnOkResponse(String message){
        return ResponseEntity.ok().body(message);
    }
    private ResponseEntity<String> returnInternalServerError(){
        return ResponseEntity.internalServerError().body("Something went wrong while processing the request");
    }
}