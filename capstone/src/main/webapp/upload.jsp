<%-- The Java code in this JSP file runs on the server when the user navigates
     to the homepage. This allows us to insert the Blobstore upload URL into the
     form without building the HTML using print statements in a servlet. --%>
<%@ page import="com.google.appengine.api.blobstore.BlobstoreService" %>
<%@ page import="com.google.appengine.api.blobstore.BlobstoreServiceFactory" %>
<%@ page import="com.google.appengine.api.blobstore.UploadOptions" %>
<% BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
    String bucketName = "video-vigilance-videos";
    String uploadServer = "/video-upload";
    UploadOptions uploadOptions = UploadOptions.Builder.withGoogleStorageBucketName(bucketName); 
    String uploadUrl = blobstoreService.createUploadUrl(uploadServer, uploadOptions);  %>
    
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
    <button onclick="getShots()">Detect shots</button>
    <br>
    <ul id="shots-list"></ul>
    <p id="loading"></p>
    <hr>

    <form id="upload-video" method="POST" enctype="multipart/form-data" action="<%= uploadUrl %>">
      <input type="file" id="video-file" name="video-file" accept="video/*">
      <button id="form-button">Submit</button>
    </form>

    <video id="video" src="" type="video/*" controls></video>
    <button onclick="firstFrame()">Show Images</button>
    <button onclick="showVideo()">Show Video</button>
    <button onclick="hideVideo()">Hide Video</button>
    <ol id="frames-list"></ol>
  </body>
</html>