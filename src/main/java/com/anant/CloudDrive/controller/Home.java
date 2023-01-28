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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@SessionAttributes("fileList")
public class Home {

    @Autowired private UserService userService;
    @Autowired private Logger logger;
    @Autowired StorageService storageService;

    @GetMapping("/user/home")
    public String UserHome(Model model, HttpSession session){
        var fileList =  storageService.getFilesListing()
                                                        .stream()
                                                        .toList();
        HashMap<Integer, String> fileListIdMapping = new HashMap<>();

        for(int i =0; i < fileList.size() ; i++){
            fileListIdMapping.put(i, fileList.get(i));
            System.out.println(fileListIdMapping.get(i));
        }
        System.out.println(session.getId());
        model.addAttribute("fileList", fileListIdMapping);
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

    @PostMapping("/user/download{id}")
    public ResponseEntity<Resource> userDownload(@RequestParam("id") int id,Model model)
            throws FileNotFoundException, MalformedURLException
    {
        //to do
        Map<Integer, String> fileList = (HashMap<Integer, String>) model.getAttribute("fileList");
        String fileToDownload = fileList.get(id);

        return ResponseEntity
                .ok()
                .body(storageService.download(fileToDownload));
    }

    @PostMapping("/user/delete{id}")
    @ResponseBody
    public String delete(@RequestParam("id") int id,Model model){

        Map<Integer, String> fileList = (HashMap<Integer, String>) model.getAttribute("fileList");
        String fileToDelete = fileList.get(id);
        boolean result =  storageService.deleteUserFile(fileToDelete);
        return result ? returnOkResponse("file deleted").toString() : returnInternalServerError().toString();
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