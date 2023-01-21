package com.anant.CloudDrive.controller;

import com.anant.CloudDrive.UserUploads.UserUploadSession;
import com.anant.CloudDrive.UserUploads.UserUploadSessions;
import com.anant.CloudDrive.dto.UserDto;
import com.anant.CloudDrive.entity.User;
import com.anant.CloudDrive.s3.S3MultiPartUpload;
import com.anant.CloudDrive.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.io.*;
import java.util.Map;

@Controller
@SessionAttributes("SessionClass")
public class Home {

    @Autowired
    private UserUploadSessions userUploadSessions;

    @Autowired
    private UserService userService;

    @Autowired
    private UserUploadSession userUploadEntries;

    @GetMapping("/user/home")
    public String UserHome(HttpSession session){

        //to do list files
        System.out.println(session.getId());
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
    public String uploadId(@RequestHeader Map<String,String> headers){
        String keyName = headers.get("filenmae");
        String userName = SecurityContextHolder.getContext().getAuthentication().getName();
        return  userUploadSessions.getUploadId(userName, keyName);
    }

    @PostMapping("/user/uploadFile")
    @ResponseBody
    public  ResponseEntity<String> uploadFile(HttpServletRequest req,
                                              @RequestHeader Map<String, String> headers)
                                                throws IOException{

        //headers.forEach((key, value) -> System.out.println(key+": " + value));
        String userName = SecurityContextHolder.getContext().getAuthentication().getName();
        String uploadId = headers.get("user-id");
        String contentLength = headers.get("content-length");
        var entry = getUserEntry(userName, uploadId);

        if(uploadId == null || entry == null || contentLength == null){
            return  ResponseEntity.badRequest().body("Invalid Request");
        }
            entry.upload(req.getInputStream(), Long.parseLong(contentLength));
            System.out.println("upload Complete for a part");

        return ResponseEntity.ok().body("dataReceived");
    }

    @PostMapping("/user/download")
    public String userDownload(){
        //to do
        return null;
    }

    @PostMapping("/user/CompleteUpload")
    @ResponseBody
    public String completeUpload(@RequestHeader Map<String, String> headers){
        //return "Upload complete for user "+ SecurityContextHolder.getContext().getAuthentication().getName();
        String uploadId = headers.get("user-id");
        String userName = SecurityContextHolder.getContext().getAuthentication().getName();
        var entry = getUserEntry(userName, uploadId);

        entry.completeUserUpload();
        return "uploadComplete for uploadId " + uploadId;
    }

    private S3MultiPartUpload getUserEntry(String userName, String uploadId){
        var session = userUploadSessions.getUserSession(userName);
        return session.getUploadEntry(uploadId);
    }
}