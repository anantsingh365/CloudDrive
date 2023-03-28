package com.anant.CloudDrive.StorageProviders;


import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.anant.CloudDrive.Utils.CommonUtils;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.io.*;

import static com.anant.CloudDrive.Constants.*;

// this code was taken and adapted from https://saravanastar.medium.com/video-streaming-over-http-using-spring-boot-51e9830a3b8
@Component
@Profile("s3")
public class LocalStorageVideoStreamService {

    @Autowired private AmazonS3 s3Client;
    @Value("${s3.bucketName}") private String bucketName;
    /**
     * Prepare the content.
     *
     * @param fileName String.
     * @param fileType String.
     * @param range    String.
     * @return ResponseEntity.
     *
     */
    public ResponseEntity<byte[]> prepareContent(final String fileName, final String range, final String fileType) {

        try {
             String fileKey = fileName ;

            long rangeStart = 0;
            long rangeEnd = CHUNK_SIZE;
            String rangeEndString = String.valueOf(rangeEnd);
            final long fileSize = getFileSize(fileKey);
            if (range == null || fileType.equals("application/pdf")) {
                if(fileSize < CHUNK_SIZE || fileType.equals("application/pdf")){
                    rangeEnd = fileSize;
                    rangeEndString = String.valueOf(rangeEnd);
                }
                byte[] data = readByteRangeNew(fileKey, rangeStart, rangeEnd);
                HttpStatus status = HttpStatus.PARTIAL_CONTENT;
                return getResponse(status, fileType, rangeEndString, rangeStart, rangeEnd, fileSize, data);
            }
            String[] ranges = range.split("-");
            rangeStart = Long.parseLong(ranges[0].substring(6));
            if (ranges.length > 1) {
                rangeEnd = Long.parseLong(ranges[1]);
            } else {
                rangeEnd = rangeStart + CHUNK_SIZE;
            }

            rangeEnd = Math.min(rangeEnd, fileSize - 1);
            final byte[] data = readByteRangeNew(fileKey, rangeStart, rangeEnd);
            final String contentLength = String.valueOf((rangeEnd - rangeStart) + 1);
            HttpStatus httpStatus = HttpStatus.PARTIAL_CONTENT;
            if (rangeEnd >= fileSize) {
                httpStatus = HttpStatus.OK;
            }
            return getResponse(httpStatus, fileType, contentLength, rangeStart, rangeEnd, fileSize, data);
        } catch (IOException e) {
           // logger.error("Exception while reading the file {}", e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    private ResponseEntity<byte[]> getResponse(HttpStatus status, String contentType, String contentLength, long rangeStart, long rangeEnd, long fileSize, byte[] data){
        return ResponseEntity.status(status)
                .header(CONTENT_TYPE, contentType)
                .header(ACCEPT_RANGES, BYTES)
                .header(CONTENT_LENGTH, contentLength)
                .header(CONTENT_RANGE, BYTES + " " + rangeStart + "-" + rangeEnd + "/" + fileSize)
                .body(data);
    }

    /**
     * ready file byte by byte.
     *
     * @param filename String.
     * @param start    long.
     * @param end      long.
     * @return byte array.
     * @throws IOException exception.
     */
    public byte[] readByteRangeNew(String filename, long start, long end) throws IOException {
        GetObjectRequest rangeObjectRequest = new GetObjectRequest(bucketName, filename).withRange(start, end);
        S3Object objectPortion = s3Client.getObject(rangeObjectRequest);
        S3ObjectInputStream objectData = objectPortion.getObjectContent();
        // for some reason read method was not reading all the bytes and only reading partial, readAllBytes() read all
        byte[] result = objectData.readAllBytes();
        objectData.close();
        System.out.println("Total number of bytes read for keyName " + filename + " - " + result.length);
        return result;
    }

    /**
     * Content length.
     *
     * @param fileName String.
     * @return Long.
     */
    public Long getFileSize(String fileName) {
        var metaData = s3Client.getObjectMetadata(bucketName, fileName);
        return metaData.getContentLength();
    }
}
