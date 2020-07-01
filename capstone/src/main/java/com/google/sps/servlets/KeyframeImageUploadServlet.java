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

@WebServlet("/keyframe-image-upload")
public class KeyframeImageUploadServlet extends HttpServlet {

 @Override
 public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

    Gson gson = new Gson();

    List<KeyframeImage> keyframeImagesFromVideo = new ArrayList<>();

    Query query = new Query("KeyframeImages_Video");
    query.addSort("timestamp",
                     Query.SortDirection.ASCENDING);

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    PreparedQuery results = datastore.prepare(query);

    for (Entity entity : results.asIterable()) {

      String urlForGCS = (String) entity.getProperty("url");

      String url = "gs:/" + urlForGCS;

      String timestamp = (String) entity.getProperty("timestamp");
      long id = entity.getKey().getId();
      String startTime = (String) entity.getProperty("startTime");
      String endTime = (String) entity.getProperty("endTime");

      KeyframeImage img = new KeyframeImage(url, timestamp, startTime, endTime);

      keyframeImagesFromVideo.add(img);

    }

    //sort keyframe images from video by timestamp now, after URL retrieved
   // keyframeImagesFromVideo = sortKeyframeImagesByTimestamp(keyframeImagesFromVideo);
    //Collections.sort(keyframeImagesFromVideo);

    response.setContentType("application/json;");
    response.getWriter().println(gson.toJson(keyframeImagesFromVideo));
 
  }
  
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

    // Get the URL of the image that the user uploaded to Blobstore.
    String imageUrl = getUploadedFileUrl(request, "image");

    // Get the timestamp
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
    entity.setProperty("url", imageUrl);
    entity.setProperty("timestamp", timestamp);
    entity.setProperty("startTime", startTime);
    entity.setProperty("endTime", endTime);
    entity.setProperty("effect", "");

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    datastore.put(entity);

    response.sendRedirect("/results.html");
  
  }

  /** Returns a URL that points to the uploaded file, or null if the user didn't upload a file. */
  private String getUploadedFileUrl(HttpServletRequest request, String formInputElementName) {
    BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
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

}