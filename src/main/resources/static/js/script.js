var renameKeyNum = -99;

// Get the modal
var modal = document.getElementById("myModal2");

// Get the button that opens the modal
var btn = document.getElementById("openRenameModalBtn");
var renameBtns = document.getElementsByClassName("rename-icon");

for(var i = 0 ; i < renameBtns.length ; ++i) {
  renameBtns[i].addEventListener("click", function(event){
    modal.style.display = "block";
    renameKeyNum = event.target.getAttribute("keyNum");
  })
}

// Get the <span> element that closes the modal
var span = document.getElementsByClassName("close")[0];

// Get the cancel button
var cancelBtn = document.getElementById("cancelBtn");

// When the user clicks the button, open the modal
// btn.onclick = function(event) {
//   modal.style.display = "block";
//   renameKeyNum = event.target.getAttribute("keyNum"); 
// }

// When the user clicks on <span> (x), close the modal
span.onclick = function() {
  modal.style.display = "none";
}

// When the user clicks anywhere outside of the modal, close it
window.onclick = function(event) {
  if (event.target == modal) {
    modal.style.display = "none";
  }
}

// When the user clicks on the cancel button, close the modal
cancelBtn.onclick = function() {
  modal.style.display = "none";
}

// When the user clicks on the rename button, perform some action (empty for now)
var modal = document.getElementById("myModal2");
var renameInput = document.getElementById("renameInput");
var renameBtn = document.getElementById("renameBtn");

renameBtn.onclick = async function() {
  const returnP = new Promise((resolve,reject) => {
   var newName = renameInput.value.trim();
  
  if (newName === "") {
    alert("Please enter a new name.");
    return;
  }

  var formData = new FormData();
  formData.append("id", renameKeyNum);
  formData.append("newFileName", newName);

   fetch("/user/renameFile", {
    method: "POST",
    body: formData
  })
  .then(response => {
    if (!response.ok) {
      throw new Error("Network response was not ok.");
    }
    return response;
  })
  .then(data => {
    console.log("Response from server:", data);
    // Handle the response from the server
  })
  .catch(error => {
    console.error("Error:", error);
    // Handle errors
  });
  // Close the modal after sending the request
  modal.style.display = "none";
  });
};
