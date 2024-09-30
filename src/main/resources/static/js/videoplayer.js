var videoQueryString = "";

window.onload = function(){
    console.log("page loaded")
    var queryString = window.location.search;
    const searchParams = new URLSearchParams(queryString);
    const id = searchParams.get('id'); 
    console.log("id is - " + id);
    videoQueryString = id
    setVideoSrc();
}

const video = document.querySelector("video");

function setVideoSrc(){
    var source = document.createElement("source");
    source.setAttribute(
      "src",
      "/user/video?id=" + videoQueryString,
    );
    source.setAttribute("type", "video/mp4");
    video.appendChild(source);
  }