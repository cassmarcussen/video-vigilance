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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Creates an AnalyzeCommentResponse for a comment that was analyzed by the PerspectiveAPI
 * to get the attribute scores.
 * Reference: https://github.com/conversationai/perspectiveapi/blob/master/2-api/methods.md#analyzecomment-response
 */
public class AnalyzeCommentResponse extends BaseResponse {

  @JsonProperty("languages")
  public List<String> languages;

  @JsonProperty("attributeScores")
  public Map<String, AttributeScores> attributeScores;

  @JsonIgnore
  private Map<String, Float> attributeSummaryScores;

  /**
   * Returns the summary score as a probability.
   * @param attr Attribute Name
   * @return the summary score as a probability
   */
  public float getAttributeSummaryScore(String attr) {
    return getAttributeSummaryScores().getOrDefault(attr, 0f);
  }

  /**
   * Returns a mapping of Attribute names to the summary score as a probability
   * @return a mapping of Attribute names to the summary score as a probability
   */
  public Map<String, Float> getAttributeSummaryScores() {
    if (attributeSummaryScores == null && attributeScores == null) {
      // No scores returned in response.
      attributeSummaryScores = Collections.emptyMap();
    } else {
      attributeSummaryScores = new HashMap<>(attributeScores.size());
      attributeScores.forEach((k, v) -> {
        if (v != null && v.summaryScore != null) {
          attributeSummaryScores.put(k, v.summaryScore.score);
        }
      });
    }
    return attributeSummaryScores;
  }
}