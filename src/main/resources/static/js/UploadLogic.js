class Upload{

    defaultPartSize = 5 * 1024 * 1024;
    fileObj;
    resumeState = {};
    uploadCompleteDoneMessage = "";
    pauseUploadFlag = false;
    uploadID = "";
    uploadfileLink = "/user/uploadFile";
    uploadIdLink = "/user/uploadId"
    isUploadCompleted = false;
    pauseUploadFlag = false;
    globalStopFlag = false;

    //events handlers associated with various upload events.
    handlersContainer;

    uploadPausedHandler;
    uploadResumeHandler;
    StartingUploadHandler;
    uploadCompleteHandler;
    uploadProgressListener;
    uploadFailedHandler;


    constructor( fileObj, uploadfileLink, uploadIdLink, handlersContainer){
        this.fileObj = fileObj;
        this.uploadfileLink = uploadfileLink;
        this.uploadIdLink = uploadIdLink;
        this.handlersContainer = handlersContainer;
    }

    setUploadIdFailedHandler(uploadIdFailedHandler){
      //  this.#uploadIdFailedHandler = uploadIdFailedHandler;
    }

    pauseUpload(){
        if(this.pauseUploadFlag == true && !this.isUploadCompleted){
            this.pauseUploadFlag = false;
            // const pauseButton = document.getElementById("pauseResumeButton");
            // pauseButton.innerHTML = "Pause Upload";
            this.startTransferOfFile(true, this.uploadID);
            const uploadResumeHandler = this.handlersContainer.uploadResumeHandler;
            if(uploadResumeHandler === undefined){
                console.log("No handler for upload Resume Event");
            }
            uploadResumeHandler();

        }else if(this.isUploadCompleted){
            console.log("upload completed, can't pause or resume")
        }
        // upload is paused, resume it
        else{
            // const pauseButton = document.getElementById("pauseResumeButton");
            // pauseButton.innerHTML = "Resume Upload";

            // pause upload handler is placed in uploadSequence because 
            // that's where we are sure if upload was successfully paused.
            this.pauseUploadFlag = true;
            const uploadPausedHandler = this.handlersContainer.uploadPausedHandler;
            if(uploadPausedHandler === undefined){
                console.log("No handler for upload Resume Event");
            }
            else{
                uploadPausedHandler(); 
        }}
    }

    cancelUploadFunc(){
        this.globalStopFlag = true;
        //if(this.cancelUploadFlag){
        console.log("cancelling the upload in the while loop");
        console.log("Upload cancelled");
        const f = this.handlersContainer.uploadCompleteHandler;
        if(f == undefined){
           console.log("No handler associated with upload paused event...");
         }else{
           f();
         }
   }
    

   async startUpload() {
    try {
        // 1st step
        const uploadId = await this.getUploadId(this.uploadIdLink, this.fileObj);
        this.uploadID = uploadId;

        const startingUploadHandler = this.handlersContainer.StartingUploadHandler;
        if (startingUploadHandler) {
            startingUploadHandler();
        } else {
            console.log("No handler associated with starting upload Event...");
        }

        console.log("Starting file transfer for upload with upload id - " + this.uploadID);

        try {
            // 2nd step
            const wasCompleted = await this.startTransferOfFile(false, uploadId);
            if (wasCompleted) {
                console.log("Transmission successful of all parts for upload id - " + uploadId);
                console.log("Attempting upload completion.....");

                // 3rd step
                const uploadCompletionResult = await this.sendUploadCompleteConfirmation(uploadId);
                const uploadCompleteHandler = this.handlersContainer.uploadCompleteHandler;
                const uploadFailedHandler = this.handlersContainer.uploadFailedHandler;

                if (uploadCompletionResult) {
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
            } else{
                const uploadPausedHandler = this.handlersContainer.uploadPausedHandler;
                    if (uploadPausedHandler) {
                        uploadPausedHandler();
                    } else {
                        console.log("No handler associated with upload paused event...");
                    }
                    console.log("Upload Paused");
                }
        } catch (err) {
            const gettingUploadIdFailedHandler = this.handlersContainer.gettingUploadIdFailedHandler;
            if (gettingUploadIdFailedHandler) {
                gettingUploadIdFailedHandler();
            } else {
                console.log("No handler associated with upload transfer failed event...");
            }
            // Handle other actions on failure
        }
    } catch (err) {
        const fetchingUploadIdFailedHandler = this.handlersContainer.gettingUploadIdFailedHandler;
        if (fetchingUploadIdFailedHandler) {
            fetchingUploadIdFailedHandler();
        } else {
            console.log("No handler associated with fetching upload id failed event...");
        }
        // Handle other actions on failure
    }
}

    async getUploadId(url, file){
        //Fetch upload Id from server
        //return uploadID
        return new Promise((resolve, reject) => {
            const UploadIdRequestBody = '{ "filename": "'+file.name+'","mimetype":"'+file.name+'", "contenttype":"'+ this.fileObj.type +'" }';
            let xhr = new XMLHttpRequest();
            xhr.open("POST", url, true);
          //  xhr.setRequestHeader("FileName", file.name);
           // xhr.setRequestHeader("MimeType", file.type);
            xhr.setRequestHeader("Content-Type","application/json")
    
            xhr.onload = ()=>{
                if(xhr.status === 200){
                    if(xhr.responseText === "Account Upgrade"){
                        reject(xhr.responseText);
                    }
                    resolve(xhr.responseText);
                }
                if (this.status >= 200 && this.status < 300) {
    
                } else {
                 reject(null);
                }
            };
    
            xhr.onerror = () => {
                console.log("error getting upload Id ");
    
            }
            xhr.send(UploadIdRequestBody);
        })
        }

        async startTransferOfFile(isResuming, uploadId){
            return await this.sendFileInPartsToUrl(this.fileObj, this.defaultPartSize, this.uploadfileLink, uploadId, isResuming);
        }


      async sendFileInPartsToUrl(fileObj, partSize, url, uploadId, isResuming) {
          // File smaller than default part size, send it directly
          if (fileObj.size < partSize) {
              await this.sendPart(fileObj, url, uploadId);
              return true;
          }

          let start = isResuming ? this.resumeState.startIndex : 0;
          partSize = isResuming ? this.defaultPartSize : partSize;
          let endIndx = start + partSize;
          let filePart;

          while (!this.pauseUploadFlag && !this.globalStopFlag) {
              if (fileObj.size - start >= partSize) {
                  filePart = fileObj.slice(start, endIndx);
                  console.log("sending a part");
                  try {
                      await this.sendPart(filePart, url, uploadId);
                  } catch (error) {
                      // Handle the error appropriately
                      console.error("Error sending part:", error);
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
                      if (uploadCompletionResult) {
                          console.log("###### Upload Completion Successful ######");
                          const uploadCompleteHandler = this.handlersContainer.uploadCompleteHandler;
                          if (uploadCompleteHandler) {
                              uploadCompleteHandler();
                          } else {
                              console.log("No handler associated with upload complete confirmation event...");
                          }
                      } else {
                          console.log("##### Upload Completion Failed #####");
                      }
                      return true;
                  } catch (error) {
                      // Handle the error appropriately
                      console.error("Error sending last part:", error);
                      return false;
                  }
              } else {
                  // Rare case: file size is perfectly divisible by partSize
                  this.isUploadCompleted = true;
                  return true;
              }
              start += filePart.size;
          }

          if (this.pauseUploadFlag) {
              console.log("Upload Paused");
              this.resumeState.startIndex = start;
          }

          // Returning false indicates that the upload was paused
          // For failure, an exception will be thrown by sendPart(), and it should be handled by uploadSequence()
          return false;
      }

      updateProgressListener(howMuchUploaded) {
          const uploadProgressListenerHandler = this.handlersContainer.uploadProgressListener;
          if (uploadProgressListenerHandler) {
              uploadProgressListenerHandler(howMuchUploaded);
          } else {
              console.log("No progress listener associated");
          }
      }

        sendPart(filePart, url, uploadId){
            return new Promise((resolve, reject)=>{
                let xhr = new XMLHttpRequest();
                xhr.open("POST", url, true);
                xhr.setRequestHeader("User-Id" ,uploadId);
                xhr.onload = ()=> {
                    console.log(xhr.responeText);
                    if(xhr.responseText === "dataReceived"){
                        console.log("file part Successfully sent");
                        resolve(true);
                       // return true;
                    }
                    if (this.status >= 200 && this.status < 300) {
        
                    }else {
                        reject({
                            status: this.status,
                            statusText: xhr.statusText
                        });
                    }
                };
                xhr.onerror = ()=> {
                    reject({
                        status: this.status,
                        statusText: xhr.statusText
                    });
                };
                xhr.send(filePart);
                console.log(filePart.size)
            });
        }

        async cancelUpload(url, uploadId){
            return new Promise((resolve, reject)=>{
                let xhr = new XMLHttpRequest();
                xhr.open("POST", url, true);
                xhr.setRequestHeader("upload-id" ,uploadId);
                xhr.onload = ()=> {
                    console.log(xhr.responeText);
                    if(xhr.responseText === "cancelled"){
                        console.log("upload cancelled");
                        resolve(true);
                       // return true;
                    }
                    if (this.status >= 200 && this.status < 300) {
        
                    }else {
                        reject({
                            status: this.status,
                            statusText: xhr.statusText
                        });
                    }
                };
                xhr.onerror = ()=> {
                    reject({
                        status: this.status,
                        statusText: xhr.statusText
                    });
                };
            });
        }

        async sendUploadCompleteConfirmation(uploadId){
            return new Promise((resolve, reject) => {
                let xhr = new XMLHttpRequest();
            xhr.open("POST", "/user/CompleteUpload", true);
            xhr.setRequestHeader("Accept", "application/json");
            xhr.setRequestHeader("FileNmae", file.name);
            xhr.setRequestHeader("upload-id" ,uploadId);
            xhr.onload =  ()=> {
                console.log(xhr.responeText);
                if(xhr.status === 200){
                    this.uploadCompleteDoneMessage = xhr.responseText;
                    console.log(this.uploadCompleteDoneMessage);
                    resolve(true);
                }
                if (this.status >= 200 && this.status < 300) {
                    return true;
                } else {
                    reject(false);
                }
            };
            xhr.onerror = ()=> {
                console.log("error completing upload Id ")
                reject(false);
            };
            xhr.send(null);
            });
        }

     removeDomElement(elem, inSeconds){
        setTimeout(()=> elem.remove(), inSeconds);
    }
}
export {Upload};