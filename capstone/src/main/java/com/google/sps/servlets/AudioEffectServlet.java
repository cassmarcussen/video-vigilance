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
 
import com.google.apphosting.api.DeadlineExceededException;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.gson.Gson;
import com.google.sps.data.Transcribe;
import com.google.sps.perspective.attributes.Attribute;
import com.google.sps.perspective.PerspectiveAPI;
import com.google.sps.perspective.PerspectiveAPIBuilder;
import com.google.sps.perspective.response.AnalyzeCommentResponse;
import java.io.*;
import java.text.DecimalFormat; 
import java.util.concurrent.ExecutionException;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
 
/** Servlet that returns the effect of a video's audio. */
@WebServlet("/audio-effect")
public class AudioEffectServlet extends HttpServlet {
 
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    // Set the content type of the response.
    response.setContentType("application/json");
    
    // Create a HashMap to contain the results to be returned to our JS. 
    HashMap<String, String> results = new HashMap<String, String>();

    // Get the gcsUri of the video to be analyzed.
    String gcsUri = request.getParameter("url");
 
    // Try to get the url of a video, generate a transcription for it, and analyze the transcription
    // under 60 seconds.
    try {
      // Get the transcription of the video and confidence level of transcription. 
      HashMap<String, String> audioResultsTemp = Transcribe.transcribeAudio(gcsUri);
 
      // Get scores for transcription.
      results = checkVIResults(audioResultsTemp);
    } catch (DeadlineExceededException e) {
      // GAE abruptly broke out of the try block because the request timed out (took longer than 60 seconds).
      results.put("error", "timeout");
    }
 
    // If for some unforseen reason, 
    if (results.isEmpty()) {
      results.put("error", "unforseen");
    }
 
    // Return the audio's effect (or error) as JSON string. 
    String audioResults = convertToJsonUsingGson(results);
    response.getWriter().println(audioResults);
  }
 
  /** 
   * Converts audio effect HashMap to JSON string using GSON library.
   */
  private String convertToJsonUsingGson(HashMap<String, String> results) {
    Gson gson = new Gson();
    String json = gson.toJson(results);
    return json;
  }
 
  /**
   * Depending on what is returned by the VI API, decide whether to create AnalyzeComment request for Perspective API
   * or return an error.
   */
  private HashMap<String, String> checkVIResults(HashMap<String, String> audioResultsTemp) {
    if (audioResultsTemp.containsKey("transcription")) {
      // If VI API was successful and response included a transcription key
      String transcription = audioResultsTemp.get("transcription");
      if (transcription.isEmpty()) {
        // If transcription returned an empty string, there is no reason to call Perspective API
        audioResultsTemp.put("error", "emptyTranscription");
      } else {
        String confidence = audioResultsTemp.get("confidence");
        audioResultsTemp = scoreTranscription(transcription, confidence);
      }
    }
    return audioResultsTemp;
  }
 
  /**
   * Make an AnalyzeComment request for a video's transcription using Perspective API.
   */
  private HashMap<String, String> scoreTranscription(String transcription, String confidence) {
    HashMap<String, String> audioResults = new HashMap<String, String>();
    try {
      // Instantiate and build the PerspectiveAPIBuilder which gets the client endpoint
      // through which the AnalyzeComment request will be sent through.
      PerspectiveAPI api = new PerspectiveAPIBuilder()
        .setApiKey("AIzaSyCx72YUXfGl2npdgwyY8ZLXLNAc-vgks7w")
        .setApiVersion("v1alpha1")
        .build();
 
      // Create an AnalyzeComment request for the transcription and store the response.
      ListenableFuture<AnalyzeCommentResponse> future = api.analyze()
        .setComment(transcription)
        .addAttribute(Attribute.ofType(Attribute.TOXICITY))
        .addAttribute(Attribute.ofType(Attribute.INSULT))
        .addAttribute(Attribute.ofType(Attribute.THREAT))
        .addAttribute(Attribute.ofType(Attribute.PROFANITY))
        .addAttribute(Attribute.ofType(Attribute.SEXUALLY_EXPLICIT))
        .addAttribute(Attribute.ofType(Attribute.IDENTITY_ATTACK))
        .setDoNotStore(true)
        .postAsync();
 
      // Get the summary scores for all attributes of the transcription [0, 10].
      AnalyzeCommentResponse commentResponse = future.get();
      audioResults = createAudioEffectResults(commentResponse);
    } catch (InterruptedException e) {
      audioResults.put("error", "Perspective");
    } catch (ExecutionException e) { 
      audioResults.put("error", "Perspective");
    }
    audioResults.put("transcription", transcription);
    audioResults.put("confidence", confidence);
    return audioResults;
  }
 
  /**
   * Create and return a HashMap with the summary scores for all attributes and determine
   * if any of these values are considered "high" and should flag the audio.
   */
  private HashMap<String, String> createAudioEffectResults(AnalyzeCommentResponse commentResponse) {
    HashMap<String, String> audioResults = new HashMap<String, String>();
 
    // Get the summary scores for all attributes [0, 1].
    Map<String, Float> attributeSummaryScores = commentResponse.getAttributeSummaryScores(); 
    for(Map.Entry<String, Float> entry: attributeSummaryScores.entrySet()) {
      audioResults.put(entry.getKey(), transformScores(entry.getValue()));
    }
    
    // Determine if any values should flag the audio.
    audioResults.put("flag", checkValuesForFlagged(audioResults));
    return audioResults;
  }
 
  /**
   * Transform all summary scores into the desired format and return to be added to HashMap.
   * From float values [0, 1] to String representations of values [0, 10].
   * Format all scores to only have two decimal places. Parse float summary scores into string.
   */
  private String transformScores(float score) {
    score = score * 10;
    DecimalFormat df = new DecimalFormat("#.##");
    String scoreString = df.format(score);
    return scoreString;
  }
 
  /**
   * Iterate through the summary score values for each attribute to determine if any raise any flags.
   */
  private String checkValuesForFlagged(HashMap<String, String> audioResults) {
    for (String score: audioResults.values()) {
      if (Float.valueOf(score) >= 5) {
        return "true";
      }
    }
    return "false";
  }
}