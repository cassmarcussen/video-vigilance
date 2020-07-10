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

import com.google.sps.perspective.request.Client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.ServletException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.mockito.Mockito.*;

/** Unit Test class for Client*/
@RunWith(JUnit4.class)
public class ClientTest {
  
  private Client client;

  @Before
  public void setUp() {
    client = new Client("key", "version");
  }

  // Passes
  @Test
  public void goodPath() {
    // TEST: the path is properly formatted: BASE_PATH / API_VERSION / endpoint
    String actual = client.getEndpoint("e");
    String expected = "https://commentanalyzer.googleapis.com/version/e?key=key";
    Assert.assertEquals(expected, actual);
  }

  // Passes
  @Test
  public void goodPath2() {
    // TEST: the path is properly formatted: BASE_PATH / API_VERSION / endpoint
    String actual = client.getEndpoint("randomendpoint0123456789");
    String expected = "https://commentanalyzer.googleapis.com/version/randomendpoint0123456789?key=key";
    Assert.assertEquals(expected, actual);
  }
}