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

/** Unit Test class for PerspectiveAPI*/
@RunWith(JUnit4.class)
public class PerspectiveAPITest {

  // Passes
  @Test
  public void noAPIKey() {
    // TEST: No API key was provided to set in the build of PerspectiveAPI
    // Should throw an IllegalArgumentException
    PerspectiveAPIBuilder builder = new PerspectiveAPIBuilder()
      .setClientToken("token")
      .setApiVersion("version")
      .setSessionId("sessionId");

    try {
      builder.build();
      Assert.fail("Expected an IllegalArgumentException because no api key was provided.");
    } catch (IllegalArgumentException e) {
      // Pass
    }
  }

  // Passes
  @Test
  public void goodAPIKey() {
    // TEST: API Key was provided to set in the build of PerspectiveAPI
    PerspectiveAPIBuilder builder = new PerspectiveAPIBuilder()
      .setApiKey("key")
      .setClientToken("token")
      .setApiVersion("version")
      .setSessionId("sessionId");
  
    try {
      builder.build();
    } catch (IllegalArgumentException e) {
      Assert.fail("Did not expect an IllegalArgumentException because api key.");
    }
  }
}