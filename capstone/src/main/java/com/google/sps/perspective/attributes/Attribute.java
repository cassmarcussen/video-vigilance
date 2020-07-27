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
   * TOXICITY is a production attribute. Production attributes have been tested across multiple
   * domains and trained on hundreds of thousands of human-annotated comments.
   */
  public static final String TOXICITY = "TOXICITY";
 
  /**
   * INSULT: insulting, inflammatory, or negative comment towards a person/group of people
   * INSULT is an experimental attribute. Experimental attributes have not been tested as
   * thoroughly as production attributes. If using these attributes, will need to update code
   * when attribute changes from experimental to production because experimental attribute will
   * be deprecated. 
   */
  public static final String INSULT = "INSULT";
 
  /**
   * THREAT: an intention to inflict pain, injury, or violence against an individual or group
   * THREAT is an experimental attribute. Experimental attributes have not been tested as
   * thoroughly as production attributes. If using these attributes, will need to update code
   * when attribute changes from experimental to production because experimental attribute will
   * be deprecated. 
   */
  public static final String THREAT = "THREAT";
 
  /**
   * PROFANITY: swear words, curse words, or other obscene or profane language
   * PROFANITY is an experimental attribute. Experimental attributes have not been tested as
   * thoroughly as production attributes. If using these attributes, will need to update code
   * when attribute changes from experimental to production because experimental attribute will
   * be deprecated. 
   */
  public static final String PROFANITY = "PROFANITY";
 
  /**
   * SEXUALLY_EXPLICIT: references to sexual acts, body parts, or other lewd content
   * SEXUALLY_EXPLICIT is an experimental attribute. Experimental attributes have not been tested as
   * thoroughly as production attributes. If using these attributes, will need to update code
   * when attribute changes from experimental to production because experimental attribute will
   * be deprecated. 
   */
  public static final String SEXUALLY_EXPLICIT = "SEXUALLY_EXPLICIT";
 
  /**
   * IDENTITY_ATTACK: negative or hateful comments targeting someone because of their identity
   * IDENTITY_ATTACK is an experimental attribute. Experimental attributes have not been tested as
   * thoroughly as production attributes. If using these attributes, will need to update code
   * when attribute changes from experimental to production because experimental attribute will
   * be deprecated. 
   */
  public static final String IDENTITY_ATTACK = "IDENTITY_ATTACK";
 
  @JsonIgnore
  public final String type;
 
  @JsonProperty("scoreType")
  public String scoreType;
 
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
}
