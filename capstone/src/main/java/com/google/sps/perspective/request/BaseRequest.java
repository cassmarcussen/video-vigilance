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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.base.Function;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import org.asynchttpclient.Response;
import org.asynchttpclient.extras.guava.ListenableFutureAdapter;
import javax.annotation.Nullable;

/**
 * The base request for an AnalyzeCommentRequest. 
 * Reference: https://github.com/conversationai/perspectiveapi/blob/master/2-api/methods.md#scoring-comments-analyzecomment
 */
abstract class BaseRequest<R> {

  Client client;

  /**
   * Sets the client of the base request.
   * @param client the client
   */
  BaseRequest(Client client) {
    this.client = client;
  }

  /**
   * Sends a POST request to the specified uri as an asynchronous operation.
   * @return the response of Perspective API
   */
  public ListenableFuture<R> postAsync() {
    String body = null;
    try {
      body = bodyJSON();
    } catch (JsonProcessingException e) {
    }

    // Prepare callback when Perspective API computation is complete.
    ListenableFuture<Response> response = ListenableFutureAdapter
      .asGuavaFuture(client.http.preparePost(getPath()).setBody(body).execute());
    
    return Futures.transform(response, new Function<Response, R>() {
      @Nullable
      @Override
      public R apply(@Nullable Response r) {
        return transform(r);
      }
    }, MoreExecutors.directExecutor());
  }

  /**
   * Returns the body of the request as Json.
   * @return the body of the request as Json
   */
  abstract String bodyJSON() throws JsonProcessingException;

  /**
   * Returns the full path, including query params, for the given request
   * @return the full path, including query params, for the given request
   */
  abstract String getPath();

  /**
   * Deserializes the Json response.
   * @return the deserialized response
   */
  abstract R transform(Response json);
}