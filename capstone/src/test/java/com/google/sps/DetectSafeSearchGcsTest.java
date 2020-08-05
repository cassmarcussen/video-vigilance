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

import java.util.HashMap;
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

  private MockDetectSafeSearchGcs mockDetectSafeSearch;
  //private HashMap<String, String> expectedSafeSearchEffects;
  private HashMap<String, String> mockSafeSearchResults;       // List of results for all videos requested (will only contain 1)

  // Mock DetectSafeSearchGcs so we can stub the detectSafeSearchGcs method for testing
  class MockDetectSafeSearchGcs extends DetectSafeSearchGcs{

    @Override
    public HashMap<String, String> detectSafeSearchGcs(String gcsPath) {
      return new HashMap<String, String>();
    }

  }

  @Before
  public void setup() {
    mockDetectSafeSearch = mock(MockDetectSafeSearchGcs.class);
    mockSafeSearchResults = new HashMap<String, String>();
    mockSafeSearchResults.put("adult", "VERY_UNLIKELY");
    mockSafeSearchResults.put("medical", "UNLIKELY");
    mockSafeSearchResults.put("spoofed", "VERY_UNLIKELY");
    mockSafeSearchResults.put("violence", "UNLIKELY");
    mockSafeSearchResults.put("racy", "VERY_UNLIKELY");

    // Specify which functions of mockDetectSafeSearchGcs
    when(mockDetectSafeSearch.detectSafeSearchGcs(anyString())).thenReturn(mockSafeSearchResults);

  }

  @Test
  public void testDetectSafeSearchGcs_incorrectBucketPathOneSlash() {
    // This test is incorrect because the 'gs' is followed by ':/' instead of '://' in the url
    mockDetectSafeSearch.detectSafeSearchGcs("gs:/video-vigilance-videos");
  }

  @Test
  public void testDetectSafeSearchGcs_incorrectBucketPathFormatNoSlashes() {
    // This test is incorrect because the 'gs' is followed by ':' instead of '://' in the url
    mockDetectSafeSearch.detectSafeSearchGcs("gs:video-vigilance-videos");
  }

  @Test
  public void testDetectSafeSearchGcs_incorrectBucketPathFormatMissingSemicolon() {
    // This test is incorrect because the 'gs' is followed by '//' instead of '://' in the url
    mockDetectSafeSearch.detectSafeSearchGcs("gs//video-vigilance-videos");
  }

  @Test
  public void testDetectSafeSearchGcs_nonexistentBucket() {
    // This test is incorrect because the fake-bucket in the url does not exist
    mockDetectSafeSearch.detectSafeSearchGcs("gs://fake-bucket");
  }

  @Test
  public void testDetectSafeSearchGcs_noFileWithPath() {
    // This test is incorrect because the file missing-image.jpg does not exist in the keyframe-images bucket
    mockDetectSafeSearch.detectSafeSearchGcs("gs://video-vigilance-videos/missing-image.jpg");
  }


  @Test
  public void connectToAPI() {
    HashMap<String, String> safeSearchResults = mockDetectSafeSearch.detectSafeSearchGcs("gs://keyframe-images-to-effect/AAANsUnmvLkSJZEVnYAh6DNG6O13zzRusbFKKRTwjdDj81ikKqNbo7wwYIvwYQUJd1bnQCW0XdNRjf82G21nk7yBGfqObtMJgw.R2GN-ZINyUODcEv1");
    
    Assert.assertEquals(mockSafeSearchResults, safeSearchResults);
  }

}
