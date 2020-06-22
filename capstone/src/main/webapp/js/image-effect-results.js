window.onload = function() {
    //alert("hello");
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

        for (var i=0; i < arrayOfKeyframeImages.length; i++){

            getImageEffect(arrayOfKeyframeImages[i]);

        }

    });
    
}

function getImageEffect(keyframeImage) {

}
