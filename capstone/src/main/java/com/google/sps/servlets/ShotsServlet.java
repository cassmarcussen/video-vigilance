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
import com.google.cloud.videointelligence.v1.AnnotateVideoProgress;
import com.google.cloud.videointelligence.v1.AnnotateVideoRequest;
import com.google.cloud.videointelligence.v1.AnnotateVideoResponse;
import com.google.cloud.videointelligence.v1.Feature;
import com.google.cloud.videointelligence.v1.VideoAnnotationResults;
import com.google.cloud.videointelligence.v1.VideoIntelligenceServiceClient;
import com.google.cloud.videointelligence.v1.VideoSegment;
import com.google.gson.Gson;
import com.google.sps.data.Shot;

import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList; 

/** Servlet that gets shot changes for uploaded video from Video Intelligence API*/
@WebServlet("/shots")
public class ShotsServlet extends HttpServlet {

  ArrayList<Shot> shots;

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    shots = new ArrayList<Shot>();

    String gcsUri = request.getParameter("url");
    
    // Get detected shot times
    try {
      detectShots(gcsUri);
    } catch (Exception e) {
    //   e.printStackTrace(response.getWriter());
    }

    // Create json String with shots objects
    Gson gson = new Gson();
    String json = gson.toJson(shots);
    response.setContentType("application/json;");
    response.getWriter().println(json);
  }

  // Performs shot analysis on the video at the provided Cloud Storage path
  private void detectShots(String gcsUri) throws Exception {
      
    // Instantiate a com.google.cloud.videointelligence.v1.VideoIntelligenceServiceClient
    try (VideoIntelligenceServiceClient client = VideoIntelligenceServiceClient.create()) {
        
      // Provide path to file hosted on GCS as "gs://bucket-name/..."
      AnnotateVideoRequest request = AnnotateVideoRequest.newBuilder()
          .setInputUri(gcsUri)
          .addFeatures(Feature.SHOT_CHANGE_DETECTION)
          .build();
 
      // Create an operation that will contain the response when the operation completes.
      OperationFuture<AnnotateVideoResponse, AnnotateVideoProgress> response =
          client.annotateVideoAsync(request);

      // Get annotations results for each video sent (we will only be sending 1 video)
      for (VideoAnnotationResults result : response.get().getAnnotationResultsList()) {
        if (result.getShotAnnotationsCount() > 0) {
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
  }
}
