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
 * The types of scores returned from the AnalyzeCommentResponse.
 * A comment is broken up into spans and each span is given a score for a given attribute,
 * hence spanScores.
 * A comment is given an overall score for the entire comment for a given attribute, hence
 * summaryScore.
 * Reference: https://github.com/conversationai/perspectiveapi/blob/master/2-api/key-concepts.md#score-types
 */
public class AttributeScores {

  /**
   * Note: spanScores would be another JsonProperty here, but our team does not care about
   * span scores in our Video Vigilance implementation.
   */

  @JsonProperty("summaryScore")
  public ProbabilityScore summaryScore;
}