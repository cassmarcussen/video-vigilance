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
      break;
      numberOfEffect = 2;
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

      var keyframeImagesContainer = document.getElementById("results-img");

      // Number of flagged images:
      var numberOfFlaggedImages = 0;

      for (var i=0; i < arrayOfKeyframeImages.length; i++) {

        var keyframeImageDiv = document.createElement("div");
        keyframeImageDiv.classList.add("mySlides");
        keyframeImageDiv.classList.add("keyframe-card-fade");

        var thisImage = arrayOfKeyframeImages[i];

        var keyframeImage = document.createElement("img");
        keyframeImage.src = thisImage.cloudBucketUrl.replace("gs://", "https://storage.cloud.google.com/");

        // This condition makes sure the the keyframe image retrieved from the database is not undefined, 
        // where undefined images either have a null src or 'undefined' in their source url. This is here 
        // because we do not want to display undefined images (i.e. displaying no image) on the Results page.
        if (keyframeImage.src != null && keyframeImage.src.indexOf("undefined") == -1) {

          keyframeImageDiv.appendChild(keyframeImage);

          var imageCaptionDiv = document.createElement("div");
          imageCaptionDiv.classList.add("container");

          var timestamp = thisImage.timestamp;
          var startTime = thisImage.startTime;
          var endTime = thisImage.endTime;
          var effect = JSON.parse(thisImage.effect);

          var effectsAsNumbers = new Map()
          effectsAsNumbers.set(effect.adult, getNumberOfEffectParameter(effect.adult));
          effectsAsNumbers.set(effect.medical, getNumberOfEffectParameter(effect.medical));
          effectsAsNumbers.set(effect.spoofed, getNumberOfEffectParameter(effect.spoofed));
          effectsAsNumbers.set(effect.violence, getNumberOfEffectParameter(effect.violence));
          effectsAsNumbers.set(effect.racy, getNumberOfEffectParameter(effect.racy));

          // Don't display the image if it has no 4 or 5 (likely or very unlikely sensitive content), 
          // i.e. only show the image if one of the effect parameters is 'likely' or 'very likely', and potentially 'possible'.
          if (!Array.from(effectsAsNumbers.values()).includes(4) && !Array.from(effectsAsNumbers.values()).includes(5)) {
            // continue is commented out temporarily for testing, so that all keyframe images are displayed instead of just those flagged for negative effect
            //continue;
          } else {
            // Else, mark the image as flagged, i.e. increase the number of flagged images by one.
            numberOfFlaggedImages++;
          }

          var keyframeImageText = document.createElement("p");
          keyframeImageText.innerHTML = '<h2>Information about the frame</h2>'
            + '<p>Timestamp of image: ' + timestamp + '</p>'
            + '<p>Start time of frame: ' + startTime + '</p>'
            + '<p>End time of frame: ' + endTime + '</p>'
            + '<hr>'
            + '<h2>Effect of the frame </h2>' 
            + htmlForEffect(effect.adult, effectsAsNumbers, "Adult")
            + htmlForEffect(effect.medical, effectsAsNumbers, "Medical")
            + htmlForEffect(effect.spoofed, effectsAsNumbers, "Spoofed")
            + htmlForEffect(effect.violence, effectsAsNumbers, "Violence")
            + htmlForEffect(effect.racy, effectsAsNumbers, "Racy")
            + '<p>Likeliness values are Unknown, Very Unlikely, Unlikely, Possible, Likely, and Very Likely</p>';

            imageCaptionDiv.appendChild(keyframeImageText);

            keyframeImageDiv.appendChild(imageCaptionDiv);

            keyframeImagesContainer.append(keyframeImageDiv);

            // Make the first image display, and first dot highlighted
            if (i == 0) {
              keyframeImageDiv.style.display = "block";
              document.getElementById("dots").innerHTML += '<span class="dot active" onclick="currentSlide(' + (i + 1) + ')"></span>';
            } else {
              document.getElementById("dots").innerHTML += '<span class="dot" onclick="currentSlide(' + (i + 1) + ')"></span>';
            }
        }

      }

      var flaggedImageText = "<h2>Number of flagged images: " + numberOfFlaggedImages + "</h2>";
      var noFlaggedImageText = "<h2>You have no flagged images. </h2>" 
          + "<p>This means that the fields of adult, medical, spoofed, violence, and racy have been determined to be very unlikely, unlikely, possible, or unknown. "
          + "No key frames have been determined to have a status of likely or very likely for any of these fields. Great job!</p>";

      var comment = numberOfFlaggedImages > 0 ? flaggedImageText : noFlaggedImageText;
      document.getElementById("results-overview").innerHTML = comment;

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

