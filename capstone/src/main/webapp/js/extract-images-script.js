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

// Array of shot time objects to keyframe images at
var keyTimes = [];

// Current index of keyTimes
var keyTimesIndex = 0;

// Time interval between frames for manually setting shot times (-1 if not using this method)
var frameInterval = -1;

// Variables for submitting the form through ajax
var blob;
var blobShotTimes;

// Input file's path
var videoPath;

// Sends GET request to ShotsServlet for the shot start and end times
function getShots() {
  // Add loading message to webpage
  const message = document.getElementById("loading");
  message.innerHTML = "Detecting shots...";

  fetch("/video-upload").then(response => response.json()).then(jsonObj => {
	console.log(jsonObj);

	// If there was an error getting the url, return
    if (jsonObj.error) {
      return;
    }

	fetch("/shots?url=gs:/" + jsonObj.url).then(response => response.json()).then(shots => {
	  // Remove loading message
	  const message = document.getElementById("loading");
	  message.innerHTML = "";

	  // Display each shot's times in a list and add the middle time of each shot to keyTimes array
	  for (const shot of shots) {
        const shotObject = {
          start: shot.start_time, 
          middle: ((shot.start_time + shot.end_time) / 2.0),
          end: shot.end_time
        };
        keyTimes.push(shotObject);			
	  }
      message.innerHTML = keyTimes.length + " shot(s) detected.";
	// Call method to capture and display image frames
	}).then(() => firstFrame());
  });
}

// Ajax code that submits video file form
$(document).ready(function() {

  // When the user submits the form to upload a video, 
  $("#upload-video").submit(function(event){
    // Cancel any default action normally occuring when the form submission triggers
    event.preventDefault(); 
    // Check that file was uploaded
    if (!saveFile()) {
      return;
    } else {
      const message = document.getElementById("loading");
      message.innerHTML = "Uploading video...";  	
      keyTimes = [];
      document.getElementById("frames-list").innerHTML = "";
      
      // Create a FormData object containing the file information
      const form = $('form')[0];
      const form_data = new FormData(form);
      // Create ajax request with the form data
      $.ajax({
        type: $(this).attr("method"),     // Use the form's 'method' attribute
        url: $(this).attr("action"),      // Use the form's 'action attribute
        data: form_data,                  // Send the video file which is stored in a FormData
        processData: false,               // Set as false so that 'data' will not be transformed into a query string
        contentType: false,               // Must be false for sending our content type (multipart/form-data)
        success: function(data) {
          console.log('Submission was successful.');
          message.innerHTML = "";
          const option = document.getElementById("shotMethod");
          if (option.value === "detect") {
            getShots();
          } else if (option.value === "interval") {
            frameInterval = promptNumberInput2();
            if (isNaN(frameInterval)) {
              return;
            }
            // If user did not Cancel and inputted a valid number of frames, call function to capture frames
            document.getElementById("frames-list").innerHTML += "Capturing frames every " + frameInterval + " seconds.";
            const shotObject = {
              start: 0, 
              middle: frameInterval,
              end: frameInterval
            };
            captureFrame(videoPath, shotObject);
          } else {
            alert("Click 'Show Video' and 'Capture Current Frame' at paused frames you want to capture.");
          }
        },
        error: function (data) {
          console.log('An error occurred.');
        },
      });
    }
  });
});

/** 
 * Saves the file path, or alerts the user that a file needs to be selected
 * 
 * @return {boolean}: Returns true if a file was selected, false otherwise
 */
function saveFile() {
  if (document.getElementById("video-file").value) { 
    videoPath = URL.createObjectURL(document.querySelector("#video-file").files[0]);
    return true;
  } else {
    alert("Please select a file.");
    return false;
  } 
}

// Gets the first frame in the video by calling captureFrame
function firstFrame() {
  const path = URL.createObjectURL(document.querySelector("#video-file").files[0]);

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
    const shotObject = {
      start: 0, 
      middle: frameInterval,
      end: frameInterval
    };
    captureFrame(path, shotObject);
  } 
  // If shots array is not empty, initialize variables and call function to capture frames
  else {
    // Otherwise, initialize variables
    keyTimesIndex = 0;
    frameInterval = -1;
    document.getElementById("frames-list").innerHTML = "";
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
function promptNumberInput2() {
  const message = 
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
 * @param {Object} shot: The start, middle, end time (seconds) of shot to be captured
 */
function captureFrame(path, shot) {
  console.log("captureFrame at " + shot.middle);
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
    const ctx = canvas.getContext("2d");

    // Draw video's current screen as an image onto canvas
    // 0, 0 sets the top left corner of where to start drawing
    // video.videoWidth, vidoe.videoHeight allows proper scaling when drawing the image
    ctx.drawImage(video, 0, 0, canvas.width, canvas.height);

    var img = document.createElement("img");
    img.id = "img-frame";
    canvas.toBlob(function(thisblob) {
      img.src = URL.createObjectURL(thisblob);

      // Upload blob to Cloud bucket by triggering the form's submit button
      blob = thisblob;
      const shotRounded = {
        start: Math.round(shot.start), 
        middle: Math.round(shot.middle),
        end: Math.round(shot.end)
      };
      blobShotTimes = shotRounded;
      document.getElementById("image-form-button").click();
    });

    // If the user watches the video, the onseeked event will trigger. Reset event to do nothing
    video.onseeked = function(){};

    // Call function that will display the frame to the page
    displayFrame(img, this.currentTime, event);
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
  console.log("displayFrame at " + secs);
  const video = document.getElementById("video");
  const li = document.createElement("li");
  li.innerHTML += "<b>Frame at second " + Math.round(secs) + ":</b><br>";

  // If video frame was successfully seeked, add the img to the document
  if (event.type == "seeked") {
    img.id = "image";
    li.appendChild(img);
  } 
  // If the video was not successfully seeked, display error message
  else {
    li.innerHTML += "Error capturing frame";
  }
  document.getElementById("frames-list").appendChild(li);

  // Check if there are more frames to capture, depending on which method of shot detection was used
  if (frameInterval != -1 && (secs + frameInterval <= video.duration)) {
    const shotObject = {
      start: secs, 
      middle: (secs + frameInterval),
      end: (secs + frameInterval)
    };
    captureFrame(video.src, shotObject);
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
  
  // Post image
  var img = document.createElement("img");
  img.id = "img-frame";
  canvas.toBlob(function(thisblob) {
    img.src = URL.createObjectURL(thisblob);

    // Upload blob to Cloud bucket by triggering the form's submit button
    blob = thisblob;
    const shotRounded = {
      start: Math.round(video.currentTime), 
      middle: start,
      end: start
    };
    blobShotTimes = shotRounded;
    document.getElementById("image-form-button").click();
  });

  // Append canvas element to webpage
  const li = document.createElement("li");
  li.innerHTML += "<b>Frame at second " + Math.round(video.currentTime) + ":</b><br>";
  li.appendChild(canvas);
  document.getElementById("frames-list").appendChild(li);
}

// Ajax code that submits the form on the jsp page to upload an image frame
$(document).ready(function() {
  $("#post-keyframe-img").submit(function(event){
    console.log("submitting form");
    event.preventDefault(); 
    var post_url = $(this).attr("action");
    var form_data = new FormData();
    form_data.append("image", blob);
    form_data.append("startTime", blobShotTimes.start);
    form_data.append("endTime", blobShotTimes.end);
    form_data.append("timestamp", blobShotTimes.middle);

    $.ajax({
      type: $(this).attr("method"),
      url: $(this).attr("action"),
      data: form_data,
      processData: false,
      contentType: false,
      success: function(data) {
        console.log('Submission was successful.');
      },
      error: function (data) {
        console.log('An error occurred.');
      },
    });
  });
});

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
