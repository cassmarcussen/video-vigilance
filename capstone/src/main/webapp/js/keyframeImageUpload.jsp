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
    <button onclick="getEffect()">Calculate Effect</button>
    <ol id="frames-list"></ol>
    
    <form method="POST" enctype="multipart/form-data" action="<%= uploadUrl %>">
        <p>Timestamp:</p>
        <textarea name="timestamp"></textarea>
        <br/>
        <p>Start Time:</p>
        <textarea name="startTime"></textarea>
        <br/>
        <p>End Time:</p>
        <textarea name="endTime"></textarea>
        <br/>
        <p>Upload an image:</p>
        <input type="file" name="image">
        <br/><br/>
        <button>Submit</button>
    </form>
        
  </body>
</html>