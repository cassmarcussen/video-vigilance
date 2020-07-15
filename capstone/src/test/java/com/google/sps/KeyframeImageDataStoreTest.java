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

import java.util.ArrayList;
import java.util.List;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/** Datastore tests for uploading the keyframe images to DataStore
  * Documentation: https://cloud.google.com/appengine/docs/standard/java/tools/localunittesting?csw=1#datastore-memcache
  */
public class KeyframeImageDataStoreTest {

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

  // Testing local datastore service with no entities
  @Test
  public void noEntities() {
    DatastoreService dataService = DatastoreServiceFactory.getDatastoreService();
    Query query = new Query("KeyframeImages_Video_TestList");
    PreparedQuery results = dataService.prepare(query);
    Assert.assertEquals(0, results.countEntities(withLimit(10)));
  }

  // Posting null url
  /*@Test
  public void nullImage() {
    DatastoreService dataService = DatastoreServiceFactory.getDatastoreService();
    Assert.assertEquals(0, dataService.prepare(new Query("KeyframeImages_Video_TestList")).countEntities(withLimit(10)));

   //do an upload 
    VideoUpload videoUpload = new VideoUpload();
    videoUpload.postUrl(dataService, null, "KeyframeImages_Video_TestList");

    // Null url should not be posted
    Query query = new Query("KeyframeImages_Video_TestList");
    PreparedQuery results = dataService.prepare(query);
    Assert.assertEquals(0, results.countEntities(withLimit(10)));
  }

  // Posting empty url
  @Test
  public void emptyUrl() {
    DatastoreService dataService = DatastoreServiceFactory.getDatastoreService();
    Assert.assertEquals(0, dataService.prepare(new Query("KeyframeImages_Video_TestList")).countEntities(withLimit(10)));

    // do an upload
    VideoUpload videoUpload = new VideoUpload();
    videoUpload.postUrl(dataService, "", "Video");

    // Empty url should not be posted
    Query query = new Query("Video");
    PreparedQuery results = dataService.prepare(query);
    Assert.assertEquals(0, results.countEntities(withLimit(10)));
  }*/

  // Posting 1 entity 
  @Test
  public void addOneEntityWithProperty() {
    DatastoreService dataService = DatastoreServiceFactory.getDatastoreService();
    Assert.assertEquals(0, dataService.prepare(new Query("KeyframeImages_Video_TestList")).countEntities(withLimit(10)));

    KeyframeImageUploadServlet keyframeImageUpload = new KeyframeImageUploadServlet();
    String testUrl = "fake.url";
    String timestamp = "0:10";
    String startTime = "0:00";
    String endTime = "0:15";
    keyframeImageUpload.createAndPostEntity(testUrl, timestamp, startTime, endTime, "KeyframeImages_Video_TestList");

    Assert.assertEquals(1, dataService.prepare(new Query("KeyframeImages_Video_TestList")).countEntities(withLimit(10)));

    // The asSingleEntity() function retrieves the one and only result for the Query
    Entity queryResult = dataService.prepare(new Query("KeyframeImages_Video_TestList")).asSingleEntity();
    // Check url property 
    Assert.assertEquals(testUrl, queryResult.getProperty("url"));
  }

  // Posting multiple entities
  @Test
  public void addMultipleEntities() {
    DatastoreService dataService = DatastoreServiceFactory.getDatastoreService();
    Assert.assertEquals(0, dataService.prepare(new Query("KeyframeImages_Video_TestList")).countEntities(withLimit(10)));

    KeyframeImageUploadServlet keyframeImageUpload = new KeyframeImageUploadServlet();
    keyframeImageUpload.createAndPostEntity("fake.url.1", "0:10", "0:00", "0:15", "KeyframeImages_Video_TestList");
    keyframeImageUpload.createAndPostEntity("fake.url.2", "0:25", "0:20", "0:30", "KeyframeImages_Video_TestList");
    keyframeImageUpload.createAndPostEntity("fake.url.3", "0:40", "0:35", "0:45", "KeyframeImages_Video_TestList");

    Query query = new Query("KeyframeImages_Video_TestList");
    PreparedQuery results = dataService.prepare(query);

    Assert.assertEquals(3, results.countEntities(withLimit(10)));
  }

  // Getting from emtpy datastore
  /*@Test
  public void getUrlWithNoVideos() {
    DatastoreService dataService = DatastoreServiceFactory.getDatastoreService();
    Assert.assertEquals(0, dataService.prepare(new Query("KeyframeImages_Video_TestList")).countEntities(withLimit(10)));

    // Expects "error" attribute in json object to be filled
    String error = "No videos uploaded to Datastore";
    String url = "";
    String expected = String.format("{\"error\": \"%s\", \"url\": \"%s\"}", error, url);

    VideoUpload videoUpload = new VideoUpload();
    String json = videoUpload.getUrl(dataService, "Video");
    Assert.assertEquals(expected, json);
  }*/

  // Getting from datastore with 1 entity 
  /*@Test
  public void getUrlWithOneVideo() {
    DatastoreService dataService = DatastoreServiceFactory.getDatastoreService();
    Assert.assertEquals(0, dataService.prepare(new Query("KeyframeImages_Video_TestList")).countEntities(withLimit(10)));

    // Add entity to datastore
    Entity entity = new Entity("KeyframeImages_Video_TestList");
    String testUrl = "fake.url";
    entity.setProperty("url", testUrl);
    entity.setProperty("timestamp", "0:10");
    entity.setProperty("startTime", "0:00");
    entity.setProperty("endTime", "0:15");
    dataService.put(entity);

    // Expects correct url to be returned
    String error = "";
    String expectedUrl = String.format("{\"error\": \"%s\", \"url\": \"%s\"}", error, testUrl);

    KeyframeImageUploadServlet keyframeImageUpload = new KeyframeImageUploadServlet();
    List<KeyframeImage> keyframeImagesFromDataStore = keyframeImageUpload.getKeyframeImagesFromDataStore();

    Assert.assertEquals(expectedUrl, keyframeImagesFromDataStore.get(0).getUrl());

  }*/

  // Getting from datastore with multiple entities
  /*@Test
  public void getUrlWithMultipleVideos() {
    DatastoreService dataService = DatastoreServiceFactory.getDatastoreService();
    Assert.assertEquals(0, dataService.prepare(new Query("KeyframeImages_Video_TestList")).countEntities(withLimit(10)));

    // Add entities to datastore
    Entity entity1 = new Entity("KeyframeImages_Video_TestList");
    entity1.setProperty("url", "fake.url.1");
    entity1.setProperty("timestamp", 1);
    Entity entity2 = new Entity("KeyframeImages_Video_TestList");
    entity2.setProperty("url", "fake.url.2");
    entity2.setProperty("timestamp", 2);
    Entity entity3 = new Entity("KeyframeImages_Video_TestList");
    entity3.setProperty("url", "fake.url.3");
    entity3.setProperty("timestamp", 3);

    dataService.put(entity1);
    dataService.put(entity2);
    dataService.put(entity3);

    // Expects most recent url to be returned
    String error = "";
    String url = "fake.url.3";
    String expected = String.format("{\"error\": \"%s\", \"url\": \"%s\"}", error, url);

    VideoUpload videoUpload = new VideoUpload();
    String json = videoUpload.getUrl(dataService, "Video");
    Assert.assertEquals(expected, json);
  }

  // Getting from datastore with multiple entities with the same timestamp
  @Test
  public void getUrlWithSameTimestamps() {
    DatastoreService dataService = DatastoreServiceFactory.getDatastoreService();
    Assert.assertEquals(0, dataService.prepare(new Query("Video")).countEntities(withLimit(10)));

    // Add entities to datastore
    Entity entity1 = new Entity("KeyframeImages_Video_TestList");
    entity1.setProperty("url", "fake.url.1");
    entity1.setProperty("timestamp", 1);
    Entity entity2 = new Entity("KeyframeImages_Video_TestList");
    entity2.setProperty("url", "fake.url.2");
    entity2.setProperty("timestamp", 1);
    Entity entity3 = new Entity("Video");
    entity3.setProperty("url", "fake.url.3");
    entity3.setProperty("timestamp", 1);

    dataService.put(entity1);
    dataService.put(entity2);
    dataService.put(entity3);

    // Expect first one put in datastore to be returned
    String error = "";
    String url = "fake.url.1";
    String expected = String.format("{\"error\": \"%s\", \"url\": \"%s\"}", error, url);

    VideoUpload videoUpload = new VideoUpload();
    String json = videoUpload.getUrl(dataService, "Video");
    Assert.assertEquals(expected, json);
  }*/
}