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
    VideoSegment.Builder segmentBuilder = createShot((long)1.0, -1, (long)4.0, -1);
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
    VideoSegment.Builder segmentBuilder = createShot((long)0, (int)154e6, (long)2, (int)2122e5);
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
  @Test
  public void multipleShotsReturned() throws Exception {
    VideoSegment.Builder segmentBuilder1 = createShot((long)0, -1, (long)5, (int)4512e5);
    resultsBuilder.addShotAnnotations(segmentBuilder1);
    VideoSegment.Builder segmentBuilder2 = createShot((long)5, (int)4512e5, (long)12, -1);
    resultsBuilder.addShotAnnotations(segmentBuilder2);
    VideoSegment.Builder segmentBuilder3 = createShot((long)12, -1, (long)13, (int)13e7);
    resultsBuilder.addShotAnnotations(segmentBuilder3);
    
    results = resultsBuilder.build();
    resultsList.add(results);
    
    ArrayList<Shot> shots = mockDetectShots.detect("gs://empty-bucket-for-tests");
    
    // Expected list of shots
    Shot shot1 = new Shot(0, 5.4512);
    Shot shot2 = new Shot(5.4512, 12);
    Shot shot3 = new Shot(12, 13.13);
    expectedShots.add(shot1);
    expectedShots.add(shot2);
    expectedShots.add(shot3);
    
    Assert.assertEquals(toJson(expectedShots), toJson(shots));
  }

  // Helper method to create a VideoSegment.Builder to be returned by mocked API call
  private VideoSegment.Builder createShot(long startTimeOffset, int startNanos, long endTimeOffset, int endNanos) {
    // Create shot segment
    VideoSegment segment = VideoSegment.getDefaultInstance();
    VideoSegment.Builder segmentBuilder = segment.toBuilder();
    
    // Create start time and add to segment
    Duration.Builder startDurationBuilder = createDuration(startTimeOffset, startNanos);
    segmentBuilder.setStartTimeOffset(startDurationBuilder);
    
    // Create end time and add to segment
    Duration.Builder endDurationBuilder = createDuration(endTimeOffset, endNanos);
    segmentBuilder.setEndTimeOffset(endDurationBuilder);

    return segmentBuilder;
  }
  
  // Helper method to create a Duration.Builder (to add to a VideoSegment.Builder)
  private Duration.Builder createDuration(long time, int nanos) {
    Duration duration = Duration.getDefaultInstance();
    Duration.Builder durationBuilder = duration.toBuilder();
    durationBuilder.setSeconds(time);

    if (nanos != -1) {
      durationBuilder.setNanos(nanos);
    }
    return durationBuilder;
  }

  // Helper function that converts ArrayList of Shot objects to a json object
  private String toJson(ArrayList<Shot> shots) {
    Gson gson = new Gson();
    return gson.toJson(shots);
  }
}