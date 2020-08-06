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

import com.google.cloud.vision.v1.AnnotateImageRequest;
import com.google.cloud.vision.v1.AnnotateImageResponse;
import com.google.cloud.vision.v1.BatchAnnotateImagesRequest;
import com.google.cloud.vision.v1.BatchAnnotateImagesResponse;
import com.google.cloud.vision.v1.ImageAnnotatorClient;
import java.util.ArrayList;
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

  private MockDetectSafeSearchGcs mockDetectSafeSearch;
  private HashMap<String, String> mockSafeSearchResults;       // List of results for all videos requested (will only contain 1)

  private List<AnnotateImageResponse> mockedBatchAnnotateImagesResponseList;

  // Mock DetectSafeSearchGcs so we can stub the detectSafeSearchGcs method for testing
  class MockDetectSafeSearchGcs extends DetectSafeSearchGcs {

    @Override 
    public List<AnnotateImageResponse> batchAnnotateImagesResponseList(List<AnnotateImageRequest> requests) {
        return new ArrayList<AnnotateImageResponse>();
    }

  }

  @Before
  public void setup() throws Exception {
    mockDetectSafeSearch = mock(MockDetectSafeSearchGcs.class);
    mockSafeSearchResults = new HashMap<String, String>();
    mockSafeSearchResults.put("adult", "UNKNOWN");
    mockSafeSearchResults.put("medical", "UNKNOWN");
    mockSafeSearchResults.put("spoofed", "UNKNOWN");
    mockSafeSearchResults.put("violence", "UNKNOWN");
    mockSafeSearchResults.put("racy", "UNKNOWN");

    mockedBatchAnnotateImagesResponseList = new ArrayList<AnnotateImageResponse>();
    AnnotateImageResponse responseAdult = AnnotateImageResponse.newBuilder().build();
    AnnotateImageResponse responseMedical = AnnotateImageResponse.newBuilder().build();
    AnnotateImageResponse responseViolence = AnnotateImageResponse.newBuilder().build();
    AnnotateImageResponse responseRacy = AnnotateImageResponse.newBuilder().build();
    AnnotateImageResponse responseSpoofed = AnnotateImageResponse.newBuilder().build();
    mockedBatchAnnotateImagesResponseList.add(responseAdult);
    mockedBatchAnnotateImagesResponseList.add(responseMedical);
    mockedBatchAnnotateImagesResponseList.add(responseViolence);
    mockedBatchAnnotateImagesResponseList.add(responseRacy);
    mockedBatchAnnotateImagesResponseList.add(responseSpoofed);

     // Specify which functions of mockDetectShots to stub
    when(mockDetectSafeSearch.batchAnnotateImagesResponseList(any(List.class))).thenReturn(mockedBatchAnnotateImagesResponseList);
    when(mockDetectSafeSearch.detectSafeSearchGcs(any(String.class))).thenCallRealMethod();

  }

  @Test
  public void testDetectSafeSearchGcs_incorrectBucketPathOneSlash() throws Exception {
    // This test is incorrect because the 'gs' is followed by ':/' instead of '://' in the url
    mockDetectSafeSearch.detectSafeSearchGcs("gs:/video-vigilance-videos");
  }

  @Test
  public void testDetectSafeSearchGcs_incorrectBucketPathFormatNoSlashes() throws Exception {
    // This test is incorrect because the 'gs' is followed by ':' instead of '://' in the url
    mockDetectSafeSearch.detectSafeSearchGcs("gs:video-vigilance-videos");
  }

  @Test
  public void testDetectSafeSearchGcs_incorrectBucketPathFormatMissingSemicolon() throws Exception {
    // This test is incorrect because the 'gs' is followed by '//' instead of '://' in the url
    mockDetectSafeSearch.detectSafeSearchGcs("gs//video-vigilance-videos");
  }

  @Test
  public void testDetectSafeSearchGcs_nonexistentBucket() throws Exception {
    // This test is incorrect because the fake-bucket in the url does not exist
    mockDetectSafeSearch.detectSafeSearchGcs("gs://fake-bucket");
  }

  @Test
  public void testDetectSafeSearchGcs_noFileWithPath() throws Exception {
    // This test is incorrect because the file missing-image.jpg does not exist in the keyframe-images bucket
    mockDetectSafeSearch.detectSafeSearchGcs("gs://video-vigilance-videos/missing-image.jpg");
  }


  @Test
  public void connectToAPI() throws Exception {
    HashMap<String, String> safeSearchResults = mockDetectSafeSearch.detectSafeSearchGcs("gs://keyframe-images-to-effect/AAANsUnmvLkSJZEVnYAh6DNG6O13zzRusbFKKRTwjdDj81ikKqNbo7wwYIvwYQUJd1bnQCW0XdNRjf82G21nk7yBGfqObtMJgw.R2GN-ZINyUODcEv1");
    
    Assert.assertEquals(mockSafeSearchResults, safeSearchResults);
  }

}
