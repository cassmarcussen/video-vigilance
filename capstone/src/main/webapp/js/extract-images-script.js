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

function getShots() {

  // Add loading message to webpage
  const message = document.getElementById("loading");
  message.innerHTML = "Detecting shots...";

  // Send GET request to servlet
  fetch("/shots").then(response => response.json()).then(shots => {
    
    // Remove loading message
    const message = document.getElementById("loading");
    message.innerHTML = "";

    // Display shot times to user
    const list = document.getElementById("shots-list");
    list.innerHTML = "";
    var count = 1;

    // Add each shot's times to a list
    shots.forEach((shot) => {
      const listElement = document.createElement("li");
      const textElement = document.createElement("span");
      textElement.innerHTML = "<b>Shot " + count + ": <b>" + shot.start_time + " - " + shot.end_time;
      listElement.appendChild(textElement);
      list.append(listElement);
      count++;
    });
  });
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
}

function pause() {
  var video = document.getElementById("video");
  video.pause();
}

function loadVideo() {
  video.src = URL.createObjectURL(document.querySelector("#vid").files[0]);
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
//   canvas.toBlob(saveFrame);

//   var imageData = ctx.getImageData(0, 0, video.videoWidth, video.videoHeight).data;
//   console.log(imageData);
//   const params = new URLSearchParams();
//   params.append("content", imageData);
//   const postRequest = new Request("/video", {
//     method: "POST",
//     body: params
//   });

//   fetch(postRequest);
}

function saveFrame(blob) {
  var newImg = document.createElement('img');
  var url = URL.createObjectURL(blob);

  // newImg.onload = function() {
  //     // no longer need to read the blob so it's revoked
  //     URL.revokeObjectURL(url);
  //   };

  newImg.src = url;
//   console.log("image source: " + newImg.src);
//   console.log(blob);

  document.body.appendChild(newImg);
  
//   blob.arrayBuffer().then(buffer => {
//     var view = new Uint8Array(buffer);
//     console.log(view);

//     const params = new URLSearchParams();
//     params.append("content", view);
//     const postRequest = new Request("/video", {
//     method: "POST",
//     body: params
//     });

//     fetch(postRequest);
//   });  
}
