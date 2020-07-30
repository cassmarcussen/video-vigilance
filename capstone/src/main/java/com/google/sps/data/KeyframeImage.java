package com.google.sps.servlets;

import java.util.HashMap;

/* The KeyframeImage class holds information about each keyframe image extracted from the video advertisement, including the 
Google Cloud Bucket URL of the image, the timestamp of the image, the start and end time of the shot that the image is from, and the 
effect of the image, which is filled in from the results of the Vision API on the keyframe image. 
This class implements Comparable, so that it can have a compareTo method which can be used for sorting the KeyframeImages by timestamp.
*/
public class KeyframeImage implements Comparable<KeyframeImage>{

  private String cloudBucketUrl;
  // The following integer variables is a time in number of seconds.
  private int timestamp;
  private boolean isManuallyFlagged;
  private HashMap<String, String> safeSearchEffect;

  public KeyframeImage(String newUrl, int newTimestamp, boolean newIsManuallyFlagged) {
    cloudBucketUrl = newUrl;
    timestamp = newTimestamp;
    isManuallyFlagged = newIsManuallyFlagged;
  }

  public KeyframeImage(String newUrl, int newTimestamp, boolean newIsManuallyFlagged, HashMap<String, String> newSafeSearchEffect) {
    cloudBucketUrl = newUrl;
    timestamp = newTimestamp;
    isManuallyFlagged = newIsManuallyFlagged;
    safeSearchEffect = newSafeSearchEffect;
  }

  public String getUrl() {
    return cloudBucketUrl;
  }

  public int getTimestamp() {
    return timestamp;
  }

  public boolean getIsManuallyFlagged() {
    return isManuallyFlagged;
  }

  public HashMap<String, String> getEffect() {
    return safeSearchEffect;
  }

  // Implement compareTo for Keyframe Images because we want to return the keyframe images in the 
  // order of increasing timestamps. DataStore queries order timestamps alphabetically, prioritizing 
  // the value of the highest order digit, which does not return a numeric ordering which is accurate, 
  // so we must implement this method to compare the Keyframe Images.
  @Override
  public int compareTo(KeyframeImage other) {
    if (timestamp < other.timestamp) {
        return -1;
    } else if (timestamp == other.timestamp) {
        return 0;
    } else {
        return 1;
    }
  }
}