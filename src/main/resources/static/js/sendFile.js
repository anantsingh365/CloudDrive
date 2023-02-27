//@ts-check
const defaultPartSize = 14 * 1024 * 1024;
var fileObj;
var uploadCompleteDoneMessage;

var submitButtonEventListener = async function(){
    const fileName = document.getElementById('file').files[0].name;
    //const metaDataString = fileName;

    const uploadId = await getUploadId("/user/uploadId", fileObj);

    if(uploadId === undefined){
        console.log("Error fetching upload id");
        return;
    }
    console.log("initiating upload for upload id -" + uploadId);
   // getJSessionId();

    sendFileInPartsToUrl(fileObj, defaultPartSize, "/user/uploadFile", uploadId)
    .then( function(value){
        console.log("Transmission successfull of all parts for upload id -" + uploadId);
        console.log("attempting upload completion.....");
        sendUploadCompleteConfirmation(uploadId).then( function(vaue){
            console.log("###### Upload Completion Successfull ######");
            showUploadCompleteMessage();
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

async function sendFileInPartsToUrl(fileObj, partSize, url, uploadId){

    var start = 0;
    var endIndx;
    var filePart;

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
             let result = await sendPart(filePart, url, uploadId)
             endIndx += partSize;

        }else if( ((fileObj.size - start) < partSize) && ((fileObj.size - start) !== 0)  ){
            //this is the last part
            endIndx = fileObj.size 
            filePart = fileObj.slice(start, endIndx)
            console.log("sending last part")
            let result2 = await sendPart(filePart, url, uploadId)
            console.log("all parts sent")
            return true;
        }else {
        // this is rare but if file size is perfectly divisible by partSize, then we will probably be here....I think?
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
                //uploadId = xhr.responseText;
                resolve(xhr.responseText);
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

async function sendUploadCompleteConfirmation(uploadId){
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

function sendPart(filePart, url, uploadId){
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