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

        //we will have a URL field for the objects (in a data Java file)

        for (var i=0; i < arrayOfKeyframeImages.length; i++){

            getImageEffect(arrayOfKeyframeImages[i]);

        }
    });   
}

function getImageEffect(keyframeImage) {
    fetch('/keyframe-effect-servlet', {
        method: 'GET', 
        image_url: keyframeImage.url
    })
    .then((response) => {
        return response.text();
    }
    );
}
