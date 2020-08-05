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

import com.google.api.gax.longrunning.OperationFuture;
import com.google.appengine.api.blobstore.BlobInfo;
import com.google.appengine.api.blobstore.BlobInfoFactory;
import com.google.appengine.api.blobstore.BlobKey;  
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;

import com.google.cloud.videointelligence.v1.AnnotateVideoProgress;
import com.google.cloud.videointelligence.v1.AnnotateVideoRequest;
import com.google.cloud.videointelligence.v1.AnnotateVideoResponse;
import com.google.cloud.videointelligence.v1.Feature;
import com.google.cloud.videointelligence.v1.VideoAnnotationResults;
import com.google.cloud.videointelligence.v1.VideoIntelligenceServiceClient;
import com.google.cloud.videointelligence.v1.VideoSegment;

import com.google.gson.Gson;
import com.google.sps.data.DetectShots;
import com.google.sps.data.Shot;
import com.google.sps.data.VideoUpload;

import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList; 
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/** Servlet that gets shot changes for uploaded video from Video Intelligence API*/
@WebServlet("/shots")
public class ShotsServlet extends HttpServlet {
  // Contains methods called by GET and POST
  VideoUpload videoUpload = new VideoUpload();
  ArrayList<Shot> shots;

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    shots = new ArrayList<Shot>();

    // Get bucket url that has video
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    Map<String, String> urlErrorMap = videoUpload.getUrl(datastore, request.getParameter("name"));

    // If there's no error fetching url, get shots
    if (urlErrorMap.get("error") == "") {
      try {
        detectShots("gs:/" + urlErrorMap.get("url"));
      } catch (Exception e) {
        //   e.printStackTrace(response.getWriter());
      }
    }

    // Create json String with shots objects (may be empty or non empty)
    // Ex output: [{"startTime":0,"endTime":3},{"startTime":3,"endTime":5}]
    Gson gson = new Gson();
    String json = gson.toJson(shots);
    response.setContentType("application/json;");
    response.getWriter().println(json);
  }

  // Posts a video's url and timestamp to Datastore
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    
    String url = getUploadedFileUrl(request, "video-file");
    videoUpload.postUrl(datastore, url, request.getParameter("name"));
    
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

  // Performs shot analysis on the video at the provided Cloud Storage path
  // GET method of ShotsServlet calls this method and catches for Exception
  private void detectShots(String gcsUri) throws Exception {
      
    // Instantiate a com.google.cloud.videointelligence.v1.VideoIntelligenceServiceClient
    try (VideoIntelligenceServiceClient client = VideoIntelligenceServiceClient.create()) {
        
      // Provide path to file hosted on GCS as "gs://bucket-name/..."
      AnnotateVideoRequest request = AnnotateVideoRequest.newBuilder()
          .setInputUri(gcsUri)
          // Request to perform the SHOT_CHANGE_DETECTION video annotation feature of Video Intelligence API 
          .addFeatures(Feature.SHOT_CHANGE_DETECTION)
          .build();
 
      // Create an operation that will contain the response when the operation completes.
      OperationFuture<AnnotateVideoResponse, AnnotateVideoProgress> response =
          client.annotateVideoAsync(request);

      // Get annotations results for each video sent (we will only be sending 1 video)
      if (response.get().getAnnotationResultsList().size() == 0) {
        return;
      }
      VideoAnnotationResults result = response.get().getAnnotationResultsList().get(0);
      // Get shot annotations for video
      for (VideoSegment segment : result.getShotAnnotationsList()) {
        // Add on nanoseconds to total seconds
        double startTime = segment.getStartTimeOffset().getSeconds()
            + segment.getStartTimeOffset().getNanos() / 1e9;
        double endTime = segment.getEndTimeOffset().getSeconds()
            + segment.getEndTimeOffset().getNanos() / 1e9;          
        // Create Shot object and add to shots ArrayList
        Shot newShot = new Shot(startTime, endTime);
        shots.add(newShot);
      }
    }
  }
}
