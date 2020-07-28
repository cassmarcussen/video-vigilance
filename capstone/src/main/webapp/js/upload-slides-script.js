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


/** Javascript functions for creating and displaying image slides */

// Current slide being displayed in slideshow
var slideIndex = 1;

// Use transparent placeholder so page doesn't scroll to the top when a new image is added to slideshow
const transparentElement = document.getElementById("transparentPlaceholder");
transparentElement.height = $("#video").videoHeight;
transparentElement.width = $("#video").videoWidth;

/** 
 * Creates the slide and corresponding dot to add to the slideshow
 * 
 * @param {Object} shotObject: The object containing shot information
 */
function createSlide(shotObject) {
  // Get the current slide number depending on what method of shot detection was used
  // Want slides to start at 1, but frameNum and keyTimesIndex start at 0
  var slideNumber;
  if (getFramesByUserInput) {
    slideNumber = ++frameNum;
  } else {
    // If using the API to detect shots or if manually capturing frames, keyTimesIndex will start at 0
    slideNumber = ++keyTimesIndex;
  }

  // Create image slide for slideshow
  const slide = document.createElement("div");
  slide.classList.add("mySlides");
  slide.classList.add("image-fade");
  
  // Create corresponding dot that links to new slide (using slideNumber)
  const dot = document.createElement("span");
  if (shotObject.manuallyCaptured) {
    dot.classList.add("blueDot");
  } else {
    dot.classList.add("grayDot");
  }
  dot.classList.add("dot");
  shotObject.dot = dot;
  
  // Append image and caption to slide
  if (typeof shotObject.img !== "undefined") {
    shotObject.img.classList.add("image");
    slide.appendChild(shotObject.img);
  }
  slide.appendChild(shotObject.caption);
  shotObject.slide = slide;
  
  var shotObjectIndex;
  if (shotObject.manuallyCaptured) {
    shotObjectIndex = sortImages(shotObject);
  } else {
    shotObjectIndex = keyTimes.length;
    shotObject.dot.onclick = function() {currentSlide(slideNumber);}
    document.getElementById("dots-container").append(shotObject.dot);
    document.getElementById("slideshow-container").append(shotObject.slide);
  }

  // If there are too many dots, lower the margin size between them
  if (slideNumber > 36) {
    document.getElementsByClassName("dot")[0].style.margin = "1px";
  } 

  //return index

  console.log(keyTimes);
}

/**
 * Sorts objects in keyTimes array by timestamp
 * 
 * @param {Object} shotObject: New shot that was added
 * @return {number}: Index that shotObject was inserted in
 */
async function sortImages(shotObject) {
  // Sort array
  keyTimes.sort((object1, object2) => (object1.timestamp - object2.timestamp));
  
  // Insert new shotObject image and dot in correct place in HTML containers
  const shotObjectIndex = keyTimes.indexOf(shotObject);
  const slideshow = document.getElementById("slideshow-container");
  const slides = document.getElementsByClassName("mySlides");
  slideshow.insertBefore(shotObject.slide, slides[shotObjectIndex]);
  const dotsContainer = document.getElementById("dots-container");
  const dots = document.getElementsByClassName("dot");
  dotsContainer.insertBefore(shotObject.dot, dots[shotObjectIndex]);

  // Update dots' onclick functions
  for (var i = shotObjectIndex; i < keyTimes.length; i++) {
    await setOnclickFunction(keyTimes[i].dot, i + 1);
  }

  return shotObjectIndex;
}

function setOnclickFunction(dot, slideNum) {
  return new Promise(resolve => dot.onclick = function() {currentSlide(slideNum);});
}

/**
 * Shows the slide n away from current slide
 * Taken from: https://www.w3schools.com/howto/howto_js_slideshow.asp
 */
function plusSlides(n) {
  showSlides(slideIndex += n);
}

/**
 * Shows slide n
 * Taken from: https://www.w3schools.com/howto/howto_js_slideshow.asp
 */
function currentSlide(n) {
  showSlides(slideIndex = n);
}

/**
 * Hides all other slides and shows slide n 
 * Taken from: https://www.w3schools.com/howto/howto_js_slideshow.asp
 * Modified to support blue and gray dots (mark which frames are manually captured)
 */
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
    dots[i].className = dots[i].className.replace(" blueActive", "");
    dots[i].className = dots[i].className.replace(" grayActive", "");
  }
  slides[slideIndex-1].style.display = "block"; 
  if (dots[slideIndex-1].className.includes("blueDot")) {
    dots[slideIndex-1].className += " blueActive";
  } else {
    dots[slideIndex-1].className += " grayActive";
  }
}
