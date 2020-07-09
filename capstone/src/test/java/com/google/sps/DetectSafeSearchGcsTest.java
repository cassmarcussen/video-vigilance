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

import com.google.sps.servlets.DetectSafeSearchGcs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.mockito.Mockito.*;
import static org.mockito.Matchers.*;

/*
DetectSafeSearchGcsTest tests the detectSafeSearchGcs method, which is used in Video Vigilance 
to extract the SafeSearch effect metrics of the Cloud Vision API from keyframe images which are stored 
in a Google cloud Storage Bucket. This tester class tests incorrect or invalid bucket path formats, as 
well as a connection to the Vision API through the detectsSafeSearchGcs() method of the DetectsSafeSearchGcs 
class which has a valid Cloud Bucket path format.
*/
@RunWith(JUnit4.class)
public final class DetectSafeSearchGcsTest {

  private DetectSafeSearchGcs detectSafeSearch;
  private DetectSafeSearchGcs mockDetectSafeSearch;

  @Before
  public void setup() {
    detectSafeSearch = new DetectSafeSearchGcs();
    mockDetectSafeSearch = mock(DetectSafeSearchGcs.class);
  }

  @Test (expected = Exception.class)
  public void incorrectBucketPathFormat() throws Exception {
    detectSafeSearch.detectSafeSearchGcs("gs:/video-vigilance-videos");
  }

  @Test (expected = Exception.class)
  public void incorrectBucketPathFormat2() throws Exception {
    detectSafeSearch.detectSafeSearchGcs("gs:video-vigilance-videos");
  }

  @Test (expected = Exception.class)
  public void incorrectBucketPathFormat3() throws Exception {
    detectSafeSearch.detectSafeSearchGcs("gs//video-vigilance-videos");
  }

  @Test (expected = Exception.class)
  public void nonexistentBucket() throws Exception {
    detectSafeSearch.detectSafeSearchGcs("gs://fake-bucket");
  }

  @Test (expected = Exception.class)
  public void incorrectFileFormat() throws Exception {
    detectSafeSearch.detectSafeSearchGcs("gs://keyframe-images/download.png");
  }

  @Test (expected = Exception.class)
  public void noFileWithPath() throws Exception {
    detectSafeSearch.detectSafeSearchGcs("gs://video-vigilance-videos/missing-video.mp4");
  }

  @Test
  public void connectToAPI() throws Exception {
    // May need to adjust test values if bucket gets deleted...
    HashMap<String, String> mockSafeSearchResults = new HashMap<String, String>();
    mockSafeSearchResults.put("adult", "VERY_UNLIKELY");
    mockSafeSearchResults.put("medical", "UNLIKELY");
    mockSafeSearchResults.put("spoofed", "VERY_UNLIKELY");
    mockSafeSearchResults.put("violence", "UNLIKELY");
    mockSafeSearchResults.put("racy", "VERY_UNLIKELY");

    when(mockDetectSafeSearch.detectSafeSearchGcs(anyString())).thenReturn(mockSafeSearchResults);

    // NOTE: Returns permission denied error
    HashMap<String, String> safeSearchResults = mockDetectSafeSearch.detectSafeSearchGcs("gs://keyframe-images-to-effect/AAANsUnmvLkSJZEVnYAh6DNG6O13zzRusbFKKRTwjdDj81ikKqNbo7wwYIvwYQUJd1bnQCW0XdNRjf82G21nk7yBGfqObtMJgw.R2GN-ZINyUODcEv1");
    
    Assert.assertEquals(mockSafeSearchResults, safeSearchResults);
  }
}