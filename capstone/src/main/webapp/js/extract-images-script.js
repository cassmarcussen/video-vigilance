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

/** Javascript functions for extracting images from video */

// Array of times to keyframe images at
var keyTimes = [1, 4.5, 6];

// Current index of keyTimes
var keyTimesIndex = 0;

/** 
 * Gets the first frame in the video by calling captureFrame
 */ 
function firstFrame() {
	if (keyTimes.length == 0) {
		return;
	}
  captureFrame(
    URL.createObjectURL(document.querySelector("#vid").files[0]),
    keyTimes[keyTimesIndex]
  );
}

/** 
 * Draws a frame of the video onto a canvas element
 * 
 * @param {string} path: The path of the video file
 * @param {number} secs: The time of the video frame to be captured in seconds
 */
function captureFrame(path, secs) {

	// Load video src (needs to be reloaded for events to be triggered)
	const video = document.getElementById("video");
  video.src = path;

	// When the metadata has been loaded, set the time of the video to be captured
	video.onloadedmetadata = function() {
    this.currentTime = secs;
  };
	
	// When the video has seeked to the specific time, draw the frame onto a canvas element
  video.onseeked = function(event) {
    const canvas = document.createElement("canvas");
    canvas.height = video.videoHeight;
    canvas.width = video.videoWidth;

		// Get 2d drawing context on canvas
    const ctx = canvas.getContext("2d");

		// Draw video's current screen as an image onto canvas
    // 0, 0 sets the top left corner of where to start drawing
    // video.videoWidth, vidoe.videoHeight allows proper scaling when drawing the image
    ctx.drawImage(video, 0, 0, canvas.width, canvas.height);

		// This way works too: pass in img element instead of canvas to displayFrame
    // var img = new Image();
    // img.src = canvas.toDataURL();

		// Call function that will display the frame to the page
    displayFrame(canvas, this.currentTime, event);
  };
	
	// If there's an error while seeking to a specific time, call function with error event
  video.onerror = function(event) {
    displayFrame(undefined, undefined, event);
  };
}

/** 
 * Adds captured frame to html page
 * 
 * @param {HTMLElement} img: The canvas element with the frame drawn on it
 * @param {number} secs: The time of the video frame that was captured in seconds
 * @param {event} event: Either a seeked event or an error event that called this function
 */
function displayFrame(img, secs, event) {
	const li = document.createElement("li");
	li.innerHTML += "<b>Frame at second " + secs + ":</b><br />";

	// If video frame was successfully seeked, add the img to the document
	if (event.type == "seeked") {
		li.appendChild(img);
	} 
	// If the video was not successfully seeked, display error message
	else {
		const errorMessage = document.createElement("p");
		errorMessage.innerHTML += "Error capturing frame";
		li.appendChild(errorMessage);
	}

	document.getElementById("olFrames").appendChild(li);

	// Check if there are more frames to capture
	if (++keyTimesIndex < keyTimes.length) {
		const path = URL.createObjectURL(document.querySelector("#vid").files[0]);
		captureFrame(path, keyTimes[keyTimesIndex]);
	};
}

// Sends GET request to ShotsServlet for the shot start and end times
function getShots() {

  // Add loading message to webpage
  const message = document.getElementById("loading");
  message.innerHTML = "Detecting shots...";

  fetch("/shots").then(response => response.json()).then(shots => {
    
    // Remove loading message
    const message = document.getElementById("loading");
    message.innerHTML = "";

    // Display shot times to user
    const list = document.getElementById("shots-list");
    list.innerHTML = "";
    var count = 1;

    // Display each shot's times in a list and add the middle time of each shot to keyTimes array\
    for (const shot of shots) {
      const listElement = document.createElement("li");
      const textElement = document.createElement("span");
      textElement.innerHTML = "<b>Shot " + count + ": <b>" + shot.start_time + " - " + shot.end_time;
      listElement.appendChild(textElement);
      list.append(listElement);

      keyTimes.push((shot.start_time + shot.end_time) / 2.0);
      count++;
    }
  });
}

// Not being used at the moment
function upload() {
  fetch("/video");
}

function loadVideo() {
  video.src = URL.createObjectURL(document.querySelector("#vid").files[0]);
}




/************************** Code below here might be used later (for saving images to bucket) *************************/

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


function saveFrame(blob) {
  var newImg = document.createElement("img");
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
