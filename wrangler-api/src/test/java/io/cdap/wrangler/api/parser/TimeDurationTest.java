
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
 * Tests for {@link TimeDuration} class
 */
public class TimeDurationTest {

  private static final long NANOS_IN_MILLI = 1_000_000L;
  private static final long NANOS_IN_SECOND = 1_000_000_000L;
  private static final long NANOS_IN_MINUTE = NANOS_IN_SECOND * 60L;
  private static final long NANOS_IN_HOUR = NANOS_IN_MINUTE * 60L;
  private static final long NANOS_IN_DAY = NANOS_IN_HOUR * 24L;
  
  @Test
  public void testNanosecondParsing() {
    TimeDuration ns1 = new TimeDuration("1ns");
    Assert.assertEquals(1L, ns1.getNanoseconds());
    
    TimeDuration ns2 = new TimeDuration("500ns");
    Assert.assertEquals(500L, ns2.getNanoseconds());
  }
  
  @Test
  public void testMillisecondParsing() {
    TimeDuration ms1 = new TimeDuration("1ms");
    Assert.assertEquals(NANOS_IN_MILLI, ms1.getNanoseconds());
    
    TimeDuration ms2 = new TimeDuration("500ms");
    Assert.assertEquals(500 * NANOS_IN_MILLI, ms2.getNanoseconds());
    
    TimeDuration ms3 = new TimeDuration("1.5 ms");
    Assert.assertEquals(1.5 * NANOS_IN_MILLI, ms3.getNanoseconds(), 1);
  }
  
  @Test
  public void testSecondParsing() {
    TimeDuration s1 = new TimeDuration("1s");
    Assert.assertEquals(NANOS_IN_SECOND, s1.getNanoseconds());
    
    TimeDuration s2 = new TimeDuration("2.5s");
    Assert.assertEquals(2.5 * NANOS_IN_SECOND, s2.getNanoseconds(), 1);
    
    TimeDuration s3 = new TimeDuration("10 seconds");
    Assert.assertEquals(10 * NANOS_IN_SECOND, s3.getNanoseconds());
  }
  
  @Test
  public void testMinuteParsing() {
    TimeDuration m1 = new TimeDuration("1m");
    Assert.assertEquals(NANOS_IN_MINUTE, m1.getNanoseconds());
    
    TimeDuration m2 = new TimeDuration("5 min");
    Assert.assertEquals(5 * NANOS_IN_MINUTE, m2.getNanoseconds());
    
    TimeDuration m3 = new TimeDuration("0.5 minutes");
    Assert.assertEquals(0.5 * NANOS_IN_MINUTE, m3.getNanoseconds(), 1);
  }
  
  @Test
  public void testHourParsing() {
    TimeDuration h1 = new TimeDuration("1h");
    Assert.assertEquals(NANOS_IN_HOUR, h1.getNanoseconds());
    
    TimeDuration h2 = new TimeDuration("2 hr");
    Assert.assertEquals(2 * NANOS_IN_HOUR, h2.getNanoseconds());
    
    TimeDuration h3 = new TimeDuration("0.5 hours");
    Assert.assertEquals(0.5 * NANOS_IN_HOUR, h3.getNanoseconds(), 1);
  }
  
  @Test
  public void testDayParsing() {
    TimeDuration d1 = new TimeDuration("1d");
    Assert.assertEquals(NANOS_IN_DAY, d1.getNanoseconds());
    
    TimeDuration d2 = new TimeDuration("2 days");
    Assert.assertEquals(2 * NANOS_IN_DAY, d2.getNanoseconds());
  }
  
  @Test
  public void testEdgeCases() {
    // Test zero
    TimeDuration zero = new TimeDuration("0ms");
    Assert.assertEquals(0L, zero.getNanoseconds());
    
    // Test large numbers
    TimeDuration large = new TimeDuration("9999s");
    Assert.assertEquals(9999L * NANOS_IN_SECOND, large.getNanoseconds());
    
    // Test decimal points
    TimeDuration decimal = new TimeDuration("1.23s");
    Assert.assertEquals(1.23 * NANOS_IN_SECOND, decimal.getNanoseconds(), 1);
  }
  
  @Test
  public void testUnitConversion() {
    TimeDuration duration = new TimeDuration("1m");
    Assert.assertEquals(NANOS_IN_MINUTE, duration.getNanoseconds());
    Assert.assertEquals(1.0, duration.to("m"), 0.001);
    Assert.assertEquals(60.0, duration.to("s"), 0.001);
    Assert.assertEquals(60_000.0, duration.to("ms"), 0.001);
    Assert.assertEquals(60_000_000_000.0, duration.to("ns"), 0.001);
    Assert.assertEquals(1/60.0, duration.to("h"), 0.0001);
    Assert.assertEquals(1/1440.0, duration.to("d"), 0.0001);
  }
  
  @Test(expected = IllegalArgumentException.class)
  public void testInvalidFormat() {
    new TimeDuration("invalid");
  }
  
  @Test(expected = IllegalArgumentException.class)
  public void testNegativeValue() {
    new TimeDuration("-10ms");
  }
  
  @Test(expected = IllegalArgumentException.class)
  public void testInvalidUnit() {
    new TimeDuration("10xs");
  }
  
  @Test
  public void testEqualityAndHashCode() {
    TimeDuration duration1 = new TimeDuration("60s");
    TimeDuration duration2 = new TimeDuration("1m");
    TimeDuration duration3 = new TimeDuration("1h");
    
    Assert.assertEquals(duration1, duration2);
    Assert.assertNotEquals(duration1, duration3);
    Assert.assertEquals(duration1.hashCode(), duration2.hashCode());
  }
  
  @Test
  public void testToString() {
    TimeDuration duration = new TimeDuration("10.5s");
    Assert.assertEquals("10.5s", duration.toString());
  }
}