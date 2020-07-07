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
    // Call audio transcription method of Cloud VI API to get speech transcription of video.
    HashMap<String, String> audioResults = Analyze.transcribeAudio();
    String transcription = audioResults.get("transcription");

    // Set the content type of the response.
    response.setContentType("application/json");

    // Get the effect of the transcription using the Perspective API.
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
    
      // Get the summary scores for all attributes of the transcription [0,1].
      AnalyzeCommentResponse commentResponse = future.get();
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

      // Return the audio's effect. 
      String audioEffectJson = convertToJsonUsingGson(audioResults);
      response.getWriter().println(audioEffectJson);
    } catch (ExecutionException e) {

    } catch (InterruptedException ioe) {

    } catch (DeadlineExceededException e) {
      
    }
  }

  /** 
   * Converts audio effect HashMap to JSON string using GSON library.
   */
  private String convertToJsonUsingGson(HashMap<String, String> audioEffect) {
    Gson gson = new Gson();
    String json = gson.toJson(audioEffect);
    return json;
  }
}