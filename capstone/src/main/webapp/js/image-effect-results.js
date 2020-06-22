window.onload = function() {
    //get request to get the images - get all of the images as arraylist
    fetchBlobstoreKeyframeImages();
};

function fetchBlobstoreKeyframeImages() {

  fetch('/keyframe-image-upload', {method: 'GET'})
    .then((response) => {
        return response.text();
    })
    .then((keyframeImages) => {
    
        var arrayOfKeyframeImages = JSON.parse(keyframeImages);

        var keyframeImagesContainer = document.getElementById("results-img");

        var keyframeImageDiv = document.createElement("div");

        //nothing in here yet
        for (var i=0; i < arrayOfKeyframeImages.length; i++){

            var thisImage = arrayOfKeyframeImages[i];

            var keyframeImage = document.createElement("img");
            keyframeImage.src = thisImage.url;

            if(keyframeImage.src != null && keyframeImage.src.indexOf("undefined") == -1){

                //for one image
                var singularImageDiv = document.createElement("div");

                var timestamp = thisImage.timestamp;
                var startTime = thisImage.startTime;
                var endTime = thisImage.endTime;

                var keyframeImageText = document.createElement("p");
                keyframeImageText.innerHTML = '<p>Timestamp of image: ' + timestamp + '</p>'
                        + '<p>Start time of frame: ' + startTime + '</p>'
                        + '<p>End time of frame: ' + endTime + '</p>';

                singularImageDiv.appendChild(keyframeImage);
                singularImageDiv.appendChild(keyframeImageText);

                var keyframeImageEffect = document.createElement("p");
                getImageEffect(arrayOfKeyframeImages[i]).then(effect => {
                    keyframeImageEffect.innerHTML = "<p>" + effect + "</p>";
                });

                singularImageDiv.appendChild(keyframeImageEffect);
                keyframeImageDiv.appendChild(singularImageDiv);

            }
        }

        keyframeImagesContainer.append(keyframeImageDiv);

    });   
}

function getImageEffect(keyframeImage) {
    var response = fetch('/keyframe-effect-servlet', {
        method: 'GET', 
        image_url: keyframeImage.url
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
