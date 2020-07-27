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
    Assert.assertEquals(0, results.countEntities());
  }

  // Posting 1 entity 
  @Test
  public void addOneEntityWithProperty() {
    DatastoreService dataService = DatastoreServiceFactory.getDatastoreService();
    Assert.assertEquals(0, dataService.prepare(new Query("KeyframeImages_Video_TestList")).countEntities());

    KeyframeImageUploadServlet keyframeImageUpload = new KeyframeImageUploadServlet();
    String testUrl = "fake.url";
    String timestamp = "0:10";
    String startTime = "0:00";
    String endTime = "0:15";
    keyframeImageUpload.createAndPostEntity(testUrl, timestamp, startTime, endTime, "KeyframeImages_Video_TestList");

    Assert.assertEquals(1, dataService.prepare(new Query("KeyframeImages_Video_TestList")).countEntities());

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
    Assert.assertEquals(0, dataService.prepare(new Query("KeyframeImages_Video_TestList")).countEntities());

    KeyframeImageUploadServlet keyframeImageUpload = new KeyframeImageUploadServlet();
    keyframeImageUpload.createAndPostEntity("fake.url.1", "0:10", "0:00", "0:15", "KeyframeImages_Video_TestList");
    keyframeImageUpload.createAndPostEntity("fake.url.2", "0:25", "0:20", "0:30", "KeyframeImages_Video_TestList");
    keyframeImageUpload.createAndPostEntity("fake.url.3", "0:40", "0:35", "0:45", "KeyframeImages_Video_TestList");

    Query query = new Query("KeyframeImages_Video_TestList");
    PreparedQuery results = dataService.prepare(query);

    Assert.assertEquals(3, results.countEntities());
  }

  // Getting from empty datastore
  @Test
  public void getListWithNoImages() {
    DatastoreService dataService = DatastoreServiceFactory.getDatastoreService();
    Assert.assertEquals(0, dataService.prepare(new Query("KeyframeImages_Video_TestList")).countEntities());

    List<KeyframeImage> emptyListOfKeyframeImages = new ArrayList<>();

    KeyframeImageUploadServlet keyframeImageUpload = new KeyframeImageUploadServlet();
    List<KeyframeImage> listOfKeyframeImagesFromDataStore = keyframeImageUpload.getKeyframeImagesFromDataStore("KeyframeImages_Video_TestList");
    Assert.assertEquals(emptyListOfKeyframeImages, listOfKeyframeImagesFromDataStore);
  }

  // Getting from datastore with 1 entity 
  @Test
  public void getListWithOneImage() {
    DatastoreService dataService = DatastoreServiceFactory.getDatastoreService();
    Assert.assertEquals(0, dataService.prepare(new Query("KeyframeImages_Video_TestList")).countEntities());

    // Add entity to datastore
    addEntityToDatastore("fake.url", "0:10", "0:00", "0:15", dataService);

    // listOfOneKeyframeImage is a hard-coded list of one keyframe image. listOfKeyframeImagesFromDataStore is the list of keyframe images 
    // returned from datastore. I want to make sure that the values returned 
    // from datastore are what we expect, and in the correct order, which is why I hard-code listOfOneKeyframeImage to use as comparison.

    List<KeyframeImage> listOfOneKeyframeImage = new ArrayList<>();
    // In the GET method for DataStore, we add "gs:/" to the front of the URL of the Keyframe Image so it can be properly 
    // served on the page and for the Vision API, so in testing we must add it to the beginning of our test URL
    KeyframeImage testKeyframeImage = new KeyframeImage("gs:/fake.url", "0:10", "0:00", "0:15");
    listOfOneKeyframeImage.add(testKeyframeImage);

    KeyframeImageUploadServlet keyframeImageUpload = new KeyframeImageUploadServlet();
    List<KeyframeImage> listOfKeyframeImagesFromDataStore = keyframeImageUpload.getKeyframeImagesFromDataStore("KeyframeImages_Video_TestList");
    Assert.assertEquals(listOfOneKeyframeImage.size(), listOfKeyframeImagesFromDataStore.size());
    Assert.assertEquals(1, listOfOneKeyframeImage.size());
    Assert.assertEquals(1, listOfKeyframeImagesFromDataStore.size());

    assertValuesEqual(listOfOneKeyframeImage.get(0), listOfKeyframeImagesFromDataStore.get(0));

  }

  // Getting from datastore with multiple entities
  @Test
  public void getMultipleImagesAndQuerySortByTimestamp() {
    DatastoreService dataService = DatastoreServiceFactory.getDatastoreService();
    Assert.assertEquals(0, dataService.prepare(new Query("KeyframeImages_Video_TestList")).countEntities());

    String testUrl1 = "fake.url.1", testUrl2 = "fake.url.2", testUrl3 = "fake.url.3";
    String timestamp1 = "0:25", timestamp2 = "0:10", timestamp3 = "0:45";
    String startTime1 = "0:20", startTime2 = "0:00", startTime3 = "0:35";
    String endTime1 = "0:30", endTime2 = "0:10", endTime3 = "0:50";

    // Add entities to datastore
    addEntityToDatastore(testUrl1, timestamp1, startTime1, endTime1, dataService);
    addEntityToDatastore(testUrl2, timestamp2, startTime2, endTime2, dataService);
    addEntityToDatastore(testUrl3, timestamp3, startTime3, endTime3, dataService);


    // listOfThreeKeyframeImages is a hard-coded list of three keyframe images. listOfKeyframeImagesFromDataStore is the list of keyframe images 
    // returned from datastore. I want to make sure that the values returned 
    // from datastore are what we expect, and in the correct order, which is why I hard-code listOfThreeKeyframeImages to use as comparison.

    List<KeyframeImage> listOfThreeKeyframeImages = new ArrayList<>();
    // In the GET method for DataStore, we add "gs:/" to the front of the URL of the Keyframe Image so it can be properly 
    // served on the page and for the Vision API, so in testing we must add it to the beginning of our test URL
    // Add these in the order they should appear for timestamp ascending order sort of query
    KeyframeImage testKeyframeImageFirstTimestamp = new KeyframeImage("gs:/" + testUrl2, timestamp2, startTime2, endTime2);
    KeyframeImage testKeyframeImageSecondTimestamp = new KeyframeImage("gs:/" + testUrl1, timestamp1, startTime1, endTime1);
    KeyframeImage testKeyframeImageThirdTimestamp = new KeyframeImage("gs:/" + testUrl3, timestamp3, startTime3, endTime3);
    listOfThreeKeyframeImages.add(testKeyframeImageFirstTimestamp);
    listOfThreeKeyframeImages.add(testKeyframeImageSecondTimestamp);
    listOfThreeKeyframeImages.add(testKeyframeImageThirdTimestamp);

    KeyframeImageUploadServlet keyframeImageUpload = new KeyframeImageUploadServlet();
    List<KeyframeImage> listOfKeyframeImagesFromDataStore = keyframeImageUpload.getKeyframeImagesFromDataStore("KeyframeImages_Video_TestList");
    Assert.assertEquals(3, listOfThreeKeyframeImages.size());
    Assert.assertEquals(3, listOfKeyframeImagesFromDataStore.size());
    Assert.assertEquals(listOfThreeKeyframeImages.size(), listOfKeyframeImagesFromDataStore.size());

    // The addresses of the keyframe images in the lists will be different, so we need to test each of the properties of the Keyframe Images
    // This tests the sorting by timestamp in the Query
    assertValuesEqual(listOfThreeKeyframeImages.get(1), listOfKeyframeImagesFromDataStore.get(1));
    assertValuesEqual(listOfThreeKeyframeImages.get(0), listOfKeyframeImagesFromDataStore.get(0));
    assertValuesEqual(listOfThreeKeyframeImages.get(2), listOfKeyframeImagesFromDataStore.get(2));

  }

  // Helper method to add an entity to DataStore
  private void addEntityToDatastore(String url, String timestamp, String startTime, String endTime, DatastoreService dataService) {
    Entity entity = new Entity("KeyframeImages_Video_TestList");
    entity.setProperty("url", url);
    entity.setProperty("timestamp", timestamp);
    entity.setProperty("startTime", startTime);
    entity.setProperty("endTime", endTime);
    dataService.put(entity); 
  }

  // Helper method which asserts two KeyframeImages to be equal by checking each of their fields
  private void assertValuesEqual(KeyframeImage firstImage, KeyframeImage secondImage) {
    Assert.assertEquals(firstImage.getUrl(), secondImage.getUrl());
    Assert.assertEquals(firstImage.getTimestamp(), secondImage.getTimestamp());
    Assert.assertEquals(firstImage.getStartTime(), secondImage.getStartTime());
    Assert.assertEquals(firstImage.getEndTime(), secondImage.getEndTime());
  }

}