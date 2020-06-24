var slideIndex = 1;

window.onload = function() {
    fetchBlobstoreKeyframeImages();
};

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

        for (var i=0; i < arrayOfKeyframeImages.length; i++){

            var keyframeImageDiv = document.createElement("div");
            keyframeImageDiv.classList.add("mySlides");
            keyframeImageDiv.classList.add("fade");

            var thisImage = arrayOfKeyframeImages[i];

            var keyframeImage = document.createElement("img");
            keyframeImage.src = thisImage.url;

            if(keyframeImage.src != null && keyframeImage.src.indexOf("undefined") == -1){

                keyframeImageDiv.appendChild(keyframeImage);

                var imageCaptionDiv = document.createElement("div");

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

                var keyframeImageText = document.createElement("p");
                keyframeImageText.innerHTML = '<p>Timestamp of image: ' + timestamp + '</p>'
                        + '<p>Start time of frame: ' + startTime + '</p>'
                        + '<p>End time of frame: ' + endTime + '</p>'
                        + '<p>Effect of frame: </p>' 
                        + '<p><label for="adult">Adult: </label> \
                            <meter id="adult" value="' + effectsAsNumbers.get(effect.adult) + '"  min="0" max="5"></meter> ' + effect.adult + '<br></p>'
                        + '<p><label for="medical">Medical: </label> \
                            <meter id="medical" value="' + effectsAsNumbers.get(effect.medical) + '"  min="0" max="5"></meter> ' + effect.medical + '<br></p>'
                        + '<p><label for="spoofed">Spoofed: </label> \
                            <meter id="spoofed" value="' + effectsAsNumbers.get(effect.spoofed) + '"  min="0" max="5"></meter> ' + effect.spoofed + '<br></p>'
                        + '<p><label for="violence">Violence: </label> \
                            <meter id="violence" value="' + effectsAsNumbers.get(effect.violence) + '"  min="0" max="5"></meter> ' + effect.violence + '<br></p>'
                        + '<p><label for="racy">Racy: </label> \
                            <meter id="racy" value="' + effectsAsNumbers.get(effect.racy) + '"  min="0" max="5"></meter> ' + effect.racy + '<br></p>';

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

    });   
}

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

