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

  canvas.toBlob(saveFrame);

  // toDataURL creates a huuuugggeee url
  // var dataURL = canvas.toDataURL('image/png');
  // var newImg = document.createElement('img');
  // newImg.src = dataURL;
  // console.log("image source: " + newImg.src);
  // document.body.appendChild(newImg);
}

function saveFrame(blob) {
  var newImg = document.createElement('img');
  var url = URL.createObjectURL(blob);

  // newImg.onload = function() {
  //     // no longer need to read the blob so it's revoked
  //     URL.revokeObjectURL(url);
  //   };

  newImg.src = url;
  console.log("image source: " + newImg.src);
  console.log(blob);

  document.body.appendChild(newImg);
}