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
var keyTimes = [];

// Current index of keyTimes
var keyTimesIndex = 0;

// Time interval between frames for manually setting shot times (-1 if not using this method)
var userInputFrameInterval = -1;
var getFramesByUserInput = false;
// Used as a counter for how many frames have been captured if getting shots by time interval
var frameNum = 0;

// Video file path
var path = "";

// Updates video being shown to match the file input (updates when user changes file)
const file = document.getElementById("video-file");
if (file) {
  file.addEventListener("change", (event) => {
  if (document.forms["upload-video"]["video-file"].value == "") {
    // If the file becomes empty (user hits cancel when selecting) hide the video and the button to "Show Video"    
    hideVideo();
    document.getElementById("showHideVideo").style.display = "none";
  } else {
    // If a new file is selected, update the HTML video element's src
    showVideo();
    document.getElementById("showHideVideo").style.display = "block";
  }
});
}

// Hides the video from the webpage
function hideVideo() {
  const video = document.getElementById("video");
  video.style.display = "none";
  // Toggle button to allow user to show video
  const showHideButton = document.getElementById("showHideVideo");
  showHideButton.onclick = showVideo;
  showHideButton.innerText = "Show Video";
}

// Displays the video to the webpage
function showVideo() {
  const video = document.getElementById("video");
  video.src = URL.createObjectURL(document.querySelector("#video-file").files[0]);
  video.style.display = "block";
  // Toggle button to allow user to hide video
  const showHideButton = document.getElementById("showHideVideo");
  showHideButton.onclick = hideVideo;
  showHideButton.innerText = "Hide Video";
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
      // Add loading message to webpage and initialize variables
      keyTimes = [];
      document.getElementById("loading").innerHTML = "Uploading video...";
      document.getElementById("loader").style.display = "block"
    
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
          // If request was successful, call function to parse shot times
          console.log("Submission was successful.");
          document.getElementById("loader").style.display = "none";
          // Determine which option was selected and call correct function
          const option = getShotsOption();
          if (option === "shotsOption") {
            getShots();
          } else if (option === "intervalOption") {
            getInterval();
          } else {
            // TODO: print instructions and show button
          }
        },
        error: function (data) {
          console.log("An error occurred.");
          document.getElementById("loader").style.display = "none";
          alert("Sorry! An error occured while trying to upload your video. Please refresh the page and try again.")
        }
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
    path = URL.createObjectURL(document.querySelector("#video-file").files[0]);
    return true;
  } else {
    alert("Please select a file.");
    return false;
  } 
}

/** 
 * Get user's choice for detecting shots
 * 
 * @return {string}: User selected option for detecting shots
 */
function getShotsOption() {
  const options = document.getElementsByName("shotsOption");
  for (const option of options) {
    if (option.checked) {
      console.log(option.value);
      return option.value;
    }
  }
}

// Gets shot times for the uploaded video
function getShots() {
  // Add loading message to webpage
  const message = document.getElementById("loading");
  message.innerHTML = "Detecting shots...";

  fetch("/video-upload?name=" + datastoreName).then(response => response.json()).then(jsonObj => {
    // If there was an error getting the url, return
    if (jsonObj.error) {
      // TODO: invoke backup shot detection methods (in another branch)
      return;
    } else {
      // Send the bucket url to the Video Intelligence API and get shot times
      fetch("/shots?url=gs:/" + jsonObj.url).then(response => response.json()).then(shots => {
        // Remove loading message
        message.innerHTML = "";
                
        // Display each shot's times in a list and add the middle time of each shot to keyTimes array
        for (const shot of shots) {
          const shotObject = {
            start: shot.startTime, 
            middle: Math.round((shot.startTime + shot.endTime) / 2.0),
            end: shot.endTime
          };
          keyTimes.push(shotObject);
        }
        // Call method to capture and display image frames
      }).then(() => checkForShots());
    }
  });
}

// Checks if any shots were returned from Video Intelligence API and initializes variables
function checkForShots() {
  path = URL.createObjectURL(document.querySelector("#video-file").files[0]);
  
  if (keyTimes.length == 0) {
    // If there are no shots to display, invoke backup method of capturing shots with a time interval 
    promptUserForTime();
    frameNum = 0;
    if (!getFramesByUserInput) {
      return;
    } else {
      // Since userInputFrameInterval is a valid time interval, the first time to capture a frame at is equal to the userInputFrameInterval
      const shotObject = {
        start: 0, 
        middle: userInputFrameInterval,
        end: userInputFrameInterval
      };
      // Hide the video before capturing frames to look cleaner
      hideVideo();
      captureFrame(path, shotObject);
    }
  } 
  else {
    // Otherwise, initialize variables
    keyTimesIndex = 0;
    userInputFrameInterval = -1;
    getFramesByUserInput = false;
    frameNum = 0;
    // Hide the video before capturing frames to look cleaner
    hideVideo();
    captureFrame(path, keyTimes[keyTimesIndex]);
  }
}

// Prompts the user for a time interval
function promptUserForTime() {
  userInputFrameInterval = promptNumberInput();
  // If promptNumberInput() returned NaN, reset userInputFrameInterval to -1 and return
  if (isNaN(userInputFrameInterval)) {
    userInputFrameInterval = -1;
    getFramesByUserInput = false;
    return;
  }
  getFramesByUserInput = true;
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
  frameNum = 0;

  // The input value is already verified before user can submit the form, so no error checking needed
  userInputFrameInterval = parseInt(document.getElementById("timeInterval").value);
  
  // Update messages to user
  document.getElementById("loading").innerHTML = "";
  
  // Create first shot object and pass to captureFrame()
  const shotObject = {
    start: 0, 
    middle: userInputFrameInterval,
    end: userInputFrameInterval
  };
  // Hide the video before capturing frames to look cleaner
  hideVideo();
  captureFrame(path, shotObject);
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

    var img = document.createElement("img");
    img.id = "img-frame";
    canvas.toBlob(function(thisblob) {
      img.src = URL.createObjectURL(thisblob);
      
      // TODO: Post frame with shot details here (implemented in another branch)
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
  // Get the current slide number depending on what method of shot detection was used
  // Want slides to start at 1, but frameNum and keyTimesIndex start at 0
  var slideNumber;
  if (getFramesByUserInput) {
    slideNumber = ++frameNum;
  } else {
    slideNumber = ++keyTimesIndex;
  }
  const video = document.getElementById("video");
 
  // Create image slide for slideshow
  const slide = document.createElement("div");
  slide.classList.add("MySlides");
  slide.classList.add("image-fade");
  
  // Create corresponding dot that links to new slide
  const dot = document.createElement("span");
  dot.classList.add("dot");
  dot.onclick = function() {currentSlide(slideNumber);}

  // Print time rounded to nearest second
//   li.innerHTML += "<b>Frame at second " + Math.round(secs) + ":</b><br>";

  // If video frame was successfully seeked, add the img to the document
  if (event.type == "seeked") {
    img.classList.add("image");
    slide.appendChild(img);
  } 
  // If the video was not successfully seeked, display error message
  else {
    // li.innerHTML += "Error capturing frame";
  }

  document.getElementById("slideshow-container").append(slide);
  document.getElementById("dots-container").append(dot);

  // Check if there are more frames to capture, depending on which method of shot detection was used
  // If getFramesByUserInput is true, this means the keyTimes array was empty and the user had to input a time interval
  // To check if there are more frames to capure, see if going to the next userInputFrameInterval exceeds the video's end
  // Ex. 
  //    userInputFrameInterval = 5 s.
  //    video.duration = 12 s.
  //    secs = 10 s. (The last frame captured was at second 10)
  //    
  //    The next frame would be at 15 s., but since this is > 12 s., do not capture another frame
  const validNextFrame = (secs + userInputFrameInterval <= video.duration);
  if (getFramesByUserInput && validNextFrame) {
    const shotObject = {
      start: secs, 
      middle: secs + userInputFrameInterval,
      end: secs + userInputFrameInterval
    };
    captureFrame(video.src, shotObject);
  }
  // Otherwise, this means the keyTimes array was not empty and all times in the array should be captured
  // Since keyTimesIndex was already incremented, check if this index exists in keyTimes
  else if (keyTimesIndex < keyTimes.length) {
    captureFrame(video.src, keyTimes[keyTimesIndex]);
  }
  // If there were no more frames to capture, show the final slideshow
  showSlides(slideIndex);
  document.getElementsByClassName("prev")[0].style.display = "block";
  document.getElementsByClassName("next")[0].style.display = "block";
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
}

var slideIndex = 1;

function plusSlides(n) {
  showSlides(slideIndex += n);
}

function currentSlide(n) {
  showSlides(slideIndex = n);
}

function showSlides(n) {
  var i;
  var slides = document.getElementsByClassName("mySlides");
  var dots = document.getElementsByClassName("dot");
  if (n > slides.length) {slideIndex = 1}    
  if (n < 1) {slideIndex = slides.length}
  for (i = 0; i < slides.length; i++) {
      slides[i].style.display = "none";  
  }
  for (i = 0; i < dots.length; i++) {
      dots[i].className = dots[i].className.replace(" active", "");
  }
  slides[slideIndex-1].style.display = "block";  
  dots[slideIndex-1].className += " active";
}