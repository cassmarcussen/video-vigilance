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

package com.google.sps.perspective.attributes;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents an attribute type that the Perspective API scores on.
 * Reference to the different attribute types supported by the Perspective API: 
 * https://github.com/conversationai/perspectiveapi/blob/master/2-api/models.md#all-attribute-types
 */
public class Attribute {
  
  /**
   * TOXICITY: rude, disrespectful, or unreasonable comment likely to make people leave a discussion
   * TOXICITY is a production attribute
   */
  public static final String TOXICITY = "TOXICITY";

  /**
   * SEVERE_TOXICITY: very hateful, aggressive, disrespectful comment or otherwise likely to make people leave a discussion or feel discouraged to share their perspective
   * SEVERE_TOXICITY is a production attribute
   */
  public static final String SEVERE_TOXICITY = "SEVERE_TOXICITY";

  /**
   * PROFANITY: swear words, curse words, or other obscene or profane language
   * PROFANITY is an experimental attribute
   */
  public static final String PROFANITY = "PROFANITY";

  /**
   * INSULT: insulting, inflammatory, or negative comment towards a person/group of people
   * INSULT is an experimental attribute
   */
  public static final String INSULT = "INSULT";

  @JsonIgnore
  public final String type;

  @JsonProperty("scoreType")
  public String scoreType;

  @JsonProperty("scoreThreshold")
  public float scoreThreshold;

  /**
   * Creates an Attribute with the passed in type.
   * @param type the type of the attribute
   */
  private Attribute(String type) {
    this.type = type;
  }

  /**
   * Returns an Attribute of the passed in type. Types include, but are not limited to:
   * Attribute.TOXICITY, Attribute.INSULT, etc...
   * @param type the type of the attribute
   * @return the Attribute with the passed in type
   */
  public static Attribute ofType(String type) {
    return new Attribute(type);
  }

  /**
   * Set the type of the score to be returned.
   * The only supported score type and the default is PROBABILITY.
   * Reference: https://github.com/conversationai/perspectiveapi/blob/master/2-api/key-concepts.md#score-types
   * @param scoreType the type of the score
   * @return the builder
   */
  public Attribute setScoreType(String scoreType) {
    this.scoreType = scoreType;
    return this;
  }
  
  /**
   * Sets the threshold that scores must be at or above in order to be returned.
   * @param scoreThreshold the threshold for scores to return
   * @return the builder
   */
  public Attribute setScoreThreshold(float scoreThreshold) {
    this.scoreThreshold = scoreThreshold;
    return this;
  }
}