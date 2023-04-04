//@ts-check
import { Upload } from "./UploadLogic.js";

const defaultPartSize = 5 * 1024 * 1024;
var fileObj;
var uploadCompleteDoneMessage;
const resumeState = {};
var pauseUploadFlag = false;
var uploadID;
const uploadfileLink = "/user/uploadFile";
const uploadIdLink = "/user/uploadId";

var uploadObj;

var pauseButtonEventListener = function (){
        //temp
        uploadObj.pauseUpload();
}

document.getElementById("pauseResumeButton")?.addEventListener("click", pauseButtonEventListener);

var submitButtonEventListener = ()=>{   
     uploadObj = new Upload(fileObj, uploadfileLink, uploadIdLink);
    uploadObj.uploadSequence();

    // document.getElementById('submitButton').disabled = true;
    // uploadSequence();
}
    document.getElementById('submitButton').addEventListener("click", submitButtonEventListener);

document.getElementById('file').addEventListener('change', async (event) => {
 // resetUploadState();
  fileObj = await event.target.files[0];
}, false)

function resetUploadState(){
    isUploadCompleted = false;
    pauseUploadFlag = false;
    resumeState.startIndex = 0;
}