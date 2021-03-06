package com.google.sps.servlets;

import com.google.appengine.api.blobstore.BlobInfo;
import com.google.appengine.api.blobstore.BlobInfoFactory;
import com.google.appengine.api.blobstore.BlobKey;  
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.appengine.api.images.ImagesService;
import com.google.appengine.api.images.ImagesServiceFactory;
import com.google.appengine.api.images.ServingUrlOptions;
import com.google.gson.Gson;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.text.CharacterPredicates;
import org.apache.commons.text.RandomStringGenerator;

/* KeyframeImageUploadServlet is a Java Servlet which handles the retrieval and posting of keyframe images 
(and their corresponding information such as timestamp in the video)
to DataStore and a corresponding Google Cloud Storage Bucket.
*/
@WebServlet("/keyframe-image-upload")
public class KeyframeImageUploadServlet extends HttpServlet {

 /* 
 The GET method is used to get each entity from the DataStore database. The url of the entity returned is given a "gs:/" at the beginning 
 to make it a viable Google Cloud Storage Bucket url, which is necessary for using the Vision API. 
 To get the effect of each keyframe image retrieved, the GET method also makes a call to detectSafeSearchGcs, which 
 returns the SafeSearch results from the Cloud Vision API for the keyframe image.
 */
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

    List<KeyframeImage> keyframeImagesFromVideo = getKeyframeImagesFromDataStore("KeyframeImages_Video");

    // Sort by numerical timestamp
    Collections.sort(keyframeImagesFromVideo);

    Gson gson = new Gson();

    response.setContentType("application/json;");
    response.getWriter().println(gson.toJson(keyframeImagesFromVideo));
 
  }
  
  // break up, for testing
  // datastoreListName is a parameter so it can get replaced in testing
  public List<KeyframeImage> getKeyframeImagesFromDataStore(String datastoreListName) {
    Query query = new Query(datastoreListName);

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    PreparedQuery results = datastore.prepare(query);

    ArrayList<KeyframeImage> keyframeImagesFromVideo = new ArrayList<KeyframeImage>();

    // Construct before the for loop for repeated use, for each keyframe image
    DetectSafeSearchGcs detectSafeSearchGcs = new DetectSafeSearchGcs();

    for (Entity entity : results.asIterable()) {

      String urlForGCS = (String) entity.getProperty("url");

      final String defaultPathForGCS = "gs:/";
      String url = defaultPathForGCS + urlForGCS;      

      // Get the SafeSearch results from the Vision API
      HashMap<String, String> effectDetectionResults = new HashMap<String, String>();
      try {
        effectDetectionResults = detectSafeSearchGcs.detectSafeSearchGcs(url);  
      } catch (Exception e) {

      }

      String timestamp = (String) entity.getProperty("timestamp");

      String isManuallySelected = (String) entity.getProperty("isManuallySelected");

      // Check to make sure we have a valid Keyframe Image to create
      if(url != null && url.length() > 0 && timestamp != null && timestamp.length() > 0 && (isManuallySelected.equals("true") || isManuallySelected.equals("false"))) {
        KeyframeImage img = new KeyframeImage(url, Integer.parseInt(timestamp), Boolean.parseBoolean(isManuallySelected), effectDetectionResults);
        keyframeImagesFromVideo.add(img);
      }
    }
    return keyframeImagesFromVideo;
  }
 

  /*
  The POST method is used to post a keyframe image, and its corresponding properties regarding timestamp to DataStore.
  */
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

    // Get the Google Cloud Storage Bucket URL of the image that the user uploaded to Blobstore.
    String imageUrl = getUploadedFileUrl(request, "image");

    // Get the timestamp
    String timestamp = request.getParameter("timestamp");
    // Get the startTime
    String isManuallySelected = request.getParameter("isManuallySelected");

    //Check for null or empty url, do not do post request if null or empty url
    if (imageUrl == null || imageUrl.contains("undefined") || imageUrl.length() == 0) {
        response.sendRedirect("js/keyframeImageUpload.jsp");
        return;
    }

    createAndPostEntity(imageUrl, timestamp, isManuallySelected, "KeyframeImages_Video");

    response.sendRedirect("/results.html");
  
  }

// break up, for testing
// datastoreListName is a parameter so it can get replaced in testing
public void createAndPostEntity(String imageUrl, String timestamp, String isManuallySelected, String datastoreListName) {
  Entity entity = new Entity(datastoreListName);
  entity.setProperty("url", imageUrl);
  entity.setProperty("timestamp", timestamp);
  entity.setProperty("isManuallySelected", isManuallySelected);
  entity.setProperty("effect", "");

  DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
  datastore.put(entity);
}


/** Returns a Google Cloud Storage Bucket URL that points to the uploaded file, or null if the user didn't upload a file. */
private String getUploadedFileUrl(HttpServletRequest request, String formInputElementName) {
BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();

  // The String keys are the upload form "name" field from the jsp upload form. 
  // The List<BlobKey> values are the BlobKeys for any files that were uploaded
  Map<String, List<BlobKey>> blobs = blobstoreService.getUploads(request);

  List<BlobKey> blobKeys = blobs.get("image");

   // User submitted form without selecting a file, so we can't get a URL. (dev server)
  if (blobKeys == null || blobKeys.isEmpty()) {
    return null;
  }

  BlobKey blobKey = blobKeys.get(0);

  BlobInfo info = new BlobInfoFactory().loadBlobInfo(blobKey);
  String gcsName = info.getGsObjectName();

  return gcsName;

}

private void setDatastoreListName(String newName) {
  dataStoreListName = newName;
}

public static String getDatastoreListName() {
  return dataStoreListName;
}

// NT reset on delete too
private static String dataStoreListName = "";

}