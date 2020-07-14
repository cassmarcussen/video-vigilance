// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.sps.data;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.FetchOptions.Builder;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;

/** A class that contains functions to get and post an uploaded video's url */

public class VideoUpload {
  
  public String getUrl(DatastoreService datastore) {

    // In case there's more than 1 Video stored, sort them starting from most recent
    Query query = new Query("Video").addSort("timestamp", SortDirection.DESCENDING);
    PreparedQuery results = datastore.prepare(query);

    int numVideos = results.countEntities(FetchOptions.Builder.withDefaults());
    String url = "";
    String error = "";
    String json = "";

    if (numVideos == 0) {
      // If there's no video stored in Datastore, print an error message 
      error = "No videos uploaded to Datastore";
    } else if (numVideos == 1) {
      // Set the url of the only video stored (asSingleEntity() retrieves the one and only result for the Query)
      Entity video = results.asSingleEntity();
      url = (String) video.getProperty("url");
    } else {
      // If there's more than 1 video stored in Datastore, return the url for the most recently added video 
      // Since the results are sorted by timestamp, just use the first one
      Entity video = results.asList(FetchOptions.Builder.withDefaults()).get(0);
      url = (String) video.getProperty("url");
    }
    json = String.format("{\"error\": %s, \"url\": %s}", error, url);
    return json;
  }

  public void postUrl(DatastoreService datastore, String url) {
    // Do not post if no file was selected
    if (url == null || url == "") {
      return;
    }

    // Create Entity to store in datastore with the url and current timestamp
    Entity entity = new Entity("Video");
    long timestamp = System.currentTimeMillis();
    entity.setProperty("url", url);
    entity.setProperty("timestamp", timestamp);

    datastore.put(entity);
  }
}
