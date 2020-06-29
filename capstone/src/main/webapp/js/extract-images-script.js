// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

function uploadFile() {
var bucket = document.forms["putFile"]["bucket"].value;
        var filename = document.forms["putFile"]["fileName"].value;
        if (bucket == null || bucket == "" || filename == null || filename == "") {
          alert("Both Bucket and FileName are required");
          return false;
        } else {
          var postData = document.forms["putFile"]["content"].value;
          document.getElementById("content").value = null;

          var request = new XMLHttpRequest();
          request.open("POST", "/gcs/" + bucket + "/" + filename, false);
          request.setRequestHeader("Content-Type", "text/plain;charset=UTF-8");
          request.send(postData);
        }
}

function upload() {
  fetch("/video");
}

function time() {
  var video = document.getElementById("video");
  video.currentTime = 5;
  console.log("time");
}

function play() {
  var video = document.getElementById("video");
  video.play();
  video.addEventListener('canplay', function() {
    this.currentTime = 5;
  });
}

function pause() {
  var video = document.getElementById("video");
  video.pause();
}

// Captures an image from the video and draws it onto the html page
function capture() {
    var canvas = document.getElementById('canvas');     
    var video = document.getElementById('video');
    canvas.width = video.videoWidth;
    canvas.height = video.videoHeight;

    // Get 2d drawing context on canvas
    var ctx = canvas.getContext("2d");
    
    // Draw video's current screen as an image onto canvas
    // 0, 0 sets the top left corner of where to start drawing
    // video.videoWidth, vidoe.videoHeight allows proper scaling when drawing the image
    ctx.drawImage(video, 0, 0, video.videoWidth, video.videoHeight);  

    // Create a Blob object representing the image contained in "canvas"
    // Can specify image format: https://developer.mozilla.org/en-US/docs/Web/API/HTMLCanvasElement/toBlob
    canvas.toBlob(saveFrame);
}

// THIS IS NOT WORKING
function saveFrame(blob) {
  const img = new Image();
  img.srcObject = blob;
  console.log(img.src);
  document.body.appendChild(img);
}