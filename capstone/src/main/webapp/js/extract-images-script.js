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
var getFramesByUserInput = false;
// Used as a counter for how many frames have been captured if getting shots by time interval
var frameNum = 0;

// Variables for submitting the form through ajax
var blob;
var blobShotTimes;

// Current status
var submitting = false;

// Gets shot times for the uploaded video
function getShots() {
  // Add loading message to webpage
  const message = document.getElementById("loading");
  message.innerHTML = "Detecting shots...";

  fetch("/video-upload").then(response => response.json()).then(jsonObj => {
    // If there was an error getting the url, use backup shots option
    if (jsonObj.error) {
      checkForShots();
    } else {
      // Send the bucket url to the Video Intelligence API and get shot times
      console.log(jsonObj.url);
      fetch("/shots?url=gs:/" + jsonObj.url).then(response => response.json()).then(shots => {
        // Remove loading message
        message.innerHTML = "";
                
        // Add the middle time of each shot to keyTimes array
        for (const shot of shots) {
          const shotObject = {
            timestamp: Math.round((shot.start_time + shot.end_time) / 2.0),
            manuallyCaptured: false
          };
          keyTimes.push(shotObject);
        }
      }).then(() => checkForShots());
    }
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
    } else if (submitting) {
      alert("Unable to submit another request. Please wait for your first request to finish.")
    } else {
      // Add loading message to webpage and initialize variables
      keyTimes = [];
      keyTimesIndex = 0;
      userInputFrameInterval = -1;
      getFramesByUserInput = false;
      frameNum = 0;
      slideIndex = 1;
      submitting = true;
      document.getElementById("loading").innerHTML = "Uploading video...";
      document.getElementById("loader").style.display = "block"
      const option = getShotsOption();
      // Create a ForData object containing the file information
      const form = $("form")[0];
      const form_data = new FormData(form);
      // Create ajax request with the form data
      $.ajax({
        type: $(this).attr("method"),     // Use the form's 'method' attribute
        url: $(this).attr("action"),      // Use the form's 'action attribute
        data: form_data,                  // Send the video file which is stored in a FormData
        processData: false,               // Set as false so that 'data' will not be transformed into a query string
        contentType: false,               // Must be false for sending our content type (multipart/form-data)
        success: function(data) {
          // Determine which option was selected and call correct function
          if (option === "detectOption") {
            getShots();
          } else if (option === "intervalOption") {
            getInterval();
          } else {
            setupManualCapture();
          }
        },
        error: function (data) {
          document.getElementById("loader").style.display = "none";
          document.getElementById("loading").innerHTML = "";
          submitting = false;
          alert("Sorry! An error occured while trying to upload your video. Please refresh the page and try again.")
        }
      });
    }
  });
});

/** 
 * Get user's choice for detecting shots
 * 
 * @return {string}: User selected option for detecting shots
 */
function getShotsOption() {
  const options = document.getElementsByName("shotsOption");
  for (const option of options) {
    if (option.checked) {
      return option.value;
    }
  }
}

// Checks if any shots were returned from Video Intelligence API and initializes variables
function checkForShots() {
  if (keyTimes.length == 0) {
    // If there are no shots to display, invoke backup method of capturing shots with a time interval 
    promptUserForTime();
    if (!getFramesByUserInput) {
      return;
    } else {
      // Since userInputFrameInterval is a valid time interval, the first time to capture a frame at is equal to the userInputFrameInterval
      const shotObject = {
        timestamp: userInputFrameInterval,
        manuallyCaptured: false
      };
      hideVideo();
      captureFrame(path, shotObject);
    }
  } 
  else {
    // Otherwise, capture frames based on keyTimes array
    hideVideo();
    captureFrame(path, keyTimes[keyTimesIndex]);
  }
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
  input = prompt(message, defaultInput);
  
  // Reprompt user for input if input was not a number and did not Cancel prompt
  while ((input != null && isNaN(input)) || input <= 0) {
    alert("Time interval must be a valid number greater than 0.");
    input = prompt(message, defaultInput);
  } 
  return parseInt(input);
}

// Initializes variables for capturing images by time interval and calls captureFrame()
function getInterval() {
  getFramesByUserInput = true;

  // The input value is already verified before user can submit the form, so no error checking needed
  userInputFrameInterval = parseInt(document.getElementById("timeInterval").value);
  
  // Update messages to user
  document.getElementById("loading").innerHTML = "";
  
  // Create first shot object and pass to captureFrame()
  const shotObject = {
    timestamp: userInputFrameInterval,
    manuallyCaptured: false
  };
  hideVideo();
  captureFrame(path, shotObject);
}

/** 
 * Draws a frame of the video onto a canvas element
 * 
 * @param {string} path: The path of the video file
 * @param {Object} shot: The start, middle, end time (seconds) of shot to be captured
 */
function captureFrame(path, shot) {
  console.log("captureFrame at " + shot.timestamp);
  // Load video src (needs to be reloaded for events to be triggered)
  const video = document.getElementById("video");
  video.src = path;

  // When the metadata has been loaded, set the time of the video to be captured
  video.onloadedmetadata = function() {
    this.currentTime = shot.timestamp;
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
    
    const img = postFrame(canvas, shot);

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
 * Posts an image to Datastore and Google Cloud Storage
 * 
 * @param {HTMLElement} canvas: The canvas element with the frame drawn on it
 * @param {Object} shot: The canvas' shot object
 * @return {HTMLElement}: An img element containing the frame's src
 */
function postFrame(canvas, shot) {
  var img = document.createElement("img");
  canvas.toBlob(function(thisblob) {
    img.src = URL.createObjectURL(thisblob);
    
    // Upload blob to Cloud bucket by triggering the form's submit button
    blob = thisblob;
    blobShotTimes = shot;
    document.getElementById("image-form-button").click();
  });
  return img;
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
  const caption = document.createElement("div");
  caption.classList.add("caption");
  
  // If video frame was successfully seeked, add the img to the document
  if (event.type == "seeked") {
    caption.innerText = "Timestamp: " + getTimestamp(Math.round(secs));
  } 
  // If the video was not successfully seeked, display error message
  else {
    caption.innerText = "Error capturing frame at " + getTimestamp(Math.round(secs));
  }
  
  createSlide(img, caption);

  // Check if there are more frames to capture, depending on which method of shot detection was used
  const validNextFrame = (secs + userInputFrameInterval <= video.duration);
  if (getFramesByUserInput && validNextFrame) {
    const shotObject = {
      timestamp: secs + userInputFrameInterval,
      manuallyCaptured: false
    };
    captureFrame(video.src, shotObject);
  }
  else if (keyTimesIndex < keyTimes.length) {
    captureFrame(video.src, keyTimes[keyTimesIndex]);
  }
  else {
    // If there were no more frames to capture, show the final slideshow
  submitting = false;
  document.getElementById("loader").style.display = "none";
  document.getElementById("loading").innerHTML = "View your captured image frames in the slideshow below." +
  " Click \"Show Video\" to see your uploaded video again. You can also pause the video and click" + 
  " the camera icon <i class=\"fa fa-camera\" style=\"color: #4285f4\"></i> to capture a frame yourself."
  " Click \"Calculate Effect\" to see your video's image and audio analysis."; 
  document.getElementsByClassName("buttonsToHide")[0].style.display = "inline";
  showSlides(slideIndex);
  document.getElementsByClassName("prev")[0].style.display = "block";
  document.getElementsByClassName("next")[0].style.display = "block";
  }
}

// Prints instructions for manual image capturing and shows buttons
function setupManualCapture() {
  submitting = false;
  document.getElementById("loading").innerHTML = "Pause your video " + 
  " and click the camera icon <i class=\"fa fa-camera\" style=\"color: #4285f4\"></i> to capture the frame." +
  " Captured frames will show in a slideshow below. Click \"Calculate Effect\" to see your video's image and audio analysis.";
  document.getElementsByClassName("buttonsToHide")[0].style.display = "inline";
  document.getElementById("loader").style.display = "none";
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
  
  // Create shot object 
  const shotObject = {
    timestamp: Math.floor(video.currentTime),
    manuallyCaptured: true
  };
  const img = postFrame(canvas, shotObject);

  // Create caption and slide
  const caption = document.createElement("div");
  caption.classList.add("caption");
  caption.innerHTML = "<i class=\"fa fa-camera\"></i>" + " Timestamp: " + getTimestamp(Math.floor(video.currentTime));
  createSlide(img, caption);

  // Show most recent capture on slideshow
  slideIndex = $(".dot").length;
  showSlides(slideIndex);
  document.getElementsByClassName("prev")[0].style.display = "block";
  document.getElementsByClassName("next")[0].style.display = "block";
}

// Ajax code that submits the form on the jsp page to upload an image frame
$(document).ready(function() {
  $("#post-keyframe-img").submit(function(event){
    console.log("submitting form");
    event.preventDefault(); 
    var post_url = $(this).attr("action");
    var form_data = new FormData();
    form_data.append("image", blob);
    form_data.append("startTime", blobShotTimes.timestamp);
    form_data.append("endTime", blobShotTimes.timestamp);
    form_data.append("timestamp", blobShotTimes.timestamp);

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

/** 
 * Converts seconds to time
 * 
 * @param {number} secs: The time of the video frame that was captured in seconds
 * @return {string}: A time format (12:31)
 */
function getTimestamp(secs) {
  const minutes = Math.floor(secs / 60);
  const seconds = secs % 60;
  var time = minutes + ":";
  if(seconds < 10) {
    time += "0";
  }
  time += seconds;
  return time;
}