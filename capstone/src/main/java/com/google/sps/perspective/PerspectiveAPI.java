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

package com.google.sps.perspective;

import com.google.sps.perspective.request.AnalyzeCommentRequest;
import com.google.sps.perspective.request.Client;

/**
 * Moderate a video file by analyzing its audio transcription to detect any element of "toxicity"
 * and the effect it may have on a potential customer utilizing Google's Perspective API.
 * Create an AnalyzeComment request.
 */
public class PerspectiveAPI {
  
  private final String clientToken;
  private final String sessionId;
  private final Client client;
  
  /**
   * Build out instance.
   * @param builder the builder for the AnalyzeCommentRequest
   */
  public PerspectiveAPI(PerspectiveAPIBuilder builder) {
    this.sessionId = builder.sessionId;
    this.clientToken = builder.clientToken;
    client = new Client(builder.apiKey, builder.apiVersion);
  }
  
  /**
   * Create AnalyzeCommentRequest with built out instance.
   * @return the response of the Perspective API AnalyzeCommentRequest
   */
  public AnalyzeCommentRequest analyze() {
    return new AnalyzeCommentRequest(client, sessionId, clientToken);
  }
}