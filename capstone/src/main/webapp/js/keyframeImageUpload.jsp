<%-- The Java code in this JSP file runs on the server when the user navigates
     to the homepage. This allows us to insert the Blobstore upload URL, which 
     is compatible with uploading to the corresponding Google Cloud Storage Bucket, into the
     form without building the HTML using print statements in a servlet. --%>
<%@ page import="com.google.appengine.api.blobstore.BlobstoreService" %>
<%@ page import="com.google.appengine.api.blobstore.BlobstoreServiceFactory" %>
<%@ page import="com.google.appengine.api.blobstore.UploadOptions" %>
<%@ page import="org.apache.commons.text.CharacterPredicates" %>
<%@ page import="org.apache.commons.text.RandomStringGenerator" %>
<% BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
    // Generate a random, unique string per user
    RandomStringGenerator generator = new RandomStringGenerator.Builder()
            .withinRange('a', 'z')
            .filteredBy(CharacterPredicates.DIGITS, CharacterPredicates.LETTERS)
            .build();
    // Generate a random alphanumberic string with 10 to 20 characters
    String datastoreName = generator.generate(10, 20);
    String bucketName = "keyframe-images-to-effect";
    String uploadServer = "/keyframe-image-upload?datastore-name=" + datastoreName;
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