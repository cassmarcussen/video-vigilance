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

package com.google.sps.servlets;

import com.google.api.gax.paging.Page;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Bucket;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.google.appengine.tools.cloudstorage.GcsFilename;
import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

/** Servlet responsible for deleting keyframe images. This will be called at the beginning of the user flow, to ensure 
that the DataStore list and the Google Cloud Bucket are emptied of any previous data.*/
@WebServlet("/keyframe-image-delete")
public class KeyframeImageDeleteServlet extends HttpServlet {

  /* doPost deletes all of the objects from the DataStore list and Google Cloud Bucket associated with the 
  keyframe images for the video, by calling helper functions for each of these tasks. Then, it redirects to the index page.
  */
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
      
    ArrayList<String> listOfUrlsOfBlobsToDelete = deleteDataStoreInfo();
    
    deleteGoogleCloudBucketInfo(listOfUrlsOfBlobsToDelete);

    // Redirect back to the HTML page.
    response.sendRedirect("/index.html");

  }

  /* Deletes all objects from the DataStore list associated with the keyframe images for the video.
    Returns an ArrayList of urls of the keyframe images to delete from the corresponding Google Cloud Storage Bucket.
  */
  private ArrayList<String> deleteDataStoreInfo() {
    
    // queryType defines the DataStore list that we should reference to access the keyframe images stored
    final String queryType = "KeyframeImages_Video";

    Query query = new Query(queryType);

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    PreparedQuery results = datastore.prepare(query);

    ArrayList<String> listOfUrlsOfBlobsToDelete = new ArrayList<String>();
    for (Entity entity : results.asIterable()) {
       listOfUrlsOfBlobsToDelete.add((String) entity.getProperty("url"));
       System.out.println((String) entity.getProperty("url"));
       datastore.delete(entity.getKey());
    }

    return listOfUrlsOfBlobsToDelete;

  }

  /* Deletes all objects from the Google Cloud Bucket associated with the keyframe images for the video.
  Reference: https://cloud.google.com/storage/docs/deleting-objects#storage-delete-object-java 
  */
  private void deleteGoogleCloudBucketInfo(ArrayList<String> listOfUrlsOfBlobsToDelete) {
    // The ID of your GCP project
    final String projectId = "video-vigilance";

    // The ID of your GCS bucket
    final String bucketName = "keyframe-images-to-effect";

    // Lists the objects in the Google Cloud Bucket associated with the keyframe images, 
    // so that they can be deleted from the Bukcet in deleteGoogleCloudBucketInfo.
    Storage storage = StorageOptions.newBuilder().setProjectId(projectId).build().getService();
    Bucket bucket = storage.get(bucketName);

    // Delete only those objects in the bucket whose urls are associated with the DataStore entries, 
    // where these urls are passed in as an ArrayList as a parameter
    for (String url : listOfUrlsOfBlobsToDelete) {
        storage.delete(bucketName, url);
    }

  }
}