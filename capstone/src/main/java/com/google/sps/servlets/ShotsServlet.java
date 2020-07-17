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
import com.google.sps.data.DetectShots;
import com.google.sps.data.Shot;

import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList; 

/** Servlet that gets shot changes for uploaded video from Video Intelligence API*/
@WebServlet("/shots")
public class ShotsServlet extends HttpServlet {

  ArrayList<Shot> shots;

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    shots = new ArrayList<Shot>();

    // Using hard coded image file for now
    String gcsUri = "gs://video-vigilance-videos/youtube_ad_test.mp4";

    // Get detected shot times
    try {
      DetectShots detectShots = new DetectShots();
      shots = detectShots.detect(gcsUri);
    } catch (Exception e) {
      e.printStackTrace(response.getWriter());
    }

    // Create json String with shots objects (may be empty or non empty)
    // Ex output: [{"start_time":0,"end_time":3},{"start_time":3,"end_time":5}]
    Gson gson = new Gson();
    String json = gson.toJson(shots);
    response.setContentType("application/json;");
    response.getWriter().println(json);
  }
}
