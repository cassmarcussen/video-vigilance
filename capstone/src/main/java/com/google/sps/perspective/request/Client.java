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

package com.google.sps.perspective.request;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.DefaultAsyncHttpClient;

/**
 * Configure the client for Perspective API. 
 */
public class Client {

  private static final String BASE_FORMAT = "https://commentanalyzer.googleapis.com/%s/%%s?key=%s";
  private final String BASE_PATH;

  final AsyncHttpClient http;
  final ObjectMapper mapper;

  /**
   * Congifure the client for Perspective API. 
   * @param apiKey the api key issued for Perspective API issue
   * @param apiVersion the version of Perspective API
   */
  public Client(String apiKey, String apiVersion) {
    http = new DefaultAsyncHttpClient();
    mapper = new ObjectMapper()
      .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
      .setSerializationInclusion(Include.NON_DEFAULT);
    BASE_PATH = String.format(BASE_FORMAT, apiVersion, apiKey);
  }

  /**
   * Returns the path for a given endpoint: BASE_PATH / API_VERSION / endpoint
   * @return the path for a given endpoint: BASE_PATH / API_VERSION / endpoint
   */
  String getEndpoint(String endpoint) {
    return String.format(BASE_PATH, endpoint);
  }
}