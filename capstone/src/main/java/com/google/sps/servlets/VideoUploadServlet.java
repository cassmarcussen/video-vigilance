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
import com.google.sps.data.VideoUpload;

import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.Map;

/** Servlet that uploads a video to Cloud bucket and stores the url in Datastore */

@WebServlet("/video-upload")
public class VideoUploadServlet extends HttpServlet {
  // Contains methods called by GET and POST
  VideoUpload videoUpload = new VideoUpload();

  // Gets the url to the Cloud bucket containing the uploaded video
  // Returns a json object with an "error" and "url" field
  // "error" will contain a message if there are no videos currently uploaded
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

    String json = videoUpload.getUrl(datastore);

    response.setContentType("application/json;");
    response.getWriter().println(json);
  }

  // Posts a video's url and timestamp to Datastore
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    
    String url = getUploadedFileUrl(request, "video-file");
    videoUpload.postUrl(datastore, url);
    
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