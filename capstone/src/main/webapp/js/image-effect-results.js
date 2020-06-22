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

            //how to return from this and not get promise? chaining maybe bad... fix later
            getImageEffect(arrayOfKeyframeImages[i]).then(effect => {
               document.getElementById("results-img").append("<p>" + effect + "</p>");
            });

            

        }
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
