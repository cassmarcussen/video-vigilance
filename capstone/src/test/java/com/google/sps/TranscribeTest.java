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
import com.google.cloud.videointelligence.v1.SpeechRecognitionAlternative;
import com.google.cloud.videointelligence.v1.SpeechTranscription;
import com.google.cloud.videointelligence.v1.SpeechTranscriptionConfig;
import com.google.cloud.videointelligence.v1.VideoIntelligenceServiceClient;
import com.google.cloud.videointelligence.v1.VideoAnnotationResults;
import com.google.cloud.videointelligence.v1.VideoAnnotationResults.Builder;
import com.google.cloud.videointelligence.v1.VideoSegment;
import com.google.cloud.videointelligence.v1.WordInfo;
import com.google.protobuf.Duration;

import com.google.gson.Gson;
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
  private VideoAnnotationResults result;
  private VideoAnnotationResults.Builder resultBuilder;
  private List<VideoAnnotationResults> resultsList; 
  
  /** 
   * Local subclass of Transcribe that makes getAnnotationResult() public so I can stub it.
   * We extend Transcribe (for the purpose of overriding transcribeAudio()).
   */
  class MockTranscribe extends Transcribe {
    @Override
    public List<VideoAnnotationResults> getAnnotationResult(String gcsUri) throws Exception {
      return new ArrayList<VideoAnnotationResults>(); 
    }
  }

  @Before
  public void setUp() throws Exception {
    transcribe = new Transcribe();
    mockTranscribe = mock(MockTranscribe.class);
    result = VideoAnnotationResults.getDefaultInstance();
    resultBuilder = result.toBuilder();
    resultsList = new ArrayList<VideoAnnotationResults>();
    when(mockTranscribe.getAnnotationResult(any(String.class))).thenReturn(resultsList);
    when(mockTranscribe.transcribeAudio(any(String.class))).thenCallRealMethod();
    when(mockTranscribe.formatConfidence(any(VideoAnnotationResults.class), any(Double.class))).thenCallRealMethod();
  }

  // Passes
  @Test
  public void testTranscribeAudio_EmptyTranscriptionAndConfidenceReturned() {
    // TEST: Passing in a correctly formatted path, this test returns an empty transcription and confidence level.
    SpeechTranscription.Builder transcriptionResults = createSpeechTranscription("", (float)0.0);
    resultBuilder.addSpeechTranscriptions(transcriptionResults);
    result = resultBuilder.build();
    resultsList.add(result);
    
    HashMap<String, String> expected = new HashMap<String, String>();
    expected.put("transcription", "");
    expected.put("confidence", "0");

    HashMap<String, String> actual = mockTranscribe.transcribeAudio("gs://video-vigilance-videos/youtube_ad_test.mp4");
    
    Assert.assertEquals(toJson(expected), toJson(actual));
  }

  // Passes
  @Test
  public void testTranscribeAudio_TranscriptionAndConfidenceReturned() {
    // TEST: Passing in a correctly formatted path, this test returns a transcription and formatted confidence level.
    SpeechTranscription.Builder transcriptionResults = createSpeechTranscription("I am a fake transcription for testing purposes.", (float)0.85);
    resultBuilder.addSpeechTranscriptions(transcriptionResults);
    result = resultBuilder.build();
    resultsList.add(result);
   
    HashMap<String, String> expected = new HashMap<String, String>();
    expected.put("transcription", "I am a fake transcription for testing purposes.");
    expected.put("confidence", "85");

    HashMap<String, String> actual = mockTranscribe.transcribeAudio("gs://video-vigilance-videos/youtube_ad_test.mp4");
    Assert.assertEquals(toJson(expected), toJson(actual));
  }

  /** 
   * Helper method to create a SpeechTranscription.Builder to be returned by mocked API call.
   */
  private SpeechTranscription.Builder createSpeechTranscription(String transcript, float confidence) {
    // Create speech transcription segment.
    SpeechTranscription speechTranscription = SpeechTranscription.getDefaultInstance();
    SpeechTranscription.Builder speechTranscriptionBuilder = speechTranscription.toBuilder();
    // Set language code.
    speechTranscriptionBuilder.setLanguageCode("en-US");
    // Create transcription alternative and add to speech transcription segment.
    SpeechRecognitionAlternative.Builder alternativeBuilder = createAlternative(transcript, confidence);
    speechTranscriptionBuilder.addAlternatives(alternativeBuilder);

    return speechTranscriptionBuilder;
  }

  /**
   * Helper method to create a List of SpeechTranscriptionAlternative.Builder (to add to SpeechTranscription.Builder).
   */
  private SpeechRecognitionAlternative.Builder createAlternative(String transcript, float confidence) {
    // Create one SpeechRecognitionAlternative.Builder.
    SpeechRecognitionAlternative alternative = SpeechRecognitionAlternative.getDefaultInstance();
    SpeechRecognitionAlternative.Builder alternativeBuilder = alternative.toBuilder();
    alternativeBuilder.setTranscript(transcript);
    alternativeBuilder.setConfidence(confidence);
    return alternativeBuilder;
  }

  /**
   * Helper function that converts HashMap to a json string.
   */
  private String toJson(HashMap<String, String> results) {
    Gson gson = new Gson();
    return gson.toJson(results);
  }
}