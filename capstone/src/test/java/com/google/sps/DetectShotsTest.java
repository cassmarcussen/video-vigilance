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

import com.google.api.gax.longrunning.OperationFuture;
import com.google.cloud.videointelligence.v1.AnnotateVideoProgress;
import com.google.cloud.videointelligence.v1.AnnotateVideoRequest;
import com.google.cloud.videointelligence.v1.AnnotateVideoResponse;
import com.google.cloud.videointelligence.v1.VideoIntelligenceServiceClient;
import com.google.cloud.videointelligence.v1.VideoAnnotationResults;
import com.google.cloud.videointelligence.v1.VideoAnnotationResults.Builder;
import com.google.cloud.videointelligence.v1.VideoSegment;
import com.google.protobuf.Duration;

import com.google.gson.Gson;
import com.google.sps.data.DetectShots;
import com.google.sps.data.Shot;

import java.util.List;
import java.util.ArrayList;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.mockito.Mockito.*;
import static org.mockito.Matchers.*;


@RunWith(JUnit4.class)
public final class DetectShotsTest {

  private DetectShots detectShots;
  private ArrayList<Shot> shotslist;
  
  // Local subclass of DetectShots that makes getAnnotationResults() public so I can stub it
  class MockDetectShots extends DetectShots {

    @Override 
    public List<VideoAnnotationResults> getAnnotationResults(String uri) throws Exception {
      return new ArrayList<VideoAnnotationResults>();
    }
  }

  // Set up mock DetectShots class before each test case
  @Before
  public void setup() {
    detectShots = new DetectShots();
    shotslist = new ArrayList<Shot>();
  }
  
  // Url missing a slash
  @Test (expected = Exception.class)
  public void incorrectBucketPathFormat() throws Exception {
    detectShots.detect("gs:/video-vigilance-videos");
  }

  // Url missing both slashes
  @Test (expected = Exception.class)
  public void incorrectBucketPathFormat2() throws Exception {
    detectShots.detect("gs:video-vigilance-videos");
  }

  // Url missing colon
  @Test (expected = Exception.class)
  public void incorrectBucketPathFormat3() throws Exception {
    detectShots.detect("gs//video-vigilance-videos");
  }
 
  // Url leads to bucket that doesn't exist
  @Test (expected = Exception.class)
  public void nonexistentBucket() throws Exception {
    detectShots.detect("gs://fake-bucket");
  }

  // Url leads to file that is not a video 
  @Test (expected = Exception.class)
  public void incorrectFileFormat() throws Exception {
    detectShots.detect("gs://keyframe-images/download.png");
  }

  // Url leads to file that doesn't exist
  @Test (expected = Exception.class)
  public void noFileWithPath() throws Exception {
    detectShots.detect("gs://video-vigilance-videos/missing-video.mp4");
  }

  // Test when API returns no shots
//   @Test
//   public void noShotsReturned() throws Exception {
//     when(mockDetectShots.detect(any(String.class))).thenReturn(shotslist);
    
//     ArrayList<Shot> shots = mockDetectShots.detect("gs://empty-bucket-for-tests");
//     Assert.assertEquals("[]", toJson(shots));
//   }
  


  // Test when API returns 1 shot
  @Test
  public void oneShotReturned() throws Exception {
    Shot shot = new Shot(1, 4);
    shotslist.add(shot);

    VideoAnnotationResults results = VideoAnnotationResults.getDefaultInstance();
    VideoAnnotationResults.Builder resultsBuilder = results.toBuilder();

    VideoSegment segment = VideoSegment.getDefaultInstance();
    VideoSegment.Builder segmentBuilder = segment.toBuilder();
    
    Duration startDuration = Duration.getDefaultInstance();
    Duration.Builder startDurationBuilder = startDuration.toBuilder();
    startDurationBuilder.setSeconds((long)0.0);
    segmentBuilder.setStartTimeOffset(startDurationBuilder);

    Duration endDuration = Duration.getDefaultInstance();
    Duration.Builder endDurationBuilder = endDuration.toBuilder();
    endDurationBuilder.setSeconds((long)5.0);
    segmentBuilder.setEndTimeOffset(endDurationBuilder);

    resultsBuilder.addShotAnnotations(segmentBuilder);
    results = resultsBuilder.build();

    List<VideoAnnotationResults> resultsList = new ArrayList<VideoAnnotationResults>();
    resultsList.add(results);
    
    MockDetectShots mockDetectShots = mock(MockDetectShots.class);
    when(mockDetectShots.getAnnotationResults(any(String.class))).thenReturn(resultsList);
    when(mockDetectShots.detect(any(String.class))).thenCallRealMethod();
    
    ArrayList<Shot> shots = mockDetectShots.detect("gs://empty-bucket-for-tests");

    Assert.assertEquals(1, shots.size());

    // String expected = "[{\"start_time\":1.0,\"end_time\":4.0}]";
    // Assert.assertEquals(expected, toJson(shots));
  }

  // Test when API returns 3 shots
  //@Test
//   public void multipleShotsReturned() throws Exception {
//     Shot shot1 = new Shot(1, 2);
//     Shot shot2 = new Shot(2, 4);
//     Shot shot3 = new Shot(4, 5);
//     shotslist.add(shot1);
//     shotslist.add(shot2);
//     shotslist.add(shot3);

//     when(mockDetectShots.detect(any(String.class))).thenReturn(shotslist);

//     ArrayList<Shot> shots = mockDetectShots.detect("gs://empty-bucket-for-tests");

//     String expected = "[{\"start_time\":1.0,\"end_time\":2.0}," +
//                        "{\"start_time\":2.0,\"end_time\":4.0}," +
//                        "{\"start_time\":4.0,\"end_time\":5.0}]";
//     Assert.assertEquals(expected, toJson(shots));
//   }
  
  // Helper function that converts ArrayList of Shot objects to a json object
  private String toJson(ArrayList<Shot> shots) {
    Gson gson = new Gson();
    return gson.toJson(shots);
  }
}