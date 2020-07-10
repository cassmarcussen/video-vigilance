var slideIndex = 1;

window.onload = function() {
  fetchBlobstoreKeyframeImages();
};

/* getNumberOfEffectParameter returns a number corresponding to the effect likelihood of 
the keyframe image. This is used for the html meter which visually displays the likelihood 
of each SafeSearch parameter on the page. The numbers returned are used to fill in the meter by 
a certain amount (the number returned divided by 5).
*/
function getNumberOfEffectParameter(effectParameter) {

  var numberOfEffect = 0;
    
  if (effectParameter == "UNKNOWN") {
    numberOfEffect = 0;
  } else if (effectParameter == "VERY_UNLIKELY") {
    numberOfEffect = 1;
  } else if (effectParameter == "UNLIKELY") {
    numberOfEffect = 2;
  } else if (effectParameter == "POSSIBLE") {
    numberOfEffect = 3;
  } else if (effectParameter == "LIKELY") {
    numberOfEffect = 4;
  } else if (effectParameter == "VERY_LIKELY") {
    numberOfEffect = 5;
  }

  return numberOfEffect;
}

/* getInformationAboutEffect returns the html for the information about the Effect of the Frame results, 
by each category returned.
*/
function getInformationAboutEffect() {
  var information = "";

  information += "Adult: Represents the adult content likelihood for the image. Adult content may contain elements such as nudity, pornographic images or cartoons, or sexual activities.<br><br>";
  information += "Spoof: The likelihood that an modification was made to the image's canonical version to make it appear funny or offensive.<br><br>";
  information += "Medical: Likelihood that this is a medical image.<br><br>";
  information += "Violence: Likelihood that this image contains violent content.<br><br>";
  information += "Racy: Likelihood that the request image contains racy content. Racy content may include (but is not limited to) skimpy or sheer clothing, strategically covered nudity, lewd or provocative poses, or close-ups of sensitive body areas.";

  return information;
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

      for (var i=0; i < arrayOfKeyframeImages.length; i++){

        var keyframeImageDiv = document.createElement("div");
        keyframeImageDiv.classList.add("mySlides");
        keyframeImageDiv.classList.add("keyframe-card-fade");

        var thisImage = arrayOfKeyframeImages[i];

        var keyframeImage = document.createElement("img");
        keyframeImage.src = thisImage.url.replace("gs://", "https://storage.cloud.google.com/");

        if(keyframeImage.src != null && keyframeImage.src.indexOf("undefined") == -1){

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
          if(!Array.from(effectsAsNumbers.values()).includes(4) && !Array.from(effectsAsNumbers.values()).includes(5)) {
            // continue is commented out temporarily for testing, so that all keyframe images are displayed instead of just those flagged for negative effect
            //continue;
          }else {
            // Else, mark the image as flagged, i.e. increase the number of flagged images by one.
            numberOfFlaggedImages++;
          }

          var keyframeImageText = document.createElement("p");
          keyframeImageText.innerHTML = '<h2>Information about the frame</h2>'
            + '<p>Timestamp of image: ' + timestamp + '</p>'
            + '<p>Start time of frame: ' + startTime + '</p>'
            + '<p>End time of frame: ' + endTime + '</p>'
            + '<hr>'
            + '<h2>Effect of the frame '
            + '<div class="tooltip-info"><i class="fa fa-info-circle" aria-hidden="true"></i><span class="tooltiptext-info">'+ getInformationAboutEffect() + '</span></div></h2>' 
            + '<p><label for="adult">Adult: ' + effect.adult + '</label> \
                <meter id="adult" value="' + effectsAsNumbers.get(effect.adult) + '"  min="0" low="3" high="4" optimum="6" max="5"></meter></p>'
            + '<p><label for="medical">Medical: ' + effect.medical + '</label> \
                <meter id="medical" value="' + effectsAsNumbers.get(effect.medical) + '"  min="0" low="3" high="4" optimum="5" max="5"></meter></p>'
            + '<p><label for="spoofed">Spoofed: ' + effect.spoofed + '</label> \
                <meter id="spoofed" value="' + effectsAsNumbers.get(effect.spoofed) + '"  min="0" low="3" high="4" optimum="5"  max="5"></meter></p>'
            + '<p><label for="violence">Violence: ' + effect.violence + '</label> \
                <meter id="violence" value="' + effectsAsNumbers.get(effect.violence) + '"   min="0" low="3" high="4" optimum="5"  max="5"></meter></p>'
            + '<p><label for="racy">Racy: ' + effect.racy + '</label> \
                <meter id="racy" value="' + effectsAsNumbers.get(effect.racy) + '"   min="0" low="3" high="4" optimum="5"  max="5"></meter></p>'
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

      if (numberOfFlaggedImages > 0) {
        document.getElementById("results-overview").innerHTML = "<h2>Number of flagged images: " + numberOfFlaggedImages + "</h2>";
      } else {
        document.getElementById("results-overview").innerHTML = "<h2>You have no flagged images. </h2>" 
          + "<p>This means that the fields of adult, medical, spoofed, violence, and racy have been determined to be very unlikely, unlikely, possible, or unknown. "
         + "No key frames have been determined to have a status of likely or very likely for any of these fields. Great job!</p>";
      }

    });   
}

/* getImageEffect makes a call to the ImageEffectServlet, which calls the Cloud Vision API's SafeSearch 
method to get the effect of the image.
*/
function getImageEffect(keyframeImage) {

  var response = fetch('/keyframe-effect-servlet?image_url=' + keyframeImage.url, 
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

