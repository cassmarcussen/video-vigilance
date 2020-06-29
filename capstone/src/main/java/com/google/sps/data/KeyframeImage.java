package com.google.sps.servlets;

public class KeyframeImage {

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

    //question: do we want setter methods?
}