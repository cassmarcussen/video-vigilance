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

  // Set up and tear down a local, executable environment before and after each test
  @Before
  public void setUp() {
    localServiceTestHelper.setUp();
    query = new Query("Video");
    videoUpload = new VideoUpload();
  }
  @After
  public void tearDown() {
    localServiceTestHelper.tearDown();
  }

  // Testing local datastore service with no entities
  @Test
  public void test_noEntities() {
    DatastoreService dataService = DatastoreServiceFactory.getDatastoreService();
    PreparedQuery results = dataService.prepare(query);
    Assert.assertEquals(0, results.countEntities(withLimit(10)));
  }

  // Posting null url
  @Test
  public void testPostUrl_nullUrl() {
    DatastoreService dataService = DatastoreServiceFactory.getDatastoreService();
    
    videoUpload.postUrl(dataService, null, "Video");

    // Null url should not be posted
    PreparedQuery results = dataService.prepare(query);
    Assert.assertEquals(0, results.countEntities(withLimit(10)));
  }

  // Posting empty url
  @Test
  public void testPostUrl_emptyUrl() {
    DatastoreService dataService = DatastoreServiceFactory.getDatastoreService();
    
    videoUpload.postUrl(dataService, "", "Video");

    // Empty url should not be posted
    PreparedQuery results = dataService.prepare(query);
    Assert.assertEquals(0, results.countEntities(withLimit(10)));
  }
  
  // Posting 1 entity 
  @Test
  public void testPostUrl_oneEntity() {
    DatastoreService dataService = DatastoreServiceFactory.getDatastoreService();

    String testUrl = "fake.url";
    videoUpload.postUrl(dataService, testUrl, "Video");

    Assert.assertEquals(1, dataService.prepare(query).countEntities(withLimit(10)));
  }

  // Posting multiple entities
  @Test
  public void testPostUrl_multipleEntities() {
    DatastoreService dataService = DatastoreServiceFactory.getDatastoreService();

    videoUpload.postUrl(dataService, "fake.url.1", "Video");
    videoUpload.postUrl(dataService, "fake.url.2", "Video");
    videoUpload.postUrl(dataService, "fake.url.3", "Video");

    PreparedQuery results = dataService.prepare(query);
    
    Assert.assertEquals(3, results.countEntities(withLimit(10)));
  }

  // Getting from emtpy datastore
  @Test
  public void testGetUrl_noEntities() {
    DatastoreService dataService = DatastoreServiceFactory.getDatastoreService();
  
    // Expects "error" attribute in json object to be filled
    String error = "No videos uploaded to Datastore";
    String url = "";
    String expected = String.format("{\"error\": \"%s\", \"url\": \"%s\"}", error, url);
      
    String json = videoUpload.getUrl(dataService, "Video");
    Assert.assertEquals(expected, json);
  }

  // Getting from datastore with 1 entity 
  @Test
  public void testGetUrl_oneEntity() {
    DatastoreService dataService = DatastoreServiceFactory.getDatastoreService();
    
    // Add entity to datastore
    Entity entity = createEntity("fake.url", 1);
    dataService.put(entity);

    // Expects correct url to be returned
    String error = "";
    String url = "fake.url";
    String expected = String.format("{\"error\": \"%s\", \"url\": \"%s\"}", error, url);
      
    String json = videoUpload.getUrl(dataService, "Video");
    Assert.assertEquals(expected, json);
  }

  // Getting from datastore with multiple entities
  @Test
  public void testGetUrl_multipleEntities() {
    DatastoreService dataService = DatastoreServiceFactory.getDatastoreService();
    
    // Add entities to datastore
    Entity entity1 = createEntity("fake.url.1", 1);
    Entity entity2 = createEntity("fake.url.2", 2);
    Entity entity3 = createEntity("fake.url.3", 3);
    
    dataService.put(entity1);
    dataService.put(entity2);
    dataService.put(entity3);
    
    // Expects most recent url to be returned
    String error = "";
    String url = "fake.url.3";
    String expected = String.format("{\"error\": \"%s\", \"url\": \"%s\"}", error, url);
      
    String json = videoUpload.getUrl(dataService, "Video");
    Assert.assertEquals(expected, json);
  }

  // Getting from datastore with multiple entities with the same timestamp
  @Test
  public void testGetUrl_sameTimestamps() {
    DatastoreService dataService = DatastoreServiceFactory.getDatastoreService();
    
    // Add entities to datastore
    Entity entity1 = createEntity("fake.url.1", 1);
    Entity entity2 = createEntity("fake.url.2", 1);
    Entity entity3 = createEntity("fake.url.3", 1);
    
    dataService.put(entity1);
    dataService.put(entity2);
    dataService.put(entity3);

    // Expect first one put in datastore to be returned
    String error = "";
    String url = "fake.url.1";
    String expected = String.format("{\"error\": \"%s\", \"url\": \"%s\"}", error, url);
      
    String json = videoUpload.getUrl(dataService, "Video");
    Assert.assertEquals(expected, json);
  }
  
  // Posting and getting null entity
  @Test
  public void testPostGetUrl_nullEntity() {
    DatastoreService dataService = DatastoreServiceFactory.getDatastoreService();
    
    videoUpload.postUrl(dataService, null, "Video");
    
    // Expects "error" attribute in json object to be filled
    String error = "No videos uploaded to Datastore";
    String url = "";
    String expected = String.format("{\"error\": \"%s\", \"url\": \"%s\"}", error, url);

    String json = videoUpload.getUrl(dataService, "Video");
    Assert.assertEquals(expected, json);
  }

   // Posting and getting one entity
  @Test
  public void testPostGetUrl_oneEntity() {
    DatastoreService dataService = DatastoreServiceFactory.getDatastoreService();
   
    String testUrl = "fake.url";
    videoUpload.postUrl(dataService, testUrl, "Video");
    
    // Expects correct url to be returned
    String error = "";
    String expected = String.format("{\"error\": \"%s\", \"url\": \"%s\"}", error, testUrl);
      
    String json = videoUpload.getUrl(dataService, "Video");
    Assert.assertEquals(expected, json);
  }

  // Posting and getting multiple entities
  @Test
  public void testPostGetUrl_multipleEntities() {

  }
  
  // Helper method to create new entity
  private Entity createEntity(String url, int timestamp) {
    Entity entity = new Entity("Video");
    entity.setProperty("url", url);
    entity.setProperty("timestamp", timestamp);
    return entity;
  }
}