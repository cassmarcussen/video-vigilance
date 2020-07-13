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
import java.util.ArrayList;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/** Datastore tests for uploading the video 
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

  @Test
  public void addOneEntity() {
    DatastoreService dataService = DatastoreServiceFactory.getDatastoreService();
    Assert.assertEquals(0, dataService.prepare(new Query("Video")).countEntities(withLimit(10)));
    dataService.put(new Entity("Video"));
    Assert.assertEquals(1, dataService.prepare(new Query("Video")).countEntities(withLimit(10)));
  }

  @Test
  public void addOneEntityWithProperty() {
    DatastoreService dataService = DatastoreServiceFactory.getDatastoreService();
    Assert.assertEquals(0, dataService.prepare(new Query("Video")).countEntities(withLimit(10)));

    // Add a Video entity with a url property in datastore
    Entity entity = new Entity("Video");
    String testUrl = "fake.url";
    entity.setProperty("url", testUrl);
    dataService.put(entity);
    Assert.assertEquals(1, dataService.prepare(new Query("Video")).countEntities(withLimit(10)));

    // asSingleEntity() retrieves the one and only result for the Query
    Entity queryResult = dataService.prepare(new Query("Video")).asSingleEntity();

    // Check url property 
    Assert.assertEquals(testUrl, queryResult.getProperty("url"));
  }

  @Test
  public void addMultipleEntities() {
    DatastoreService dataService = DatastoreServiceFactory.getDatastoreService();
    Assert.assertEquals(0, dataService.prepare(new Query("Video")).countEntities(withLimit(10)));

    // Add Video entities with a url property and timestamp in datastore
    Entity entity1 = new Entity("Video");
    entity1.setProperty("url", "fake.url1");
    entity1.setProperty("timestamp", 1);
    Entity entity2 = new Entity("Video");
    entity2.setProperty("url", "fake.url2");
    entity2.setProperty("timestamp", 2);
    Entity entity3 = new Entity("Video");
    entity3.setProperty("url", "fake.url3");
    entity3.setProperty("timestamp", 3);

    ArrayList<Entity> entityList = new ArrayList<Entity>();
    entityList.add(entity1);
    entityList.add(entity2);
    entityList.add(entity3);

    dataService.put(entity1);
    dataService.put(entity2);
    dataService.put(entity3);
    
    Query query = new Query("Video").addSort("timestamp", SortDirection.ASCENDING);
    PreparedQuery results = dataService.prepare(query);
    
    Assert.assertEquals(3, results.countEntities(withLimit(10)));
    Assert.assertEquals(entityList, results.asList(FetchOptions.Builder.withDefaults()));
  }

}