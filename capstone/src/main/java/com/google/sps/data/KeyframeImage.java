package com.google.sps.servlets;

/* The KeyframeImage class holds information about each keyframe image extracted from the video advertisement, including the 
Google Cloud Bucket URL of the image, the timestamp of the image, the start and end time of the shot that the image is from, and the 
effect of the image, which is filled in from the results of the Vision API on the keyframe image. 
This class implements Comparable, so that it can have a compareTo method which can be used for sorting the KeyframeImages by timestamp.
*/
public class KeyframeImage implements Comparable<KeyframeImage>{

    private String cloudBucketUrl;
    //will these be Strings?
    private String timestamp;
    private String startTime;
    private String endTime;
    private String safeSearchEffect;

    public KeyframeImage(String myUrl, String myTimestamp, String myStartTime, String myEndTime) {
        cloudBucketUrl = myUrl;
        timestamp = myTimestamp;
        startTime = myStartTime;
        endTime = myEndTime;
        safeSearchEffect = "";
    }

    public String getUrl() {
        return cloudBucketUrl;
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
        return safeSearchEffect;
    }

    public void setEffect(String newEffect) {
        safeSearchEffect = newEffect;
    }

    /* compareTo for KeyframeImage, where images with earlier timeframes are considered less than images with later timeframes.
    We consider images with earlier timeframes less since this is used to sort Keyframe Images to display on the Results page. 
    We want the Keyframe Images to be displayed in chronological order corresponding to their timestamp in the video ad.
    */
    @Override
    public int compareTo(KeyframeImage o){ 
        return timestamp.compareTo(o.getTimestamp());
    }

}