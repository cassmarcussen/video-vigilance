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

/* The DetectSafeSearchGcs class handles the user flow component of extracting the effect from a keyframe image. 
It calls the Cloud Vision API's SafeSearch method to get the effect of the image, in terms of the parameters 
of 'adult', 'medical', 'spoofed', 'violence', and 'racy'.
GCS stands for 'Google Cloud Storage'. The reason GCS is included in the name of the class and method is because 
the keyframe images whose effect is extracted using this class and its methods must be stored in a Google Cloud Storage 
bucket.
*/
public class DetectSafeSearchGcs {

  /* Detects whether the specified image on Google Cloud Storage has features you would want to moderate. */
  public HashMap<String, String> detectSafeSearchGcs(String gcsPath) throws IOException {

   // String gcsUrl = "gs://keyframe-images-for-effect/nyc.jpg";
    ImageSource imageSource = ImageSource.newBuilder().setGcsImageUri(gcsPath).build();
    Image image = Image.newBuilder().setSource(imageSource).build();
    Feature safeSearchDetectionFeature = Feature.newBuilder().setType(Type.SAFE_SEARCH_DETECTION).build();
   
    List<AnnotateImageRequest> requests = new ArrayList<>();
    AnnotateImageRequest request =
        AnnotateImageRequest.newBuilder().addFeatures(safeSearchDetectionFeature).setImage(image).build();
    requests.add(request);

    // HashMap with the name and effect of each SafeSearch category. Declare before the try because returned outside of the try.
    HashMap<String, String> safeSearchResults = new HashMap<String, String>();

    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests. After completing all of your requests, call
    // the "close" method on the client to safely clean up any remaining background resources.
    try (ImageAnnotatorClient client = ImageAnnotatorClient.create()) {
      BatchAnnotateImagesResponse response = client.batchAnnotateImages(requests);
      List<AnnotateImageResponse> responses = response.getResponsesList();

      for (AnnotateImageResponse res : responses) {
        if (res.hasError()) {
          safeSearchResults.put("adult", "UNKNOWN");
          safeSearchResults.put("medical", "UNKNOWN");
          safeSearchResults.put("spoofed", "UNKNOWN");
          safeSearchResults.put("violence", "UNKNOWN");
          safeSearchResults.put("racy", "UNKNOWN");
          break;
        }

        // For full list of available annotations, see http://g.co/cloud/vision/docs
        SafeSearchAnnotation annotation = res.getSafeSearchAnnotation();

        safeSearchResults.put("adult", annotation.getAdult().toString());
        safeSearchResults.put("medical", annotation.getMedical().toString());
        safeSearchResults.put("spoofed", annotation.getSpoof().toString());
        safeSearchResults.put("violence", annotation.getViolence().toString());
        safeSearchResults.put("racy", annotation.getRacy().toString());  
      }

      client.close();
    }

    return safeSearchResults;
  }
}