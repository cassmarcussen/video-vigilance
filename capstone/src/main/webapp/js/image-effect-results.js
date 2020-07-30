var slideIndex = 1;
sharedArrayOfKeyframeImages = [];

window.onload = function() {
  sharedArrayOfKeyframeImages = [];
  fetchBlobstoreKeyframeImages(false);
};

function displayFlaggedImages() {
  document.getElementById("display-all-images").style.color = "black";
  document.getElementById("display-flagged-images").style.color = "#4285f4";
  document.getElementById("display-all-images").style.fontWeight = "normal";
  document.getElementById("display-flagged-images").style.fontWeight = "bold";

  clearDisplayOfDots();
  setupUnloadedDisplayOnButtonClick();
  var shouldDisplayOnlyFlaggedImages = true;

  // If we haven't gotten the keyframe images array yet
  if (sharedArrayOfKeyframeImages.length == 0) {
    fetchBlobstoreKeyframeImages(shouldDisplayOnlyFlaggedImages);
  } else {
    // Just do the display part
    createHtmlDisplay(sharedArrayOfKeyframeImages, shouldDisplayOnlyFlaggedImages);
  }

}

function displayAllImages() {
  document.getElementById("display-all-images").style.color = "#4285f4";
  document.getElementById("display-flagged-images").style.color = "black";
  document.getElementById("display-all-images").style.fontWeight = "bold";
  document.getElementById("display-flagged-images").style.fontWeight = "normal";

  clearDisplayOfDots();
  setupUnloadedDisplayOnButtonClick();
  var shouldDisplayOnlyFlaggedImages = false;

  // If we haven't gotten the keyframe images array yet
  if (sharedArrayOfKeyframeImages.length == 0) {
    fetchBlobstoreKeyframeImages(shouldDisplayOnlyFlaggedImages);
  } else {
    // Just do the display part
    createHtmlDisplay(sharedArrayOfKeyframeImages, shouldDisplayOnlyFlaggedImages);
  }
}

/**
 * Readable versions of each effect to display on the Results page
 * @enum {String}
 */
const ReadableEffects = {
  UNKNOWN: "Unknown",
  VERY_UNLIKELY: "Very Unlikely",
  UNLIKELY: "Unlikely", 
  POSSIBLE: "Possible",
  LIKELY: "Likely",
  VERY_LIKELY: "Very Likely"
};

function htmlForEffect(effectForACategory, effectsAsNumbers, categoryName) {
  var htmlForEffect = '<label for="adult">' + categoryName + ': ';
  htmlForEffect += ReadableEffects[effectForACategory];
  htmlForEffect += '<div class="tooltip-info"><i class="fa fa-info-circle" aria-hidden="true"></i><span class="tooltiptext-info">'+ getInformationAboutEffect(categoryName) + '</span></div>' ;
  htmlForEffect += '</label><meter id="adult" value="' + effectsAsNumbers.get(categoryName.toLowerCase()) + '"  min="0" low="3" high="4" optimum="6" max="5"></meter>';
  return htmlForEffect;
}

function calculateOverallVisualEffect(sumOfCategoryWeights, arrayOfKeyframeImagesLength) {
  var xVariable = sumOfCategoryWeights + (arrayOfKeyframeImagesLength / 200);
  var xScalingParameter = 200 / arrayOfKeyframeImagesLength;
  const yScalingParameter = 10;
  return Math.round(yScalingParameter * Math.log2((xVariable) * xScalingParameter));
}

/* Displays an overall visual score for the video advertisement, based on the algorithm described in 
the document: https://docs.google.com/document/d/1o-ZbfJRUGNjWO-pmYmsIbPomY4Wvcmu9avrIa3R0pTc/edit
*/
function displayOverallVisualScore(arrayOfKeyframeImages) {

  var sumOfCategoryWeights = 0;

  for (var i=0; i < arrayOfKeyframeImages.length; i++) {

    var thisImage = arrayOfKeyframeImages[i];

    //actually, is effect for now? and safeSearchEffect when we get the changes from cem-optimize-java-servlet-calls?
    // var imageEffect = thisImage.safeSearchEffect;
    //var imageEffect = thisImage.effect;
    var imageEffect = JSON.parse(thisImage.effect);

    var effectsAsNumbers = setEffectsAsNumbers(imageEffect);

    // 5 because we have 5 categories per image
    keys = ['adult', 'racy', 'medical', 'spoofed', 'violence'];
    for (var j=0; j < keys.length; j++) {

      var nextKey = keys[j];

      var nextValue = effectsAsNumbers.get(nextKey);
      
      if(nextValue == 3) {
        sumOfCategoryWeights += 0.5;
      } else if(nextValue == 4) {
        sumOfCategoryWeights += 0.75;
      } else if (nextValue == 5) {
        sumOfCategoryWeights += 1;
      }
    }

  }

  var overallVisualNegativityScore = 0;
  if (arrayOfKeyframeImages.length > 0) {
    overallVisualNegativityScore = calculateOverallVisualEffect(sumOfCategoryWeights, arrayOfKeyframeImages.length);
  } else {
    overallVisualNegativityScore = "unknown";
  }


  document.getElementById("visual-score-overall").innerHTML = overallVisualNegativityScore + "%";

}

/**
 * Numbers corresponding to each likelihood for an effect
 * @enum {int}
 */
const NumberOfEffectParameter = {
  UNKNOWN: 0,
  VERY_UNLIKELY: 1,
  UNLIKELY: 2, 
  POSSIBLE: 3,
  LIKELY: 4,
  VERY_LIKELY: 5
};

/* getNumberOfEffectParameter returns a number corresponding to the effect likelihood of 
the keyframe image. This is used for the html meter which visually displays the likelihood 
of each SafeSearch parameter on the page. The numbers returned are used to fill in the meter by 
a certain amount (the number returned divided by 5).
*/
function getNumberOfEffectParameter(effectParameter) {

  var numberOfEffect = 0;

  numberOfEffect = NumberOfEffectParameter[effectParameter];

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

  effectsAsNumbers.set('adult', getNumberOfEffectParameter(effect.adult));
  effectsAsNumbers.set('medical', getNumberOfEffectParameter(effect.medical));
  effectsAsNumbers.set('spoofed', getNumberOfEffectParameter(effect.spoofed));
  effectsAsNumbers.set('violence', getNumberOfEffectParameter(effect.violence));
  effectsAsNumbers.set('racy', getNumberOfEffectParameter(effect.racy));

  return effectsAsNumbers;
}

/* createKeyframeImageTextInnerHTML creates the html that displays the information about the keyframe image 
that is displayed on the card shown to the user
*/
function createKeyframeImageTextInnerHTML(thisImage, timestampDisplayer) {
  // timestamp - the value is a number of seconds, so we need to convert this to a readable format, i.e. [number of minutes]:[number of seconds]
  var timestamp = getReadableTimeFormat(thisImage.timestamp);

  var effect = thisImage.safeSearchEffect;

  var effectsAsNumbers = setEffectsAsNumbers(effect);

  var htmlForAdultEffect = htmlForEffect(effect.adult, effectsAsNumbers, "Adult");
  var htmlForMedicalEffect =  htmlForEffect(effect.medical, effectsAsNumbers, "Medical");
  var htmlForSpoofedEffect = htmlForEffect(effect.spoofed, effectsAsNumbers, "Spoofed");
  var htmlForViolenceEffect = htmlForEffect(effect.violence, effectsAsNumbers, "Violence");
  var htmlForRacyEffect = htmlForEffect(effect.racy, effectsAsNumbers, "Racy");

  timestampDisplayer.innerText = "Timestamp: " + timestamp;
  var keyframeImageTextInnerHTML = '<h2 class="card-title">Effect of the frame </h2>' 
  + '<div class="card-text">'
  + '<p>Likeliness values are: Unknown, Very Unlikely, Unlikely, Possible, Likely, and Very Likely</p>'
  + htmlForAdultEffect + htmlForMedicalEffect + htmlForSpoofedEffect + htmlForViolenceEffect + htmlForRacyEffect;
  + '</div>'

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

  var keyframeImageDiv = document.createElement("div"); 
  keyframeImageDiv.classList.add("mySlides");
  keyframeImageDiv.classList.add("keyframe-card-fade");
  keyframeImageDiv.classList.add("card-horizontal");

  var keyframeImageWrapper = document.createElement("div");
  keyframeImageWrapper.classList.add("img-square-wrapper");
  var timestampDisplayer = document.createElement("div");
  timestampDisplayer.id = "timestamp";
  keyframeImageWrapper.append(timestampDisplayer);
  var keyframeImage = document.createElement("img");
  keyframeImage.src = thisImage.cloudBucketUrl.replace("gs://", "https://storage.cloud.google.com/");
  keyframeImageWrapper.appendChild(keyframeImage);

  keyframeImageDiv.appendChild(keyframeImage);

  keyframeImageDiv.appendChild(keyframeImageWrapper);

  var imageCaptionDiv = document.createElement("div");
  // card-body is defined by bootstrap. To add properties, add a keyframe-card-body class too (specified in our CSS file)
  imageCaptionDiv.classList.add("card-body");
  imageCaptionDiv.classList.add("keyframe-card-body");

  var effect = thisImage.safeSearchEffect;

  var effectsAsNumbers = setEffectsAsNumbers(effect);
    
  // Don't display the image if it has no 4 or 5 (likely or very unlikely sensitive content), 
  // i.e. only show the image if one of the effect parameters is 'likely' or 'very likely', and potentially 'possible'.
  if (Array.from(effectsAsNumbers.values()).includes(4) || Array.from(effectsAsNumbers.values()).includes(5)) {
    imageIsFlagged = true;
  }
  
  if(!imageIsFlagged && shouldDisplayOnlyFlaggedImages) {

  }else {
    var keyframeImageText = document.createElement("p");
    keyframeImageText.innerHTML = createKeyframeImageTextInnerHTML(thisImage, timestampDisplayer);

    imageCaptionDiv.appendChild(keyframeImageText);

    keyframeImageDiv.appendChild(imageCaptionDiv);

    keyframeImagesContainer.append(keyframeImageDiv);

    setDisplayOfDots(index, keyframeImageDiv, numberOfKeyframeImages);
  }

  return imageIsFlagged;
}

function setupUnloadedDisplayOnButtonClick() {
  // After first image created, then add in the arrows < > to get from one image to the next
  document.getElementsByClassName('prev')[0].style.display = "none";
  document.getElementsByClassName('next')[0].style.display = "none";

  // Show the loader
  document.getElementById('keyframeimage-loader').style.display = "block";

  document.getElementById('results-img').style.display = "none";
}

function setupLoadedDisplay() {
    // After first image created, then add in the arrows < > to get from one image to the next
    document.getElementsByClassName('prev')[0].style.display = "block";
    document.getElementsByClassName('next')[0].style.display = "block";
    // Hide the loader
    document.getElementById('keyframeimage-loader').style.display = "none";
    document.getElementById("keyframe-display-allorflagged-buttons").style.display = "block";

    document.getElementById('results-img').style.display = "flex";
}

function setupUnloadedDisplayOnButtonClick() {
    // After first image created, then add in the arrows < > to get from one image to the next
    document.getElementsByClassName('prev')[0].style.display = "none";
    document.getElementsByClassName('next')[0].style.display = "none";

    document.getElementById('results-img').style.display = "none";
    /* Show the loader */
    document.getElementById('keyframeimage-loader').style.display = "block";
}

function setupLoadedDisplay() {
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
    document.getElementById('results-img').style.width = "650px";
  } else {
    document.getElementById('results-img').style.width = "1250px";
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

/* fetchBobstoreKeyframeImages calls the GET method of the KeyframeImageUploadServlet to get the 
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

      return arrayOfKeyframeImages;
        
    })
    .then((arrayOfKeyframeImages) => {
      createHtmlDisplay(arrayOfKeyframeImages, shouldDisplayOnlyFlaggedImages);
    });   
}

function deleteEntries() {

  // Need to get the datastore list name from the POST... or store that somewhere... (if unique list name for each user)

  const responseDeletePromise = fetch('/keyframe-image-delete', { method: 'POST'});

  /* location.reload() is a predefined JS function for a predefined JS class named location. */
  location.reload();
}


/* Based on https://www.w3schools.com/howto/howto_js_slideshow.asp */

// Next/previous controls
function plusSlides(numberOfIndicesToIncrementBy) {
  showSlides(slideIndex += numberOfIndicesToIncrementBy);
}

// Thumbnail image controls
function currentSlide(indexNumberToDisplay) {
  showSlides(slideIndex = indexNumberToDisplay);
}

function showSlides(indexNumberToDisplay) {
  var slides = document.getElementsByClassName("mySlides");
  var dots = document.getElementsByClassName("dot");

  //Clear before showing, in case one of the tabs for showing/hiding non-flagged images is clicked
  dots.innerHTML = "";

  if (indexNumberToDisplay > slides.length) {slideIndex = 1}
  if (indexNumberToDisplay < 1) {slideIndex = slides.length}
  for (var index = 0; index < slides.length; index++) {
      slides[index].style.display = "none";
  }

  for (var index = 0; index < dots.length; index++) {
      dots[index].className = dots[index].className.replace(" active", "");
  }

  slides[slideIndex-1].style.display = "flex";

  dots[slideIndex-1].className += " active";
}

