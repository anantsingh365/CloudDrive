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
var onGoingUploadInstances = [];
var numOfUploads = 0;


var uploadInstance;

// var pauseButtonEventListener = function (){
//         //temp
//         uploadInstance.pauseUpload();
// }

function addUploadProgressListener(handlersObj){
    const func = (howMuchUploaded) =>{
        const uploadDoneForUploadInstanceSelector = "uploadDoneText" + handlersObj.uploadInstanceId;
        const uploadDoneSpan = document.getElementById(uploadDoneForUploadInstanceSelector);
        uploadDoneSpan.innerText = "( " + howMuchUploaded + " / ";
    }
    handlersObj.uploadProgressListener = func;
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
function addFetchingUploadIdSuccessHandler(handlersObj){
    //to do
    const func = () => {
        //adding uploadInstance to uploadsContainer only when upload Id is fetched successfully
        // because that means upload will be inititiated
        const uploadInstanceId = handlersObj.uploadInstanceId;
        const fileObj = handlersObj.fileObj;
        addUploadInstanceToOnGoingUploadsContainer(uploadInstanceId, fileObj);
    };
    handlersObj.fetchingUploadIdSuccessHandler = func;
}
function addFileTransferSuccessHandler(handlersObj){
    //to do
    const func = () => {
        console.log("transfer complete handler placeholder");
    };
    handlersObj.fileTransferSuccessHandler = func;
}
function addUploadCompleteConfirmationSuccessHandler(handlersObj){
    //to do
    const func = () => {
        // before removing the upload Instance from container show successful message for some time
        const uploadInstanceId = handlersObj.uploadInstanceId;

        const totalFileSizeProgressText = " ( " + fileObj.size + " bytes )";
        const totalUploadSizeText = document.getElementById('totalUploadSizeText');
        totalUploadSizeText.innerText = "";
      //  totalFileSizeProgressText.innerText = "Upload Complete";
        setTimeout(()=>{removeUploadInstanceToOnGoingUploadsContainer(uploadInstanceId);}, 3000);
        
    };
    handlersObj.uploadCompleteConfirmationSuccessHandler = func;
}
function addGettingUploadIdFailedHandler(handlersObj){
    //to do
    const func = () => {};
    handlersObj.gettingUploadIdFailedHandler = func;
}
function addFileTransferFailedHandler(handlersObj){
    //to do
    const func = () => {};
    handlersObj.fileTransferFailedHandler = func;
}
function addUploadCompleteConfirmationFailedHandler(handlersObj){
    //to do
    const func = () => {};
    handlersObj.uploadCompleteConfirmationFailedHandler = func;
}

function addUploadInstanceToOnGoingUploadsContainer(uploadInstanceId, fileObj){
    const onGoingUploadsContainer = document.getElementById('onGoingUploadsContainer');
    const uploadInstanceHtml = `<div uploadInstance = ${uploadInstanceId}>\n\
    <h2>Uploading ${fileObj.name}</h1>\n\
    <p><span id = "uploadDoneText${uploadInstanceId}"> </span><span id = "totalUploadSizeText${uploadInstanceId}">${fileObj.size} )</span>\n\
    <button id = "uploadPauseButton${uploadInstanceId}">Pause Upload</button>\n\
    </p>\n\
</div>`;
 //

    onGoingUploadsContainer.innerHTML = onGoingUploadsContainer.innerHTML + uploadInstanceHtml;
    const querySelectorString=  `[uploadPauseButton = "${uploadInstanceId}"]`;
    const pauseButtonId=  `uploadPauseButton${uploadInstanceId}`;
    const pauseButton = document.getElementById(pauseButtonId);

    const uploadInstanceAttachedToButton = onGoingUploadInstances.find((uploadInstance) =>{
        return  uploadInstance.handlersContainer.uploadInstanceId === uploadInstanceId;
 });
    const pauseButtonEventListener = () =>{
        console.log("Pause event listener added and called");
        uploadInstanceAttachedToButton.pauseUpload();
    };
    pauseButton.addEventListener('click', pauseButtonEventListener);
}

// function pauseEventHandlder(e){
//     e.target.getAttribute("id")
//     const uploadInstanceAttachedToButton = onGoingUploadInstances.find((uploadInstance) =>{
//         return  uploadInstance.handlersContainer.uploadInstanceId === uploadInstanceId;
//  });

//     const pauseButtonEventListener = () =>{uploadInstanceAttachedToButton.pauseUpload()};
// }

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
    addFetchingUploadIdSuccessHandler(uploadHandlers)
    addFileTransferSuccessHandler(uploadHandlers)
    addUploadCompleteConfirmationSuccessHandler(uploadHandlers)
    addGettingUploadIdFailedHandler(uploadHandlers)
    addFileTransferFailedHandler(uploadHandlers)
    addUploadCompleteConfirmationFailedHandler(uploadHandlers);
    addUploadProgressListener(uploadHandlers); 
       
    uploadInstance = new Upload(fileObj, uploadfileLink, uploadIdLink, uploadHandlers);
    onGoingUploadInstances.push(uploadInstance);

    uploadInstance.startUpload();
    ++numOfUploads;
}

// const pauseButtonEventListener = (e) => {

//     // to do
//     const parentElem = e.parentNode;
//     const idOfparentOfParentElem = parentElem.parentNode;
//     const uploadInstanceId = idOfparentOfParentElem.getAttribute('uploadInstance');
//     const uploadToPause = onGoingUploadInstances.filter((x) => {x.uploadInstanceId === uploadInstanceId});
//    // uploadToPause.
// }


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