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

    <form method="POST" enctype="multipart/form-data" action="<%= uploadUrl %>">
        <p>Timestamp:</p>
        <textarea name="timestamp"></textarea>
        <br/>
        <p>isManuallyFlagged:</p>
        <textarea name="isManuallySelected"></textarea>
        <br/>
        <p>Upload an image:</p>
        <input type="file" name="image">
        <br/><br/>
        <button>Submit</button>
    </form>
        
  </body>
</html>