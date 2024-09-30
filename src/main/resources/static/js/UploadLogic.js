class Upload {
    defaultPartSize = 5 * 1024 * 1024;
    fileObj;
    resumeState = {};
    uploadCompleteDoneMessage = "";
    pauseUploadFlag = false;
    uploadID = "";
    uploadfileLink = "/user/uploadFile";
    uploadIdLink = "/user/uploadId";
    isUploadCompleted = false;
    globalStopFlag = false;
    handlersContainer;

    constructor(fileObj, uploadfileLink, uploadIdLink, handlersContainer) {
        this.fileObj = fileObj;
        this.uploadfileLink = uploadfileLink;
        this.uploadIdLink = uploadIdLink;
        this.handlersContainer = handlersContainer;
    }

    pauseUpload() {
         if (this.pauseUploadFlag && !this.isUploadCompleted) {
            this.pauseUploadFlag = false;
            this.startTransferOfFile(true, this.uploadID);
            const uploadResumeHandler = this.handlersContainer.uploadResumeHandler;
            if (uploadResumeHandler === undefined) {
                console.log("No handler for upload Resume Event");
               }
            uploadResumeHandler();
             }
            else if (this.isUploadCompleted) {
                console.log("Upload completed, can't pause or resume");
            }
            else {
               this.pauseUploadFlag = true;
               const uploadPausedHandler = this.handlersContainer.uploadPausedHandler;
               if (uploadPausedHandler === undefined) {
                  console.log("No handler for upload Pause Event");
                }else{
                    uploadPausedHandler();
                }
            }
        }

    cancelUploadFunc() {
                this.globalStopFlag = true;
                console.log("Cancelling the upload in the while loop");
                console.log("Upload cancelled");
                const uploadCompleteHandler = this.handlersContainer.uploadCompleteHandler;
                if (uploadCompleteHandler === undefined) {
                    console.log("No handler associated with upload paused event...");
                } else {
                    uploadCompleteHandler();
                }
            }

    async startUpload() {
        try {
            const uploadIdResponse = await this.getUploadId(this.uploadIdLink, this.fileObj);
            const uploadIdResponseObj = JSON.parse(uploadIdResponse)

            const uploadId = uploadIdResponseObj.createdUploadId
            this.uploadID = uploadId;

            this.handleEvent("StartingUploadHandler");

            console.log("Starting file transfer for upload with upload id - " + this.uploadID);

            try {
                const wasCompleted = await this.startTransferOfFile(false, uploadId);
                if (wasCompleted) {
                    console.log("Transmission successful of all parts for upload id - " + uploadId);
                    console.log("Attempting upload completion.....");

                    const uploadCompletionResult = await this.sendUploadCompleteConfirmation(uploadId);
                    this.handleUploadResult(uploadCompletionResult);
                } else {
                    this.handleUploadPaused();
                }
            } catch (err) {
                this.handleUploadTransferFailed(err);
            }
        } catch (err) {
            this.handleGettingUploadIdFailed(err);
        }
    }

    async getUploadId(url, file) {
        return new Promise((resolve, reject) => {
            const requestBody = {
                filename: file.name,
                mimetype: file.name,
                contenttype: this.fileObj.type
            };

//json representation of the
//            "{
//            "filename": "fileName",
//            "mimetype": "mimetype",
//            "contentType": "Hello"
//            }"

            const xhr = this.createHttpRequest("POST", url, "application/json", resolve, reject);
            xhr.send(JSON.stringify(requestBody));
        });
    }

    async startTransferOfFile(isResuming, uploadId) {
        return await this.sendFileInPartsToUrl(this.fileObj, this.defaultPartSize, this.uploadfileLink, uploadId, isResuming);
    }

    async sendFileInPartsToUrl(fileObj, partSize, url, uploadId, isResuming) {
        if (fileObj.size < partSize) {
            await this.sendPart(fileObj, url, uploadId);
            return true;
        }

        let start = isResuming ? this.resumeState.startIndex : 0;
        partSize = isResuming ? this.defaultPartSize : partSize;
        let endIndx = start + partSize;
        let filePart;

        //todo(simplify)
        while (!this.pauseUploadFlag && !this.globalStopFlag) {
            if (fileObj.size - start >= partSize) {
                filePart = fileObj.slice(start, endIndx);
                console.log("sending a part");
                try {
                    await this.sendPart(filePart, url, uploadId);
                } catch (error) {
                    console.error("Error sending a part:", error);
                    return false;
                }
                this.updateProgressListener(endIndx);
                endIndx += partSize;
            } else if (fileObj.size - start < partSize && fileObj.size - start !== 0) {
                endIndx = fileObj.size;
                filePart = fileObj.slice(start, endIndx);
                console.log("sending last part");
                try {
                    await this.sendPart(filePart, url, uploadId);
                    console.log("all parts sent");
                    this.isUploadCompleted = true;
                    const uploadCompletionResult = await this.sendUploadCompleteConfirmation(uploadId);
                    this.handleUploadResult(uploadCompletionResult);
                    return true;
                } catch (error) {
                    console.error("Error sending last part:", error);
                    return false;
                }
            } else {
                this.isUploadCompleted = true;
                return true;
            }
            start += filePart.size;
        }

        if (this.pauseUploadFlag) {
            console.log("Upload Paused");
            this.resumeState.startIndex = start;
        }
        return false;
    }

    async sendPart(filePart, url, uploadId) {
        return new Promise((resolve, reject) => {
            const xhr = this.createHttpRequest("POST", url, null, resolve, reject);
            xhr.setRequestHeader("User-Id", uploadId);
            xhr.send(filePart);
            console.log(filePart.size);
        });
    }

    async cancelUpload(url, uploadId) {
        return new Promise((resolve, reject) => {
            const xhr = this.createHttpRequest("POST", url, null, resolve, reject);
            xhr.setRequestHeader("upload-id", uploadId);
        });
    }

    async sendUploadCompleteConfirmation(uploadId) {
        return new Promise((resolve, reject) => {
            const xhr = this.createHttpRequest("POST", "/user/CompleteUpload", "application/json", resolve, reject);
            xhr.setRequestHeader("Accept", "application/json");
            xhr.setRequestHeader("FileNmae", this.fileObj.name);
            xhr.setRequestHeader("upload-id", uploadId);
            xhr.send(null);
        });
    }

    handleEvent(eventName) {
        const handler = this.handlersContainer[eventName];
        if (handler) {
            handler();
        } else {
            console.log("No handler associated with " + eventName + " Event...");
        }
    }

    handleUploadResult(result) {
        const uploadCompleteHandler = this.handlersContainer.uploadCompleteHandler;
        const uploadFailedHandler = this.handlersContainer.uploadFailedHandler;

        if (result) {
            if (uploadCompleteHandler) {
                uploadCompleteHandler();
            } else {
                console.log("No handler associated with upload complete confirmation event...");
            }
            console.log("###### Upload Completion Successful ######");
            // Additional actions on successful upload completion
        } else {
            if (uploadFailedHandler) {
                uploadFailedHandler();
            } else {
                console.log("No handler associated with upload failed event...");
            }
        }
    }

    handleUploadPaused() {
        const uploadPausedHandler = this.handlersContainer.uploadPausedHandler;
        if (uploadPausedHandler) {
            uploadPausedHandler();
        } else {
            console.log("No handler associated with upload paused event...");
        }
        console.log("Upload Paused");
    }

    handleGettingUploadIdFailed(err) {
        const handler = this.handlersContainer.gettingUploadIdFailedHandler;
        if (handler) {
            handler();
        } else {
            console.log("No handler associated with fetching upload id failed event...");
        }
        // Handle other actions on failure
    }

    handleUploadTransferFailed(err) {
        const handler = this.handlersContainer.gettingUploadIdFailedHandler;
        if (handler) {
            handler();
        } else {
            console.log("No handler associated with upload transfer failed event...");
        }
        // Handle other actions on failure
    }

    createHttpRequest(method, url, contentType, resolve, reject) {
        const xhr = new XMLHttpRequest();
        xhr.open(method, url, true);

        if (contentType) {
            xhr.setRequestHeader("Content-Type", contentType);
        }

        xhr.onload = () => {
            if (xhr.status === 200) {
                resolve(xhr.responseText);
            } else {
                reject({
                    status: xhr.status,
                    statusText: xhr.statusText
                });
            }
        };

        xhr.onerror = () => {
            reject({
                status: xhr.status,
                statusText: xhr.statusText
            });
        };

        return xhr;
    }

    updateProgressListener(howMuchUploaded) {
        const uploadProgressListenerHandler = this.handlersContainer.uploadProgressListener;
        if (uploadProgressListenerHandler) {
            uploadProgressListenerHandler(howMuchUploaded);
        } else {
            console.log("No progress listener associated");
        }
    }

    removeDomElement(elem, inSeconds) {
        setTimeout(() => elem.remove(), inSeconds);
    }
}

export { Upload };
