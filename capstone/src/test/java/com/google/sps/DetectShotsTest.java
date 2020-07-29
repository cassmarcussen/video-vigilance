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
  private MockDetectShots mockDetectShots;
  private ArrayList<Shot> expectedShots;
  private VideoAnnotationResults results;                 // Shots returned for a video from API
  private VideoAnnotationResults.Builder resultsBuilder;  // Variable used to create a VideoAnnotationResults 
  private List<VideoAnnotationResults> resultsList;       // List of results for all videos requested (will only contain 1)
  
  // Local subclass of DetectShots that makes getAnnotationResults() public so I can stub it
  class MockDetectShots extends DetectShots {

    @Override 
    public List<VideoAnnotationResults> getAnnotationResults(String uri) throws Exception {
      return new ArrayList<VideoAnnotationResults>();
    }
  }

  @Before
  public void setup() throws Exception {
    detectShots = new DetectShots();
    mockDetectShots = mock(MockDetectShots.class);
    expectedShots = new ArrayList<Shot>();
    results = VideoAnnotationResults.getDefaultInstance();
    resultsBuilder = results.toBuilder();
    resultsList = new ArrayList<VideoAnnotationResults>();

    // Specify which functions of mockDetectShots to stub
    when(mockDetectShots.getAnnotationResults(any(String.class))).thenReturn(resultsList);
    when(mockDetectShots.detect(any(String.class))).thenCallRealMethod();
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
  @Test
  public void noShotsReturned() throws Exception {
    resultsList.add(results);
    ArrayList<Shot> shots = mockDetectShots.detect("gs://empty-bucket-for-tests");
    Assert.assertEquals(0, shots.size());
  }

  // Test when API returns 1 shot
  @Test
  public void oneShotReturned() throws Exception {
    // Create 1 shot and add to resultsList  
    VideoSegment.Builder segmentBuilder = addShot((long)1.0, (long)4.0);
    resultsBuilder.addShotAnnotations(segmentBuilder);
    results = resultsBuilder.build();
    resultsList.add(results);
    
    ArrayList<Shot> shots = mockDetectShots.detect("gs://empty-bucket-for-tests");
    
    // Expected list of shots
    Shot shot = new Shot(1, 4);
    expectedShots.add(shot);
    
    Assert.assertEquals(toJson(expectedShots), toJson(shots));
  }

  // Test when API returns 1 shot with nanoseconds
  @Test
  public void oneShotReturnedWithNanos() throws Exception {
    // Create 1 shot and add to resultsList  
    VideoSegment.Builder segmentBuilder = addShotWithNanos((long)0, (int)154e6, (long)2, (int)2122e5);
    resultsBuilder.addShotAnnotations(segmentBuilder);
    results = resultsBuilder.build();
    resultsList.add(results);
    
    ArrayList<Shot> shots = mockDetectShots.detect("gs://empty-bucket-for-tests");
    
    // Expected list of shots
    Shot shot = new Shot(0.154, 2.2122);
    expectedShots.add(shot);
    
    Assert.assertEquals(toJson(expectedShots), toJson(shots));
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

  // Helper method to create a VideoSegment.Builder to be returned by mocked API call
  private VideoSegment.Builder addShot(long startTimeOffset, long endTimeOffset) {
    // Create shot segment
    VideoSegment segment = VideoSegment.getDefaultInstance();
    VideoSegment.Builder segmentBuilder = segment.toBuilder();
    
    // Create start time and add to segment
    Duration startDuration = Duration.getDefaultInstance();
    Duration.Builder startDurationBuilder = startDuration.toBuilder();
    startDurationBuilder.setSeconds(startTimeOffset);
    segmentBuilder.setStartTimeOffset(startDurationBuilder);
    
    // Create end time and add to segment
    Duration endDuration = Duration.getDefaultInstance();
    Duration.Builder endDurationBuilder = endDuration.toBuilder();
    endDurationBuilder.setSeconds(endTimeOffset);
    segmentBuilder.setEndTimeOffset(endDurationBuilder);

    return segmentBuilder;
  }

  // Helper method to create a VideoSegment.Builder with nanosecond offsets
  private VideoSegment.Builder addShotWithNanos(long startTimeOffset, int startNanos, long endTimeOffset, int endNanos) {
    // Create shot segment
    VideoSegment segment = VideoSegment.getDefaultInstance();
    VideoSegment.Builder segmentBuilder = segment.toBuilder();
    
    // Create start time and add to segment
    Duration startDuration = Duration.getDefaultInstance();
    Duration.Builder startDurationBuilder = startDuration.toBuilder();
    startDurationBuilder.setSeconds(startTimeOffset);
    startDurationBuilder.setNanos(startNanos);
    segmentBuilder.setStartTimeOffset(startDurationBuilder);
    
    // Create end time and add to segment
    Duration endDuration = Duration.getDefaultInstance();
    Duration.Builder endDurationBuilder = endDuration.toBuilder();
    endDurationBuilder.setSeconds(endTimeOffset);
    endDurationBuilder.setNanos(endNanos);
    segmentBuilder.setEndTimeOffset(endDurationBuilder);

    return segmentBuilder;
  }

  // Helper function that converts ArrayList of Shot objects to a json object
  private String toJson(ArrayList<Shot> shots) {
    Gson gson = new Gson();
    return gson.toJson(shots);
  }
}