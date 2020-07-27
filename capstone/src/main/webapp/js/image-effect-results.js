var slideIndex = 1;

/*window.onload = function() {
  fetchBlobstoreKeyframeImages();
};*/

var sharedArrayOfKeyframeImages = [];

// Called on Results page onload
function fetchImageEffect() {
  sharedArrayOfKeyframeImages = [];
  fetchBlobstoreKeyframeImages(false);
}

function displayFlaggedImages() {
  //document.getElementById("modifiable-content").innerHTML = "";
  console.log("displayFlaggedImages");

  clearDisplayOfDots();
  setupUnloadedDisplayOnButtonClick();
  var shouldDisplayOnlyFlaggedImages = true;

  // If we haven't gotten the keyframe images array yet
  if (sharedArrayOfKeyframeImages.length == 0) {
    fetchBlobstoreKeyframeImages(shouldDisplayOnlyFlaggedImages);
  } else {
    //just do the display part
    createHtmlDisplay(sharedArrayOfKeyframeImages, shouldDisplayOnlyFlaggedImages);
  }

}

function displayAllImages() {
  //document.getElementById("modifiable-content").innerHTML = "";

  console.log("displayAllImages");
  clearDisplayOfDots();
  setupUnloadedDisplayOnButtonClick();
  var shouldDisplayOnlyFlaggedImages = false;
  fetchBlobstoreKeyframeImages(shouldDisplayOnlyFlaggedImages);

  // If we haven't gotten the keyframe images array yet
  if (sharedArrayOfKeyframeImages.length == 0) {
    fetchBlobstoreKeyframeImages(shouldDisplayOnlyFlaggedImages);
  } else {
    //just do the display part
    createHtmlDisplay(sharedArrayOfKeyframeImages, shouldDisplayOnlyFlaggedImages);
  }
}

function htmlForEffect(effectForACategory, effectsAsNumbers, categoryName) {
  var htmlForEffect = '<label for="score">' + categoryName + ': ';
  htmlForEffect += effectForACategory;
  htmlForEffect += '<div class="tooltip-info"><i class="fa fa-info-circle" aria-hidden="true"></i><span class="tooltiptext-info">'+ getInformationAboutEffect(categoryName) + '</span></div>' ;
  htmlForEffect += '</label><meter id="score" value="' + effectsAsNumbers.get(effectForACategory) + '"  min="0" low="3" high="4" optimum="6" max="5"></meter>';
  return htmlForEffect;
}

/* getNumberOfEffectParameter returns a number corresponding to the effect likelihood of 
the keyframe image. This is used for the html meter which visually displays the likelihood 
of each SafeSearch parameter on the page. The numbers returned are used to fill in the meter by 
a certain amount (the number returned divided by 5).
*/
function getNumberOfEffectParameter(effectParameter) {

  var numberOfEffect = 0;

  switch (effectParameter) {
    case 'UNKNOWN':
      numberOfEffect = 0;
      break;
    case 'VERY_UNLIKELY':
      numberOfEffect = 1;
      break;
    case 'UNLIKELY':
      numberOfEffect = 2;
      break;
    case 'POSSIBLE':
      numberOfEffect = 3;
      break;
    case 'LIKELY':
      numberOfEffect = 4;
      break;
    case 'VERY_LIKELY':
      numberOfEffect = 5;
      break;
    default:
      numberOfEffect = 0;
      break;
  }

  return numberOfEffect;
}

/* getInformationAboutEffect returns the html for the information about the Effect of the Frame results, 
by each category returned.
*/
function getInformationAboutEffect(categoryName) {
  var information = "";

  switch (categoryName) {
    case 'Adult':
      information += "Adult: Represents the adult content likelihood for the image. Adult content may contain elements such as nudity, pornographic images or cartoons, or sexual activities.";
      break;
    case 'Spoofed':
      information += "Spoofed: The likelihood that an modification was made to the image's canonical version to make it appear funny or offensive.";
      break;
    case 'Medical':
      information += "Medical: Likelihood that this is a medical image.";
      break;
    case 'Violence':
      information += "Violence: Likelihood that this image contains violent content.";
      break;
    case 'Racy':
      information += "Racy: Likelihood that the request image contains racy content. Racy content may include (but is not limited to) skimpy or sheer clothing, strategically covered nudity, lewd or provocative poses, or close-ups of sensitive body areas.";
      break;
    default:
      break;
  }

  return information;
}

/* All of the KeyframeImage parameters related to time represent the time in number of seconds. 
getReadableTimeFormat converts this time in seconds to a readable time in the format: [number of minutes]:[number of seconds]
*/
function getReadableTimeFormat(timeInSeconds) {
  // Get number of minutes by dividing by 60 and rounding down
  var minutes = Math.floor(timeInSeconds / 60);

  // Get number of seconds by finding the remainder when dividing by 60, i.e. mod 60
  var seconds = timeInSeconds % 60;

  var readableTimeFormatMinutesAndSeconds = minutes + ":";
  
  if(seconds < 10) {
    readableTimeFormatMinutesAndSeconds += "0";
  }

  readableTimeFormatMinutesAndSeconds += seconds;

  return readableTimeFormatMinutesAndSeconds;
}

/* setEffectsAsNumbers sets the effectsAsNumbers map which has keys which are the effect categories and 
values which are the numbers corresponding to the likelihood value for each of these effect categories
*/
function setEffectsAsNumbers(effect) {

  var effectsAsNumbers = new Map();

  effectsAsNumbers.set(effect.adult, getNumberOfEffectParameter(effect.adult));
  effectsAsNumbers.set(effect.medical, getNumberOfEffectParameter(effect.medical));
  effectsAsNumbers.set(effect.spoofed, getNumberOfEffectParameter(effect.spoofed));
  effectsAsNumbers.set(effect.violence, getNumberOfEffectParameter(effect.violence));
  effectsAsNumbers.set(effect.racy, getNumberOfEffectParameter(effect.racy));

  return effectsAsNumbers;
}

/* createKeyframeImageTextInnerHTML creates the html that displays the information about the keyframe image 
that is displayed on the card shown to the user
*/
function createKeyframeImageTextInnerHTML(thisImage) {
  // timestamp, startTime, and endTime are the values as number of seconds, so we need to convert this to a readable format, i.e. [number of minutes]:[number of seconds]
  var timestamp = getReadableTimeFormat(thisImage.timestamp);
  var effect = thisImage.effect;
  var effectParsed = JSON.parse(effect);

  var effectsAsNumbers = setEffectsAsNumbers(effectParsed);

  var keyframeImageTextInnerHTML = '<h2>Timestamp of image: ' + timestamp + '</h2>' 
  + '<hr>'
  + '<h2>Effect of the frame </h2>' 
  + '<p>Likeliness values are: Unknown, Very Unlikely, Unlikely, Possible, Likely, and Very Likely</p>'
  + htmlForEffect(effectParsed.adult, effectsAsNumbers, "Adult")
  + htmlForEffect(effectParsed.medical, effectsAsNumbers, "Medical")
  + htmlForEffect(effectParsed.spoofed, effectsAsNumbers, "Spoofed")
  + htmlForEffect(effectParsed.violence, effectsAsNumbers, "Violence")
  + htmlForEffect(effectParsed.racy, effectsAsNumbers, "Racy");

  return keyframeImageTextInnerHTML;
}

/* setFlaggedImageSummaryComment creates and sets the html that displays the summary about how many 
keyframe images have been flagged for sensitive content by the Vision API's SafeSearch detection method
*/
function setFlaggedImageSummaryComment(numberOfFlaggedImages) {


  var flaggedImageText = "<h2>Number of flagged images: " + numberOfFlaggedImages + "</h2>";
  var noFlaggedImageText = "<h2>You have no flagged images. </h2>" 
    + "<p>This means that the fields of adult, medical, spoofed, violence, and racy have been determined to be very unlikely, unlikely, possible, or unknown. "
    + "No key frames have been determined to have a status of likely or very likely for any of these fields. Great job!</p>";

  var flaggedImageSummaryComment = numberOfFlaggedImages > 0 ? flaggedImageText : noFlaggedImageText;
  document.getElementById("results-overview").innerHTML = flaggedImageSummaryComment;

}

function clearDisplayOfDots() {
    document.getElementById("dots").innerHTML = "";
}

/* setDisplayAndHtmlOfDots makes the first image display on the page, and the first dot below the slideshow of images highlighted
*/
function setDisplayOfDots(index, keyframeImageDiv, numberOfKeyframeImages) {
 
  if (index == 0) {
    document.getElementById("dots").innerHTML += '<span class="dot active" onclick="currentSlide(' + (index + 1) + ')"></span>';
  } else if (index < numberOfKeyframeImages) {
    // Only add more dots if this won't be redundant, i.e. as long as we haven't reached our maxiumum number of dots to add
    document.getElementById("dots").innerHTML += '<span class="dot" onclick="currentSlide(' + (index + 1) + ')"></span>';
  }
}

/* createSingularKeyframeImageCard sets up the variable HTML code for displaying a singular keyframe image card 
in the slideshow of keyframe images. It sets up the CSS classes, the HTML elements to add, and the effect displayed.
It returns isFlagged, a value which true if the particular keyframe image is flagged.
*/
function createSingularKeyframeImageCard(thisImage, index, shouldDisplayOnlyFlaggedImages, numberOfKeyframeImages) {

  var imageIsFlagged = false;

  var keyframeImagesContainer = document.getElementById("results-img"); 

  //var modifiableNumberOfFlaggedImages = numberOfFlaggedImages;

  var keyframeImageDiv = document.createElement("div"); 
  keyframeImageDiv.classList.add("mySlides");
  keyframeImageDiv.classList.add("keyframe-card-fade");

  var keyframeImage = document.createElement("img");
  keyframeImage.src = thisImage.cloudBucketUrl.replace("gs://", "https://storage.cloud.google.com/");

  // This condition makes sure the the keyframe image retrieved from the database is not undefined, 
  // where undefined images either have a null src or 'undefined' in their source url. This is here 
  // because we do not want to display undefined images (i.e. displaying no image) on the Results page.
  if (keyframeImage.src != null && keyframeImage.src.indexOf("undefined") == -1) {

    keyframeImageDiv.appendChild(keyframeImage);

    var imageCaptionDiv = document.createElement("div");
    imageCaptionDiv.classList.add("container");

    var effect = JSON.parse(thisImage.effect);

    var effectsAsNumbers = setEffectsAsNumbers(effect);

    // Don't display the image if it has no 4 or 5 (likely or very unlikely sensitive content), 
    // i.e. only show the image if one of the effect parameters is 'likely' or 'very likely', and potentially 'possible'.
    if (!Array.from(effectsAsNumbers.values()).includes(4) && !Array.from(effectsAsNumbers.values()).includes(5)) {
      // continue is commented out temporarily for testing, so that all keyframe images are displayed instead of just those flagged for negative effect
      //continue;
     // return;
    } else {
      // Else, mark the image as flagged, i.e. increase the number of flagged images by one.
     // modifiableNumberOfFlaggedImages++;
      imageIsFlagged = true;
    }

    if(!imageIsFlagged && shouldDisplayOnlyFlaggedImages) {

    }else {
      var keyframeImageText = document.createElement("p");
      keyframeImageText.innerHTML = createKeyframeImageTextInnerHTML(thisImage);

      imageCaptionDiv.appendChild(keyframeImageText);

      keyframeImageDiv.appendChild(imageCaptionDiv);

      keyframeImagesContainer.append(keyframeImageDiv);

      setDisplayOfDots(index, keyframeImageDiv, numberOfKeyframeImages);
    }

    //keyframeImageDiv.style.display = "block";

  }

  return imageIsFlagged;
}

function setupUnloadedDisplayOnButtonClick() {

    document.getElementById('image-result-message').innerText = "Extracting the visual effect of the video. Please wait a minute for our application to analyze your video. Do not refresh the page as you wait.";
    // After first image created, then add in the arrows < > to get from one image to the next
    document.getElementsByClassName('prev')[0].style.display = "none";
    document.getElementsByClassName('next')[0].style.display = "none";

    document.getElementById('results-img').style.display = "none";
    /* Show the loader */
    document.getElementById('keyframeimage-loader').style.display = "block";
}

function setupLoadedDisplay() {
    document.getElementById('image-result-message').innerText = "The following keyframe images have been flagged for negative effect. The type of negative content detected is also displayed below.";
    // After first image created, then add in the arrows < > to get from one image to the next
    document.getElementsByClassName('prev')[0].style.display = "block";
    document.getElementsByClassName('next')[0].style.display = "block";
    /* Hide the loader */
    document.getElementById('keyframeimage-loader').style.display = "none";
    document.getElementById("keyframe-display-allorflagged-buttons").style.display = "block";

    document.getElementById('results-img').style.display = "block";
}

/* createKeyframeImageSlideshow creates the slideshow of cards with keyframe images and their corresponding 
information and SafeSearch detected effect. It does so by iterating through the array of keyframe images 
returned from DataStore and calling createSingularKeyframeImageCard for each keyframe image to create a card in the 
slideshow for each flagged keyframe image.
*/
function createKeyframeImageSlideshow(arrayOfKeyframeImages, shouldDisplayOnlyFlaggedImages) {

  var keyframeImagesContainer = document.getElementById("results-img"); 

  keyframeImagesContainer.innerHTML = "";

  var numberOfFlaggedImages = 0;

  for (var i=0; i < arrayOfKeyframeImages.length; i++) {

    var thisImage = arrayOfKeyframeImages[i];

    var imageIsFlagged = createSingularKeyframeImageCard(thisImage, i, shouldDisplayOnlyFlaggedImages, arrayOfKeyframeImages.length);

    setupLoadedDisplay();

    if (imageIsFlagged) {
        numberOfFlaggedImages++;
    }

  }

  // If no images to show in the carousel
  if((shouldDisplayOnlyFlaggedImages && numberOfFlaggedImages == 0) || (arrayOfKeyframeImages.length == 0)) {
    keyframeImagesContainer.innerHTML = "<div id='filler-box'>No images</div>";
    document.getElementsByClassName('prev')[0].style.display = "none";
    document.getElementsByClassName('next')[0].style.display = "none";
  }


  return numberOfFlaggedImages;
}

function createHtmlDisplay(arrayOfKeyframeImages, shouldDisplayOnlyFlaggedImages) {
  // Number of flagged images:
  var numberOfFlaggedImages = 0;

  numberOfFlaggedImages = createKeyframeImageSlideshow(arrayOfKeyframeImages, shouldDisplayOnlyFlaggedImages);

  setFlaggedImageSummaryComment(numberOfFlaggedImages);

  // Show slides on 1st entry, if we have an entry to show
  if (arrayOfKeyframeImages.length > 0) {
    if(numberOfFlaggedImages > 0 || !shouldDisplayOnlyFlaggedImages) {
      showSlides(1);
    }

  } else {
    //maybe also say there are no images
    document.getElementById('keyframeimage-loader').style.display = "none";
  }

}

/* fetchBlobstoreKeyframeImages calls the GET method of the KeyframeImageUploadServlet to get the 
keyframe images from DataStore and the Google Cloud Bucket. It then gets the image's effect using 
the Google Cloud Vision API (called from Java), and displays keyframe images that are flagged for 
possible, likely, or very likely sensitive content.
*/
async function fetchBlobstoreKeyframeImages(shouldDisplayOnlyFlaggedImages) {

  console.log("Fetching blobstore keyframe images");

  fetch('/keyframe-image-upload', {method: 'GET'})
    .then((response) => {
      return response.text();
    })
    .then(async function (keyframeImages) {

      var arrayOfKeyframeImages = JSON.parse(keyframeImages);

      for (var i = 0; i < arrayOfKeyframeImages.length; i++) {

        var myEffect = await getImageEffect(arrayOfKeyframeImages[i]);

        arrayOfKeyframeImages[i].effect = myEffect;
      }

      return arrayOfKeyframeImages;
        
    })
    .then((arrayOfKeyframeImages) => {

      sharedArrayOfKeyframeImages = arrayOfKeyframeImages;

      createHtmlDisplay(arrayOfKeyframeImages, shouldDisplayOnlyFlaggedImages);

    });   
}

/* getImageEffect makes a call to the ImageEffectServlet, which calls the Cloud Vision API's SafeSearch 
method to get the effect of the image.
*/
function getImageEffect(keyframeImage) {

  var response = fetch('/keyframe-effect-servlet?image_url=' + keyframeImage.cloudBucketUrl, 
    {
      method: 'GET'
    })
    .then((response) => {
      return response.text();
    })
    .then((response) => {
      return response;
    }
  );

  return response;

}

function deleteEntries() {

  //NT get the bucket name from the POST... or store that somewhere...

  const responseDeletePromise = fetch('/keyframe-image-delete', { method: 'POST'});

  /* location.reload() is a predefined JS function for a predefined JS class named location. */
  location.reload();
}


/* Based on https://www.w3schools.com/howto/howto_js_slideshow.asp */

// Next/previous controls
function plusSlides(n) {
  showSlides(slideIndex += n);
}

// Thumbnail image controls
function currentSlide(n) {
  showSlides(slideIndex = n);
}

function showSlides(n) {
  var i;
  var slides = document.getElementsByClassName("mySlides");
  var dots = document.getElementsByClassName("dot");
  //Clear before showing, in case one of the tabs for showing/hiding non-flagged images is clicked
  dots.innerHTML = "";

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

