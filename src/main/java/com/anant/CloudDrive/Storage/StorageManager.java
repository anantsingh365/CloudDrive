package com.anant.CloudDrive.Storage;

import com.anant.CloudDrive.Storage.Models.UploadIdRequest;
import com.anant.CloudDrive.Storage.Models.UploadPartRequest;
import com.anant.CloudDrive.Storage.Models.UserFileMetaData;
import com.anant.CloudDrive.Utils.CommonUtils;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class StorageManager {

    private final StorageProvider storageProvider;
    private final SubscriptionService subscriptionService;
    private final LocalStorageVideoStreamService videoStreamService;
    private final UploadRecords records;

    public StorageManager(@Autowired StorageProvider storageProvider,
                          @Autowired SubscriptionService subscriptionService,
                          @Autowired LocalStorageVideoStreamService videoStreamService,
                          @Autowired UploadRecords records) {
        this.storageProvider = storageProvider;
        this.subscriptionService = subscriptionService;
        this.videoStreamService = videoStreamService;
        this.records = records;
    }

    public String getUploadId(final UploadIdRequest req, final String userName) {
        if (verifyUserHasSpaceQuotaLeft(userName)) {
            String newId = records.generateUploadId(userName);
            try {
                var record = records.getRecord(userName, newId);
                boolean res = storageProvider.initializeUpload(userName, record, req);
                if (!res) {
                    //cleanup
                    return "failed";
                }
                if (record.getState() == UploadRecord.UploadState.ABORTED) {
                    // client has aborted the upload
                    records.removeRecord(userName, newId);
                    return "Upload Aborted";
                }
                record.setState(UploadRecord.UploadState.INITIALIZED);
                return newId;
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
        return null;
    }

    public boolean uploadPart(final UploadPartRequest req) {
        try {
            UploadRecord record = records.getRecord(CommonUtils.getUserData(CommonUtils.signedInUser.GET_USERNAME), req.getUploadId());
            if (record == null || record.getState() == UploadRecord.UploadState.COMPLETED ||
                record.getState() == UploadRecord.UploadState.ABORTED || record.getState() == UploadRecord.UploadState.NOT_CREATED) {
                return false;
            }
            return this.storageProvider.uploadPart(record, req);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean cancelUpload(final String uploadID, final String userName, final String sessionID) {
        try {
            var record = records.getRecord(userName, uploadID);
            //var record = getExistingRecord(sessionID, uploadID);

            //completed upload can't be cancelled
            if (record.getState() == UploadRecord.UploadState.COMPLETED) return false;
            //doing nothing when aborting already aborted record return true;
            if (record.getState() == UploadRecord.UploadState.ABORTED) return true;

            boolean isCancelled = this.storageProvider.abortUpload(record);
            if (isCancelled) {
                record.setState(UploadRecord.UploadState.ABORTED);
                return isCancelled;
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        return false;
    }

    public boolean completeUpload(final String uploadId) {
        try {
            UploadRecord record_ = records.getRecord(CommonUtils.getUserData(CommonUtils.signedInUser.GET_USERNAME), uploadId);
            if (record_ != null) {
                boolean res = storageProvider.completeUpload(record_);
                return res;
            }
            return false;
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public Resource download(final String fileName) {
        return storageProvider.download(fileName);
    }

    public List<UserFileMetaData> getUserObjectsMetaData(String userName) {
        return storageProvider.getUserObjectsMetaData(userName);
    }

    public boolean deleteUserFile(final String fileName) {
        return storageProvider.deleteFile(fileName);
    }

    public boolean renameFile(final String originalName, final String newName) {
        return storageProvider.renameFile(originalName, newName);
    }

    public long getStorageUsedByUser(final String userName) {
        var userObjectListing = getUserObjectsMetaData(userName);
        long sum = 0;
        for (UserFileMetaData file : userObjectListing) {
            sum += file.getSize();
        }
        return sum;
    }

    public ResponseEntity<byte[]> getBlob(String fileName, String range, String contentType) {
        return videoStreamService.getBlob(fileName, range, contentType);
    }

    private boolean verifyUserHasSpaceQuotaLeft(String userName) {
        String storageTier = subscriptionService.getTier(userName);
        int storageTierInt = Integer.parseInt(storageTier);

        long storageQuotaInMB = (int) storageProvider.getStorageUsedByUser(userName) / 1048576;
        if (storageQuotaInMB < storageTierInt) {
            System.out.println("User has space quota left");
            return true;
        }
        System.out.println("User has exhausted the space quota");
        return false;
    }

    @Component
    public static class UploadRecords {
        //to be identified by uploadId as the key (each record will further
        // be associated with a username)
        Map<String, UploadRecord> recordEntries = new ConcurrentHashMap<>();
        private final BeanFactory factory;

        public UploadRecords(@Autowired BeanFactory factory) {
            this.factory = factory;
        }

        //generate a newID and associate that with a userName
        public String generateUploadId(String userName) {
            final String newId = UUID.randomUUID().toString();
            var newRecord = factory.getBean(UploadRecord.class);
            newRecord.setAssociatedWithUser(userName);
            recordEntries.put(newId, newRecord);
            System.out.println("Created the Upload Id" + newId);
            return newId;
        }

        //we are only going to return the record if and only if it is associated with the provided username
        // i.e, ( currently logged-in user ).
        public UploadRecord getRecord(String userName, String uploadId) throws IllegalAccessException {
            var record = recordEntries.get(uploadId);
            if (record.getAssociatedWithUser().equals(userName)) {
                System.out.println("Returning the Record with upload ID - " + uploadId + " for user " + userName);
                return record;
            }
            throw new IllegalAccessException("UserName - " + userName + " is not associated with the record of ID  " + record.getUploadId());
        }

        public void removeRecord(String userName, String uploadId) {
            var record = recordEntries.get(uploadId);
            if (record.getAssociatedWithUser().equals(userName)) {
                recordEntries.remove(uploadId);
            }
        }
    }
}
