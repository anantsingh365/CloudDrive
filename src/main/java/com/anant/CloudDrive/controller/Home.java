package com.anant.CloudDrive.controller;

import com.anant.CloudDrive.CloudDriveApplication;
import com.anant.CloudDrive.StorageProviders.requests.*;
import com.anant.CloudDrive.StorageProviders.StorageProvider;
import com.anant.CloudDrive.StorageProviders.UserFileMetaData;

import static com.anant.CloudDrive.Constants.CONTENT_TYPE;
import static com.anant.CloudDrive.Utils.CommonUtils.*;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpSession;

import org.slf4j.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@SessionAttributes("fileList")
public class Home {

    @Autowired private Logger logger;
    @Autowired
    StorageProvider storageProvider;

    @GetMapping("/user/home")
    public String UserHome(@Autowired @Qualifier("randomString") CloudDriveApplication.requestScopeTest requestScopeTest,
                           Model model,
                           HttpSession session,
                           @Autowired ServletContext servletContext
                           ){
        System.out.println(servletContext.getContextPath());
        System.out.println("Random Request Scoped bean is " + requestScopeTest.getMethod());
        this.addHomePageAttributes(model);
        System.out.println(session.getId());
        return "UserHome";
    }

    @PostMapping("/user/uploadId")
    @ResponseBody
    public ResponseEntity<String> uploadId(@RequestBody Map<String, String> uploadIdPayLoad){
        var uploadIdRequest = new UploadIdRequest(uploadIdPayLoad.get("filename"), uploadIdPayLoad.get("contenttype"));
        return uploadIdRequest.isRequestValid() ? returnOkResponse(storageProvider.getUploadId(uploadIdRequest)) : returnBadResponse("filname or content type missing");
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
        var req = new UploadPartRequest(ins, uploadId, Long.parseLong(contentLength));
        return  storageProvider.uploadPart(req) ? returnOkResponse("dataReceived") : returnInternalServerError();
    }

    @GetMapping("/user/download{id}")
    @ResponseBody
    public ResponseEntity<Resource> download(@RequestParam("id") int id,
                                           Model model) throws IOException {

        Map<String, UserFileMetaData> fileList = (HashMap<String, UserFileMetaData>) model.getAttribute("fileList");
        UserFileMetaData fileMetaData = fileList.get(id);
        String fileToDownload = fileList.get(id).getName();
        String fileContentType = fileMetaData.getContentType();

        if(fileToDownload == null){
           // Resource res = new ByteArrayResource("no file to download".getBytes(StandardCharsets.UTF_8));
            return ResponseEntity.badRequest().body(null);
        }
        Resource res = storageProvider.download(fileToDownload);
        return ResponseEntity.ok()
                .header(CONTENT_TYPE, fileContentType)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileToDownload.substring(fileToDownload.indexOf("/")) + "\"")
                .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(fileMetaData.getSize()))
                .body(res);
    }

    @PostMapping("/user/renameFile")
    @ResponseBody
    public ResponseEntity<String> renameFile(Model model, @RequestBody Map<String, String> renameRequestPayLoad){
        String id = renameRequestPayLoad.get("id");
        String newFileName = renameRequestPayLoad.get("newFileName");
        String originalFileName = resolveFileNameFromId(id, model);
        boolean result = storageProvider.renameFile(originalFileName, newFileName);

        return result ? returnOkResponse("renameDone") : returnBadResponse("rename failed");
    }
    @GetMapping("/user/video{id}")
    @ResponseBody
    public ResponseEntity<byte[]> videoStream(@RequestParam("id") int id, Model model, @RequestHeader(value = "Range", required = false) String httpRangeList){
        Map<Integer, UserFileMetaData> fileList = (HashMap<Integer, UserFileMetaData>) model.getAttribute("fileList");
        UserFileMetaData fileMetaData = fileList.get(id);
        String contentType = fileMetaData.getContentType();
        String fileToStream = fileList.get(id).getName();

        if(fileToStream == null){
            return ResponseEntity.badRequest().body(null);
        }
        return storageProvider.getFileBytes(fileToStream,httpRangeList, contentType);
    }

    @GetMapping("/user/delete{id}")
    public ResponseEntity<String> delete(@RequestParam("id") String id, Model model){
        String fileToDelete = this.resolveFileNameFromId(id, model);
        if(fileToDelete == null){
            return returnBadResponse("there was no file with that id");
        }
        boolean result =  storageProvider.deleteUserFile(fileToDelete);
        return result ? returnOkResponse("file deleted") : returnInternalServerError();
    }

    @PostMapping("/user/CompleteUpload")
    @ResponseBody
    public ResponseEntity<String> completeUpload(@RequestHeader ("upload-id") String uploadId){
        if(uploadId == null) {
            logger.info("complete upload failed for user " + getUserData(signedInUser.GET_USERNAME) + ", upload id missing");
            return returnBadResponse("UploadId Missing");
        }
        boolean completeUploadResult = storageProvider.completeUpload(uploadId);

        if(completeUploadResult){
            logger.info("Upload Complete for User " + getUserData(signedInUser.GET_USERNAME) +" upload id " + uploadId);
            return  returnOkResponse("uploadComplete for uploadId " + uploadId);
        }
        return returnBadResponse("couldn't complete upload for upload id - " + uploadId);
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
        model.addAttribute("fileList", userFileListingMap(storageProvider.getUserObjectsMetaData()));
        model.addAttribute("loggedInUser", SecurityContextHolder.getContext().getAuthentication().getName());
        model.addAttribute("userQuota", storageProvider.getStorageUsedByUser() / (1024 * 1024));
    }

    private Map<String, UserFileMetaData> userFileListingMap(List<UserFileMetaData> fileList) {
        Map<String, UserFileMetaData> fileListIdMapping = new HashMap<>();
        for (int i = 0; i < fileList.size(); i++) {
            fileListIdMapping.put(String.valueOf(i), fileList.get(i));
        }
        return fileListIdMapping;
    }

    private String resolveFileNameFromId(String id, Model model){
        HashMap<String, UserFileMetaData> fileList = (HashMap<String, UserFileMetaData>) model.getAttribute("fileList");
        var removedElem = fileList.get(id);
        if(removedElem == null){
            return null;
        }
        return removedElem.getName();
    }
}