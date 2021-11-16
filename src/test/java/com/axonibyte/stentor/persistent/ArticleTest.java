/*
 * Copyright (c) 2021 Axonibyte Innovations, LLC. All rights reserved.
 * 
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *   
 *   https://apache.org/licenses/LICENSE-2.0
 *   
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the license.
 */
package com.axonibyte.stentor.persistent;

import java.util.UUID;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Tests the {@link Article} model.
 * 
 * @author Caleb L. Power
 */
public final class ArticleTest {
  
  private final Article article = new Article();
  
  /**
   * Tests the ID setter and getter.
   */
  @Test public void testSetAndGetID() {
    UUID id = UUID.randomUUID();
    var returned = article.setID(id);
    Assert.assertEquals(article.getID(), id);
    Assert.assertEquals(returned, article);
  }
  
  /**
   * Tests the author setter and getter.
   */
  @Test public void testSetAndGetAuthor() {
    UUID id = UUID.randomUUID();
    var returned = article.setAuthor(id);
    Assert.assertEquals(article.getAuthor(), id);
    Assert.assertEquals(returned, article);
  }
  
  /**
   * Tests the title setter and getter.
   */
  @Test public void testSetAndGetTitle() {
    String title = "TITLE";
    var returned = article.setTitle(title);
    Assert.assertEquals(article.getTitle(), title);
    Assert.assertEquals(returned, article);
  }
  
  /**
   * Tests the content setter and getter.
   */
  @Test public void testSetAndGetContent() {
    String content = "CONTENT";
    var returned = article.setContent(content);
    Assert.assertEquals(article.getContent(), content);
    Assert.assertEquals(returned, article);
  }
  
  /**
   * Tests the timestamp setter and getter.
   */
  @Test public void testSetAndGetTimestamp() {
    long timestamp = System.currentTimeMillis();
    var returned = article.setTimestamp(timestamp);
    Assert.assertEquals(article.getTimestamp(), timestamp);
    Assert.assertEquals(returned, article);
  }
  
}
