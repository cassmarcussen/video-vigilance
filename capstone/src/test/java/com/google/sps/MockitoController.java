package com.google.sps.servlets;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpServletRequest;

import java.io.IOException;

import java.time.LocalDateTime;

@Controller
public class MockitoController {

    private KeyframeImageUploadServlet uploadServlet;

    @RequestMapping(value = "/keyframe-image-upload", method = RequestMethod.POST)
    public String post() throws IOException {
        
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        request.addParameter("", "");
        request.addParameter("", "");
        request.addParameter("", "");
        request.addParameter("", "");

        uploadServlet.doPost(request, response);

        System.out.println("POSTING...");
        return "post...";
    }

    @RequestMapping(value = "/keyframe-image-upload", method = RequestMethod.GET)
    public String get() throws IOException {

        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        uploadServlet.doGet(request, response);

        System.out.println("GET...");
        return "get...";
    }

}