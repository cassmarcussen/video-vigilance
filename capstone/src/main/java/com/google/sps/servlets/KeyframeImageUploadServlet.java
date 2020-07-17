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
(and their corresponding information such as timestamp and the start and end times of their shot in the video)
to DataStore and a corresponding Google Cloud Storage Bucket.
*/
@WebServlet("/keyframe-image-upload")
public class KeyframeImageUploadServlet extends HttpServlet {

 /* 
 The GET method is used to get each entity from the DataStore database. The url of the entity returned is given a "gs:/" at the beginning 
 to make it a viable Google Cloud Storage Bucket url, which is necessary for using the Vision API.
 */
 @Override
 public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

    // queryType defines the DataStore list that we should reference to access the keyframe images stored
    final String queryType = "KeyframeImages_Video";
   // final String queryType = getDatastoreListName();
    Query query = new Query(queryType);

    // We sort in ASCENDING so that the timestamps are sorted from earliest to latest. This ensures that keyframe
    // images which are earlier in the video ad are shown in the slideshow of images on the Results page earlier.
    query.addSort("timestamp",
                     Query.SortDirection.ASCENDING);

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    PreparedQuery results = datastore.prepare(query);

    ArrayList<KeyframeImage> keyframeImagesFromVideo = new ArrayList<KeyframeImage>();

    for (Entity entity : results.asIterable()) {

      String urlForGCS = (String) entity.getProperty("url");

      final String defaultPathForGCS = "gs:/";
      String url = defaultPathForGCS + urlForGCS;

      String timestamp = (String) entity.getProperty("timestamp");
      String startTime = (String) entity.getProperty("startTime");
      String endTime = (String) entity.getProperty("endTime");

      KeyframeImage img = new KeyframeImage(url, Integer.parseInt(timestamp), Integer.parseInt(startTime), Integer.parseInt(endTime));

      keyframeImagesFromVideo.add(img);

    }

    Gson gson = new Gson();
    response.setContentType("application/json;");
    response.getWriter().println(gson.toJson(keyframeImagesFromVideo));
 
  }
  
  /*
  The POST method is used to post a keyframe image, and its corresponding properties regarding timestamp, 
  and start and end time of its shot in the video, to DataStore.
  */
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

    // Get the Google Cloud Storage Bucket URL of the image that the user uploaded to Blobstore.
    String imageUrl = getUploadedFileUrl(request, "image");

    // pass in as string or int?
    String timestamp = request.getParameter("timestamp");
    // Get the startTime
    String startTime = request.getParameter("startTime");
    // Get the endTime
    String endTime = request.getParameter("endTime");

    //Check for null, do not do post request if null url
    if (imageUrl == null || imageUrl.contains("undefined")) {
        response.sendRedirect("js/keyframeImageUpload.jsp");
        return;
    }

    Entity entity = new Entity("KeyframeImages_Video");

    // Generate a random unique string per user for the DataStore list name, 
    // if this has not been done already in the flow.
    /*if (getDatastoreListName().length() == 0) {
        RandomStringGenerator generator = new RandomStringGenerator.Builder()
        .withinRange('a', 'z')
        .filteredBy(CharacterPredicates.DIGITS, CharacterPredicates.LETTERS)
        .build();

        // Generate a random alphanumeric string with 10 to 20 characters
        String randomDatastoreListName = generator.generate(10, 20);

        setDatastoreListName(randomDatastoreListName);
    }*/

   // Entity entity = new Entity(getDatastoreListName());
    entity.setProperty("url", imageUrl);
    entity.setProperty("timestamp", timestamp);
    entity.setProperty("startTime", startTime);
    entity.setProperty("endTime", endTime);
    entity.setProperty("effect", "");

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    datastore.put(entity);

    response.sendRedirect("/results.html");
  
  }

  /** Returns a Google Cloud Storage Bucket URL that points to the uploaded file, or null if the user didn't upload a file. */
  private String getUploadedFileUrl(HttpServletRequest request, String formInputElementName) {
    BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();

    // The String keys are the upload form "name" field from the jsp upload form. 
    //The List<BlobKey> values are the BlobKeys for any files that were uploaded
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

  private String getDatastoreListName() {
    return dataStoreListName;
  }

  //NT reset on delete too
  private String dataStoreListName = "";

}