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
    String uploadUrl = blobstoreService.createUploadUrl(uploadServer, uploadOptions);  %>
<!-- save generated name as a javascript variable for use in javascript files -->
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
    <link rel="shortcut icon" href="images/bluevv.png" type="image/png">
    <link rel="icon" href="images/bluevv.png" type="image/png">
    <link rel="stylesheet" href="https://fonts.googleapis.com/css?family=Montserrat">
    <script data-search-pseudo-elements="" defer="" src="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/5.11.2/js/all.min.js" crossorigin="anonymous"></script>
    <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/4.0.0/css/bootstrap.min.css" integrity="sha384-Gn5384xqQ1aoWXA+058RXPxPg6fy4IWvTNh0E263XmFcJlSAwiGgFAW/dAiS6JXm" crossorigin="anonymous">
    <link rel="stylesheet" href="css/upload.css">
    <title>Video Vigilance</title>
  </head>
  <body>
    <!-- Main Content -->
    <div id="content">
      <h1 class="center">Upload your Video</h1>
      <br><br>
      <form id="upload-video" method="POST" enctype="multipart/form-data" action="<%= uploadUrl %>">
        <input type="file" id="video-file" name="video-file" accept="video/*"><br>
        <label for="shotMethod">Choose a method for extracting your images:</label>
        <select name="shotMethod" id="shotMethod">
          <option value="detect" title="Detect shot changes using the Video Intelligence API and capture 1 frame per shot">Detect shot changes</option>
          <option value="interval" title="Select a fixed time interval for when to capture frames">Use a time interval</option>
          <option value="none" title="Capture frames yourself with the 'Capture Current Frame' button">None</option>
        </select>
        <button id="video-form-button">Submit</button>
      </form>
      <p id="loading"></p>
      <video id="video" src="" type="video/*" controls></video><br>
      <button onclick="captureCurrentFrame()">Capture Current Frame</button>
      <button onclick="showVideo()">Show Video</button>
      <button onclick="hideVideo()">Hide Video</button>
      <button onclick="document.location='results.html'">Show Results</button>
      <ol id="frames-list"></ol>

  <!-- JQuery library, Popper JS, and Bootstrap JS -->
    <script src="https://code.jquery.com/jquery-3.2.1.slim.min.js" integrity="sha384-KJ3o2DKtIkvYIK3UENzmM7KCkRr/rE9/Qpg6aAZGJwFDMVNA/GpGFF93hXpG5KkN" crossorigin="anonymous"></script>
    <script src="https://cdnjs.cloudflare.com/ajax/libs/popper.js/1.12.9/umd/popper.min.js" integrity="sha384-ApNbgh9B+Y1QKtv3Rn7W3mgPxhU9K/ScQsAP7hUibX39j7fakFPskvXusvfa0b4Q" crossorigin="anonymous"></script>
    <script src="https://maxcdn.bootstrapcdn.com/bootstrap/4.0.0/js/bootstrap.min.js" integrity="sha384-JZR6Spejh4U02d8jOt6vLEHfe/JQGiRRSQQxSfFWpi1MquVdAyjUar5+76PVCmYl" crossorigin="anonymous"></script>
    <script src="https://ajax.googleapis.com/ajax/libs/jquery/3.5.1/jquery.min.js"></script> 
    <script src="js/extract-images-script.js"></script>
  </body>
</html>