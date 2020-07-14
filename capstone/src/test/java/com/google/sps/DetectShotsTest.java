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

import com.google.gson.Gson;
import com.google.sps.data.DetectShots;
import com.google.sps.data.Shot;

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
  private DetectShots mockDetectShots;

  @Before
  public void setup() {
    detectShots = new DetectShots();
    mockDetectShots = mock(DetectShots.class);
  }

  @Test (expected = Exception.class)
  public void incorrectBucketPathFormat() throws Exception {
    detectShots.detect("gs:/video-vigilance-videos");
  }

  @Test (expected = Exception.class)
  public void incorrectBucketPathFormat2() throws Exception {
    detectShots.detect("gs:video-vigilance-videos");
  }

  @Test (expected = Exception.class)
  public void incorrectBucketPathFormat3() throws Exception {
    detectShots.detect("gs//video-vigilance-videos");
  }

  @Test (expected = Exception.class)
  public void nonexistentBucket() throws Exception {
    detectShots.detect("gs://fake-bucket");
  }

  @Test (expected = Exception.class)
  public void incorrectFileFormat() throws Exception {
    detectShots.detect("gs://keyframe-images/download.png");
  }

  @Test (expected = Exception.class)
  public void noFileWithPath() throws Exception {
    detectShots.detect("gs://video-vigilance-videos/missing-video.mp4");
  }
  
  private String toJson(ArrayList<Shot> shots) {
    Gson gson = new Gson();
    return gson.toJson(shots);
  }

  @Test
  public void noShotsReturned() throws Exception {
    ArrayList<Shot> shotslist = new ArrayList<Shot>();
    when(mockDetectShots.detect(any(String.class))).thenReturn(shotslist);
    
    ArrayList<Shot> shots = mockDetectShots.detect("gs://empty-bucket-for-tests");
    Assert.assertEquals("[]", toJson(shots));
  }

  @Test
  public void oneShotReturned() throws Exception {
    ArrayList<Shot> shotslist = new ArrayList<Shot>();
    Shot shot = new Shot(1, 4);
    shotslist.add(shot);

    when(mockDetectShots.detect(any(String.class))).thenReturn(shotslist);

    ArrayList<Shot> shots = mockDetectShots.detect("gs://empty-bucket-for-tests");

    String expected = "[{\"start_time\":1.0,\"end_time\":4.0}]";
    Assert.assertEquals(expected, toJson(shots));
  }

  @Test
  public void multipleShotsReturned() throws Exception {
    ArrayList<Shot> shotslist = new ArrayList<Shot>();
    Shot shot1 = new Shot(1, 2);
    Shot shot2 = new Shot(2, 4);
    Shot shot3 = new Shot(4, 5);
    shotslist.add(shot1);
    shotslist.add(shot2);
    shotslist.add(shot3);

    when(mockDetectShots.detect(any(String.class))).thenReturn(shotslist);

    ArrayList<Shot> shots = mockDetectShots.detect("gs://empty-bucket-for-tests");

    String expected = "[{\"start_time\":1.0,\"end_time\":2.0}," +
                       "{\"start_time\":2.0,\"end_time\":4.0}," +
                       "{\"start_time\":4.0,\"end_time\":5.0}]";
    Assert.assertEquals(expected, toJson(shots));
  }

}