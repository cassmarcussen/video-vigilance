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

  @Test
  public void connectToAPI() throws Exception {
    ArrayList<Shot> shotslist = new ArrayList<Shot>();
    Shot shot = new Shot(1, 4);
    shotslist.add(shot);

    when(mockDetectShots.detect(any(String.class))).thenReturn(shotslist);

    ArrayList<Shot> shots = mockDetectShots.detect("gs://empty-bucket-for-tests");
    Assert.assertEquals(shotslist, shots);
  }
}