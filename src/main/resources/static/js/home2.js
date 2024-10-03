var filelisting;

function renameForm(elem){
  console.log("rename Btn clicked");
}

function submitForm(elem){
  console.log("download Btn clicked");
}

function deleteFile(elem){
  console.log("delete Btn clicked");
}

window.onload = async function(){
   console.log("Home loaded");
     var res = await getFileList();
     filelisting = res; 
     console.log(res);

     setTimeout(() =>{populateFileListing()}, 3000);
    // populateFileListing();
    }

async function getFileList(){
    var endpoint = "/user/fetchFileList";
    try{
      const res = await fetch(endpoint);
      return await res.json();
     }catch(err){
       console.log("err getting filelist from backend")
       return "error"
      }
    }


function populateFileListing(){
  var rows = [];
  filelisting.forEach(row => {
   let rowData = `
        <td><span>${row.name}</span><br></td>
        <td><span>${row.size}</span></td>
        <td><span >${row.lastModified}</span></td>
        <td>
            <form style="display: none;" action="/user/download" method="GET">
                <input type="hidden" name="id" value = 0"/>
            </form>
            <button class="delete-icon" type="button" onclick="deleteFile(this)"><span>&#x1f5d1;</span></button>
            <button class="download-icon" type="button" onclick="submitForm(this)">&#x2b07;</button>
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

