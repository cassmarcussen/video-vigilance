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

package com.google.sps.data;

import com.google.api.gax.longrunning.OperationFuture;
import com.google.cloud.videointelligence.v1.AnnotateVideoProgress;
import com.google.cloud.videointelligence.v1.AnnotateVideoRequest;
import com.google.cloud.videointelligence.v1.AnnotateVideoResponse;
import com.google.cloud.videointelligence.v1.Feature;
import com.google.cloud.videointelligence.v1.VideoAnnotationResults;
import com.google.cloud.videointelligence.v1.VideoIntelligenceServiceClient;
import com.google.cloud.videointelligence.v1.VideoSegment;
import com.google.sps.data.Shot;

import java.util.ArrayList; 

/** A final class that contains a function to use Video Intelligence API's shot detection feature */
public class DetectShots {
  
  public ArrayList<Shot> detect(String gcsUri) throws Exception {

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
    
      // Instantiate list to add shots to
      ArrayList<Shot> shots = new ArrayList<Shot>();

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
      return shots;
    }
  }
}