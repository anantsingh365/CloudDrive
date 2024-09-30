   function submitForm(event) {
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

   function deleteFile(event) {
       // Your delete file logic here
    console.log("In delete Func");
    var immediateUpperForm  =  event.previousElementSibling;
     for(var i = 0 ; i < 3 ; ++i){
        if(immediateUpperForm && immediateUpperForm.tagName === "FORM"){
            console.log("deleting the element");
            immediateUpperForm.action = "/user/delete";
            immediateUpperForm.submit();
            break;
       }
       immediateUpperForm = immediateUpperForm.previousElementSibling;
     }
   }

   //todo(fetchRenameFile(id)
   function renameFile() {}