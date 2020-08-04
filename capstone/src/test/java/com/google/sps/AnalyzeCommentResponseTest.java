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

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.sps.perspective.response.AnalyzeCommentResponse; 
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import java.io.IOException;

/** Unit Test class for AnalyzeCommentResponse*/
@RunWith(JUnit4.class)
public class AnalyzeCommentResponseTest {

  private AnalyzeCommentResponse response;
  private ObjectMapper mapper;
  private static final String mockResponseJSON = "{\n"
    + "  \"attributeScores\": {\n"
    + "    \"TOXICITY\": {\n"
    + "      \"summaryScore\": {\n"
    + "        \"value\": 0.5,\n"
    + "        \"type\": \"PROBABILITY\"\n"
    + "      }\n"
    + "    },\n"
    + "    \"INSULT\": {\n"
    + "      \"summaryScore\": {\n"
    + "        \"value\": 0.4,\n"
    + "        \"type\": \"PROBABILITY\"\n"
    + "      }\n"
    + "    },\n"
    + "    \"THREAT\": {\n"
    + "      \"summaryScore\": {\n"
    + "        \"value\": 0.6,\n"
    + "        \"type\": \"PROBABILITY\"\n"
    + "      }\n"
    + "    },\n"
    + "    \"PROFANITY\": {\n"
    + "      \"summaryScore\": {\n"
    + "        \"value\": 0.8,\n"
    + "        \"type\": \"PROBABILITY\"\n"
    + "      }\n"
    + "    },\n"
    + "    \"ADULT\": {\n"
    + "      \"summaryScore\": {\n"
    + "        \"value\": 0.1,\n"
    + "        \"type\": \"PROBABILITY\"\n"
    + "      }\n"
    + "    },\n"
    + "    \"IDENTITY_ATTACK\": {\n"
    + "      \"summaryScore\": {\n"
    + "        \"value\": 0.2,\n"
    + "        \"type\": \"PROBABILITY\"\n"
    + "      }\n"
    + "    }\n"
    + "  },\n"
    + "  \"languages\": [\n"
    + "    \"en\"\n"
    + "  ]\n"
    + "}";

  @Before
  public void setUp() {
    mapper = new ObjectMapper()
      .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
      .setSerializationInclusion(Include.NON_DEFAULT);
  }

  // Passes
  @Test
  public void testAnalyzeCommentResponse_getAttributeSummaryScore_AllRealAttributes() throws IOException {
    // TEST: Retrieve the summary score of each attribute in the mock response
    response = mapper.readValue(AnalyzeCommentResponseTest.mockResponseJSON, AnalyzeCommentResponse.class);
    Assert.assertNotNull(response.attributeScores);
    Assert.assertNotNull(response.languages);
    Assert.assertEquals(0.5f, response.getAttributeSummaryScore("TOXICITY"), 0);
    Assert.assertEquals(0.4f, response.getAttributeSummaryScore("INSULT"), 0);
    Assert.assertEquals(0.6f, response.getAttributeSummaryScore("THREAT"), 0);
    Assert.assertEquals(0.8f, response.getAttributeSummaryScore("PROFANITY"), 0);
    Assert.assertEquals(0.1f, response.getAttributeSummaryScore("ADULT"), 0);
    Assert.assertEquals(0.2f, response.getAttributeSummaryScore("IDENTITY_ATTACK"), 0);
  }

  // Passes
  @Test
  public void testAnalyzeCommentResponse_getAttributeSummaryScore_FakeAttribute() throws IOException {
    // TEST: Returns a default float value of 0 for an attribute that is not in the mock response
    response = mapper.readValue(AnalyzeCommentResponseTest.mockResponseJSON, AnalyzeCommentResponse.class);
    Assert.assertNotNull(response.attributeScores);
    Assert.assertNotNull(response.languages);
    Assert.assertEquals(0f, response.getAttributeSummaryScore("FAKE_ATTRIBUTE"), 0); 
  }

}