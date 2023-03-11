package com.anant.CloudDrive.controller;

import com.anant.CloudDrive.service.LocalStorageVideoStreamService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class MediaStreamingController {

    @Autowired
    private LocalStorageVideoStreamService videoStreamService;

    @GetMapping("/user/stream/{fileType}/{fileName}")
    public ResponseEntity<byte[]> streamVideo(@Autowired HttpServletResponse response, @RequestHeader(value = "Range", required = false) String httpRangeList,
                                              @PathVariable("fileType") String fileType,
                                              @PathVariable("fileName") String fileName) {

        return videoStreamService.prepareContent(fileName, fileType, httpRangeList);
    }
}
