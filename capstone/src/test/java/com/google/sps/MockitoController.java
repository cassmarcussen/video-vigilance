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

@Controller
public class MockitoController {

    @RequestMapping(value = "/keyframe-image-upload-test", method = RequestMethod.POST)
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

    @RequestMapping(value = "/keyframe-image-upload-test", method = RequestMethod.GET)
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

}