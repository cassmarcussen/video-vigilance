<%-- The Java code in this JSP file runs on the server when the user navigates
     to the homepage. This allows us to insert the Blobstore upload URL into the
     form without building the HTML using print statements in a servlet. --%>
<%@ page import="com.google.appengine.api.blobstore.BlobstoreService" %>
<%@ page import="com.google.appengine.api.blobstore.BlobstoreServiceFactory" %>
<%@ page import="com.google.appengine.api.blobstore.UploadOptions" %>
<%@ page import="org.apache.commons.text.RandomStringGenerator" %>
<%@ page import="org.apache.commons.text.RandomStringGenerator.Builder" %>
<%@ page import="org.apache.commons.text.CharacterPredicates" %>
<% BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
    // Generate a random, unique string per user
    RandomStringGenerator generator = new RandomStringGenerator.Builder()
        .withinRange('0', 'z')
        .filteredBy(CharacterPredicates.DIGITS, CharacterPredicates.LETTERS)
        .build();
    // Generate a random alphanumberic string with 10 to 20 characters
    String datastoreName = "Video_" + generator.generate(10, 20);
    String bucketName = "video-vigilance-videos";
    String uploadServer = "/video-upload?name=" + datastoreName;
    UploadOptions uploadOptions = UploadOptions.Builder.withGoogleStorageBucketName(bucketName); 
    String uploadUrl = blobstoreService.createUploadUrl(uploadServer, uploadOptions);  
    
    // Url for images
    String imageBucketName = "keyframe-images";
    String imageUploadServer = "/keyframe-image-upload";
    UploadOptions imageUploadOptions = UploadOptions.Builder.withGoogleStorageBucketName(imageBucketName); 
    String imageUploadUrl = blobstoreService.createUploadUrl(imageUploadServer, imageUploadOptions);  %>
<!-- Save generated name for video as a javascript variable for use in javascript files -->
<script type="text/javascript">
  const datastoreName = "<%= datastoreName %>";
</script>

<!DOCTYPE html>
<html lang="en">
  <head>
    <!-- Meta -->
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0, minimum-scale=1.0, shrink-to-fit=no">
    <meta http-equiv="x-ua-compatible" content="IE=edge">
    <!-- CSS -->
    <!-- <link rel="shortcut icon" href="images/bluevv.png" type="image/png"> -->
    <!-- <link rel="icon" href="images/bluevv.png" type="image/png"> -->
    <link rel="stylesheet" href="https://fonts.googleapis.com/css?family=Montserrat">
    <script data-search-pseudo-elements="" defer="" src="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/5.11.2/js/all.min.js" crossorigin="anonymous"></script>
    <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/4.0.0/css/bootstrap.min.css" integrity="sha384-Gn5384xqQ1aoWXA+058RXPxPg6fy4IWvTNh0E263XmFcJlSAwiGgFAW/dAiS6JXm" crossorigin="anonymous">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/4.7.0/css/font-awesome.min.css">
    <link rel="stylesheet" href="css/upload.css">
    <title>Video Vigilance</title>
  </head>
  <body>
    <div id="content">
      <h1 class="center">Upload your Video</h1>
      <br><br>
      <!-- Upload video form -->
      <form id="upload-video" name="upload-video" method="POST" enctype="multipart/form-data" action="<%= uploadUrl %>">
        <input type="file" id="video-file" name="video-file" accept="video/*">
        <div class="form"> 
          <br>
          <h2><b>Choose a method to capture your images:</b></h2>
          <div id=options>
            <label class="radio-options"><b>Detect shot changes:</b>
              Use the Cloud Video Intelligence API to detect shot changes in your video. The middle image frame in each shot will be captured for analysis.
              <input type="radio" checked="checked" name="shotsOption" value="detectOption">
              <span class="checkmark"></span>
            </label>
            <label class="radio-options"><b>Use a time interval:</b>
              Input a fixed time interval for when to capture image frames.
              <input type="radio" name="shotsOption" value="intervalOption">
              <span class="checkmark"></span>
            </label>
            <label for="timeInterval" id="input-label">Time interval (in seconds):</label>
              <input type="number" id="timeInterval" name="timeInterval" min="1" value="5", step="1">
              <label class="radio-options"><b>Manually capture frames:</b>
              Capture frames yourself after your video uploads to our server.
              <input type="radio" name="shotsOption" value="manualOption" >
              <span class="checkmark"></span>
            </label>
          </div>
        </div>
      </form>
      <button type="submit" form="upload-video" id="video-form-button" class="form" name="video-form-button">Submit</button>
      <!-- Elements for loading statements -->
      <h2 id="loading"></h2>
      <div class="loader" id="loader"></div>
      <br>
      <!-- Initially hidden buttons and video player -->
      <button id="show-hide-video" onclick="hideVideo()">Hide Video</button>
      <button id="camera-button" onclick="captureCurrentFrame()" title="Capture the video's current frame">
        <i class="fa fa-camera" style="font-size:22px"></i>
      </button>
      <button id="results-button" class="right" onclick="document.location='results.html'" title="See your video's analyzed effects">Analyze</button>
      <video id="video" src="" type="video/*" controls></video><br><br>
      <!-- Slideshow and dots container from https://www.w3schools.com/howto/howto_js_slideshow.asp-->
      <div id="slideshow-container">
        <a class="prev" onclick="plusSlides(-1)">&#10094;</a>
        <a class="next" onclick="plusSlides(1)">&#10095;</a>
      </div>
      <div id="dots-container" class="center">
      </div>
      <br><br>
      <div id="transparent-placeholder"></div>
      <br>
      <!-- This form is hidden to the user (Images must be submitted from jsp file) -->
      <form id="post-keyframe-img" method="POST" enctype="multipart/form-data" action="<%= imageUploadUrl %>">
        <p>Timestamp:</p>
        <textarea id="timestamp" name="timestamp"></textarea>
        <p>Start Time:</p>
        <textarea id="startTime" name="startTime"></textarea>
        <p>End Time:</p>
        <textarea id="endTime" name="endTime"></textarea>
        <p>Upload an image:</p>
        <input type="file" id="image" name="image"></input>
        <button id="image-form-button">Submit</button>
      </form>
    </div>
  </body>
  <!-- JS -->
  <script src="https://ajax.googleapis.com/ajax/libs/jquery/3.5.1/jquery.min.js"></script> 
  <script src="js/extract-images-script.js"></script>
  <script src="js/upload-slides-script.js"></script>
  <script src="js/video-file-script.js"></script>
</html>