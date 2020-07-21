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
 * Builds out the instance of PerspectiveAPI.
 */
public class PerspectiveAPIBuilder {

  String apiKey;
  String apiVersion = "v1alpha1";

  /**
   * Builds instance of PerspectiveAPI.
   * @return the builder
   */
  public PerspectiveAPI build() {
    if (apiKey == null) {
      throw new IllegalArgumentException("No API key provided");
    }
    return new PerspectiveAPI(this);
  }


  /**
   * Sets the api key for Perspective API.
   * @param apiKey the api key for Perspective API
   * @return the builder
   */
  public PerspectiveAPIBuilder setApiKey(String apiKey) {
    this.apiKey = apiKey;
    return this;
  }
  
  /**
   * Sets the api version for Perspective API for score normalization.
   * @param apiVersion the api version for Perspective API
   * @return the builder
   */
  public PerspectiveAPIBuilder setApiVersion(String apiVersion) {
    this.apiVersion = apiVersion;
    return this;
  }
}