package com.google.sps;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.time.LocalDateTime;

@Controller
public class MockitoController {

    @RequestMapping(value = "/keyframe-image-upload", method = RequestMethod.POST)
    public String post() {

        System.out.println("POSTING...");
        return "index";
    }

}