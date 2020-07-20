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

import com.google.common.util.concurrent.ListenableFuture;
import com.google.gson.Gson;
import com.google.sps.data.Transcribe;
import com.google.sps.perspective.attributes.Attribute;
import com.google.sps.perspective.response.AnalyzeCommentResponse;
import com.google.sps.perspective.PerspectiveAPI;
import com.google.sps.perspective.PerspectiveAPIBuilder;
import java.io.*;
import java.util.concurrent.ExecutionException;
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
    String audioTranscription = Transcribe.transcribeAudio();

    // Set the content type of the response.
    response.setContentType("application/json");

    // Get the effect of the transcription using the PerspectiveGradle Folder - Java Wrapper.
    try {
      // Instantiate and build the PerspectiveAPIBuilder with my API key.
      PerspectiveAPI api = new PerspectiveAPIBuilder()
        .setApiKey("AIzaSyCx72YUXfGl2npdgwyY8ZLXLNAc-vgks7w")
        .build();
      
      // Create an AnalyzeCommentRequest for the transcription and store the response.
      ListenableFuture<AnalyzeCommentResponse> future = api.analyze()
        .setComment(audioTranscription)
        .addAttribute(Attribute.ofType(Attribute.TOXICITY))
        .postAsync();
    
      // Get the perceived toxicity score of the transcription.
      AnalyzeCommentResponse commentResponse = future.get();
      float summaryScore = commentResponse.getAttributeSummaryScore(Attribute.TOXICITY);
      
      // Return the audio's effect. 
      String audioEffectJson = convertToJsonUsingGson(summaryScore);
      response.getWriter().println(audioEffectJson);
    } catch (ExecutionException e) {
      Gson gson = new Gson();
      response.getWriter().println(gson.toJson("I am sorry. Execution Exception."));
    } catch (InterruptedException ioe) {
      Gson gson = new Gson();
      response.getWriter().println(gson.toJson("I am sorry. Interrupted Exception."));
    }
  }

  /** 
   * Converts audio effect to JSON string using GSON library.
   */
  private String convertToJsonUsingGson(float audioEffect) {
    Gson gson = new Gson();
    String json = gson.toJson(audioEffect);
    return json;
  }
}