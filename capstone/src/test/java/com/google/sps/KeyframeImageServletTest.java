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

/*
KeyframeImageServletTest tests the POST and GET requests of KeyframeImageUploadServlet, which correspondingly 
post keyframe images and their corresponding information to DataStore and Google Cloud Bucket and retrieve the keyframe 
images and their corresponding information from DataStore and GCB. KeyframeImageServletTest also tests the POST request 
of KeyframeImageDeleteServlet, which deletes all keyframe images and their corresponding information from DataStore and 
Google Cloud Bucket. This delete serves as a refresh to be called at the start of the user flow.
*/
public class KeyframeImageServletTest {

    private MockMvc mockMvc;

    @Before
    public void init(){
        MockitoAnnotations.initMocks(this);
        this.mockMvc = MockMvcBuilders.standaloneSetup(new KeyframeImageMockitoController()).build();
    }

    @Test
    public void test_post_keyframeimage_success() throws Exception {

      mockMvc.perform(
        post("/keyframe-image-upload-tester")
          .param("image", "test_url")
          .param("timestamp", "1:00")
          .param("startTime", "0:50")
          .param("endTime", "1:20"))
        .andDo(MockMvcResultHandlers.print())
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("post")));

    }

    @Test
    public void test_empty_post_keyframeimage() throws Exception {

      mockMvc.perform(
        post("/keyframe-image-upload-tester")
          .param("image", "")
          .param("timestamp", "")
          .param("startTime", "")
          .param("endTime", ""))
        .andDo(MockMvcResultHandlers.print())
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("post")));

    }

    @Test
    public void test_get_keyframeimage_success() throws Exception {

      mockMvc.perform(
        get("/keyframe-image-upload-tester"))
        .andDo(MockMvcResultHandlers.print())
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("get")));

    }


    @Test
    public void test_delete_keyframeimages() throws Exception {

      mockMvc.perform(
        post("/keyframe-image-delete-tester"))
        .andDo(MockMvcResultHandlers.print())
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("delete")));

    }
    
}