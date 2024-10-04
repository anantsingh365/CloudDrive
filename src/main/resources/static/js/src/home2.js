var filelisting;

function renameForm(elem){
  console.log("rename Btn clicked");
}

function downloadFile(elem){
  const queryParm1 = elem.getAttribute("fileToDownload"); 
  const queryParm2 = elem.getAttribute("fileContentType"); 
  const queryParm3 = elem.getAttribute("fileSize"); 

  const url = `/user/download2?fileToDownload=${queryParm1}&contentType=${queryParm2}&fileSize=${queryParm3}`;
  location.replace(url);
}

function submitForm(elem){
  console.log("download Btn clicked");
  console.log("In download Func");
  var immediateUpperForm  =  event.previousElementSibling;
  for(var i = 0 ; i < 3 ; ++i){
     if(immediateUpperForm && immediateUpperForm.tagName === "FORM"){
          console.log("downloading the element");
          immediateUpperForm.submit();
          break;
     }
     immediateUpperForm = immediateUpperForm.previousElementSibling;
     }
}

async function deleteFile(elem){

  var fileToDeleteWithUserName = elem.getAttribute("fileToDelete");
  var indexOfUserNameSlash = fileToDeleteWithUserName.indexOf("/"); 
  var fileToDeleteWithoutUserName = fileToDeleteWithUserName.substring(indexOfUserNameSlash + 1);
  console.log("Initiating Delete File for - ", fileToDeleteWithoutUserName);
  
  const payLoad = {
    fileToDelete: fileToDeleteWithoutUserName
  }

  const response = await fetch("/user/delete2", {
    method: "DELETE",
    body: JSON.stringify(payLoad),
    headers: new Headers({'content-type': 'application/json'}) 
  });

  const responseJson = await response.text(); 
  console.log(responseJson);
}

window.onload = async function(){
   console.log("Home loaded");
     var res = await getFileList();
     filelisting = res; 
     //console.log(res);
     populateFileListing();   
    }

async function getFileList(){
    var endpoint = "/user/fetchFileList";
    try{
      const res = await fetch(endpoint);
      return await res.json();
     }catch(err){
       console.log("err getting filelist from backend " + err);
       return "error"
      }
    }

function populateFileListing(){
  var rows = [];
  filelisting.forEach(row => {
   var indexOfUserNameSeparator = row.name.indexOf("/");
   var fileNameWithoutUserName = row.name.substring(indexOfUserNameSeparator + 1);
   let rowData = `
        <td><span>${fileNameWithoutUserName}</span><br></td>
        <td><span>${row.size}</span></td>
        <td><span >${row.lastModified}</span></td>
        <td>
            <form style="display: none;" action="/user/download" method="GET">
                <input type="hidden" name="id" value = 0"/>
            </form>
            <button class="delete-icon" fileToDelete="${row.name}" type="button" onclick="deleteFile(this)"><span>&#x1f5d1;</span></button>
            <button class="download-icon" type="button" fileToDownload="${row.name}"  fileContentType="${row.contentType}" fileSize="${row.size}" onclick="downloadFile(this)">&#x2b07;</button>
            <button keyNum = 0 type="button" onclick ="renameForm(this)" class="rename-icon"><span>&#x270e;</span></button>
            <button class="play-button" type="button"><a th:href="/user/videoplayer?id=0">player</a>
            </button>
        </td>
    `;
    rows.push(rowData);
  })

  var htmlTableRows = [];

  rows.forEach(row => {
    let tableRow = document.createElement("tr");
    tableRow.insertAdjacentHTML("beforeend", row);
    htmlTableRows.push(tableRow);
  });

  const tableBody = document.getElementById("fileListTableBody");

  htmlTableRows.forEach(htmlTableRow =>{
    tableBody.appendChild(htmlTableRow);
  })
}

