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

import com.google.sps.perspective.attributes.AnalyzeCommentBody;
import com.google.sps.perspective.attributes.Attribute;
import com.google.sps.perspective.attributes.Comment;
import com.google.sps.perspective.response.AnalyzeCommentResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.base.Function;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import org.asynchttpclient.Response;
import org.asynchttpclient.extras.guava.ListenableFutureAdapter;
import java.io.IOException;
import javax.annotation.Nullable;

/**
 * Creates an AnalyzeComment request to be sent to the Perspective API client endpoint so
 * Perspective API can perform an analysis on the comment we send and score the comment based on
 * the attributes we set to score on in the request. 
 * Reference to the fields that can be included in an AnlyzeComment request: 
 * https://github.com/conversationai/perspectiveapi/blob/master/2-api/methods.md#analyzecomment-request
 */
public class AnalyzeCommentRequest {

  private AnalyzeCommentBody requestBody;
  private Client client;
  
  /**
   * Creates a new instance of an AnalyzeCommentRequest.
   * @param client the client
   */
  public AnalyzeCommentRequest(Client client) {
    this.client = client;
    requestBody = new AnalyzeCommentBody();
  }

  /**
   * Creates a new instance of Comment.
   * @param comment the text of the comment
   * @param type the type of the text of the comment
   * @return the AnalyzeCommentRequest reference
   */
  public AnalyzeCommentRequest setComment(String comment, String type) {
    requestBody.comment = new Comment(comment, type);
    return this;
  }

  /**
   * Creates a new instance of Comment.
   * @param comment the text of the comment
   * @return the AnalyzeCommentRequest reference
   */
  public AnalyzeCommentRequest setComment(String comment) {
    return setComment(comment, "PLAIN_TEXT");
  }

  /**
   * Sets whether Perspective API is permitted to store the request of our invocation.
   * Default is false, meaning Perspective API has permission to store.
   * Should be set to true if data being submitted is private (i.e. not publicly accessible),
   * or if the data submitted contains content written by someone under 13 years old.
   * @param doNotStore the api version for Perspective API
   * @return the AnalyzeCommentRequest reference
   */
  public AnalyzeCommentRequest setDoNotStore(boolean doNotStore) {
    requestBody.doNotStore = doNotStore;
    return this;
  }

  /**
   * Adds languages to the AnalyzeCommentRequest.
   * @param lang the language to add 
   * @return the AnalyzeCommentRequest reference
   */
  public AnalyzeCommentRequest addLanguage(String lang) {
    requestBody.languages.add(lang);
    return this;
  }

  /**
   * Adds a requested attribute to the AnalyzeComment request on which to score the comment on.
   * Must set at least 1 attribute. May add multiple.
   * @param attr the attribute to score on
   * @return the AnalyzeCommentRequest reference
   */
  public AnalyzeCommentRequest addAttribute(Attribute attr) {
    requestBody.requestedAttributes.put(attr.type, attr);
    return this;
  }

  /**
   * Sends a POST request to the specified uri as an asynchronous operation.
   * @return the response of Perspective API
   */
  public ListenableFuture<AnalyzeCommentResponse> postAsync() {
    String body = null;
    try {
      body = bodyJSON();
    } catch (JsonProcessingException e) {
      // Failed to serialize request body.
      // Nothing to do here. Allow body to return null.
    }

    // Prepare callback when Perspective API computation is complete.
    ListenableFuture<Response> response = ListenableFutureAdapter
      .asGuavaFuture(client.http.preparePost(getPath()).setBody(body).execute());
    
    return Futures.transform(response, new Function<Response, AnalyzeCommentResponse>() {
      @Nullable
      @Override
      public AnalyzeCommentResponse apply(@Nullable Response r) {
        return transform(r);
      }
    }, MoreExecutors.directExecutor());
  }


  /**
   * Serialize Java object into Json string.
   * @return the request body as Json
   */ 
  public String bodyJSON() throws JsonProcessingException {
    if (requestBody.comment == null) {
      throw new IllegalArgumentException("A comment must be provided");
    }
    if (requestBody.requestedAttributes.isEmpty()) {
      throw new IllegalArgumentException("At least 1 attribute must be provided");
    }
    return client.mapper.writeValueAsString(requestBody);
  }

  /**
   * @return the path for the request
   */ 
  public String getPath() {
    return client.getEndpoint("comments:analyze");
  }

  /**
   * @return the deserialized response of Perspective API
   */ 
  public AnalyzeCommentResponse transform(Response response) {
    try {
      return client.mapper.readValue(response.getResponseBodyAsStream(), AnalyzeCommentResponse.class);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  
}