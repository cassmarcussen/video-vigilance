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
 */
public class Comment {

  @JsonProperty("text")
  public String text;

  @JsonProperty("type")
  public String type;

  /**
   * Creates an instance of a comment to be analyzed by Perspective API.
   * @param text is the comment text
   * @param type is the type of the text for the comment, PLAIN_TEXT vs HTML
   */
  public Comment(String text, String type) {
    this.text = text;
    this.type = type;
  }
}