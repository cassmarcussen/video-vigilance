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

  private DetectSafeSearchGcs detectSafeSearch;
  private DetectSafeSearchGcs mockDetectSafeSearch;
  //private HashMap<String, String> expectedSafeSearchEffects;
  private HashMap<String, String> mockSafeSearchResults;       // List of results for all videos requested (will only contain 1)
  
  // Local subclass of DetectShots that makes getAnnotationResults() public so I can stub it
 /* class MockDetectSafeSearchGcs extends DetectSafeSearchGcs {

     // can't do, b/c can't override static Java methods
    @Override 
    public static HashMap<String, String> detectSafeSearchGcs(String gcsPath) {
      return new HashMap<String, String>();
    }

  }*/

  @Before
  public void setup() throws Exception {
    detectSafeSearch = new DetectSafeSearchGcs();
    mockDetectSafeSearch = mock(DetectSafeSearchGcs.class);
    mockSafeSearchResults = new HashMap<String, String>();
    mockSafeSearchResults.put("adult", "VERY_UNLIKELY");
    mockSafeSearchResults.put("medical", "UNLIKELY");
    mockSafeSearchResults.put("spoofed", "VERY_UNLIKELY");
    mockSafeSearchResults.put("violence", "UNLIKELY");
    mockSafeSearchResults.put("racy", "VERY_UNLIKELY");

    // Specify which functions of mockDetectSafeSearchGcs
    when(mockDetectSafeSearch.detectSafeSearchGcs(anyString())).thenReturn(mockSafeSearchResults);

  }

  @Test (expected = Exception.class)
  public void testDetectSafeSearchGcs_incorrectBucketPathOneSlash() throws Exception {
    // This test is incorrect because the 'gs' is followed by ':/' instead of '://' in the url
    detectSafeSearch.detectSafeSearchGcs("gs:/video-vigilance-videos");
  }

  @Test (expected = Exception.class)
  public void testDetectSafeSearchGcs_incorrectBucketPathFormatNoSlashes() throws Exception {
    // This test is incorrect because the 'gs' is followed by ':' instead of '://' in the url
    detectSafeSearch.detectSafeSearchGcs("gs:video-vigilance-videos");
  }

  @Test (expected = Exception.class)
  public void testDetectSafeSearchGcs_incorrectBucketPathFormatMissingSemicolon() throws Exception {
    // This test is incorrect because the 'gs' is followed by '//' instead of '://' in the url
    detectSafeSearch.detectSafeSearchGcs("gs//video-vigilance-videos");
  }

  @Test (expected = Exception.class)
  public void testDetectSafeSearchGcs_nonexistentBucket() throws Exception {
    // This test is incorrect because the fake-bucket in the url does not exist
    detectSafeSearch.detectSafeSearchGcs("gs://fake-bucket");
  }

  @Test (expected = Exception.class)
  public void testDetectSafeSearchGcs_noFileWithPath() throws Exception {
    // This test is incorrect because the file missing-image.jpg does not exist in the keyframe-images bucket
    detectSafeSearch.detectSafeSearchGcs("gs://video-vigilance-videos/missing-image.jpg");
  }

  /* Permission Denied errors for all tests below here */
  /*@Test 
  public void effectUnknownReturn() throws Exception {
    HashMap<String, String> mockSafeSearchResults = new HashMap<String, String>();
    mockSafeSearchResults.put("adult", "UNKNOWN");
    mockSafeSearchResults.put("medical", "UNKNOWN");
    mockSafeSearchResults.put("spoofed", "UNKNOWN");
    mockSafeSearchResults.put("violence", "UNKNOWN");
    mockSafeSearchResults.put("racy", "UNKNOWN");

    HashMap<String, String> safeSearchResults = mockDetectSafeSearch.detectSafeSearchGcs("gs://keyframe-images-to-effect/AAANsUnmvLkSJZEVnYAh6DNG6O13zzRusbFKKRTwjdDj81ikKqNbo7wwYIvwYQUJd1bnQCW0XdNRjf82G21nk7yBGfqObtMJgw.R2GN-ZINyUODcEv1");
    Assert.assertEquals(5, safeSearchResults.size());
  }*/

 /* @Test
  public void connectToAPI() throws Exception {

    // NOTE: Returns permission denied error
    HashMap<String, String> safeSearchResults = mockDetectSafeSearch.detectSafeSearchGcs("gs://keyframe-images-to-effect/AAANsUnmvLkSJZEVnYAh6DNG6O13zzRusbFKKRTwjdDj81ikKqNbo7wwYIvwYQUJd1bnQCW0XdNRjf82G21nk7yBGfqObtMJgw.R2GN-ZINyUODcEv1");
    
    Assert.assertEquals(mockSafeSearchResults, safeSearchResults);

  }

  public void connectToAPIErrorCase() throws Exception {
    // May need to adjust test values if bucket gets deleted...
    HashMap<String, String> mockSafeSearchResults = new HashMap<String, String>();
    mockSafeSearchResults.put("adult", "UNKNOWN");
    mockSafeSearchResults.put("medical", "UNKNOWN");
    mockSafeSearchResults.put("spoofed", "UNKNOWN");
    mockSafeSearchResults.put("violence", "UNKNOWN");
    mockSafeSearchResults.put("racy", "UNKNOWN");

    when(mockDetectSafeSearch.detectSafeSearchGcs(anyString())).thenReturn(mockSafeSearchResults);

    // NOTE: Returns permission denied error
   // What to make this for error case?
    HashMap<String, String> safeSearchResults = mockDetectSafeSearch.detectSafeSearchGcs("gs://keyframe-images-to-effect/AAANsUnmvLkSJZEVnYAh6DNG6O13zzRusbFKKRTwjdDj81ikKqNbo7wwYIvwYQUJd1bnQCW0XdNRjf82G21nk7yBGfqObtMJgw.R2GN-ZINyUODcEv1");
    
    Assert.assertEquals(mockSafeSearchResults, safeSearchResults);
  }*/


}
