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
import java.io.IOException;
 
 
/** Class that detects shot changes in a video */
public class DetectShots {
 
  // Performs shot analysis on the video at the provided Cloud Storage path
  public static void analyzeShots(String gcsUri) throws Exception {
 
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
 
      System.out.println("Waiting for operation to complete...");
      // Print detected shot changes and their location ranges in the analyzed video.
      for (VideoAnnotationResults result : response.get().getAnnotationResultsList()) {
        if (result.getShotAnnotationsCount() > 0) {
          System.out.println("Shots: ");
          for (VideoSegment segment : result.getShotAnnotationsList()) {
            double startTime = segment.getStartTimeOffset().getSeconds()
                + segment.getStartTimeOffset().getNanos() / 1e9;
            double endTime = segment.getEndTimeOffset().getSeconds()
                + segment.getEndTimeOffset().getNanos() / 1e9;
            System.out.printf("Location: %.3f:%.3f\n", startTime, endTime);
          }
        } else {
          System.out.println("No shot changes detected in " + gcsUri);
        }
      }
    }
  }
}
