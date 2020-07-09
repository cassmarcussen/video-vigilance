package com.google.sps.servlets;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpServletRequest;

import java.io.IOException;

import java.time.LocalDateTime;

/*
KeyframeImageMockitoController creates mock http request and response objects, 
which it adds parameters to and passes to the uploadServlet or deleteServlet's 
GET and POST methods. Due to Blobstore's restrictions about where POST methods are called from, 
we expect the POST method to throw an exception. We also expect exceptions from GET and DELETE.
*/
@Controller
public class KeyframeImageMockitoController {

    /* Due to Blobstore-specific setup, we expect the exception: 
    java.lang.IllegalStateException: Must be called from a blob upload callback request. */
    @RequestMapping(value = "/keyframe-image-upload", method = RequestMethod.POST)
    public 
    @ResponseBody
    String post(
        @RequestParam("image") String image,
        @RequestParam("timestamp") String timestamp,
        @RequestParam("startTime") String startTime,
        @RequestParam("endTime") String endTime
        ) throws IOException {
        
        KeyframeImageUploadServlet uploadServlet = new KeyframeImageUploadServlet();
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        request.addParameter("image", image);
        request.addParameter("timestamp", timestamp);
        request.addParameter("startTime", startTime);
        request.addParameter("endTime", endTime);

        try {
            uploadServlet.doPost(request, response);
        } catch (Exception e) {
            System.err.println("exception: " + e);
        }

        System.out.println("POSTING...");
        return "post...";
    }

    /* We expect the exception:  java.lang.NullPointerException: No API environment is registered for this thread. */
    @RequestMapping(value = "/keyframe-image-upload", method = RequestMethod.GET)
    public 
    @ResponseBody
    String get() throws IOException {

        KeyframeImageUploadServlet uploadServlet = new KeyframeImageUploadServlet();
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        try {
            uploadServlet.doGet(request, response);
        } catch (Exception e) {
            System.err.println("exception: " + e);
        }

        System.out.println("GET...");
        return "get...";
    }

   /* We expect the exception:  java.lang.NullPointerException: No API environment is registered for this thread. */
    @RequestMapping(value = "/keyframe-image-delete", method = RequestMethod.POST)
    public 
    @ResponseBody
    String post() throws IOException {
        
        KeyframeImageDeleteServlet deleteServlet = new KeyframeImageDeleteServlet();
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        try {
            deleteServlet.doPost(request, response);
        } catch (Exception e) {
            System.err.println("exception: " + e);
        }

        System.out.println("DELETE...");
        return "delete...";
    }

}