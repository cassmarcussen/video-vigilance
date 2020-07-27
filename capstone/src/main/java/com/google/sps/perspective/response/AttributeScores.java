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
 * Deserializes the type of the scores returned by the Perspective API in the AnalyzeComment response.
 * A comment is broken up into spans and each span is given a score for each given attribute,
 * hence spanScores.
 * Reference to span scores for attributes returned by the Perspective API:
 * https://github.com/conversationai/perspectiveapi/blob/master/2-api/key-concepts.md#span
 * A comment is given an overall score for the entire comment (encompassing all spans) for each given attribute, hence
 * summaryScore.
 * Reference to summary scores for attributes returned by the Perspective API:
 * https://github.com/conversationai/perspectiveapi/blob/master/2-api/key-concepts.md#summary-score
 */
public class AttributeScores {
 
  /**
   * Note: spanScores would be another JsonProperty here, but our team does not care about
   * span scores for our Video Vigilance implementation. We are only interested in summaryScore.
   */
 
  @JsonProperty("summaryScore")
  public SummaryScore summaryScore;
}
