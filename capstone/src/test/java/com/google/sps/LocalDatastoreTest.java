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
public class LocalDatastoreTest {
  
  // Configures the local datastore service to keep all data in memory
  private final LocalServiceTestHelper helper =
      new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());

  // Set up and tear down a local, executable environment before and after each test
  @Before
  public void setUp() {
    helper.setUp();
  }
  @After
  public void tearDown() {
    helper.tearDown();
  }

  // Adds 1 Video entity to dataService param and returns its url
  private String addOneEntity(DatastoreService dataService) {
    Entity entity = new Entity("Video");
    String testUrl = "fake.url";
    entity.setProperty("url", testUrl);
    entity.setProperty("timestamp", 1);
    dataService.put(entity);
    return testUrl;
  }

  // Adds 3 Video entities to dataService param
  // Returns list of entities (sorted by descending timestamps)
  private ArrayList<Entity> addThreeEntities(DatastoreService dataService) {
    Entity entity1 = new Entity("Video");
    entity1.setProperty("url", "fake.url1");
    entity1.setProperty("timestamp", 1);
    Entity entity2 = new Entity("Video");
    entity2.setProperty("url", "fake.url2");
    entity2.setProperty("timestamp", 2);
    Entity entity3 = new Entity("Video");
    entity3.setProperty("url", "fake.url3");
    entity3.setProperty("timestamp", 3);

    // Add to a list from most recent to least recent timestamps
    ArrayList<Entity> entityList = new ArrayList<Entity>();
    entityList.add(entity3);
    entityList.add(entity2);
    entityList.add(entity1);
    
    dataService.put(entity1);
    dataService.put(entity2);
    dataService.put(entity3);
    
    return entityList;
  }

  // Testing local datastore service with no entities
  @Test
  public void noEntities() {
    DatastoreService dataService = DatastoreServiceFactory.getDatastoreService();
    Query query = new Query("Video");
    PreparedQuery results = dataService.prepare(query);
    Assert.assertEquals(0, results.countEntities(withLimit(10)));
  }
  
  // Testing local datastore service with 1 entity 
  @Test
  public void addOneEntityWithProperty() {
    DatastoreService dataService = DatastoreServiceFactory.getDatastoreService();
    Assert.assertEquals(0, dataService.prepare(new Query("Video")).countEntities(withLimit(10)));

    String testUrl = addOneEntity(dataService);
    Assert.assertEquals(1, dataService.prepare(new Query("Video")).countEntities(withLimit(10)));

    // asSingleEntity() retrieves the one and only result for the Query
    Entity queryResult = dataService.prepare(new Query("Video")).asSingleEntity();

    // Check url property 
    Assert.assertEquals(testUrl, queryResult.getProperty("url"));
  }

  // Testing local datastore service with multiple entities
  @Test
  public void addMultipleEntities() {
    DatastoreService dataService = DatastoreServiceFactory.getDatastoreService();
    Assert.assertEquals(0, dataService.prepare(new Query("Video")).countEntities(withLimit(10)));

    ArrayList<Entity> entityList = addThreeEntities(dataService);

    // Sort results by timestamps so we can compare lists
    Query query = new Query("Video").addSort("timestamp", SortDirection.DESCENDING);
    PreparedQuery results = dataService.prepare(query);
    
    Assert.assertEquals(3, results.countEntities(withLimit(10)));
    Assert.assertEquals(entityList, results.asList(FetchOptions.Builder.withDefaults()));
  }

  // Testing VideoUpload.java with emtpy datastore
  @Test
  public void getUrlWithNoVideos() {
    DatastoreService dataService = DatastoreServiceFactory.getDatastoreService();
    Assert.assertEquals(0, dataService.prepare(new Query("Video")).countEntities(withLimit(10)));
  
    // Expects "error" attribute in json object to be filled
    String error = "No videos uploaded to Datastore";
    String url = "";
    String expected = String.format("{\"error\": %s, \"url\": %s}", error, url);
      
    VideoUpload videoUpload = new VideoUpload();
    String json = videoUpload.getUrl(dataService);
    
    Assert.assertEquals(expected, json);
  }

  // Testing VideoUpload.java with 1 entity in datastore
  @Test
  public void getUrlWithOneVideo() {
    DatastoreService dataService = DatastoreServiceFactory.getDatastoreService();
    Assert.assertEquals(0, dataService.prepare(new Query("Video")).countEntities(withLimit(10)));
    
    String testUrl = addOneEntity(dataService);

    // Expects correct url to be returned
    String error = "";
    String expected = String.format("{\"error\": %s, \"url\": %s}", error, testUrl);
      
    VideoUpload videoUpload = new VideoUpload();
    String json = videoUpload.getUrl(dataService);
    
    Assert.assertEquals(expected, json);
  }

  // Testing VideoUpload.java with multiple entities in datastore
  @Test
  public void getUrlWithMultipleVideos() {
    DatastoreService dataService = DatastoreServiceFactory.getDatastoreService();
    Assert.assertEquals(0, dataService.prepare(new Query("Video")).countEntities(withLimit(10)));
    
    ArrayList<Entity> entityList = addThreeEntities(dataService);
    
    // Expect most recent url to be returned
    String error = "";
    String url = (String) entityList.get(0).getProperty("url");
    String expected = String.format("{\"error\": %s, \"url\": %s}", error, url);
      
    VideoUpload videoUpload = new VideoUpload();
    String json = videoUpload.getUrl(dataService);
    
    Assert.assertEquals(expected, json);
  }
}