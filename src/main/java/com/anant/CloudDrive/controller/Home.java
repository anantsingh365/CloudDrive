package com.anant.CloudDrive.controller;

import com.amazonaws.Response;
import com.anant.CloudDrive.dto.UserDto;
import com.anant.CloudDrive.entity.User;
import com.anant.CloudDrive.requests.UploadRequest;
import com.anant.CloudDrive.service.StorageService;
import com.anant.CloudDrive.service.UserService;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Controller
@SessionAttributes("fileList")
public class Home {

    @Autowired private UserService userService;
    @Autowired private Logger logger;
    @Autowired StorageService storageService;

    @GetMapping("/user/home")
    public String UserHome(Model model, HttpSession session){
        this.addHomePageAttributes(model);
        System.out.println(session.getId());
//        var fileList =  storageService.getFilesListing();
//        HashMap<Integer, String> fileListIdMapping = new HashMap<>();
//
//        for(int i =0; i < fileList.size() ; i++){
//            fileListIdMapping.put(i, fileList.get(i));
//            System.out.println(fileListIdMapping.get(i));
//        }
//        System.out.println(session.getId());
//        model.addAttribute("fileList", fileListIdMapping);
//        model.addAttribute("loggedInUser", SecurityContextHolder.getContext().getAuthentication().getName());
        return "UserHome";
    }

    @GetMapping("/register")
    public String registerPage(Model model){
        return "register";
    }

    @GetMapping("/login")
    public String loginForm(Model model) {
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
        return fileName != null ?  returnOkResponse(storageService.getUploadId(fileName)):returnBadResponse("filname missing");
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

    @GetMapping("/user/download{id}")
    public ResponseEntity<Resource> userDownload(@RequestParam("id") int id,Model model) throws IOException {
        //to do
        Map<Integer, String> fileList = (HashMap<Integer, String>) model.getAttribute("fileList");
        String fileToDownload = fileList.get(id);

        if(fileToDownload == null){
            Resource res = new ByteArrayResource("no file to download".getBytes(StandardCharsets.UTF_8));
            return ResponseEntity.badRequest().body(res);
        }
        //return ResponseEntity.ok().body(storageService.download(fileToDownload));
        Resource res = storageService.download(fileToDownload);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("audio/x-flac"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileToDownload.substring(fileToDownload.indexOf("/")) + "\"")
                .body(res);
    }

    @PostMapping("/user/delete{id}")
    @ResponseBody
    public ResponseEntity<String> delete(@RequestParam("id") int id, Model model){
        String fileToDelete = this.resolveFileToDelete(id, model);
        if(fileToDelete == null){
            return returnBadResponse("there was no file with that id");
        }

        boolean result =  storageService.deleteUserFile(fileToDelete);
        return result ? returnOkResponse("file deleted") : returnInternalServerError();
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
    private void addHomePageAttributes(Model model){
        var fileList =  storageService.getFilesListing();
        HashMap<Integer, String> fileListIdMapping = new HashMap<>();
        for(int i =0; i < fileList.size() ; i++){
            fileListIdMapping.put(i, fileList.get(i));
            System.out.println(fileListIdMapping.get(i));
        }
        model.addAttribute("fileList", fileListIdMapping);
        model.addAttribute("loggedInUser", SecurityContextHolder.getContext().getAuthentication().getName());
    }
    private String resolveFileToDelete(int id, Model model){
        var fileList = (HashMap<Integer, String>) model.getAttribute("fileList");
        String fileToDelete = fileList.remove(id);
        return fileToDelete;
    }
}