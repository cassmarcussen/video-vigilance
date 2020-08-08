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
import com.google.cloud.videointelligence.v1.Entity;
import com.google.cloud.videointelligence.v1.ExplicitContentFrame;
import com.google.cloud.videointelligence.v1.Feature;
import com.google.cloud.videointelligence.v1.LabelAnnotation;
import com.google.cloud.videointelligence.v1.LabelSegment;
import com.google.cloud.videointelligence.v1.SpeechRecognitionAlternative;
import com.google.cloud.videointelligence.v1.SpeechTranscription;
import com.google.cloud.videointelligence.v1.SpeechTranscriptionConfig;
import com.google.cloud.videointelligence.v1.VideoAnnotationResults;
import com.google.cloud.videointelligence.v1.VideoContext;
import com.google.cloud.videointelligence.v1.VideoIntelligenceServiceClient;
import com.google.cloud.videointelligence.v1.VideoSegment;
import com.google.cloud.videointelligence.v1.WordInfo;
import com.google.protobuf.ByteString;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat; 
import java.util.concurrent.TimeUnit;
import java.util.HashMap;
import java.util.List;

/**
 * Using call to Video Intelligence API, generate a transcription for the video.
 */
public class Transcribe {
  
  /**
   * Format the confidence level to the correct range and variable type.
   * @param result : the video results
   * @param tempConfidence : the mean confidence over all segments [0, 1] 
   * @return the confidence level of the transcription as a String representation of a Double [0, 100]
   */
  public String formatConfidence(VideoAnnotationResults result, Double tempConfidence) {
    // Calculate the mean confidence level of the overall transcription over all the segments.
    Double confidence = tempConfidence / result.getSpeechTranscriptionsList().size();
    // Multiply by 100 to get confidence level as [0, 100] for percentage representation.
    confidence = confidence * 100;
    // Format confidence level to only have two decimal places.
    DecimalFormat df = new DecimalFormat("#.##");
    String confidenceString = df.format(confidence);
    return confidenceString;
  }

  /**
   * Transcribe video stored in GCS. 
   * @param gcsUri : the path for the video file stored in GCS to be analyzed
   * @return a hashmap containing the transcription of the video file and confidence level of transcription
   */
  public HashMap<String, String> transcribeAudio(String gcsUri) {
    // Create a HashMap of transcription and confidence of transcription.
    HashMap<String, String> transcriptionResults = new HashMap<String, String>();
    String tempTranscript = "";
    Double tempConfidence = 0.0;
 
    try {
      // Retrieve the first anottation result since only one video was processed.
      List<VideoAnnotationResults> resultsList = getAnnotationResult(gcsUri); 
      VideoAnnotationResults result = resultsList.get(0);
      // Go through each segment of the transcription and append the most confident alternative.
      for (SpeechTranscription speechTranscription : result.getSpeechTranscriptionsList()) {
        try {
          // Gather the transcription and confidence level information.
          if (speechTranscription.getAlternativesCount() > 0) {
            // Get the most likely transcription and the confidence level of the transcription.
            SpeechRecognitionAlternative alternative = speechTranscription.getAlternatives(0);
            tempTranscript = tempTranscript + alternative.getTranscript();
            tempConfidence = tempConfidence + alternative.getConfidence();
          }
        } catch (IndexOutOfBoundsException ioe) {
        }
      }
      // Format results before returning.
      transcriptionResults.put("confidence", formatConfidence(result, tempConfidence));
      transcriptionResults.put("transcription", tempTranscript);
    } catch(Exception e) {
      transcriptionResults.put("error", "VI");
    }
    return transcriptionResults;
  }

  /**
   * Instantiate a call to the Video Intelligence API to process the video and generate a 
   * speech transcription.
   * @param gcsUri : the path for the video file stored in GCS to be analyzed
   */
  protected List<VideoAnnotationResults> getAnnotationResult(String gcsUri) throws Exception {
    // Instantiate Video Intelligence in a try-with-resources statement. This will automatically
    // close the instance of Video Intelligence regardless of whether try statement completes
    // normally or abruptly. 
    try (VideoIntelligenceServiceClient client = VideoIntelligenceServiceClient.create()) {
      // Set the language code to English US.
      SpeechTranscriptionConfig config = SpeechTranscriptionConfig.newBuilder()
        .setLanguageCode("en-US")
        .setEnableAutomaticPunctuation(true)
        .build();

      // Set the video context with the above configuration.
      VideoContext context = VideoContext.newBuilder()
        .setSpeechTranscriptionConfig(config)
        .build();

      // Create the request. 
      AnnotateVideoRequest request = AnnotateVideoRequest.newBuilder()
        .setInputUri(gcsUri)
        .addFeatures(Feature.SPEECH_TRANSCRIPTION)
        .setVideoContext(context)
        .build();
      
      // Asynchronously perform speech transcription on videos. Create an operation that will contain 
      // the response when operation is complete.
      OperationFuture<AnnotateVideoResponse, AnnotateVideoProgress> future = client.annotateVideoAsync(request);

      // Wait for the video to be processed/for above operation to be complete.
      // future.get() will block until the operation is created, which may take over a minute. 
      // This may result in a timeout error from GAE being thrown, which would also throw an ExceutionException/InterruptedException here.
      AnnotateVideoResponse response = future.get(600, TimeUnit.SECONDS);
      
      // Return the first result since only one video was processed.
      return response.getAnnotationResultsList();
    }
  }
}