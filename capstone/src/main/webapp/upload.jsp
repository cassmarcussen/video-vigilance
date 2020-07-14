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
<html>
<head>
  <meta charset="UTF-8">
  <title>Video Vigilance</title>
  <link rel="stylesheet" href="css/style.css">
  <script src="https://ajax.googleapis.com/ajax/libs/jquery/3.5.1/jquery.min.js"></script> 
  <script src="js/extract-images-script.js"></script>
</head>
  <body>
    <form id="upload-video" method="POST" enctype="multipart/form-data" action="<%= videoUploadUrl %>">
      <input type="file" id="video-file" name="video-file" accept="video/*">
      <button id="video-form-button">Submit</button>
    </form>
    <ul id="shots-list"></ul>
    <p id="loading"></p>
    <video id="video" src="" type="video/*" controls></video>
    <button onclick="showVideo()">Show Video</button>
    <button onclick="hideVideo()">Hide Video</button>
    <ol id="frames-list"></ol>

    <form id="post-keyframe-img" method="POST" enctype="multipart/form-data" action="<%= imageUploadUrl %>">
      <p>Timestamp:</p>
      <textarea id="timestamp" name="timestamp"></textarea>
      <br/>
      <p>Start Time:</p>
      <textarea id="startTime" name="startTime"></textarea>
      <br/>
      <p>End Time:</p>
      <textarea id="endTime" name="endTime"></textarea>
      <br/>
      <p>Upload an image:</p>
      <input type="file" id="image" name="image"></input>
      <br/><br/>
      <button id="image-form-button">Submit</button>
    </form>
  </body>
</html>