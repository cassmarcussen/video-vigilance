<%-- The Java code in this JSP file runs on the server when the user navigates
     to the homepage. This allows us to insert the Blobstore upload URL into the
     form without building the HTML using print statements in a servlet. --%>
<%@ page import="com.google.appengine.api.blobstore.BlobstoreService" %>
<%@ page import="com.google.appengine.api.blobstore.BlobstoreServiceFactory" %>
<% BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
   String uploadUrl = blobstoreService.createUploadUrl("/keyframe-image-upload"); %>

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