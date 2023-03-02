package com.anant.CloudDrive.controller;

import com.anant.CloudDrive.dto.UserDto;
import com.anant.CloudDrive.entity.User;
import com.anant.CloudDrive.requests.UploadRequest;
import com.anant.CloudDrive.service.StorageService;
import com.anant.CloudDrive.service.UserFileMetaData;
import com.anant.CloudDrive.service.UserService;
import static com.anant.CloudDrive.Utils.CommonUtils.*;

import jakarta.servlet.http.HttpServletRequest;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@SessionAttributes("fileList")
public class Home {

    @Autowired private Logger logger;
    @Autowired StorageService storageService;

    @GetMapping("/user/home")
    public String UserHome(Model model, HttpSession session){
        this.addHomePageAttributes(model);
        System.out.println(session.getId());
        return "UserHome";
    }

    @PostMapping("/user/uploadId")
    @ResponseBody
    public ResponseEntity<String> uploadId(@RequestBody Map<String, String> uploadIdPayLoad){
        String fileName = uploadIdPayLoad.get("filename");
        System.out.println(uploadIdPayLoad.get("filename is - " + fileName));
        return fileName != null ?  returnOkResponse(storageService.getUploadId(fileName)):returnBadResponse("filname missing");
    }

    @PostMapping("/user/uploadFile")
    @ResponseBody
    public ResponseEntity<String> uploadFile(InputStream ins,
                                              @RequestHeader ("user-id") String uploadId,
                                              @RequestHeader ("content-length") String contentLength)
    {
        if( uploadId == null || contentLength == null ){
            return  returnBadResponse("Headers missing");
        }

        var req = new UploadRequest(ins, uploadId, Long.parseLong(contentLength));
        return  storageService.upload(req) ? returnOkResponse("dataReceived") : returnInternalServerError();
    }

    @GetMapping("/user/download{id}")
    public ResponseEntity<Resource> download(@RequestParam("id") int id, Model model) throws IOException {

        Map<Integer, UserFileMetaData> fileList = (HashMap<Integer, UserFileMetaData>) model.getAttribute("fileList");
        UserFileMetaData fileMetaData = fileList.get(id);
        String fileToDownload = fileList.get(id).getName();

        if(fileToDownload == null){
           // Resource res = new ByteArrayResource("no file to download".getBytes(StandardCharsets.UTF_8));
            return ResponseEntity.badRequest().body(null);
        }
        Resource res = storageService.download(fileToDownload);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("audio/x-flac"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileToDownload.substring(fileToDownload.indexOf("/")) + "\"")
                .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(fileMetaData.getSize()))
                .body(res);
    }

    @GetMapping("/user/delete{id}")
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
    public ResponseEntity<String> completeUpload(@RequestHeader ("upload-id") String uploadId){
        if(uploadId == null) {
            logger.info("complete upload failed for user " + getUserData(signedInUser.GET_USERNAME) + ", upload id missing");
            return returnBadResponse("UploadId Missing");
        }
        boolean completeUploadResult = storageService.completeUpload(uploadId);
        if(completeUploadResult){
            logger.info("Upload Complete for User " + getUserData(signedInUser.GET_USERNAME) +" upload id " + uploadId);
            return  returnOkResponse("uploadComplete for uploadId " + uploadId);
        }
        return returnBadResponse("couldn't complete upload for upload id - " + uploadId);
    }

    @GetMapping("/user/playMedia/{id}")
    public String playMedia(@RequestParam int id){

        return null;
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
        List<UserFileMetaData> fileList =  storageService.getUserObjectsMetaData();
        HashMap<Integer, UserFileMetaData> fileListIdMapping = new HashMap<>();

        for(int i =0; i < fileList.size() ; i++){
            fileListIdMapping.put(i, fileList.get(i));
            System.out.println(fileListIdMapping.get(i));
        }
        model.addAttribute("fileList", fileListIdMapping);
        model.addAttribute("loggedInUser", SecurityContextHolder.getContext().getAuthentication().getName());
        model.addAttribute("userQuota", addUserStorageQuota());
    }
    private double addUserStorageQuota(){
        //System.out.println(storageService.getUserStorageQuota());
        long userQuota = storageService.getStorageUsedByUser();
        return (double) (userQuota/1048576);
    }
    private String resolveFileToDelete(int id, Model model){
        Map<Integer, UserFileMetaData> fileList = (HashMap<Integer, UserFileMetaData>) model.getAttribute("fileList");
        var removedElem = fileList.remove(id);
        if(removedElem == null){
            return null;
        }
        return removedElem.getName();
    }
}