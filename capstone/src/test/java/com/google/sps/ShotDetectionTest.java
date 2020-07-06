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

/** */
@RunWith(JUnit4.class)
public final class ShotDetectionTest {

  private DetectShots detectShots;

  @Test
  public void incorrectBucketPathFormat() {
    // expects Exception to be thrown
    Assert.assertEquals(0, 0);
  }

  @Test
  public void incorrectVideoFormat() {
    // expects Exception to be thrown
  }

  @Test
  public void emptyBucket() {
    // expects empty list
  }

  @Test
  public void videoWithOneShot() {
    
  }

  @Test
  public void videoWithMultipleShots() {

  }
}