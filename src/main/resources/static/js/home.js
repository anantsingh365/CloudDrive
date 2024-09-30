    window.onload = async function(){
       console.log("Home loaded");
       var res = await getFileList();
       console.log(res);
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

const submitForm =  function(event) {
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

const deleteFile =  function(event) {
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
