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
import java.util.concurrent.TimeUnit;
import java.util.HashMap;
import java.util.List;

/**
 * One java file that centralizes the methods of Cloud Video Intelligence.
 * Ensures organization and clarity of code. Prevents duplicate code.
 */
public class Analyze {

  /**
   * Set the gcsUri for video file being analyzed.
   * @return a hashmap containing the transcription of the video file and confidence level of transcription
   */
  public static HashMap<String, String> transcribeAudio() {
    try {
      // Hardcoded gcsUri for testing: "gs://cloud-samples-data/video/cat.mp4".
      // Another hardcoded gcsUri for testing: "gs://video-vigilance-videos/youtube_ad_test.mp4"
      // Another hardcoded gcsUri for testing: "gs://video-vigilance-videos/youtube_ad_test_2.mp4"
      String gcsUri = "gs://video-vigilance-videos/youtube_ad_test_2.mp4";
      return transcribeAudio(gcsUri);
    } catch (Exception e) {
      System.out.println("Exception while running:\n" + e.getMessage() + "\n");
      e.printStackTrace(System.out);
      HashMap<String, String> error = new HashMap<>();
      error.put("error", e.getMessage());
      return error;
    }
  }

  /**
   * Transcribe video stored in GCS. 
   * @param gcsUri : the path for the video file stored in GCS being analyzed
   * @return a hashmap containing the transcription of the video file and confidence level of transcription
   */
  public static HashMap<String, String> transcribeAudio(String gcsUri) throws Exception {
    
    // Create HashMap of transcription and confidence of transcription.
    HashMap<String, String> transcription = new HashMap<String, String>();
    String tempTranscript = "";
    Double confidence = 0.0;
    String tempConfidenceString = "";

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
      OperationFuture<AnnotateVideoResponse, AnnotateVideoProgress> response = 
        client.annotateVideoAsync(request);

      // Grab the transcription and confidence level.
      for (VideoAnnotationResults result : response.get(600, TimeUnit.SECONDS).getAnnotationResultsList()) {
        if (result.getSpeechTranscriptionsList().size() == 0) {
          continue;
        }
        for (SpeechTranscription speechTranscription : result.getSpeechTranscriptionsList()) {
          try {
            // Return the transcription and confidence level.
            if (speechTranscription.getAlternativesCount() > 0) {
              // Get the most likely transcription and the confidence level of the transcription.
              SpeechRecognitionAlternative alternative = speechTranscription.getAlternatives(0);
              tempTranscript = tempTranscript + alternative.getTranscript();
              confidence = confidence + alternative.getConfidence();

              /**
              for (WordInfo wordInfo : alternative.getWordsList()) {
                double startTime = wordInfo.getStartTime().getSeconds()
                        + wordInfo.getStartTime().getNanos() / 1e9;
                double endTime = wordInfo.getEndTime().getSeconds()
                        + wordInfo.getEndTime().getNanos() / 1e9;
                System.out.printf("\t%4.2fs - %4.2fs: %s\n",
                        startTime, endTime, wordInfo.getWord());
              }
              */
            } else {
              System.out.println("No transcription found");
            }
          } catch (IndexOutOfBoundsException ioe) {
            System.out.println("Could not retrieve frame: " + ioe.getMessage());
          }
        }
        // Calculate mean confidence level of overall transcription.
        Double tempConfidence = confidence / result.getSpeechTranscriptionsList().size();
        // Multiply by 100 to get confidence level in percentage form. Confidence level is [0, 100].
        tempConfidence = tempConfidence * 100;
        // Parse into String.
        tempConfidenceString = Double.toString(tempConfidence);
      }
      // Add transcript and confidence level to hash map.
      transcription.put("transcription", tempTranscript);
      transcription.put("confidence", tempConfidenceString);
    }
    return transcription;
  }
}