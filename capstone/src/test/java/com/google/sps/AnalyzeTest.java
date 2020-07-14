// // Copyright 2019 Google LLC
// //
// // Licensed under the Apache License, Version 2.0 (the "License");
// // you may not use this file except in compliance with the License.
// // You may obtain a copy of the License at
// //
// //     https://www.apache.org/licenses/LICENSE-2.0
// //
// // Unless required by applicable law or agreed to in writing, software
// // distributed under the License is distributed on an "AS IS" BASIS,
// // WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// // See the License for the specific language governing permissions and
// // limitations under the License.
// package com.google.sps;

// import com.google.sps.data.Analyze;

// import java.util.ArrayList;
// import java.util.Arrays;
// import java.util.Collection;
// import java.util.Collections;
// import java.util.HashMap;
// import java.util.List;

// import org.junit.Assert;
// import org.junit.Before;
// import org.junit.Test;
// import org.junit.runner.RunWith;
// import org.junit.runners.JUnit4;

// import static org.mockito.Mockito.*;
// import static org.mockito.Matchers.*;

// /** Unit Test class for Analyze*/
// @RunWith(JUnit4.class)
// public final class AnalyzeTest {

//   private Analyze analyze;
//   private Analyze mockAnalyze;

//   @Before
//   public void setUp() {
//     analyze = new Analyze();
//     mockAnalyze = mock(Analyze.class);
//   }

//   // Passes
//   @Test (expected = Exception.class)
//   public void incorrectBucketPathFormat() throws Exception {
//     // Test: Given an incorrectly formatted path to a GCS bucket, Exception should be caught
//     analyze.transcribeAudio("gs:/video-vigilance-videos");
//   }

//   // Passes 
//   @Test (expected = Exception.class)
//   public void incorrectBucketPathFormat2() throws Exception {
//     // Test: Given an incorrectly formatted path to a GCS bucket, Exception should be caught
//     analyze.transcribeAudio("gs:video-vigilance-videos");
//   }

//   // Passes
//   @Test (expected = Exception.class)
//   public void incorrectBucketPathFormat3() throws Exception {
//     // Test: Given an incorrectly formatted path to a GCS bucket, Exception should be caught
//     analyze.transcribeAudio("gs//video-vigilance-videos");
//   }

//   // Passes
//   @Test (expected = Exception.class)
//   public void nonexistentBucket() throws Exception {
//     // Test: Given a path to a GCS bucket that does not exist, Exception should be caught
//     analyze.transcribeAudio("gs://fake-bucket");
//   }
  
//   // Passes
//   @Test (expected = Exception.class)
//   public void incorrectFileFormat() throws Exception {
//     // Test: Given an path to an unsupported file type, Exception should be caught
//     analyze.transcribeAudio("gs://keyframe-images/download.png");
//   }

//   // Passes
//   @Test (expected = Exception.class)
//   public void noFileWithPath() throws Exception {
//     // Test: Given an correctly formatted path to a GCS bucket with no video file, Exception should be caught
//     analyze.transcribeAudio("gs://video-vigilance-videos/missing-video.mp4");
//   }
  
//   // Note: giving PermissionDeniedException
//   @Test
//   public void connectToAPI() throws Exception {
//     HashMap<String, String> expected = new HashMap<String, String>();
//     expected.put("transcription", "I am a fake transcription.");
//     expected.put("confidence", "85.5");

//     when(mockAnalyze.transcribeAudio(anyString())).thenReturn(expected);

//     HashMap<String, String> actual = mockAnalyze.transcribeAudio("gs://video-vigilance-videos/missing-video.mp4");
    
//     Assert.assertEquals(expected, actual);
//   }
// }