// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.sps.servlets;

import com.google.appengine.api.blobstore.BlobInfo;
import com.google.appengine.api.blobstore.BlobInfoFactory;
import com.google.appengine.api.blobstore.BlobKey;  
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.FetchOptions.Builder;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;

import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.Map;

/** Servlet that uploads a video to Cloud bucket*/

@WebServlet("/video-upload")
public class VideoUploadServlet extends HttpServlet {

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

    // In case there's more than 1 Video stored, sort them starting from most recent
    Query query = new Query("Video").addSort("timestamp", SortDirection.DESCENDING);
    PreparedQuery results = datastore.prepare(query);

    int numVideos = results.countEntities(FetchOptions.Builder.withDefaults());
    String url = "";
    String error = "";
    String json = "";

    if (numVideos == 0) {
      // If there's no video stored in Datastore, print an error message 
      error = "No videos uploaded to Datastore";
    } else if (numVideos == 1) {
      // Set the url of the only video stored (asSingleEntity() retrieves the one and only result for the Query)
      Entity video = results.asSingleEntity();
      url = (String) video.getProperty("url");
    } else {
      // If there's more than 1 video stored in Datastore, return the url for the most recently added video 
      // Since the results are sorted by timestamp, just use the first one
      Entity video = results.asList(FetchOptions.Builder.withDefaults()).get(0);
      url = (String) video.getProperty("url");
    }
    json = String.format("{\"error\": %s, \"url\": %s}", error, url);
    response.setContentType("application/json;");
    response.getWriter().println(json);
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String url = getUploadedFileUrl(request, "video-file");

    // Do not post if no file was selected
    if (url == null) {
      response.sendRedirect("/upload.jsp");
      return;
    }

    // Create Entity to store in datastore with the url and current timestamp
    Entity entity = new Entity("Video");
    long timestamp = System.currentTimeMillis();
    entity.setProperty("url", url);
    entity.setProperty("timestamp", timestamp);

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    datastore.put(entity);

    response.sendRedirect("/upload.jsp");
  }

  // Returns a Cloud Storage bucket URL that points to the uploaded file, or null if the user didn't upload a file
  private String getUploadedFileUrl(HttpServletRequest request, String formInputElementName) {
    BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
    Map<String, List<BlobKey>> blobs = blobstoreService.getUploads(request);
    List<BlobKey> blobKeys = blobs.get(formInputElementName);

    // User submitted form without selecting a file
    if (blobKeys == null || blobKeys.isEmpty()) {
      return null;
    }

    // Form only contains a single file input
    BlobKey blobKey = blobKeys.get(0);

    // Get bucket URL containing the video
    BlobInfo info = new BlobInfoFactory().loadBlobInfo(blobKey);
    String gcsName = info.getGsObjectName();

    return gcsName; 
  }

} 