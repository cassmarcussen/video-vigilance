package com.google.sps.servlets;

public class KeyframeImage {

    private String url;
    //will these be Strings?
    private String timestamp;
    private String startTime;
    private String endTime;

    public KeyframeImage(String myUrl, String myTimestamp, String myStartTime, String myEndTime) {
        url = myUrl;
        timestamp = myTimestamp;
        startTime = myStartTime;
        endTime = myEndTime;
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

    //question: do we want setter methods?
}