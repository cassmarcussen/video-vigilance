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

// Array of shot objects to keyframe images at
const keyTimes = [];

// Current index of keyTimes
var keyTimesIndex = 0;

// Time interval between frames for manually setting shot times (-1 if not using this method)
var frameInterval = -1;

// Video file path
var path = "";

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

    // Display each shot's times in a list and add start, middle, end time of each shot to keyTimes array
    for (const shot of shots) {
      const listElement = document.createElement("li");
      const textElement = document.createElement("span");
      textElement.innerHTML = "<b>Shot " + count + ": <b>" + Math.round(shot.startTime) + " - " + Math.round(shot.endTime);
      listElement.appendChild(textElement);
      list.append(listElement);
      const shotObject = {
        start: shot.startTime, 
        middle: Math.round((shot.startTime + shot.endTime) / 2.0),
        end: shot.endTime
      };
      keyTimes.push(shotObject);
      count++;
    }
  });
}

// Checks if any shots need to be captured and initializes variables
function checkForShots() {
  path = URL.createObjectURL(document.querySelector("#video-file").files[0]);
  
  if (keyTimes.length == 0 || !document.getElementById("video-file").value) {
    // If there are no shots to display or no file is selected, show error message
    document.getElementById("frames-list").innerHTML = "No shots returned from Video Intelligence API.<br>";
    promptUserForTime();
  } 
  else {
    // Otherwise, initialize variables
    keyTimesIndex = 0;
    frameInterval = -1;
    document.getElementById("frames-list").innerHTML = "";
    captureFrame(path, keyTimes[keyTimesIndex]);
  }
}

// Prompts the user for a time interval
function promptUserForTime() {
  frameInterval = promptNumberInput();
  // If promptNumberInput() returned NaN, reset frameInterval to -1 and return
  if (isNaN(frameInterval)) {
    frameInterval = -1;
    return;
  }
  // If user did not Cancel and inputted a valid time interval, call function to capture frames
  document.getElementById("frames-list").innerHTML += "Capturing frames every " + frameInterval + " seconds.";
  // Since frameInterval is a valid time interval, the first time to capture a frame at is equal to the frameInterval
  const shotObject = {
    start: 0, 
    middle: frameInterval,
    end: frameInterval
  };
  captureFrame(path, shotObject);
}

/** 
 * Prompts user for the time interval they want to capture frames
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
 * Draws a frame of the video onto a canvas element. If the middle of the shot time is longer 
 * than the video's duration, the very last frame of the video will be captured.
 * 
 * @param {string} path: The path of the video file
 * @param {Object} shot: The start, middle, end time (seconds) of shot to be captured
 */
function captureFrame(path, shot) {
  // Load video src (needs to be reloaded for events to be triggered)
  const video = document.getElementById("video");
  video.src = path;

  // When the metadata has been loaded, set the time of the video to be captured
  video.onloadedmetadata = function() {
    this.currentTime = shot.middle;
  };
	
  // When the video has seeked to the specific time, draw the frame onto a canvas element
  video.onseeked = function(event) {
    const canvas = document.createElement("canvas");
    canvas.height = video.videoHeight;
    canvas.width = video.videoWidth;

    // Get 2d drawing context on canvas
    const canvasContext = canvas.getContext("2d");

    // Draw video's current screen as an image onto canvas
    // 0, 0 sets the top left corner of where to start drawing
    // video.videoWidth, vidoe.videoHeight allows proper scaling when drawing the image
    canvasContext.drawImage(video, 0, 0, canvas.width, canvas.height);

    // TODO: Post frame with shot details here (implemented in another branch)
    
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

  // Print time rounded to nearest second
  li.innerHTML += "<b>Frame at second " + Math.round(secs) + ":</b><br>";

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
  // If frameInterval is not -1, this means the keyTimes array was empty and the user had to input a time interval
  // To check if there are more frames to capure, see if going to the next frameInterval exceeds the video's end
  // Ex. 
  //    frameInterval = 5 s.
  //    video.duration = 12 s.
  //    secs = 10 s. (The last frame captured was at second 10)
  //    
  //    The next frame would be at 15 s., but since this is > 12 s., do not capture another frame
  if (frameInterval != -1 && (secs + frameInterval <= video.duration)) {
    const shotObject = {
      start: secs, 
      middle: secs + frameInterval,
      end: secs + frameInterval
    };
    captureFrame(video.src, shotObject);
  }
  // If frameInterval is -1, this means the keyTimes array was not empty and all times in the array should be captured
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

  // TODO: Post frame with shot details here (implemented in another branch)
  
  // Append canvas element to webpage
  const li = document.createElement("li");
  li.innerHTML += "<b>Frame at second " + Math.round(video.currentTime) + ":</b><br>";
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
