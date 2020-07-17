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

    // null and empty are validated in POST not helper, so can't test with helper unless rearrange...?
  // Posting null url
 /* @Test
  public void nullImage() {
    DatastoreService dataService = DatastoreServiceFactory.getDatastoreService();
    Assert.assertEquals(0, dataService.prepare(new Query("KeyframeImages_Video_TestList")).countEntities(withLimit(10)));

   //do an upload 
    KeyframeImageUploadServlet keyframeImageUpload = new KeyframeImageUploadServlet();
    String testUrl = null;
    String timestamp = "0:10";
    String startTime = "0:00";
    String endTime = "0:15";
    keyframeImageUpload.createAndPostEntity(testUrl, timestamp, startTime, endTime, "KeyframeImages_Video_TestList");

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
    KeyframeImageUploadServlet keyframeImageUpload = new KeyframeImageUploadServlet();
    String testUrl = "";
    String timestamp = "0:10";
    String startTime = "0:00";
    String endTime = "0:15";
    keyframeImageUpload.createAndPostEntity(testUrl, timestamp, startTime, endTime, "KeyframeImages_Video_TestList");

    // Empty url should not be posted
    Query query = new Query("KeyframeImages_Video_TestList");
    PreparedQuery results = dataService.prepare(query);
    Assert.assertEquals(0, results.countEntities(withLimit(10)));
  }
*/

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
    Assert.assertEquals(timestamp, queryResult.getProperty("timestamp"));
    Assert.assertEquals(startTime, queryResult.getProperty("startTime"));
    Assert.assertEquals(endTime, queryResult.getProperty("endTime"));
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

  // Getting from empty datastore
  @Test
  public void getListWithNoImages() {
    DatastoreService dataService = DatastoreServiceFactory.getDatastoreService();
    Assert.assertEquals(0, dataService.prepare(new Query("KeyframeImages_Video_TestList")).countEntities(withLimit(10)));

    List<KeyframeImage> emptyListOfKeyframeImages = new ArrayList<>();

    KeyframeImageUploadServlet keyframeImageUpload = new KeyframeImageUploadServlet();
    List<KeyframeImage> listOfKeyframeImages = keyframeImageUpload.getKeyframeImagesFromDataStore("KeyframeImages_Video_TestList");
    Assert.assertEquals(emptyListOfKeyframeImages, listOfKeyframeImages);
  }

  // Getting from datastore with 1 entity 
  @Test
  public void getListWithOneImage() {
    DatastoreService dataService = DatastoreServiceFactory.getDatastoreService();
    Assert.assertEquals(0, dataService.prepare(new Query("KeyframeImages_Video_TestList")).countEntities(withLimit(10)));

    // Add entity to datastore
    Entity entity = new Entity("KeyframeImages_Video_TestList");
    String testUrl = "fake.url";
    String timestamp = "0:10";
    String startTime = "0:00";
    String endTime = "0:15";
    entity.setProperty("url", testUrl);
    entity.setProperty("timestamp", timestamp);
    entity.setProperty("startTime", startTime);
    entity.setProperty("endTime", endTime);
    dataService.put(entity);

    List<KeyframeImage> listOfOneKeyframeImage = new ArrayList<>();
    // In the GET method for DataStore, we add "gs:/" to the front of the URL of the Keyframe Image so it can be properly 
    // served on the page and for the Vision API, so in testing we must add it to the beginning of our test URL
    KeyframeImage testKeyframeImage = new KeyframeImage("gs:/" + testUrl, timestamp, startTime, endTime);
    listOfOneKeyframeImage.add(testKeyframeImage);

    KeyframeImageUploadServlet keyframeImageUpload = new KeyframeImageUploadServlet();
    List<KeyframeImage> listOfKeyframeImages = keyframeImageUpload.getKeyframeImagesFromDataStore("KeyframeImages_Video_TestList");
    Assert.assertEquals(listOfOneKeyframeImage.size(), listOfKeyframeImages.size());
    Assert.assertEquals(1, listOfOneKeyframeImage.size());
    Assert.assertEquals(1, listOfKeyframeImages.size());

    // The addresses of the keyframe images in the lists will be different, so we need to test each of the properties of the Keyframe Images
    Assert.assertEquals(listOfOneKeyframeImage.get(0).getUrl(), listOfKeyframeImages.get(0).getUrl());
    Assert.assertEquals(listOfOneKeyframeImage.get(0).getTimestamp(), listOfKeyframeImages.get(0).getTimestamp());
    Assert.assertEquals(listOfOneKeyframeImage.get(0).getStartTime(), listOfKeyframeImages.get(0).getStartTime());
    Assert.assertEquals(listOfOneKeyframeImage.get(0).getEndTime(), listOfKeyframeImages.get(0).getEndTime());

  }

  // Getting from datastore with multiple entities
  @Test
  public void getMultipleImagesAndQuerySortByTimestamp() {
    DatastoreService dataService = DatastoreServiceFactory.getDatastoreService();
    Assert.assertEquals(0, dataService.prepare(new Query("KeyframeImages_Video_TestList")).countEntities(withLimit(10)));

    // Add entity to datastore
    
    Entity entity1 = new Entity("KeyframeImages_Video_TestList");
    String testUrl1 = "fake.url.1";
    String timestamp1 = "0:25";
    String startTime1 = "0:20";
    String endTime1 = "0:30";
    entity1.setProperty("url", testUrl1);
    entity1.setProperty("timestamp", timestamp1);
    entity1.setProperty("startTime", startTime1);
    entity1.setProperty("endTime", endTime1);
    dataService.put(entity1);

    Entity entity2 = new Entity("KeyframeImages_Video_TestList");
    String testUrl2 = "fake.url.2";
    String timestamp2 = "0:10";
    String startTime2 = "0:00";
    String endTime2 = "0:15";
    entity2.setProperty("url", testUrl2);
    entity2.setProperty("timestamp", timestamp2);
    entity2.setProperty("startTime", startTime2);
    entity2.setProperty("endTime", endTime2);
    dataService.put(entity2);

    Entity entity3 = new Entity("KeyframeImages_Video_TestList");
    String testUrl3 = "fake.url.3";
    String timestamp3 = "0:45";
    String startTime3 = "0:35";
    String endTime3 = "0:50";
    entity3.setProperty("url", testUrl3);
    entity3.setProperty("timestamp", timestamp3);
    entity3.setProperty("startTime", startTime3);
    entity3.setProperty("endTime", endTime3);
    dataService.put(entity3);


    List<KeyframeImage> listOfOneKeyframeImage = new ArrayList<>();
    // In the GET method for DataStore, we add "gs:/" to the front of the URL of the Keyframe Image so it can be properly 
    // served on the page and for the Vision API, so in testing we must add it to the beginning of our test URL
    // Add these in the order they should appear for timestamp ascending order sort of query
    KeyframeImage testKeyframeImageFirstTimestamp = new KeyframeImage("gs:/" + testUrl2, timestamp2, startTime2, endTime2);
    KeyframeImage testKeyframeImageSecondTimestamp = new KeyframeImage("gs:/" + testUrl1, timestamp1, startTime1, endTime1);
    KeyframeImage testKeyframeImageThirdTimestamp = new KeyframeImage("gs:/" + testUrl3, timestamp3, startTime3, endTime3);
    listOfOneKeyframeImage.add(testKeyframeImageFirstTimestamp);
    listOfOneKeyframeImage.add(testKeyframeImageSecondTimestamp);
    listOfOneKeyframeImage.add(testKeyframeImageThirdTimestamp);

    KeyframeImageUploadServlet keyframeImageUpload = new KeyframeImageUploadServlet();
    List<KeyframeImage> listOfKeyframeImages = keyframeImageUpload.getKeyframeImagesFromDataStore("KeyframeImages_Video_TestList");
    Assert.assertEquals(listOfOneKeyframeImage.size(), listOfKeyframeImages.size());
    Assert.assertEquals(3, listOfOneKeyframeImage.size());
    Assert.assertEquals(3, listOfKeyframeImages.size());

    // The addresses of the keyframe images in the lists will be different, so we need to test each of the properties of the Keyframe Images
    // This tests the sorting by timestamp in the Query
    Assert.assertEquals(listOfOneKeyframeImage.get(1).getUrl(), listOfKeyframeImages.get(1).getUrl());
    Assert.assertEquals(listOfOneKeyframeImage.get(1).getTimestamp(), listOfKeyframeImages.get(1).getTimestamp());
    Assert.assertEquals(listOfOneKeyframeImage.get(1).getStartTime(), listOfKeyframeImages.get(1).getStartTime());
    Assert.assertEquals(listOfOneKeyframeImage.get(1).getEndTime(), listOfKeyframeImages.get(1).getEndTime());

    Assert.assertEquals(listOfOneKeyframeImage.get(0).getUrl(), listOfKeyframeImages.get(0).getUrl());
    Assert.assertEquals(listOfOneKeyframeImage.get(0).getTimestamp(), listOfKeyframeImages.get(0).getTimestamp());
    Assert.assertEquals(listOfOneKeyframeImage.get(0).getStartTime(), listOfKeyframeImages.get(0).getStartTime());
    Assert.assertEquals(listOfOneKeyframeImage.get(0).getEndTime(), listOfKeyframeImages.get(0).getEndTime());

    Assert.assertEquals(listOfOneKeyframeImage.get(2).getUrl(), listOfKeyframeImages.get(2).getUrl());
    Assert.assertEquals(listOfOneKeyframeImage.get(2).getTimestamp(), listOfKeyframeImages.get(2).getTimestamp());
    Assert.assertEquals(listOfOneKeyframeImage.get(2).getStartTime(), listOfKeyframeImages.get(2).getStartTime());
    Assert.assertEquals(listOfOneKeyframeImage.get(2).getEndTime(), listOfKeyframeImages.get(2).getEndTime());

  }
}