package com.google.sps.servlets;

import com.google.cloud.vision.v1.AnnotateImageRequest;
import com.google.cloud.vision.v1.AnnotateImageResponse;
import com.google.cloud.vision.v1.BatchAnnotateImagesResponse;
import com.google.cloud.vision.v1.Feature;
import com.google.cloud.vision.v1.Feature.Type;
import com.google.cloud.vision.v1.Image;
import com.google.cloud.vision.v1.ImageAnnotatorClient;
import com.google.cloud.vision.v1.ImageSource;
import com.google.cloud.vision.v1.SafeSearchAnnotation;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;

public class DetectSafeSearchGcs {

  public static HashMap<String, String> detectSafeSearchGcs(KeyframeImage image) throws IOException {
    String filePath = image.getUrl();
    return detectSafeSearchGcs(filePath);
  }

  // Detects whether the specified image on Google Cloud Storage has features you would want to
  // moderate.
  public static HashMap<String, String> detectSafeSearchGcs(String gcsPath) throws IOException {

    System.out.println("minor change to see if push works");
    HashMap<String, String> safeSearchResults = new HashMap<String, String>();
    List<AnnotateImageRequest> requests = new ArrayList<>();

    String gcsUrl = "gs://keyframe-images-for-effect/nyc.jpg";
    ImageSource imgSource = ImageSource.newBuilder().setGcsImageUri(gcsUrl).build();
    Image img = Image.newBuilder().setSource(imgSource).build();
    Feature feat = Feature.newBuilder().setType(Type.SAFE_SEARCH_DETECTION).build();
    AnnotateImageRequest request =
        AnnotateImageRequest.newBuilder().addFeatures(feat).setImage(img).build();
    requests.add(request);

   // String outputResult;

    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests. After completing all of your requests, call
    // the "close" method on the client to safely clean up any remaining background resources.
    try (ImageAnnotatorClient client = ImageAnnotatorClient.create()) {
      BatchAnnotateImagesResponse response = client.batchAnnotateImages(requests);
      List<AnnotateImageResponse> responses = response.getResponsesList();

      for (AnnotateImageResponse res : responses) {
        if (res.hasError()) {
          System.out.format("Error: %s%n", res.getError().getMessage());
          safeSearchResults.put("adult", res.getError().getMessage());
          safeSearchResults.put("medical", "UNKNOWN");
          safeSearchResults.put("spoofed", "UNKNOWN");
          safeSearchResults.put("violence", "UNKNOWN");
          safeSearchResults.put("racy", "UNKNOWN");
          return safeSearchResults;
        }

        // For full list of available annotations, see http://g.co/cloud/vision/docs
        SafeSearchAnnotation annotation = res.getSafeSearchAnnotation();
      /*  outputResult = String.format(
            "adult: %s%nmedical: %s%nspoofed: %s%nviolence: %s%nracy: %s%n",
            annotation.getAdult(),
            annotation.getMedical(),
            annotation.getSpoof(),
            annotation.getViolence(),
            annotation.getRacy());*/

        safeSearchResults.put("adult", annotation.getAdult().toString());
        safeSearchResults.put("medical", annotation.getMedical().toString());
        safeSearchResults.put("spoofed", annotation.getSpoof().toString());
        safeSearchResults.put("violence", annotation.getViolence().toString());
        safeSearchResults.put("racy", annotation.getRacy().toString());
        
      }
    }

    //return outputResult;

   /* safeSearchResults.put("adult", "VERY_LIKELY");
    safeSearchResults.put("medical", "VERY_UNLIKELY");
    safeSearchResults.put("spoofed", "UNLIKELY");
    safeSearchResults.put("violence", "POSSIBLE");
    safeSearchResults.put("racy", "LIKELY");*/

    return safeSearchResults;
    //return gcsPath + " adult: medical: spoofed: violence: racy: ";
    //return "adult: medical: spoofed: violence: racy: ";
  }
}