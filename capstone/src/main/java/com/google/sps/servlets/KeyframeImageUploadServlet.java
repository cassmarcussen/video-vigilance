package com.google.sps.servlets;

import com.google.gson.Gson;
import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;

/** Servlet that returns some example content. TODO: modify this file to handle comments data */
@WebServlet("/keyframe-image-upload")
public class KeyframeImageUploadServlet extends HttpServlet {

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

    Gson gson = new Gson();

    ArrayList<KeyframeImage> keyframeImagesFromVideo = new ArrayList<KeyframeImage>();

    response.setContentType("application/json;");
    response.getWriter().println(gson.toJson(keyframeImagesFromVideo));
  }
}