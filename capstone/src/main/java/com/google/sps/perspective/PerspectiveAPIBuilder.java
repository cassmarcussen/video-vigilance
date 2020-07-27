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
 
/**
 * Using a Builder pattern, builds out a new instance of PerspectiveAPIBuilder to return
 * on every invocation to analyze a transcription using Perspective API.
 */
public class PerspectiveAPIBuilder {
 
  public String apiKey;
  public String apiVersion;
 
  /**
   * Sets the api key issued for our application's use of Perspective API.
   * @param apiKey the api key for Perspective API use
   * @return the PerspectiveAPIBuilder reference
   */
  public PerspectiveAPIBuilder setApiKey(String apiKey) {
    this.apiKey = apiKey;
    return this;
  }
  
  /**
   * Sets the api version of Perspective API that our application uses for score normalization.
   * @param apiVersion the api version for Perspective API
   * @return the PerspectiveAPIBuilder reference
   */
  public PerspectiveAPIBuilder setApiVersion(String apiVersion) {
    this.apiVersion = apiVersion;
    return this;
  }
 
  /**
   * Sets the appropriate instance fields and constructs PerspectiveAPI.
   * @return the constructed PerspectiveAPI reference
   */
  public PerspectiveAPI build() {
    if (apiKey == null) {
      throw new IllegalArgumentException("No API key provided");
    }
    if (apiVersion == null) {
      throw new IllegalArgumentException("No API version provided");
    }
    return new PerspectiveAPI(this);
  }
}