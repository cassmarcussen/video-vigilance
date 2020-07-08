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
const keyTimes = [];

// Current index of keyTimes
var keyTimesIndex = 0;

// Time interval between frames for manually setting shot times (-1 if not using this method)
var frameInterval = -1;

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

    // Display each shot's times in a list and add the middle time of each shot to keyTimes array
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

// Gets the first frame in the video by calling captureFrame
function firstFrame() {
  const path = URL.createObjectURL(document.querySelector("#video-file").files[0]);
  
  document.getElementById("frames-list").innerHTML = "";

  // If there are no shots to display, show error message
  if (keyTimes.length == 0) {
    document.getElementById("frames-list").innerHTML = "No shots returned from Video Intelligence API.<br>";
    
    // Ask user for the time interval between frames to capture or Cancel
    frameInterval = promptNumberInput();
    if (isNaN(frameInterval)) {
      return;
    }
    // If user did not Cancel and inputted a valid number of frames, call function to capture frames
    document.getElementById("frames-list").innerHTML += "Capturing frames every " + frameInterval + " seconds.";
    captureFrame(path, frameInterval);
  } 
  // If shots array is not empty, initialize variables and call function to capture frames
  else {
    keyTimesIndex = 0;
    frameInterval = -1;
    captureFrame(path, keyTimes[keyTimesIndex]);
  }
}

/** 
 * Prompts user for the number of frames they want to capture
 * 
 * @returns {number | NaN}: The user's input or NaN if Cancelled
 */
function promptNumberInput() {
  const message = "Error detecting shots in video. Check that your format is one of the following:" +
                  "\n.MOV, .MPEG4, .MP4, .AVI, formats decodable by ffmpeg. \n\n" + 
                  "Enter the time interval (in seconds) between image frames to analyze or " + 
                  "click Cancel to submit another file.";
  const defaultInput = 5;
  var input = "";
  // Reprompt user for input if input was not a number and did not Cancel prompt
  do {
    input = prompt(message, defaultInput);
  } while (input != null && isNaN(input));
  return parseInt(input);
}

/** 
 * Draws a frame of the video onto a canvas element
 * 
 * @param {string} path: The path of the video file
 * @param {number} secs: The time (seconds) of frame to be captured, truncated to last frame of video
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

    // If the user watches the video, the onseeked event will trigger. Reset event to do nothing
    video.onseeked = function(){};

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
  const video = document.getElementById("video");
  const li = document.createElement("li");
  li.innerHTML += "<b>Frame at second " + secs + ":</b><br>";

  // If video frame was successfully seeked, add the img to the document
  if (event.type == "seeked") {
    li.appendChild(img);
  } 
  // If the video was not successfully seeked, display error message
  else {
    li.innerHTML += "Error capturing frame";
  }
  document.getElementById("frames-list").appendChild(li);

  // Check if there are more frames to capture, depending on which method of shot detection was used
  if (frameInterval != -1 && (secs + frameInterval <= video.duration)) {
    captureFrame(video.src, secs + frameInterval);
  }
  else if (++keyTimesIndex < keyTimes.length) {
    captureFrame(video.src, keyTimes[keyTimesIndex]);
  }
}

// Captures the current frame of the video that is displayed 
function captureCurrentFrame() {
  const video = document.getElementById("video");

  // Draw video frame onto canvas element
  const canvas = document.createElement("canvas");
  canvas.height = video.videoHeight;
  canvas.width = video.videoWidth;
  const ctx = canvas.getContext("2d");
  ctx.drawImage(video, 0, 0, canvas.width, canvas.height);
  
  // Append canvas element to webpage
  const li = document.createElement("li");
  li.innerHTML += "<b>Frame at second " + video.currentTime + ":</b><br>";
  li.appendChild(canvas);
  document.getElementById("frames-list").appendChild(li);
}

// Displays the video to the webpage
function showVideo() {
  const video = document.getElementById("video");
  video.src = URL.createObjectURL(document.querySelector("#video-file").files[0]);
  video.style.display = "block";
}

// Hides the video from the webpage
function hideVideo() {
  const video = document.getElementById("video");
  video.style.display = "none";
}

function getEffect() {
  const list = document.getElementById("frames-list");
  const frames = list.getElementsByTagName("li");
  for (var i = 0; i < frames.length; i++) {
  
  }
}