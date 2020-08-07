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

package com.google.sps.perspective.response;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The base response for an AnalyzeCommentResponse. 
 * Reference: https://github.com/conversationai/perspectiveapi/blob/master/2-api/methods.md#analyzecomment-response
 */
public class BaseResponse {

  @JsonProperty("error")
  public Error error;

  @JsonProperty("clientToken")
  public String clientToken;
}