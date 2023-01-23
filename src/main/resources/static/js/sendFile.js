const defaultPartSize = 3000 * 1024 * 1024;
var fileObj;
var uploadId;
var uploadCompleteDoneMessage;

var uploadIdButton = function(){
    uploadId = getUploadId("/user/uploadId", fileObj);
}

var completeDownloadButton = function(){
    sendUploadCompleteConfirmation();
}

document.getElementById('completeDownload').addEventListener("click", completeDownloadButton);

document.getElementById('getUploadId').addEventListener("click", uploadIdButton);

var submitButtonEventListener = function(){
    var fileName = document.getElementById('file').files[0].name;
    var metaDataString = fileName;

    console.log(uploadId);
   // getJSessionId();

    sendFileInPartsToUrl(fileObj, defaultPartSize, "/user/uploadFile")
    .then(function(value){

        console.log("Transmission successfull")
        console.log("Upload for id- " + uploadId + " successful");

    }, function(error){
       console.log("Transmission unsuccessful")
       console.log(error.status)
       console.log(error.statusText)
    });
}

document.getElementById('submitButton').addEventListener("click", submitButtonEventListener);

async function sendFileInPartsToUrl(fileObj, partSize, url){

    let start = 0;
    let endIndx;
    var filePart;
    let uploadID;

    //file smaller than 14 mb, send it directly
    if(fileObj.size < (partSize)){
        await sendPart(fileObj, url, true)
        return true;
    }

    if(partSize === null){
        //set default size as 14 MB
        partSize = defaultPartSize;
        endIndx = partSize;
    }
    else{
        endIndx = partSize;
    }

    while(true){
        if ( ((fileObj.size - start) >= partSize) ) {

             filePart = fileObj.slice(start, endIndx)
             let result = await sendPart(filePart, url)
             endIndx += partSize;

        }else if( ((fileObj.size - start) < partSize) && ((fileObj.size - start) !== 0)  ){
            //this is the last part
            endIndx = fileObj.size 
            filePart = fileObj.slice(start, endIndx)
            let result2 = await sendPart(filePart, url)
            console.log("all parts sent")
            return true;
        }else {
            return true;
        }

        start += filePart.size; 
    }
}

function getUploadId(url, file){
    //Fetch upload Id from server
    //return uploadID
    let xhr = new XMLHttpRequest();
            xhr.open("GET", url, true);
            xhr.setRequestHeader("Accept", "application/json");
            xhr.setRequestHeader("FileName", file.name);
            xhr.setRequestHeader("MimeType", file.type);
            xhr.onload = function () {
                console.log(xhr.responeText);
                if(xhr.status === 200){
                    console.log("Upload Id succesfully received");
                    uploadId = xhr.responseText;
                   // return true;
                }
                if (this.status >= 200 && this.status < 300) {

                } else {

                }
            };
            xhr.onerror = function () {
                console.log("error getting upload Id ")
            };
            xhr.send(null);
    }


function sendUploadCompleteConfirmation(){
    // send the upload id to server indicating transfer complete from client side.
    // return true
    let xhr = new XMLHttpRequest();
    xhr.open("POST", "/user/CompleteUpload", true);
    xhr.setRequestHeader("Accept", "application/json");
    xhr.setRequestHeader("FileNmae", file.name);
    xhr.setRequestHeader("User-Id" ,uploadId);
    xhr.onload = function () {
        console.log(xhr.responeText);
        if(xhr.status === 200){
            console.log("Upload Id succesfully received");
            uploadCompleteDoneMessage = xhr.responseText;
            console.log(uploadCompleteDoneMessage);
           // return true;
        }
        if (this.status >= 200 && this.status < 300) {

        } else {

        }
    };
    xhr.onerror = function () {
        console.log("error getting upload Id ")
    };
    xhr.send(null);

}

function sendPart(filePart, url){
    return new Promise(function (resolve, reject) {
        let xhr = new XMLHttpRequest();
        xhr.open("POST", url, true);
        xhr.setRequestHeader("Accept", "application/json");
        xhr.setRequestHeader("Content-Type", "octet-stream");
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
        console.log("byte1arr len ---")
        console.log(filePart.size)
    });
}

function getJSessionId(){
        var jsId = document.cookie;
        if(jsId != null) {
            if (jsId instanceof Array)
                jsId = jsId[0].substring(11);
            else
                jsId = jsId.substring(11);
        }
        console.log(jsId);
        return jsId;
    }

document.querySelector('input').addEventListener('change', async (event) => {
  fileObj = await event.target.files[0]

}, false)