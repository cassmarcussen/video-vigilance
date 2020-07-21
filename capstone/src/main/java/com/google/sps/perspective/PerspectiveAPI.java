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
 * Create an AnalyzeComment request.
 * Reference: 
 */
public class PerspectiveAPI {
  
  private final Client client;
  
  /**
   * Build out instance.
   * @param builder the builder for the AnalyzeCommentRequest
   */
  public PerspectiveAPI(PerspectiveAPIBuilder builder) {
    client = new Client(builder.apiKey, builder.apiVersion);
  }
  
  /**
   * Create AnalyzeCommentRequest with built out instance.
   * @return the response of the Perspective API AnalyzeCommentRequest
   */
  public AnalyzeCommentRequest analyze() {
    return new AnalyzeCommentRequest(client);
  }
}