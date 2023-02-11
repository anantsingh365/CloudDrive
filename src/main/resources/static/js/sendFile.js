const defaultPartSize = 14 * 1024 * 1024;
var fileObj;
var uploadId;
var uploadCompleteDoneMessage;

var uploadIdButton = function(){
    uploadId = getUploadId("/user/uploadId", fileObj);
}

var completeDownloadButton = function(){
    sendUploadCompleteConfirmation();
}

var submitButtonEventListener = async function(){
    var fileName = document.getElementById('file').files[0].name;
    var metaDataString = fileName;

    await getUploadId("/user/uploadId", fileObj);

    console.log("initiating upload for upload id -" + uploadId);
   // getJSessionId();

    sendFileInPartsToUrl(fileObj, defaultPartSize, "/user/uploadFile")
    .then( function(value){
        console.log("Transmission successfull for upload id -" + uploadId);
        console.log("attempting upload completion.....");
        sendUploadCompleteConfirmation().then( function(vaue){
            console.log("###### Everthing is completed ######");
        },
        function(error){
            console.log("##### Upload Completetion Failed #####");
        }
        );
         
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

    //file smaller than default part size, send it directly
    if(fileObj.size < (partSize)){
        await sendPart(fileObj, url, true)
        return true;
    }
    if(partSize === null){
        //set part size to default size
        partSize = defaultPartSize;
        endIndx = partSize;
    }
    else{
        endIndx = partSize;
    }

    while(true){
        if ( ((fileObj.size - start) >= partSize) ) {
             filePart = fileObj.slice(start, endIndx)
             console.log("sending a part")
             let result = await sendPart(filePart, url)
             endIndx += partSize;

        }else if( ((fileObj.size - start) < partSize) && ((fileObj.size - start) !== 0)  ){
            //this is the last part
            endIndx = fileObj.size 
            filePart = fileObj.slice(start, endIndx)
            console.log("sending last part")
            let result2 = await sendPart(filePart, url)
            console.log("all parts sent")
            return true;
        }else {
        // this is rare but if file size is perfectly divisible by partSize, then we will probably be here
            return true;
        }
        start += filePart.size; 
    }
}
async function getUploadId(url, file){
    //Fetch upload Id from server
    //return uploadID

    return new Promise((resolve, reject) => {
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
                resolve(true);
               // return true;
            }
            if (this.status >= 200 && this.status < 300) {

            } else {
             reject(null);
            }
        };

        xhr.onerror = function () {
            console.log("error getting upload Id ")
        };
        xhr.send(null);
    }) 
    }

async function sendUploadCompleteConfirmation(){
    // send the upload id to server indicating transfer complete from client side.
    // return true

    return new Promise((resolve, reject) => {
        let xhr = new XMLHttpRequest();
    xhr.open("POST", "/user/CompleteUpload", true);
    xhr.setRequestHeader("Accept", "application/json");
    xhr.setRequestHeader("FileNmae", file.name);
    xhr.setRequestHeader("User-Id" ,uploadId);
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
document.querySelector('input').addEventListener('change', async (event) => {
  fileObj = await event.target.files[0]

}, false)