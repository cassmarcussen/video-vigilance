var slideIndex = 1;

window.onload = function() {
  fetchBlobstoreKeyframeImages();
};

function htmlForEffect(effectForACategory, effectsAsNumbers, categoryName) {
  var htmlForEffect = '<p><label for="adult">' + categoryName + ': ';
  htmlForEffect += effectForACategory;
  htmlForEffect += '</label><meter id="adult" value="' + effectsAsNumbers.get(effectForACategory) + '"  min="0" low="3" high="4" optimum="6" max="5"></meter></p>';
  return htmlForEffect;
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

  numberOfEffect = NumberOfEffectParameter.effectParameter;

  return numberOfEffect;
}

/* All of the KeyframeImage parameters related to time represent the time in number of seconds. 
getReadableTimeFormat converts this time in seconds to a readable time in the format: [number of minutes]:[number of seconds]
*/
function getReadableTimeFormat(timeInSeconds) {
  // Get number of minutes by dividing by 60 and rounding down
  var minutes = Math.floor(timeInSeconds / 60);

  // Get number of seconds by finding the remainder when dividing by 60, i.e. mod 60
  var seconds = timeInSeconds % 60;

  var readableTimeFormatMinutesAndSeconds = minutes + ":" + seconds;

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

  var htmlForAdultEffect = htmlForEffect(effect.adult, effectsAsNumbers, "Adult");
  var htmlForMedicalEffect =  htmlForEffect(effect.medical, effectsAsNumbers, "Medical");
  var htmlForSpoofedEffect = htmlForEffect(effect.spoofed, effectsAsNumbers, "Spoofed");
  var htmlForViolenceEffect = htmlForEffect(effect.violence, effectsAsNumbers, "Violence");
  var htmlForRacyEffect = htmlForEffect(effect.racy, effectsAsNumbers, "Racy");

  var keyframeImageTextInnerHTML = '<h2>Information about the frame</h2>'
  + '<p>Timestamp of image: ' + timestamp + '</p>' 
  + '<p>Start time of frame: ' + startTime + '</p>'
  + '<p>End time of frame: ' + endTime + '</p>'
  + '<hr>'
  + '<h2>Effect of the frame </h2>' 
  + htmlForAdultEffect + htmlForMedicalEffect + htmlForSpoofedEffect + htmlForViolenceEffect + htmlForRacyEffect
  + '<p>Likeliness values are Unknown, Very Unlikely, Unlikely, Possible, Likely, and Very Likely</p>';

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

/* setDisplayAndHtmlOfDots makes the first image display on the page, and the first dot below the slideshow of images highlighted
*/
function setDisplayAndHtmlOfDots(index) {
  if (index == 0) {
    keyframeImageDiv.style.display = "block";
    document.getElementById("dots").innerHTML += '<span class="dot active" onclick="currentSlide(' + (index + 1) + ')"></span>';
  } else {
    document.getElementById("dots").innerHTML += '<span class="dot" onclick="currentSlide(' + (index + 1) + ')"></span>';
  }
}

/* createSingularKeyframeImageCard sets up the variable HTML code for displaying a singular keyframe image card 
in the slideshow of keyframe images. It sets up the CSS classes, the HTML elements to add, and the effect displayed.
It returns modifiableNumberOfFlaggedImages, a value which is the number of flagged images incremented by 1 if the particular 
keyframe image is flagged.
*/
function createSingularKeyframeImageCard(thisImage, numberOfFlaggedImages) {

  var modifiableNumberOfFlaggedImages = numberOfFlaggedImages;

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
      // continue;
    } else {
      // Else, mark the image as flagged, i.e. increase the number of flagged images by one.
      modifiableNumberOfFlaggedImages++;
    }

    var keyframeImageText = document.createElement("p");
    keyframeImageText.innerHTML = createKeyframeImageTextInnerHTML(thisImage);

    imageCaptionDiv.appendChild(keyframeImageText);

    keyframeImageDiv.appendChild(imageCaptionDiv);

    keyframeImagesContainer.append(keyframeImageDiv);

    setDisplayAndHtmlOfDots();
  }

  return modifiableNumberOfFlaggedImages;
}

/* createKeyframeImageSlideshow creates the slideshow of cards with keyframe images and their corresponding 
information and SafeSearch detected effect. It does so by iterating through the array of keyframe images 
returned from DataStore and calling createSingularKeyframeImageCard for each keyframe image to create a card in the 
slideshow for each flagged keyframe image.
*/
function createKeyframeImageSlideshow(arrayOfKeyframeImages) {

  var numberOfFlaggedImages = 0;
  var keyframeImagesContainer = document.getElementById("results-img"); 

  for (var i=0; i < arrayOfKeyframeImages.length; i++) {

    var thisImage = arrayOfKeyframeImages[i];

    numberOfFlaggedImages = createSingularKeyframeImageCard(thisImage, numberOfFlaggedImages);

  }

  return numberOfFlaggedImages;
}

/* fetchBobstoreKeyframeImages calls the GET method of the KeyframeImageUploadServlet to get the 
keyframe images from DataStore and the Google Cloud Bucket. It then gets the image's effect using 
the Google Cloud Vision API (called from Java), and displays keyframe images that are flagged for 
possible, likely, or very likely sensitive content.
*/
async function fetchBlobstoreKeyframeImages() {

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

      // Number of flagged images:
      var numberOfFlaggedImages = 0;

      numberOfFlaggedImages = createKeyframeImageSlideshow(arrayOfKeyframeImages);

      setFlaggedImageSummaryComment(numberOfFlaggedImages);
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

  if (indexNumberToDisplay > slides.length) {slideIndex = 1}
  if (indexNumberToDisplay < 1) {slideIndex = slides.length}
  for (var index = 0; index < slides.length; index++) {
      slides[index].style.display = "none";
  }

  for (var index = 0; index < dots.length; index++) {
      dots[index].className = dots[index].className.replace(" active", "");
  }

  slides[slideIndex-1].style.display = "block";
  dots[slideIndex-1].className += " active";
}

