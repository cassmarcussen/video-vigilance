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
</head>
  <body>

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