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

package com.google.sps;

import static com.google.appengine.api.datastore.FetchOptions.Builder.withLimit;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.FetchOptions.Builder;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.sps.data.VideoUpload;

import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/** Datastore tests for uploading the video (testing code in VideoUploadServlet.java, VideoUpload.java)
  * Documentation: https://cloud.google.com/appengine/docs/standard/java/tools/localunittesting?csw=1#datastore-memcache
  */
public class VideoUploadTest {
  
  // Configures the local datastore service to keep all data in memory
  private final LocalServiceTestHelper localServiceTestHelper =
      new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());
  private Query query;
  private VideoUpload videoUpload;
  private DatastoreService dataService ;

  // Set up and tear down a local, executable environment before and after each test
  @Before
  public void setUp() {
    localServiceTestHelper.setUp();
    videoUpload = new VideoUpload();
    dataService = DatastoreServiceFactory.getDatastoreService();
  }
  @After
  public void tearDown() {
    localServiceTestHelper.tearDown();
  }

  // Posting null url
  @Test
  public void testPostUrl_nullUrl() {
    videoUpload.postUrl(dataService, null, "Video");

    // Null url should not be posted
    testDataServiceResults(dataService, 0);
  }

  // Posting empty url
  @Test
  public void testPostUrl_emptyUrl() {    
    videoUpload.postUrl(dataService, "", "Video");

    // Empty url should not be posted
    testDataServiceResults(dataService, 0);
  }
  
  // Posting 1 entity 
  @Test
  public void testPostUrl_oneEntity() {
    String testUrl = "fake.url";
    videoUpload.postUrl(dataService, testUrl, "Video");
    
    testDataServiceResults(dataService, 1);
  }

  // Posting multiple entities
  @Test
  public void testPostUrl_multipleEntities() {
    videoUpload.postUrl(dataService, "fake.url.1", "Video");
    videoUpload.postUrl(dataService, "fake.url.2", "Video");
    videoUpload.postUrl(dataService, "fake.url.3", "Video");

    testDataServiceResults(dataService, 3);
  }

  // Getting from emtpy datastore
  @Test
  public void testGetUrl_noEntities() {
    // Expects "error" attribute in json object to be filled
    String url = "";
    String error = "No videos uploaded to Datastore";
    Map<String, String> expected = createMap(url, error);

    Map<String, String> actual = videoUpload.getUrl(dataService, "Video");
    Assert.assertEquals(expected, actual);
  }

  // Getting from datastore with 1 entity 
  @Test
  public void testGetUrl_oneEntity() {
    // Add entity to datastore
    Entity entity = createEntity("fake.url", 1);
    dataService.put(entity);

    // Expects correct url to be returned
    String url = "fake.url";
    String error = "";
    Map<String, String> expected = createMap(url, error);
      
    Map<String, String> actual = videoUpload.getUrl(dataService, "Video");
    Assert.assertEquals(expected, actual);
  }

  // Getting from datastore with multiple entities
  @Test
  public void testGetUrl_multipleEntities() {
    // Add entities to datastore
    Entity entity1 = createEntity("fake.url.1", 1);
    Entity entity2 = createEntity("fake.url.2", 2);
    Entity entity3 = createEntity("fake.url.3", 3);
    
    dataService.put(entity1);
    dataService.put(entity2);
    dataService.put(entity3);
    
    // Expects most recent url to be returned
    String url = "fake.url.3";
    String error = "";
    Map<String, String> expected = createMap(url, error);
      
    Map<String, String> actual = videoUpload.getUrl(dataService, "Video");
    Assert.assertEquals(expected, actual);
  }

  // Getting from datastore with multiple entities with the same timestamp
  @Test
  public void testGetUrl_sameTimestamps() {
    // Add entities to datastore
    Entity entity1 = createEntity("fake.url.1", 1);
    Entity entity2 = createEntity("fake.url.2", 1);
    Entity entity3 = createEntity("fake.url.3", 1);
    
    dataService.put(entity1);
    dataService.put(entity2);
    dataService.put(entity3);

    // Expect first one put in datastore to be returned
    String url = "fake.url.1";
    String error = "";
    Map<String, String> expected = createMap(url, error);
      
    Map<String, String> actual = videoUpload.getUrl(dataService, "Video");
    Assert.assertEquals(expected, actual);
  }
  
  // Posting and getting null entity
  @Test
  public void testPostGetUrl_nullEntity() {
    videoUpload.postUrl(dataService, null, "Video");
    
    // Expects "error" attribute in json object to be filled
    String error = "No videos uploaded to Datastore";
    String url = "";
    Map<String, String> expected = createMap(url, error);

    Map<String, String> actual = videoUpload.getUrl(dataService, "Video");
    Assert.assertEquals(expected, actual);
  }

   // Posting and getting one entity
  @Test
  public void testPostGetUrl_oneEntity() {
    String testUrl = "fake.url";
    videoUpload.postUrl(dataService, testUrl, "Video");
    
    // Expects correct url to be returned
    String error = "";
    Map<String, String> expected = createMap(testUrl, error);
      
    Map<String, String> actual = videoUpload.getUrl(dataService, "Video");
    Assert.assertEquals(expected, actual);
  }

  // Posting and getting multiple entities
  @Test
  public void testPostGetUrl_multipleEntities() {
    videoUpload.postUrl(dataService, "fake.url.1", "Video");
    videoUpload.postUrl(dataService, "fake.url.2", "Video");
    videoUpload.postUrl(dataService, "fake.url.3", "Video");
    videoUpload.postUrl(dataService, "", "Video");

    // Expects most recent (valid) url to be returned
    String error = "";
    Map<String, String> expected = createMap("fake.url.3", error);
    
    Map<String, String> actual = videoUpload.getUrl(dataService, "Video");
    Assert.assertEquals(expected, actual);
  }
  
  // Helper method to test number of results
  private void testDataServiceResults(DatastoreService dataService, int expectedResults) {
    query = new Query("Video");
    PreparedQuery results = dataService.prepare(query);
    Assert.assertEquals(expectedResults, results.countEntities(withLimit(10)));
  }
  
  // Helper method to create new entity
  private Entity createEntity(String url, int timestamp) {
    Entity entity = new Entity("Video");
    entity.setProperty("url", url);
    entity.setProperty("timestamp", timestamp);
    return entity;
  }

  // Helper method to create expected HashMap result of getUrl()
  private Map<String, String> createMap(String url, String error) {
    Map<String, String> urlErrorMap = new HashMap<String, String>();
    urlErrorMap.put("url", url);
    urlErrorMap.put("error", error);
    return urlErrorMap;
  }
}