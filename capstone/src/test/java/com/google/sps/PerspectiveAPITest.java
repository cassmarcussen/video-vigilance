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

import com.google.sps.perspective.PerspectiveAPI;
import com.google.sps.perspective.PerspectiveAPIBuilder;

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

import static org.mockito.Mockito.*;

/** Unit Test class for PerspectiveAPI and PerspectiveAPIBuilder*/
@RunWith(JUnit4.class)
public class PerspectiveAPITest {

  // Passes
  @Test (expected = IllegalArgumentException.class)
  public void testBuild_noAPIKey() {
    // TEST: No API key was provided to set in the build of PerspectiveAPI
    // Should throw an IllegalArgumentException with message: "No API key provided"
    PerspectiveAPIBuilder builder = new PerspectiveAPIBuilder()
      .setApiVersion("version");
    try {
      builder.build();
    } catch (IllegalArgumentException e) {
      String expectedErrorMessage = "No API key provided";
      Assert.assertEquals(expectedErrorMessage, e.getMessage());
      throw e;
    }
  }

  // Passes
  @Test (expected = IllegalArgumentException.class)
  public void testBuild_noAPIVersion() {
    // TEST: No API version was provided to set in the build of PerspectiveAPI
    // Should throw an IllegalArgumentException with message: "No API version provided"
    PerspectiveAPIBuilder builder = new PerspectiveAPIBuilder()
      .setApiKey("key");
    try {
      builder.build();
    } catch (IllegalArgumentException e) {
      String expectedErrorMessage = "No API version provided";
      Assert.assertEquals(expectedErrorMessage, e.getMessage());
      throw e;
    }
  } 

  // Passes
  @Test
  public void testBuild_withAPIKey_withAPIVersion() {
    // TEST: API Key and API version were provided and set in the build of PerspectiveAPI
    PerspectiveAPIBuilder builder = new PerspectiveAPIBuilder()
      .setApiKey("key")
      .setApiVersion("version");
    try {
      builder.build();
    } catch (IllegalArgumentException e) {
      Assert.fail("Did not expect an IllegalArgumentException because both api key and version fields were set.");
    }
  }
}