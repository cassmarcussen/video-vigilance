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
package com.google.sps;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.sps.perspective.attributes.AnalyzeCommentBody;
import com.google.sps.perspective.attributes.Attribute;
import com.google.sps.perspective.request.AnalyzeCommentRequest; 
import com.google.sps.perspective.request.Client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit Test class for AnalyzeCommentRequest*/
@RunWith(JUnit4.class)
public class AnalyzeCommentRequestTest {

  private AnalyzeCommentRequest request;
  private Client client;

  @Before
  public void setUp() {
    client = new Client("key", "version");
    request = new AnalyzeCommentRequest(client);
  }

  // Passes
  @Test
  public void testAnalyzeCommentRequest_withComment_withAttribute() throws JsonProcessingException {
    // TEST: creating a good AnalyzeCommentRequest instance by building with good request body
    final String expected = "{\"comment\":{"
        + "\"text\":\"I am a fake transcription passed in as a comment.\","
        + "\"type\":\"PLAIN_TEXT\""
      + "},"
      + "\"requestedAttributes\":{"
        + "\"TOXICITY\":{}"
      + "}"
      + "}";
    
    request.setComment("I am a fake transcription passed in as a comment.")
      .addAttribute(Attribute.ofType(Attribute.TOXICITY));

    Assert.assertEquals(expected, request.bodyJSON());
  }

  // Passes
  @Test
  public void testAnalyzeCommentRequest_withComment_withAttribute_withLanguage() throws JsonProcessingException {
    // Test: creating a good AnalyzeCommentRequest instance and adding a language in the good request body
    final String expected = "{\"comment\":{"
        + "\"text\":\"I am a fake transcription passed in as a comment.\","
        + "\"type\":\"PLAIN_TEXT\""
      + "},"
      + "\"requestedAttributes\":{"
        + "\"TOXICITY\":{}"
      + "},"
      + "\"languages\":[\"en\"]"
      + "}";
    
    request.setComment("I am a fake transcription passed in as a comment.")
        .addLanguage("en")
        .addAttribute(Attribute.ofType(Attribute.TOXICITY));

    Assert.assertEquals(expected, request.bodyJSON());
  }

  // Passes 
  @Test (expected = IllegalArgumentException.class)
  public void testAnalyzeCommentRequest_noComment() throws JsonProcessingException {
    // TEST: creating a bad comment instance by not setting a comment in request body 
    // Should throw an IllegalArgumentException with message: "A comment must be provided"
    request.addAttribute(Attribute.ofType(Attribute.TOXICITY));
    try {
      request.bodyJSON();
    } catch (IllegalArgumentException e) {
      String expectedErrorMessage = "A comment must be provided";
      Assert.assertEquals(expectedErrorMessage, e.getMessage());
      throw e;
    }
  }

  // Passes
  @Test (expected = IllegalArgumentException.class)
  public void testAnalyzeCommentRequest_noAttribute() throws JsonProcessingException {
    // TEST: creating a bad comment instance by not adding an attribute of which to rate the comment on in request body
    // Should throw an IllegalArgumentException with message: "At least 1 attribute must be provided"
    request.setComment("I am a fake transcription passed in as a comment.");
     try {
      request.bodyJSON();
    } catch (IllegalArgumentException e) {
      String expectedErrorMessage = "At least 1 attribute must be provided";
      Assert.assertEquals(expectedErrorMessage, e.getMessage());
      throw e;
    }
  }
}