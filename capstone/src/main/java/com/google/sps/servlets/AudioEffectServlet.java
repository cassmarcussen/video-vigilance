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

import com.google.gson.Gson;
/**
import com.google.sps.perspective.attributes.Attribute;
import com.google.sps.perspective.attributes.AttributeScore;
import com.google.sps.perspective.attributes.ContentType;
import com.google.sps.perspective.attributes.Entry;
import com.google.sps.perspective.request.AnalyzeCommentRequest;
import com.google.sps.perspective.response.AnalyzeCommentResponse;
import com.google.sps.perspective.PerspectiveAPI;
*/
import com.google.apphosting.api.DeadlineExceededException;
import com.google.sps.data.Analyze;
import com.google.sps.perspective.attributes.Attribute;
import com.google.sps.perspective.response.AnalyzeCommentResponse;
import com.google.sps.perspective.PerspectiveAPI;
import com.google.sps.perspective.PerspectiveAPIBuilder;
import com.google.common.util.concurrent.ListenableFuture;
import java.io.*;
import java.util.concurrent.ExecutionException;
import java.util.HashMap;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Servlet that returns the effect of a video's audio. */
@WebServlet("/audio-effect")
public class AudioEffectServlet extends HttpServlet {

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    // Get the url of the video file.
    String gcsUri = request.getParameter("url");
    // Set the content type of the response.
    response.setContentType("application/json");
    // HashMap to return to JS with scores, transcription, and confidence level OR error message.
    HashMap<String, String> audioResults = new HashMap<String, String>();
    
    try {
      // response.getWriter().println("Url being sent to VI API: " + gcsUri);
      HashMap<String, String> audioResultsTemp = Analyze.transcribe(gcsUri);
      // response.getWriter().println("VI API results: " + audioResultsTemp);

      if (audioResultsTemp.containsKey("transcription")) {
        // If VI API was successful and returned a transcription
        String transcription = audioResultsTemp.get("transcription");
        // response.getWriter().println("Transferring VI API results into new HashMap: " + audioResults);
        try {
          // Instantiate and build the PerspectiveAPIBuilder with my API key.
          PerspectiveAPI api = new PerspectiveAPIBuilder()
            .setApiKey("AIzaSyCx72YUXfGl2npdgwyY8ZLXLNAc-vgks7w")
            .build();
      
          // Create an AnalyzeCommentRequest for the transcription and store the response.
          ListenableFuture<AnalyzeCommentResponse> future = api.analyze()
            .setComment(transcription)
            .addAttribute(Attribute.ofType(Attribute.TOXICITY))
            .addAttribute(Attribute.ofType(Attribute.INSULT))
            .addAttribute(Attribute.ofType(Attribute.THREAT))
            .addAttribute(Attribute.ofType(Attribute.PROFANITY))
            .addAttribute(Attribute.ofType(Attribute.SEXUALLY_EXPLICIT))
            .addAttribute(Attribute.ofType(Attribute.IDENTITY_ATTACK))
            .postAsync();

          // Get the summary scores for all attributes of the transcription [0, 10].
          AnalyzeCommentResponse commentResponse = future.get();
          // response.getWriter().println("Perspective API response: " + commentResponse);
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

    // Return the audio's effect (or error) as JSON string. 
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

  private HashMap<String, String> createAudioEffectResults(AnalyzeCommentResponse commentResponse) {
    // Create a HashMap containing the summary scores for all attributes.
    HashMap<String, String> audioResults = new HashMap<String, String>();

    // Get the summary scores for all attributes [0, 1].
    float toxicityScore = commentResponse.getAttributeSummaryScore(Attribute.TOXICITY);
    float insultScore = commentResponse.getAttributeSummaryScore(Attribute.INSULT);
    float threatScore = commentResponse.getAttributeSummaryScore(Attribute.THREAT);
    float profanityScore = commentResponse.getAttributeSummaryScore(Attribute.PROFANITY);
    float adultScore = commentResponse.getAttributeSummaryScore(Attribute.SEXUALLY_EXPLICIT);
    float identityAttackScore = commentResponse.getAttributeSummaryScore(Attribute.IDENTITY_ATTACK);

    // Multiply by 10 to get summary scores as [0, 10] for meter representation.
    toxicityScore = toxicityScore * 10;
    insultScore = insultScore * 10;
    threatScore = threatScore * 10;
    profanityScore = profanityScore * 10;
    adultScore = adultScore * 10;
    identityAttackScore = identityAttackScore * 10;

    // Parse summary scores into string.
    String toxicityScoreString = Float.toString(toxicityScore);
    String insultScoreString = Float.toString(insultScore);
    String threatScoreString = Float.toString(threatScore);
    String profanityScoreString = Float.toString(profanityScore);
    String adultScoreString = Float.toString(adultScore);
    String identityAttackScoreString = Float.toString(identityAttackScore);

    // Add summary scores to HashMap.
    audioResults.put("toxicityScore", toxicityScoreString);
    audioResults.put("insultScore", insultScoreString);
    audioResults.put("threatScore", threatScoreString);
    audioResults.put("profanityScore", profanityScoreString);
    audioResults.put("adultScore", adultScoreString);
    audioResults.put("identityAttackScore", identityAttackScoreString);

    return audioResults;
  }
}