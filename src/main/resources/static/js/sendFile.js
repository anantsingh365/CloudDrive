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
var fileType;
var fileText;

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

var submitButtonEventListener = ()=>{
    document.getElementById('submitButton').disabled = true;
    uploadSequence();
}
document.getElementById('submitButton').addEventListener("click", submitButtonEventListener);

// 1- fetch uploadId
// 2- send file in parts or chunks to server
// 3- send upload complete request to server
async function uploadSequence(){
    //1st step
    try{
        const uploadId = await getUploadId(uploadIdLink, fileObj);
        uploadID = uploadId;
        console.log("starting file transfer for upload id -" + uploadId);
        try{
            //2nd step
            const wasCompleted = await startTransferOfFile(false, uploadId);
            if(wasCompleted){
                console.log("Transmission successfull of all parts for upload id -" + uploadId);
                console.log("attempting upload completion.....");

                //3rd step
                const uploadCompletionResult = await sendUploadCompleteConfirmation(uploadId);
                if(uploadCompletionResult){
                    console.log("###### Upload Completion Successfull ######");
                    showUploadCompleteMessage();
                    fileInputReset();
                    document.getElementById('submitButton').disabled = false;

                }else{
                    fileInputReset();
                    console.log("##### Upload Completetion Failed #####");
                }
               }else{
                   // false promise resolve means upload was paused
                    console.log("Upload Paused")
               }
        }catch(err){
            fileInputReset();
            uploadFailed(err);
        }
    }catch(err){
        fetchingUploadIdFailedHandler(err);
    }
}

function fetchingUploadIdFailedHandler(error){
    const rejectMessage = error;
    console.log("Error fetching upload id");
    switch(rejectMessage){
        case "Account Upgrade":
            console.log("Please Upgrade Your Account");
            const fileInput = document.getElementById('file');
            const para = document.createElement('span');
            para.setAttribute('id', 'AccountUpgradeMessage');
            para.textContent = "Please Upgrade Your Account to Upload More Files";
            fileInput?.after(para);

            //removing success message after 3 seconds
            const elem = document.getElementById('AccountUpgradeMessage');
            removeDomElement(elem, 3000);

            //re enable submit button
            document.getElementById('submitButton').disabled = false;
            break;

        default: 
            console.log("Unknown Error Occured while getting uploadId");    
            document.getElementById('submitButton').disabled = false;
    }
    return;
}

async function startTransferOfFile(isResuming, uploadId){
    return await sendFileInPartsToUrl(fileObj, defaultPartSize, uploadfileLink, uploadId, isResuming);
}

async function sendFileInPartsToUrl(fileObj, partSize, url, uploadId, isResuming){

    //file smaller than default part size, send it directly

    var totalFileSizeProgressText = " / " + fileObj.size + " bytes)";
    var totalUploadSizeText = document.getElementById('totalUploadSizeText');
    totalUploadSizeText.innerText = totalFileSizeProgressText;
    var uploadDoneText = document.getElementById('uploadDoneText');

    if(fileObj.size < (partSize)){
        await sendPart(fileObj, url, uploadId)
        return true;
    }

    var start = 0;
    var endIndx;
    var filePart;

    if(!isResuming){
        uploadDoneText.innerText = ' (0';
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

    var pauseButton = document.getElementById("pauseResumeButton");
    pauseButton.style.visibility = 'visible';

    // show progress text
    var progressText = document.getElementById("progressText");
    progressText.style.display = 'inline';

    while(true && !pauseUploadFlag){
        if ( ((fileObj.size - start) >= partSize) ) {
             filePart = fileObj.slice(start, endIndx)
             console.log("sending a part")
             let result = await sendPart(filePart, url, uploadId)

             //uploadDoneText size 
             uploadDoneText.innerText = " (" + endIndx;

             endIndx += partSize;

        }else if( ((fileObj.size - start) < partSize) && ((fileObj.size - start) !== 0)  ){
            //this is the last part
            pauseButton.style.visibility = 'hidden';
            endIndx = fileObj.size
            filePart = fileObj.slice(start, endIndx)

            //uploadDoneTextUpdate
            uploadDoneText.innerText = " (" + fileObj.size;

            console.log("sending last part")
            let result2 = await sendPart(filePart, url, uploadId);
            console.log("all parts sent")
            isUploadCompleted = true;

            // hide progress text 
            var progressText = document.getElementById("progressText");
            progressText.style.display = 'none';

            //this is temporary
            const uploadCompletionResult = await sendUploadCompleteConfirmation(uploadId);
                if(uploadCompletionResult){
                    console.log("###### Upload Completion Successfull ######");
                    showUploadCompleteMessage();
                    fileInputReset();
                    document.getElementById('submitButton').disabled = false;

                }else{
                    console.log("##### Upload Completetion Failed #####");
                }
            return true;
        }else {
        // this is rare but if file size is perfectly divisible by partSize, then we will probably be here....I think?
            isUploadCompleted = true;
            pauseButton.style.visibility = 'hidden';
            return true;
        }
        start += filePart.size;
    }

    if(pauseUploadFlag){
        console.log("Upload Paused");
        resumeState.startIndex = start;
    }
    //returning false indicates that upload was paused
    //for failure an exception will be thrown by sendPart() and should be handled by uploadSequence().
    return false;
}

function uploadFailed(error){
    console.log("Transmission unsuccessful")
    console.log(error.status)
    console.log(error.statusText)
    return false;
}
function fileInputReset(){
    document.getElementById("file").value = "";
}

function showUploadCompleteMessage(){
    const fileInput = document.getElementById('file');
    const para = document.createElement('span');
    para.setAttribute('id', 'uploadCompleteMessage');
    para.textContent = "Upload Complete";
    fileInput?.after(para);

    //removing success message after 3 seconds
    const elem = document.getElementById('uploadCompleteMessage');
    removeDomElement(elem, 3000);
}

function removeDomElement(elem, inSeconds){
    setTimeout(()=> elem.remove(), inSeconds);
}

async function getUploadId(url, file){
    //Fetch upload Id from server
    //return uploadID
    return new Promise((resolve, reject) => {
        const UploadIdRequestBody = '{ "filename": "'+file.name+'","mimetype":"'+file.name+'", "contenttype":"'+fileType+'" }';
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

async function sendUploadCompleteConfirmation(uploadId){
    return new Promise((resolve, reject) => {
        let xhr = new XMLHttpRequest();
    xhr.open("POST", "/user/CompleteUpload", true);
    xhr.setRequestHeader("Accept", "application/json");
    xhr.setRequestHeader("FileNmae", file.name);
    xhr.setRequestHeader("upload-id" ,uploadId);
    xhr.onload =  ()=> {
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
    xhr.onerror = ()=> {
        console.log("error completing upload Id ")
        reject(false);
    };
    xhr.send(null);

    });
}

function sendPart(filePart, url, uploadId){
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

document.getElementById('file').addEventListener('change', async (event) => {
  resetUploadState();
  fileObj = await event.target.files[0];
  fileType = fileObj.type;
}, false)

function resetUploadState(){
    isUploadCompleted = false;
    pauseUploadFlag = false;
    resumeState.startIndex = 0;
}