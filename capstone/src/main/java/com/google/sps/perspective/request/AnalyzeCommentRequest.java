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
import org.asynchttpclient.Response;
import java.io.IOException;

/**
 * Creates an AnalyzeCommentRequest for a comment to be analyzed by the PerspectiveAPI.
 */
public class AnalyzeCommentRequest extends BaseRequest<AnalyzeCommentResponse> {

  private AnalyzeCommentBody requestBody;
  
  /**
   * Creates a new instance of an AnalyzeCommentRequest.
   * @param client the client
   * @param sessionId an opaque session id
   * @param clientToken an opaque client token echoed back in the response
   */
  public AnalyzeCommentRequest(Client client, String sessionId, String clientToken) {
    super(client);
    requestBody = new AnalyzeCommentBody();
    requestBody.sessionId = sessionId;
    requestBody.clientToken = clientToken;
  }

  /**
   * Overrides bodyJson() from BaseRequest.
   * Serialize Java object into Json string.
   * @return the request body as Json
   */ 
  @Override
  String bodyJSON() throws JsonProcessingException {
    if (requestBody.comment == null) {
      throw new IllegalArgumentException("A comment must be provided");
    }
    if (requestBody.requestedAttributes.isEmpty()) {
      throw new IllegalArgumentException("At least 1 attribute must be provided");
    }
    return client.mapper.writeValueAsString(requestBody);
  }

  /**
   * Overrides getPath() from BaseRequest
   * @return the path for the request
   */ 
  @Override
  String getPath() {
    return client.getEndpoint("comments:analyze");
  }

  /**
   * Overrides transform() from BaseRequest
   * @return the deserialized response of Perspective API
   */ 
  @Override
  AnalyzeCommentResponse transform(Response response) {
    try {
      return client.mapper.readValue(response.getResponseBodyAsStream(), AnalyzeCommentResponse.class);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Creates a new instance of Comment.
   * @param comment the text of the comment
   * @param type the type of the text of the comment
   * @return the AnalyzeCommentRequest
   */
  public AnalyzeCommentRequest setComment(String comment, String type) {
    requestBody.comment = new Comment(comment, type);
    return this;
  }

  /**
   * Creates a new instance of default Comment.
   * @param comment the text of the comment
   * @return the AnalyzeCommentRequest
   */
  public AnalyzeCommentRequest setComment(String comment) {
    return setComment(comment, null);
  }

  /**
   * Adds languages to the AnalyzeCommentRequest.
   * @param lang the language to add 
   * @return the AnalyzeCommentRequest
   */
  public AnalyzeCommentRequest addLanguage(String lang) {
    requestBody.languages.add(lang);
    return this;
  }

  /**
   * Adds a requested attribute to the AnalyzeCommentRequest.
   * You must set at least 1 attribute but you may add as many as you need.
   * @param attr the attribute to score on
   * @return the AnalyzeCommentRequest
   */
  public AnalyzeCommentRequest addAttribute(Attribute attr) {
    requestBody.requestedAttributes.put(attr.type, attr);
    return this;
  }
}