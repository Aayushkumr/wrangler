
/*
 * Copyright © 2017-2019 Cask Data, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package io.cdap.wrangler.api.parser;

import org.junit.Assert;
import org.junit.Test;

/**
 * Tests for {@link ByteSize} class
 */
public class ByteSizeTest {

  @Test
  public void testByteParsing() {
    // Test basic byte formats
    ByteSize b1 = new ByteSize("1024B");
    Assert.assertEquals(1024L, b1.getBytes());
    
    ByteSize b2 = new ByteSize("1b");
    Assert.assertEquals(1L, b2.getBytes());
  }
  
  @Test
  public void testKilobyteParsing() {
    // Test kilobytes with different formats
    ByteSize kb1 = new ByteSize("1KB");
    Assert.assertEquals(1024L, kb1.getBytes());
    
    ByteSize kb2 = new ByteSize("1.5 kb");
    Assert.assertEquals(1536L, kb2.getBytes());
    
    ByteSize kb3 = new ByteSize("2 KB");
    Assert.assertEquals(2048L, kb3.getBytes());
  }
  
  @Test
  public void testMegabyteParsing() {
    // Test megabytes with different formats
    ByteSize mb1 = new ByteSize("1MB");
    Assert.assertEquals(1024L * 1024L, mb1.getBytes());
    
    ByteSize mb2 = new ByteSize("2.5mb");
    Assert.assertEquals(2.5 * 1024L * 1024L, mb2.getBytes(), 1);
  }
  
  @Test
  public void testGigabyteParsing() {
    // Test gigabytes
    ByteSize gb1 = new ByteSize("1GB");
    Assert.assertEquals(1024L * 1024L * 1024L, gb1.getBytes());
    
    ByteSize gb2 = new ByteSize("0.5 gb");
    Assert.assertEquals(0.5 * 1024L * 1024L * 1024L, gb2.getBytes(), 1);
  }
  
  @Test
  public void testTerabyteParsing() {
    // Test terabytes
    ByteSize tb1 = new ByteSize("1TB");
    Assert.assertEquals(1024L * 1024L * 1024L * 1024L, tb1.getBytes());
  }
  
  @Test
  public void testEdgeCases() {
    // Test zero
    ByteSize zero = new ByteSize("0KB");
    Assert.assertEquals(0L, zero.getBytes());
    
    // Test large numbers
    ByteSize large = new ByteSize("9999MB");
    Assert.assertEquals(9999L * 1024L * 1024L, large.getBytes());
    
    // Test decimal points
    ByteSize decimal = new ByteSize("1.23KB");
    Assert.assertEquals(1.23 * 1024L, decimal.getBytes(), 1);
  }
  
  @Test
  public void testUnitConversion() {
    ByteSize size = new ByteSize("1024KB");
    Assert.assertEquals(1024 * 1024, size.getBytes());
    Assert.assertEquals(1024.0, size.to("kb"), 0.001);
    Assert.assertEquals(1.0, size.to("mb"), 0.001);
    Assert.assertEquals(0.001, size.to("gb"), 0.001);
    Assert.assertEquals(0.000001, size.to("tb"), 0.000001);
  }
  
  @Test(expected = IllegalArgumentException.class)
  public void testInvalidFormat() {
    new ByteSize("invalid");
  }
  
  @Test(expected = IllegalArgumentException.class)
  public void testNegativeValue() {
    new ByteSize("-10KB");
  }
  
  @Test(expected = IllegalArgumentException.class)
  public void testInvalidUnit() {
    new ByteSize("10XB");
  }
  
  @Test
  public void testEqualityAndHashCode() {
    ByteSize size1 = new ByteSize("1MB");
    ByteSize size2 = new ByteSize("1024KB");
    ByteSize size3 = new ByteSize("1GB");
    
    Assert.assertEquals(size1, size2);
    Assert.assertNotEquals(size1, size3);
    Assert.assertEquals(size1.hashCode(), size2.hashCode());
  }
  
  @Test
  public void testToString() {
    ByteSize size = new ByteSize("10.5MB");
    Assert.assertEquals("10.5MB", size.toString());
  }
}