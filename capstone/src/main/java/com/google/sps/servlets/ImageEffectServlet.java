package com.google.sps.servlets;

import com.google.gson.Gson;
import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;

/** Servlet that returns the keyframe image's SafeSearch results by calling the DetectSafeSearchGcs class' detectsSafeSearchGcs method on 
the url of the image (as a Google Cloud Bucket url).*/
@WebServlet("/keyframe-effect-servlet")
public class ImageEffectServlet extends HttpServlet {

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

    Gson gson = new Gson();
    DetectSafeSearchGcs detectSafeSearchGcs = new DetectSafeSearchGcs();

    String imageUrl = request.getParameter("image_url");

    HashMap<String, String> effectDetectionResults = new HashMap<String, String>();
    try {
      effectDetectionResults = detectSafeSearchGcs.detectSafeSearchGcs(imageUrl);
    } catch (Exception e) {

    }

    response.setContentType("application/json;");
    response.getWriter().println(gson.toJson(effectDetectionResults));

  }
}