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

import com.google.sps.data.Transcribe;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.mockito.Mockito.*;
import static org.mockito.Matchers.*;

/** Unit Test class for Transcribe*/
@RunWith(JUnit4.class)
public final class TranscribeTest {

  private Transcribe transcribe;
  private MockTranscribe mockTranscribe;
  
  /** 
   * Note: We do not extend Transcribe (which would be for the purpose of overriding transcribeAudio()) because 
   * transcribeAudio() for the Transcribe class is static, so it cannot be overridden. Instead, we 
   * create a MockTranscribe that does not extend any other class.
   */
  class MockTranscribe {
    public HashMap<String, String> transcribeAudio(String gcsUri) {
      return new HashMap<String, String>();
    }
  }

  @Before
  public void setUp() {
    transcribe = new Transcribe();
    mockTranscribe = mock(MockTranscribe.class);
  }

  // Passes
  @Test
  public void testTranscribeAudio_IncorrectPath_MissingOneSlash() {
    // TEST: The path being passed is incorrect because it is missing '/'. Correctly formatted paths begin with 'gs://". 
    HashMap<String, String> expected = new HashMap<String, String>();
    expected.put("error", "VI");
    when(mockTranscribe.transcribeAudio(anyString())).thenReturn(expected);
    
    HashMap<String, String> actual = mockTranscribe.transcribeAudio("gs:/video-vigilance-videos/youtube_ad_test.mp4");
    
    Assert.assertEquals(expected, actual);
  }

  // Passes
  @Test
  public void testTranscribeAudio_IncorrectPath_MissingTwoSlashes() {
    // TEST: The path being passed is incorrect because it is missing '//'. Correctly formatted paths begin with 'gs://".
    HashMap<String, String> expected = new HashMap<String, String>();
    expected.put("error", "VI");
    when(mockTranscribe.transcribeAudio(anyString())).thenReturn(expected);
    
    HashMap<String, String> actual = mockTranscribe.transcribeAudio("gs:video-vigilance-videos/youtube_ad_test.mp4");
    
    Assert.assertEquals(expected, actual);
  }

  // Passes
  @Test
  public void testTranscribeAudio_IncorrectPath_MissingColon() {
    // TEST: The path being passed is incorrect because it is missing a ':'. Correctly formatted paths begin with 'gs://'.
    HashMap<String, String> expected = new HashMap<String, String>();
    expected.put("error", "VI");
    when(mockTranscribe.transcribeAudio(anyString())).thenReturn(expected);
    
    HashMap<String, String> actual = mockTranscribe.transcribeAudio("gs//video-vigilance-videos/youtube_ad_test.mp4");
  
    Assert.assertEquals(expected, actual);
  }

  // Passes
  @Test
  public void testTranscribeAudio_IncorrectPath_FakeBucket() {
    // TEST: The path being passed is incorrect because the bucket does not exist.
    HashMap<String, String> expected = new HashMap<String, String>();
    expected.put("error", "VI");
    when(mockTranscribe.transcribeAudio(anyString())).thenReturn(expected);
    
    HashMap<String, String> actual = mockTranscribe.transcribeAudio("gs://fake-bucket/youtube_ad_test.mp4");
  
    Assert.assertEquals(expected, actual);
  }

  // Passes
  @Test
  public void testTranscribeAudio_IncorrectPath_MissingFileInBucket() {
    // TEST: The path being passed is incorrect because the path points to no file.
    HashMap<String, String> expected = new HashMap<String, String>();
    expected.put("error", "VI");
    when(mockTranscribe.transcribeAudio(anyString())).thenReturn(expected);
    
    HashMap<String, String> actual = mockTranscribe.transcribeAudio("gs://video-vigilance-videos/nonexistent_file.mp4");
  
    Assert.assertEquals(expected, actual);
  }

  // Passes
  @Test
  public void testTranscribeAudio_TranscriptionAndConfidenceReturned() {
    // TEST: Passing in a correctly formatted path, this test returns a transcription and confidence level.
    HashMap<String, String> expected = new HashMap<String, String>();
    expected.put("transcription", "");
    expected.put("confidence", "");
    when(mockTranscribe.transcribeAudio(anyString())).thenReturn(expected);
    
    HashMap<String, String> actual = mockTranscribe.transcribeAudio("gs://video-vigilance-videos/AAANsUnXaZfCIk7nKQ2HcvaftTjFiapEesmGff0_kkY7syRPO4EMxTJq2ESuMKW4Va6BPtBoAHGCT2i50XLbHB4NPz4gCkhYhQ.O0rB9S43kQFHL9xp");
    Assert.assertEquals(expected, actual);
  }
}