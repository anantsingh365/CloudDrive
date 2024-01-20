//@ts-check
import { Upload } from "./UploadLogic.js";

var fileObj;
const resumeState = {};
const uploadfileLink = "/user/uploadFile";
const uploadIdLink = "/user/uploadId";
var onGoingUploadInstances = [];
var numOfUploads = 0;

var uploadInstance;

function addUploadProgressListener(handlersObj){
    const func = (howMuchUploaded) =>{
        const uploadDoneForUploadInstanceSelector = "uploadDoneText" + handlersObj.uploadInstanceId;
        const uploadDoneSpan = document.getElementById(uploadDoneForUploadInstanceSelector);
        uploadDoneSpan.innerText = "( " + howMuchUploaded + " / ";
    }
    handlersObj.uploadProgressListener = func;
}

function addUploadCancelHandler(handlersObj){
      const func = () => {
        // before removing the upload Instance from container show successful message for some time
        const uploadInstanceId = handlersObj.uploadInstanceId;

         const totalUploadSizeText = document.getElementById('totalUploadSizeText'+uploadInstanceId);
         const uploadPauseResumeButton = document.getElementById('uploadPauseButton'+uploadInstanceId);
         totalUploadSizeText.remove();
         uploadPauseResumeButton.remove();

         --numOfUploads;
        // removeUploadsContainerIfUploadsZero();

         const uploadDoneText = document.getElementById('uploadDoneText'+uploadInstanceId);
        uploadDoneText.innerText = "Upload Cancelled";

        setTimeout(()=>{removeUploadInstanceToOnGoingUploadsContainer(uploadInstanceId);}, 3000);  
    };
    handlersObj.uploadCompleteHandler = func;
}

function addUploadPausedHandler(handlersObj){
    //to do 
    const func = () => {
        const uploadInstanceId = handlersObj.uploadInstanceId;
        const querySelectorString=  `[uploadInstance = "${uploadInstanceId}"]`;
        const uploadInstanceElem = document.querySelector(querySelectorString);
        //   `"uploadPauseButton${uploadInstanceId}"`;
        const pauseButtonIdString =  "uploadPauseButton" + uploadInstanceId;
        const pauseResumeButton = document.getElementById(pauseButtonIdString);
        pauseResumeButton.innerText = "Resume Upload";
    };
    handlersObj.uploadPausedHandler = func;
   // return handlersObj;
}
function addUploadResumeHandler(handlersObj){
    //to do
        const func = () => {
            const uploadInstanceId = handlersObj.uploadInstanceId;
            // const querySelectorString=  `[uploadInstance = "${uploadInstanceId}"]`;
            // const uploadInstance = document.querySelector(querySelectorString);
            const pauseButtonId=  `uploadPauseButton${uploadInstanceId}`;
            const pauseButton = document.getElementById(pauseButtonId);
            pauseButton.innerText = "PauseUpload";
        };
    handlersObj.uploadResumeHandler = func;
}
function addUploadStartingHandler(handlersObj){
    //to do
    const func = () => {
        // this will be called when upload is about to start
        const uploadInstanceId = handlersObj.uploadInstanceId;
        const fileObj = handlersObj.fileObj;
        addUploadInstanceToOnGoingUploadsContainer(uploadInstanceId, fileObj);
    };
    handlersObj.StartingUploadHandler = func;
}

function addUploadCompleteConfirmationSuccessHandler(handlersObj){
    //to do
    const func = () => {
        // before removing the upload Instance from container show successful message for some time
        const uploadInstanceId = handlersObj.uploadInstanceId;

         const totalUploadSizeText = document.getElementById('totalUploadSizeText'+uploadInstanceId);
         const uploadPauseResumeButton = document.getElementById('uploadPauseButton'+uploadInstanceId);
         totalUploadSizeText.remove();
         uploadPauseResumeButton.remove();

         --numOfUploads;
        // removeUploadsContainerIfUploadsZero();

         const uploadDoneText = document.getElementById('uploadDoneText'+uploadInstanceId);
        uploadDoneText.innerText = "Upload Complete";
       

        setTimeout(()=>{removeUploadInstanceToOnGoingUploadsContainer(uploadInstanceId);}, 3000);  
    };
    handlersObj.uploadCompleteHandler = func;
}

function removeUploadsContainerIfUploadsZero(){
    if(numOfUploads === 0){
        setTimeout(()=>{
            const onGoingUploadsContainer = document.getElementById('onGoingUploadsContainer');
            onGoingUploadsContainer?.remove();
        },4500);
    }
}

function addUploadInstanceToOnGoingUploadsContainer(uploadInstanceId, fileObj){
    const onGoingUploadsContainer = document.getElementById('onGoingUploadsContainer');
    const uploadInstanceHtml = `<div uploadInstance = ${uploadInstanceId}>\n\
    <h2>Uploading ${fileObj.name}</h1>\n\
    <p><span id = "uploadDoneText${uploadInstanceId}"> </span><span id = "totalUploadSizeText${uploadInstanceId}">${fileObj.size} )</span>\n\
    <button id = "uploadPauseButton${uploadInstanceId}">Pause Upload</button>\n\
    <button id = "uploadCancelButton${uploadInstanceId}">Cancel Upload</button>\n\
    </p>\n\
</div>`;

    onGoingUploadsContainer.insertAdjacentHTML("beforeend", uploadInstanceHtml);
   // onGoingUploadsContainer.innerHTML = onGoingUploadsContainer.innerHTML + uploadInstanceHtml;
    const querySelectorString=  `[uploadPauseButton = "${uploadInstanceId}"]`;
    const pauseButtonId=  `uploadPauseButton${uploadInstanceId}`;
    const pauseButton = document.getElementById(pauseButtonId);
    const cancelButtonId=  `uploadCancelButton${uploadInstanceId}`;
    const cancelButton = document.getElementById(cancelButtonId);

    const uploadInstanceAttachedToButton = onGoingUploadInstances.find((uploadInstance) =>{
        return  uploadInstance.handlersContainer.uploadInstanceId === uploadInstanceId;
 });
    const pauseButtonEventListener = () =>{
        uploadInstanceAttachedToButton.pauseUpload();
    };

    const cancelUploadButtonEventListener = () =>{
        uploadInstanceAttachedToButton.cancelUploadFunc();
    }
    pauseButton.addEventListener('click', pauseButtonEventListener);
    cancelButton.addEventListener('click', cancelUploadButtonEventListener);
}

function removeUploadInstanceToOnGoingUploadsContainer(uploadInstanceId){
    const querySelectorString=  `[uploadInstance = "${uploadInstanceId}"]`;
    const uploadInstanceElem = document.querySelector(querySelectorString);

    uploadInstanceElem.remove();
}
//document.getElementById("pauseResumeButton").addEventListener("click", pauseButtonEventListener);

var submitButtonEventListener = () => { 
    
    var uploadHandlers = {};
    uploadHandlers.fileObj = fileObj;
    uploadHandlers.uploadInstanceId = numOfUploads;
    
    //adding various handlers
    addUploadPausedHandler(uploadHandlers)
    addUploadResumeHandler(uploadHandlers)
    addUploadStartingHandler(uploadHandlers)
    addUploadCompleteConfirmationSuccessHandler(uploadHandlers)
    addUploadProgressListener(uploadHandlers); 
    addUploadCancelHandler(uploadHandlers);
       
    uploadInstance = new Upload(fileObj, uploadfileLink, uploadIdLink, uploadHandlers);
    onGoingUploadInstances.push(uploadInstance);

    uploadInstance.startUpload();
    ++numOfUploads;
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