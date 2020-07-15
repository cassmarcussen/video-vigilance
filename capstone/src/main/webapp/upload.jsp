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
    String datastoreName = "Video" + generator.generate(10, 20);
    String bucketName = "video-vigilance-videos";
    String uploadServer = "/video-upload?name=" + datastoreName;
    UploadOptions uploadOptions = UploadOptions.Builder.withGoogleStorageBucketName(bucketName); 
    String uploadUrl = blobstoreService.createUploadUrl(uploadServer, uploadOptions);  %>
<!-- save generated name as a javascript variable for use in javascript files -->
<script type="text/javascript">
  const datastoreName = "<%= datastoreName %>";
</script>
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
    <form id="upload-video" method="POST" enctype="multipart/form-data" action="<%= uploadUrl %>">
      <input type="file" id="video-file" name="video-file" accept="video/*">
      <button id="form-button">Submit</button>
    </form>
    <ul id="shots-list"></ul>
    <p id="loading"></p>
    <video id="video" src="" type="video/*" controls></video>
    <button onclick="showVideo()">Show Video</button>
    <button onclick="hideVideo()">Hide Video</button>
    <ol id="frames-list"></ol>
  </body>
</html>