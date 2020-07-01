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

abstract class BaseRequest<R> {

  Client client;

  BaseRequest(Client client) {
    this.client = client;
  }

  public ListenableFuture<R> postAsync() {
    String body = null;
    try {
      body = bodyJSON();
    } catch (JsonProcessingException e) {
      // TODO: Handle accordingly
    }

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
   *
   * @return the body of the request as Json
   */
  abstract String bodyJSON() throws JsonProcessingException;

  /**
   *
   * @return the full path, including query params, for the given request
   */
  abstract String getPath();

  abstract R transform(Response json);
}