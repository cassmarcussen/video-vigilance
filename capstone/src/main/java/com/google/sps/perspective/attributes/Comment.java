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
 
import com.fasterxml.jackson.annotation.JsonProperty;
 
/**
 * Creates an instance of a comment to be analyzed by Perspective API.
 * Serializes the text and type of a comment into JSON so Perspective API may interpret
 * the comment we send inside our AnalyzeComment request.
 * Reference to the fields of a comment: 
 * https://github.com/conversationai/perspectiveapi/blob/master/2-api/methods.md#analyzecomment-request
 */
public class Comment {
 
  @JsonProperty("text")
  public String text;
 
  @JsonProperty("type")
  public String type;
 
  /**
   * Creates an instance of a comment to be analyzed by Perspective API and sets the text and type.
   * @param text is the text to score
   * @param type is the type the comment.text. Either "HTML" or "PLAIN_TEXT." Currently, only "PLAIN_TEXT" is supported.
   */
  public Comment(String text, String type) {
    this.text = text;
    this.type = type;
  }
}
