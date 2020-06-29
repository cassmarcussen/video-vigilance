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
import com.google.sps.data.Analyze;
import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Servlet that returns video audio transcription.*/
@WebServlet("/audio-transcription")
public class AudioTranscriptionServlet extends HttpServlet {

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    // Call audio transcription method of Cloud VI API. 
    String audioTranscription = Analyze.transcribeAudio();
    // Return the audio transcription. 
    response.setContentType("application/json");
    String audioTranscriptionJson = convertToJsonUsingGson(audioTranscription);
    response.getWriter().println(audioTranscriptionJson);
  }

  /** 
   * Converts audio transcription to JSON string using GSON library.
   */
  private String convertToJsonUsingGson(String audioTranscription) {
    Gson gson = new Gson();
    String json = gson.toJson(audioTranscription);
    return json;
  }
}