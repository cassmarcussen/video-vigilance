<%-- The Java code in this JSP file runs on the server when the user navigates
     to the homepage. This allows us to insert the Blobstore upload URL into the
     form without building the HTML using print statements in a servlet. --%>
<%@ page import="com.google.appengine.api.blobstore.BlobstoreService" %>
<%@ page import="com.google.appengine.api.blobstore.BlobstoreServiceFactory" %>
<%@ page import="com.google.appengine.api.blobstore.UploadOptions" %>
<% BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
    String videoBucketName = "video-vigilance-videos";
    String videoUploadServer = "/video-upload";
    UploadOptions videoUploadOptions = UploadOptions.Builder.withGoogleStorageBucketName(videoBucketName); 
    String videoUploadUrl = blobstoreService.createUploadUrl(videoUploadServer, videoUploadOptions);

    String imageBucketName = "keyframe-images";
    String imageUploadServer = "/keyframe-image-upload";
    UploadOptions imageUploadOptions = UploadOptions.Builder.withGoogleStorageBucketName(imageBucketName); 
    String imageUploadUrl = blobstoreService.createUploadUrl(imageUploadServer, imageUploadOptions);  %>

<!DOCTYPE html>
<html lang="en">
  <head>
    <!-- Meta -->
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0, minimum-scale=1.0, shrink-to-fit=no">
    <meta http-equiv="x-ua-compatible" content="IE=edge">
    <!-- CSS -->
    <link rel="shortcut icon" href="images/bluevv.png" type="image/png">
    <link rel="icon" href="images/bluevv.png" type="image/png">
    <link rel="stylesheet" href="https://fonts.googleapis.com/css?family=Montserrat">
    <script data-search-pseudo-elements="" defer="" src="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/5.11.2/js/all.min.js" crossorigin="anonymous"></script>
    <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/4.0.0/css/bootstrap.min.css" integrity="sha384-Gn5384xqQ1aoWXA+058RXPxPg6fy4IWvTNh0E263XmFcJlSAwiGgFAW/dAiS6JXm" crossorigin="anonymous">
    <link rel="stylesheet" href="css/style.css">
    <link rel="stylesheet" href="css/upload.css">
    <title>Video Vigilance</title>
  </head>
  <body>
    <!-- Navbar -->
    <nav class="navbar navbar-default navbar-expand-md navbar-dark fixed-top shadow">
      <div class="container">
        <!-- Brand Logo that acts as link -->
        <a class="navbar-brand" href="index.html">
          <img src="images/bluevv.png" width="30" height="30" class="d-inline-block-align-top" alt="">
          Video Vigilance
        </a>
        <!-- Toggler/Collapsible Button -->
        <button class="navbar-toggler" type="button" data-toggle="collapse" data-target="#navbarContent" aria-controls="navbarContent" aria-expanded="false" aria-label="Toggle Navigation">
          <i class="fas fa-bars"></i>
        </button>
        <!-- Navbar links -->
        <div class="collapse navbar-collapse" id="navbarContent">
          <ul class="navbar-nav ml-auto">
            <li class="nav-item">
              <a class="nav-link" href="index.html#overview">Overview</a>
            </li>
            <li class="nav-item">
              <a class="nav-link" href="index.html#team">Team</a>
            </li>
          </ul>
        </div>
      </div>
    </nav>

    <!-- Main Content -->
    <div id="content">
      <h1>Upload your Video</h1>
      <br>
      <form id="upload-video" method="POST" enctype="multipart/form-data" action="<%= videoUploadUrl %>">
        <input type="file" id="video-file" name="video-file" accept="video/*"><br>
        <button id="video-form-button">Submit</button>
      </form>
      <p id="loading"></p>
      <video id="video" src="" type="video/*" controls></video><br>
      <button onclick="firstFrame()">Capture Frames</button>
      <button onclick="captureCurrentFrame()">Capture Current Frame</button>
      <button onclick="showVideo()">Show Video</button>
      <button onclick="hideVideo()">Hide Video</button>
      <ol id="frames-list"></ol>

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

  <!-- JQuery library, Popper JS, and Bootstrap JS -->
    <script src="https://code.jquery.com/jquery-3.2.1.slim.min.js" integrity="sha384-KJ3o2DKtIkvYIK3UENzmM7KCkRr/rE9/Qpg6aAZGJwFDMVNA/GpGFF93hXpG5KkN" crossorigin="anonymous"></script>
    <script src="https://cdnjs.cloudflare.com/ajax/libs/popper.js/1.12.9/umd/popper.min.js" integrity="sha384-ApNbgh9B+Y1QKtv3Rn7W3mgPxhU9K/ScQsAP7hUibX39j7fakFPskvXusvfa0b4Q" crossorigin="anonymous"></script>
    <script src="https://maxcdn.bootstrapcdn.com/bootstrap/4.0.0/js/bootstrap.min.js" integrity="sha384-JZR6Spejh4U02d8jOt6vLEHfe/JQGiRRSQQxSfFWpi1MquVdAyjUar5+76PVCmYl" crossorigin="anonymous"></script>
    <script src="https://ajax.googleapis.com/ajax/libs/jquery/3.5.1/jquery.min.js"></script> 
    <script src="js/extract-images-script.js"></script>
  </body>
</html>
