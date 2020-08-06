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

package com.google.sps.servlets;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.*;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

import static org.junit.matchers.JUnitMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
KeyframeImageUploadServletTest tests the get and post methods of KeyframeImageUploadServlet and the post method 
of KeyframeImageDeleteServlet */
public class KeyframeImageUploadServletTest {

    private MockMvc mockMvc;

    @Before
    public void init(){
        MockitoAnnotations.initMocks(this);
        this.mockMvc = MockMvcBuilders.standaloneSetup(new MockitoController()).build();
    }

    @Test
    public void KeyframeImageUploadServlet_test_post_keyframeimage_success() throws Exception {

      mockMvc.perform(
        post("/keyframe-image-upload-tester")
          .param("image", "test_url")
          .param("timestamp", "1:00")
          .param("isManuallyCaptured", "true")
        .andDo(MockMvcResultHandlers.print())
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("post")));

    }

    @Test
    public void KeyframeImageUploadServlet_test_empty_post_keyframeimage() throws Exception {

      mockMvc.perform(
        post("/keyframe-image-upload-tester")
          .param("image", "")
          .param("timestamp", "")
          .param("isManuallyCaptured", "")
        .andDo(MockMvcResultHandlers.print())
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("post")));

    }

    @Test
    public void KeyframeImageUploadServlet_test_get_keyframeimage_success() throws Exception {

      mockMvc.perform(
        get("/keyframe-image-upload-tester"))
        .andDo(MockMvcResultHandlers.print())
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("get")));

    }


    @Test
    public void KeyframeImageUploadServlet_test_delete_keyframeimages() throws Exception {

      mockMvc.perform(
        post("/keyframe-image-delete-tester"))
        .andDo(MockMvcResultHandlers.print())
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("delete")));

    }

}