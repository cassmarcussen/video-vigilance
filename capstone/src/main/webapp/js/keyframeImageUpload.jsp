<%-- The Java code in this JSP file runs on the server when the user navigates
     to the homepage. This allows us to insert the Blobstore upload URL into the
     form without building the HTML using print statements in a servlet. --%>
<%@ page import="com.google.appengine.api.blobstore.BlobstoreService" %>
<%@ page import="com.google.appengine.api.blobstore.BlobstoreServiceFactory" %>
<%@ page import="com.google.appengine.api.blobstore.UploadOptions" %>
<% BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
    String bucketName = "keyframe-images-to-effect";
    String uploadServer = "/keyframe-image-upload";
    UploadOptions uploadOptions = UploadOptions.Builder.withGoogleStorageBucketName(bucketName); 
    String uploadUrl = blobstoreService.createUploadUrl(uploadServer, uploadOptions);  %>


<!DOCTYPE html>
<html>
<head>
  <meta charset="UTF-8">
  <title>Video Vigilance</title>
  <script src="keyframe-image-upload.js"></script>
  <script src="https://ajax.googleapis.com/ajax/libs/jquery/3.5.1/jquery.min.js"></script> 
  <script src="extract-images-script.js"></script>
</head>
  <body>
    <button onclick="getShots()">Detect shots</button>
    <br>
    <ul id="shots-list"></ul>
    <p id="loading"></p>
    <hr>
    <input type="file" id="video-file" name="video-file" accept="video/*">
    <video id="video" src="" type="video/*" controls></video>
    <button onclick="firstFrame()">Show Images</button>
    <button onclick="captureCurrentFrame()">Capture Current Frame</button>
    <button onclick="showVideo()">Show Video</button>
    <button onclick="hideVideo()">Hide Video</button>
    <button id="post-button">Post Images</button>
    <ol id="frames-list"></ol>
    
    <form id="post-keyframe-img" method="POST" enctype="multipart/form-data" action="<%= uploadUrl %>">
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
        <!--input id="image" type="file" name="image"-->
        <input type="file" id="image" name="image"></input>
        <br/><br/>
        <button>Submit</button>
    </form>
        
  </body>
</html>