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
    HashMap<String, String> audioResults = new HashMap<String, String>();

    // Set the content type of the response.
    response.setContentType("application/json");

    try {
      // Attempt to get the transcription of a video and confidence level of transcription. 
      HashMap<String, String> audioResultsTemp = Transcribe.transcribeAudio();

      if (audioResultsTemp.containsKey("transcription")) {
        // If VI API was successful and returned a transcription
        String transcription = audioResultsTemp.get("transcription");
        try {
          // Instantiate and build the NewPerspectiveAPIBuilder which gets the point (the client endpoint)
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
          audioResults.put("transcription", transcription);
          audioResults.put("confidence", audioResultsTemp.get("confidence"));
        } catch (InterruptedException e) {
          audioResults.put("error", "Perspective");
        } catch (ExecutionException e) { 
          audioResults.put("error", "Perspective");
        }
      } else if (audioResultsTemp.containsKey("error")) {
        // VI API was not successful and returned an error
        audioResults.put("error", audioResultsTemp.get("error"));
      } else {
        // VI API did not return an error or a transcription for some unforeseen reason.
        audioResults.put("error", "unforseen");
      }
    } catch (DeadlineExceededException e) {
      // GAE abruptly broke out of the try method because the request timed out.
      audioResults.put("error", "timeout");
    }

    // If for some unforseen reason, 
    if (audioResults.isEmpty()) {
      audioResults.put("error", "unforseen");
    }

    // Return the audio's effect (or errors) as JSON string. 
    // If no transcription was generated in VI API, audioResults will contain a key-value error-error message pair.
    String audioEffectJson = convertToJsonUsingGson(audioResults);
    response.getWriter().println(audioEffectJson);
  }

  /** 
   * Converts audio effect HashMap to JSON string using GSON library.
   */
  private String convertToJsonUsingGson(HashMap<String, String> audioEffect) {
    Gson gson = new Gson();
    String json = gson.toJson(audioEffect);
    return json;
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