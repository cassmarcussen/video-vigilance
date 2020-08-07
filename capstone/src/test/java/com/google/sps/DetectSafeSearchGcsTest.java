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
import com.google.cloud.vision.v1.Likelihood;
import com.google.cloud.vision.v1.SafeSearchAnnotation;
import com.google.cloud.vision.v1.SafeSearchAnnotation.Builder;
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

    mockedBatchAnnotateImagesResponseList = new ArrayList<AnnotateImageResponse>();

     // Specify which functions of mockDetectShots to stub
    when(mockDetectSafeSearch.batchAnnotateImagesResponseList(any(List.class))).thenReturn(mockedBatchAnnotateImagesResponseList);
    when(mockDetectSafeSearch.detectSafeSearchGcs(any(String.class))).thenCallRealMethod();

  }

  @Test
  public void detectSafeSearchGcs_emptyResponse() throws Exception {
    // The error below comes from the empty AnnotateImageResponses being built

    mockSafeSearchResults = new HashMap<String, String>();
    mockSafeSearchResults.put("adult", "UNKNOWN");
    mockSafeSearchResults.put("medical", "UNKNOWN");
    mockSafeSearchResults.put("spoofed", "UNKNOWN");
    mockSafeSearchResults.put("violence", "UNKNOWN");
    mockSafeSearchResults.put("racy", "UNKNOWN");

    AnnotateImageResponse responseUnfilledSafeSearch = AnnotateImageResponse.newBuilder().build();
    mockedBatchAnnotateImagesResponseList.add(responseUnfilledSafeSearch);

    HashMap<String, String> safeSearchResults = mockDetectSafeSearch.detectSafeSearchGcs("gs://keyframe-images-to-effect/AAANsUnmvLkSJZEVnYAh6DNG6O13zzRusbFKKRTwjdDj81ikKqNbo7wwYIvwYQUJd1bnQCW0XdNRjf82G21nk7yBGfqObtMJgw.R2GN-ZINyUODcEv1");
    
    Assert.assertEquals(mockSafeSearchResults, safeSearchResults);
  }


  @Test
  public void detectSafeSearchGcs_workingCase() throws Exception {

    mockSafeSearchResults = new HashMap<String, String>();
    mockSafeSearchResults.put("adult", "UNLIKELY");
    mockSafeSearchResults.put("medical", "LIKELY");
    mockSafeSearchResults.put("spoofed", "VERY_UNLIKELY");
    mockSafeSearchResults.put("violence", "VERY_LIKELY");
    mockSafeSearchResults.put("racy", "POSSIBLE");

    SafeSearchAnnotation safeSearchAnnotation = SafeSearchAnnotation.newBuilder()
                        .setAdult(Likelihood.UNLIKELY)
                        .setMedical(Likelihood.LIKELY)
                        .setSpoof(Likelihood.VERY_UNLIKELY)
                        .setViolence(Likelihood.VERY_LIKELY)
                        .setRacy(Likelihood.POSSIBLE)
                        .build();

    AnnotateImageResponse responseSafeSearch = AnnotateImageResponse.newBuilder()
                        .setSafeSearchAnnotation(safeSearchAnnotation)
                        .build();
                        
    mockedBatchAnnotateImagesResponseList.add(responseSafeSearch);

    HashMap<String, String> safeSearchResults = mockDetectSafeSearch.detectSafeSearchGcs("gs://keyframe-images-to-effect/AAANsUnmvLkSJZEVnYAh6DNG6O13zzRusbFKKRTwjdDj81ikKqNbo7wwYIvwYQUJd1bnQCW0XdNRjf82G21nk7yBGfqObtMJgw.R2GN-ZINyUODcEv1");
    
    Assert.assertEquals(mockSafeSearchResults, safeSearchResults);

  }

}
