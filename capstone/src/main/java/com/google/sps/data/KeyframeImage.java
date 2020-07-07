package com.google.sps.servlets;

/* The KeyframeImage class holds information about each keyframe image extracted from the video advertisement, including the 
Google Cloud Bucket URL of the image, the timestamp of the image, and the start and end time of the shot that the image is from. 
This class implements Comparable, so that it can have a compareTo method which can be used for sorting the KeyframeImages by timestamp.
*/
public class KeyframeImage implements Comparable<KeyframeImage>{

    private String url;
    //will these be Strings?
    private String timestamp;
    private String startTime;
    private String endTime;
    private String effect;

    public KeyframeImage(String myUrl, String myTimestamp, String myStartTime, String myEndTime) {
        url = myUrl;
        timestamp = myTimestamp;
        startTime = myStartTime;
        endTime = myEndTime;
        effect = "";
    }

    public String getUrl() {
        return url;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public String getStartTime() {
        return startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public String getEffect() {
        return effect;
    }

    public void setEffect(String newEffect) {
        effect = newEffect;
    }

    // compareTo for KeyframeImage, where images with earlier timeframes are considered less than images with later timeframes
    @Override
    public int compareTo(KeyframeImage o){ 
        return this.timestamp.compareTo(o.getTimestamp());
    }

}