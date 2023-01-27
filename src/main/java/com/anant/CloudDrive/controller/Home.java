package com.anant.CloudDrive.controller;

import com.anant.CloudDrive.dto.UserDto;
import com.anant.CloudDrive.entity.User;
import com.anant.CloudDrive.requests.UploadRequest;
import com.anant.CloudDrive.service.StorageService;
import com.anant.CloudDrive.service.UserService;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import java.io.*;
import java.net.MalformedURLException;
import java.util.List;

@Controller
@SessionAttributes("SessionClass")
public class Home {

    @Autowired private UserService userService;
    @Autowired private Logger logger;
    @Autowired StorageService storageService;

    @GetMapping("/user/home")
    public String UserHome(Model model, HttpSession session){
        storageService.getFilesListing()
                .forEach(System.out::println);
        System.out.println(session.getId());
        //model.addAttribute("fileListing", fileListing);

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
        return fileName == null ? returnBadResponse("filname missing") : returnOkResponse(storageService.getUploadId(fileName));
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
        return  storageService.upload(req) ? returnOkResponse("upload Complete for a part") : returnInternalServerError();
    }

    @PostMapping("/user/download")
    public ResponseEntity<Resource> userDownload() throws FileNotFoundException, MalformedURLException {
        //to do
        return ResponseEntity
                .ok()
                .body(storageService.download(23));
    }

    @PostMapping("/user/CompleteUpload")
    @ResponseBody
    public ResponseEntity<String> completeUpload(@RequestHeader ("user-id") String uploadId){
        if(uploadId == null) {
            return returnBadResponse("UploadId Missing");
        }
        return storageService.completeUpload(uploadId) ? returnOkResponse("uploadComplete for uploadId " + uploadId) : returnBadResponse("couldn't " +
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