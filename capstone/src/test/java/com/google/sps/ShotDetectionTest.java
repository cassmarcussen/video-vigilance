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
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public final class ShotDetectionTest {

  private DetectShots detectShots = new DetectShots();

  @Test (expected = Exception.class)
  public void incorrectBucketPathFormat() throws Exception {
    detectShots.detect("gs:/video-vigilance-videos");
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

//   @Test
//   public void emptyBucket() throws Exception {
//     ArrayList<Shot> shots = detectShots.detect("gs://empty-bucket-for-tests");
//     Assert.assertEquals(0, shots.size());
//   }

//   @Test
//   public void videoWithOneShot() {
    
//   }

//   @Test
//   public void videoWithMultipleShots() {

//   }
}