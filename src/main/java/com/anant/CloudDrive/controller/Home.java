package com.anant.CloudDrive.controller;

import com.anant.CloudDrive.CloudDriveApplication;
import com.anant.CloudDrive.Storage.Models.UserFileMetaData;

import static com.anant.CloudDrive.Constants.CONTENT_TYPE;
import static com.anant.CloudDrive.Utils.CommonUtils.*;

import com.anant.CloudDrive.Storage.StorageManager;
import com.anant.CloudDrive.Storage.Models.UploadIdRequest;
import com.anant.CloudDrive.Storage.Models.UploadPartRequest;
import com.anant.CloudDrive.Utils.CommonUtils;
import com.anant.CloudDrive.controller.Responses.Upload.UploadCompleteResponse;
import com.anant.CloudDrive.controller.Responses.Upload.UploadIdGeneratedResponse;
import com.anant.CloudDrive.controller.Responses.Upload.UploadPartResponse;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
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
    @Autowired StorageManager storageManager;

    @GetMapping("/user/home")
    public String UserHome(@Autowired @Qualifier("randomString") CloudDriveApplication.requestScopeTest requestScopeTest,
                           Model model,
                           HttpSession session,
                           @Autowired ServletContext servletContext,
                           @Autowired HttpServletRequest httpServletRequest
                           ){
        System.out.println(httpServletRequest.getRequestURL());
        System.out.println(servletContext.getContextPath());
        System.out.println("Random Request Scoped bean is " + requestScopeTest.getMethod());
        this.addHomePageAttributes(model);
        System.out.println(session.getId());
        return "UserHome";
    }

    @PostMapping("/user/uploadId")
    @ResponseBody
    public ResponseEntity<UploadIdGeneratedResponse> uploadId(@RequestBody Map<String, String> uploadIdPayLoad){
        var uploadIdRequest = new UploadIdRequest(uploadIdPayLoad.get("filename"), uploadIdPayLoad.get("contenttype"));

        if(uploadIdRequest.isRequestValid()){
            String IdGenerated = storageManager.getUploadId(uploadIdRequest, CommonUtils.getUserData(signedInUser.GET_SESSIONID), CommonUtils.getUserData(signedInUser.GET_USERNAME));
           if(IdGenerated != null){
                var SuccessUploadIdGeneratedResponse = new UploadIdGeneratedResponse(true, "",IdGenerated);
                return ResponseEntity.ok().body(SuccessUploadIdGeneratedResponse);
            }
        }
        var failedUploadIdGeneratedResponse = new UploadIdGeneratedResponse(false, "FileName or content type missing from request body",null);
        return ResponseEntity.internalServerError().body(failedUploadIdGeneratedResponse);
    }

    @PostMapping("/user/uploadFile")
    @ResponseBody
    public ResponseEntity<UploadPartResponse> uploadFile(InputStream ins,
                                                         @RequestHeader ("user-id") String uploadId,
                                                         @RequestHeader ("content-length") String contentLength)
    {
        if( uploadId == null || contentLength == null || uploadId.isEmpty() || contentLength.isEmpty()){
            var errorResponse = new UploadPartResponse(false,"required headers missing/empty", uploadId);
            return ResponseEntity.ok().body(errorResponse);
        }

        var uploadPartRequest = new UploadPartRequest(ins, uploadId, Long.parseLong(contentLength));

        if(storageManager.uploadPart(uploadPartRequest, CommonUtils.getUserData(signedInUser.GET_SESSIONID))){
            var successResponse = new UploadPartResponse(true, "Upload Complete for a part", uploadId);
            return ResponseEntity.ok().body(successResponse);
        }

        return ResponseEntity.internalServerError().body(new UploadPartResponse(false, "Something went wrong", uploadId));
    }

    @PostMapping
    @ResponseBody
    public String cancelUpload(@RequestBody Map<String, String> cancelUploadReq){
       String uploadID = cancelUploadReq.get("upload-id");
       String sessionID = CommonUtils.getUserData(signedInUser.GET_SESSIONID);
       String userName = CommonUtils.getUserData(signedInUser.GET_USERNAME);

       if(uploadID != null){
          boolean uploadCancelled =  storageManager.cancelUpload(uploadID, userName, sessionID);
          if(uploadCancelled){
              return "cancelled";
          }
       }

       return "error cancelling the upload";
    }

    @GetMapping("/user/download{id}")
    @ResponseBody
    public ResponseEntity<Resource> download(@RequestParam("id") String id,
                                           Model model) throws IOException {

        Map<String, UserFileMetaData> fileList = (HashMap<String, UserFileMetaData>) model.getAttribute("fileList");
        UserFileMetaData fileMetaData = fileList.get(id);
        String fileToDownload = fileList.get(id).getName();
        String fileContentType = fileMetaData.getContentType();

        if(fileToDownload == null){
           // Resource res = new ByteArrayResource("no file to download".getBytes(StandardCharsets.UTF_8));
            return ResponseEntity.badRequest().body(null);
        }
        Resource res = storageManager.download(fileToDownload);
        return ResponseEntity.ok()
                .header(CONTENT_TYPE, fileContentType)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileToDownload.substring(fileToDownload.indexOf("/")) + "\"")
                .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(fileMetaData.getSize()))
                .body(res);
    }

    @PostMapping("/user/renameFile")
    @ResponseBody
    public ResponseEntity<String> renameFile(Model model, @RequestParam(name = "newFileName") String newFileName, @RequestParam(name = "id") String id ){
        //String id = renameRequestPayLoad.get("id");
       // String newFileName = renameRequestPayLoad.get("newFileName");
        String originalFileName = resolveFileNameFromId(id, model);
        //boolean result = storageManager.renameFile(originalFileName, CommonUtils.getUserData(CommonUtils.signedInUser.GET_USERNAME) +"/" + newFileName);
        boolean result = storageManager.renameFile(originalFileName, CommonUtils.getUserData(CommonUtils.signedInUser.GET_USERNAME) +"/" + newFileName);
        return result ? returnOkResponse("renameDone") : returnBadResponse("rename failed");
    }
    @GetMapping("/user/video{id}")
    @ResponseBody
    public ResponseEntity<byte[]> videoStream(@RequestParam("id") String id, Model model, @RequestHeader(value = "Range", required = false) String httpRangeList){
        Map<String, UserFileMetaData> fileList = (HashMap<String, UserFileMetaData>) model.getAttribute("fileList");
        UserFileMetaData fileMetaData = fileList.get(id);
        String contentType = fileMetaData.getContentType();
        String fileToStream = fileList.get(id).getName();

        if(fileToStream == null){
            return ResponseEntity.badRequest().body(null);
        }
        return storageManager.getBlob(fileToStream, httpRangeList, contentType);
    }

    @GetMapping("/user/delete{id}")
    public ResponseEntity<String> delete(@RequestParam("id") String id, Model model){
        String fileToDelete = this.resolveFileNameFromId(id, model);
        if(fileToDelete == null){
            return returnBadResponse("there was no file with that id");
        }
        boolean result =  storageManager.deleteUserFile(fileToDelete);
        return result ? returnOkResponse("file deleted") : returnInternalServerError();
    }

    @PostMapping(value = "/user/CompleteUpload", produces = "application/json")
    @ResponseBody
    public ResponseEntity<UploadCompleteResponse> completeUpload(@RequestHeader ("upload-id") String uploadId){

        if(uploadId == null || uploadId.isEmpty()) {
            logger.info("complete upload failed for user " + getUserData(signedInUser.GET_USERNAME) + ", Upload id missing");
            var res = new UploadCompleteResponse(false,"upload-id header missing from request",null);
            return ResponseEntity
                    .internalServerError()
                    .body(res);
        }

        boolean completeUploadResult = storageManager.completeUpload(uploadId,
                CommonUtils.getUserData(signedInUser.GET_SESSIONID));

        if(completeUploadResult){
            logger.info("Upload Complete for User " + getUserData(signedInUser.GET_USERNAME) +" upload id " + uploadId);
            return ResponseEntity
                    .ok()
                    .body(new UploadCompleteResponse(true,"Upload Completed",uploadId));
        }
        return ResponseEntity
                .internalServerError()
                .body(new UploadCompleteResponse(false,"Internal Error",uploadId));
    }

//    private ResponseEntity<Object>

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
        model.addAttribute("fileList", userFileListingMap(storageManager.getUserObjectsMetaData(CommonUtils.getUserData(signedInUser.GET_USERNAME))));
        model.addAttribute("loggedInUser", SecurityContextHolder.getContext().getAuthentication().getName());
        model.addAttribute("userQuota", storageManager.getStorageUsedByUser(CommonUtils.getUserData(signedInUser.GET_USERNAME)) / (1024 * 1024));
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