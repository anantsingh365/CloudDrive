//@ts-check
const defaultPartSize = 5 * 1024 * 1024;
var fileObj;
var uploadCompleteDoneMessage;
const resumeState = {};
var pauseUploadFlag = false;
var uploadID;
const uploadfileLink = "/user/uploadFile";
const uploadIdLink = "/user/uploadId"
var isUploadCompleted = false;

// const uploadIdHeaders = {header1:""}

var pauseButtonEventListener = function (){
    // upload underway, pause it  
    if(pauseUploadFlag == true && !isUploadCompleted){
        pauseUploadFlag = false;
        const pauseButton = document.getElementById("pauseResumeButton");
        pauseButton.innerHTML = "Pause Upload";
        startTransferOfFile(true, uploadID);
    }else if(isUploadCompleted){
        console.log("upload completed, can't pause or resume")
    }
    // upload is paused, resume it
    else{
        const pauseButton = document.getElementById("pauseResumeButton");
        pauseButton.innerHTML = "Resume Upload";
        pauseUploadFlag = true;
    }
}

document.getElementById("pauseResumeButton")?.addEventListener("click", pauseButtonEventListener);

var submitButtonEventListener = async function(){
   getUploadId(uploadIdLink, fileObj)
    .then
    ((uploadId)=>{
        uploadID = uploadId;
        console.log("initiating upload for upload id -" + uploadId);
        startTransferOfFile(false, uploadId);
    },
    (error)=>{
        console.log("Error fetching upload id");
        return;
    });
}

function startTransferOfFile(isResuming, uploadId){
    sendFileInPartsToUrl(fileObj, defaultPartSize, uploadfileLink, uploadId, isResuming)
    .then(function(value){
        wasUploadSuccessfulOrPaused(value, uploadId);
    },
    function(error){
      wasUploadFailed(error);
    });
}

async function sendFileInPartsToUrl(fileObj, partSize, url, uploadId, isResuming){

    //file smaller than default part size, send it directly
    if(fileObj.size < (partSize)){
        await sendPart(fileObj, url, uploadId)
        return true;
    }

    var start = 0;
    var endIndx;
    var filePart;

    if(!isResuming){
        if(partSize === null){
        //set part size to default size
        partSize =  defaultPartSize;
        endIndx = partSize;
        }
        else{
        endIndx = partSize;
        }
    }
    else{
        start = resumeState.startIndex;
        partSize = defaultPartSize;
        endIndx = start + partSize;
    }

    var elem = document.getElementById("pauseResumeButton");
    elem.style.visibility = 'visible';

    while(true && !pauseUploadFlag){
        if ( ((fileObj.size - start) >= partSize) ) {
             filePart = fileObj.slice(start, endIndx)
             console.log("sending a part")
             let result = await sendPart(filePart, url, uploadId)
             endIndx += partSize;

        }else if( ((fileObj.size - start) < partSize) && ((fileObj.size - start) !== 0)  ){
            //this is the last part
            endIndx = fileObj.size
            filePart = fileObj.slice(start, endIndx)
            console.log("sending last part")
            let result2 = await sendPart(filePart, url, uploadId)
            console.log("all parts sent")
            isUploadCompleted = true;
            elem.style.visibility = 'hidden';
            return true;
        }else {
        // this is rare but if file size is perfectly divisible by partSize, then we will probably be here....I think?
            isUploadCompleted = true;
            elem.style.visibility = 'hidden';
            return true;
        }
        start += filePart.size;
    }

    if(pauseUploadFlag){
        resumeState.startIndex = start;
    }
    return false;
}

function wasUploadFailed(error){

    console.log("Transmission unsuccessful")
    console.log(error.status)
    console.log(error.statusText)
    return false;
}

function wasUploadSuccessfulOrPaused(value, uploadId){

    if(wasUploadPaused(value, uploadId)){
        return;
    }
    console.log("Transmission successfull of all parts for upload id -" + uploadId);
    console.log("attempting upload completion.....");
    sendUploadCompleteConfirmation(uploadId).then( function(vaue){
    console.log("###### Upload Completion Successfull ######");
    showUploadCompleteMessage();
    return true;
    },
    function(error){
        console.log("##### Upload Completetion Failed #####");
        return false;
    });
}

function wasUploadPaused(value, uploadId){

    if(value == false && !isUploadCompleted){
        console.log("Upload Paused")
        return true;
    }
}

function showUploadCompleteMessage(){

    const fileInput = document.getElementById('file');
    const para = document.createElement('span');
    para.setAttribute('id', 'uploadCompleteMessage');
    para.textContent = "Upload Complete";
    fileInput?.after(para);

    //removing after 3 seconds
    setTimeout(function(){
     const elem = document.getElementById('uploadCompleteMessage');
     elem?.remove();
    }, 3000);
}

document.getElementById('submitButton').addEventListener("click", submitButtonEventListener);

async function getUploadId(url, file){
    //Fetch upload Id from server
    //return uploadID
    return new Promise((resolve, reject) => {
        const UploadIdRequestBody = '{ "filename": "'+file.name+'","mimetype":"'+file.name+'"}';
        let xhr = new XMLHttpRequest();
        xhr.open("POST", url, true);
      //  xhr.setRequestHeader("FileName", file.name);
       // xhr.setRequestHeader("MimeType", file.type);
        xhr.setRequestHeader("Content-Type","application/json")

        xhr.onload = ()=>{
            if(xhr.status === 200){
                resolve(xhr.responseText);
            }
            if (this.status >= 200 && this.status < 300) {

            } else {
             reject(null);
            }
        };

        xhr.onerror = () => console.log("error getting upload Id ");
        xhr.send(UploadIdRequestBody);
    })
    }

async function sendUploadCompleteConfirmation(uploadId){
    return new Promise((resolve, reject) => {
        let xhr = new XMLHttpRequest();
    xhr.open("POST", "/user/CompleteUpload", true);
    xhr.setRequestHeader("Accept", "application/json");
    xhr.setRequestHeader("FileNmae", file.name);
    xhr.setRequestHeader("upload-id" ,uploadId);
    xhr.onload = function () {
        console.log(xhr.responeText);
        if(xhr.status === 200){
            uploadCompleteDoneMessage = xhr.responseText;
            console.log(uploadCompleteDoneMessage);
            resolve(true);
        }
        if (this.status >= 200 && this.status < 300) {
            return true;
        } else {
            reject(false);
        }
    };
    xhr.onerror = function () {
        console.log("error getting upload Id ")
        return false;
    };
    xhr.send(null);

    });

}

function sendPart(filePart, url, uploadId){
    return new Promise(function (resolve, reject) {
        let xhr = new XMLHttpRequest();
        xhr.open("POST", url, true);
        xhr.setRequestHeader("User-Id" ,uploadId);
        xhr.onload = function () {
            console.log(xhr.responeText);
            if(xhr.responseText === "dataReceived"){
                console.log("file part Successfully sent");
                resolve(true);
               // return true;
            }
            if (this.status >= 200 && this.status < 300) {

            } else {
                reject({
                    status: this.status,
                    statusText: xhr.statusText
                });
            }
        };
        xhr.onerror = function () {
            reject({
                status: this.status,
                statusText: xhr.statusText
            });
        };
        xhr.send(filePart);
        console.log(filePart.size)
    });
}

document.querySelector('input').addEventListener('change', async (event) => {
  isUploadCompleted = false;
  pauseUploadFlag = false;
  resumeState.startIndex = 0;
  fileObj = await event.target.files[0]
}, false)