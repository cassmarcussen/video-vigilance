package com.google.sps.servlets;

/* The KeyframeImage class holds information about each keyframe image extracted from the video advertisement, including the 
Google Cloud Bucket URL of the image, the timestamp of the image, the start and end time of the shot that the image is from, and the 
effect of the image, which is filled in from the results of the Vision API on the keyframe image. 
This class implements Comparable, so that it can have a compareTo method which can be used for sorting the KeyframeImages by timestamp.
*/
public class KeyframeImage {

    private String cloudBucketUrl;
    // The following three int variables are times in number of seconds.
    private int timestamp;
    private int startTime;
    private int endTime;
    private String safeSearchEffect;

    public KeyframeImage(String myUrl, int myTimestamp, int myStartTime, int myEndTime) {
        cloudBucketUrl = myUrl;
        timestamp = myTimestamp;
        startTime = myStartTime;
        endTime = myEndTime;
        safeSearchEffect = "";
    }

    public String getUrl() {
        return cloudBucketUrl;
    }

    public int getTimestamp() {
        return timestamp;
    }

    public int getStartTime() {
        return startTime;
    }

    public int getEndTime() {
        return endTime;
    }

    public String getEffect() {
        return safeSearchEffect;
    }

    public void setEffect(String newEffect) {
        safeSearchEffect = newEffect;
    }
}