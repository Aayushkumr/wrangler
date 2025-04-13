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

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.cdap.wrangler.api.annotations.PublicEvolving;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represents a time duration with unit (ns, ms, s, m, h, d).
 * Parses values like "100ms", "5s", "2.5h", etc.
 */
@PublicEvolving
public class TimeDuration implements Token {
  private static final Pattern TIME_PATTERN = Pattern.compile("(\\d+(?:\\.\\d+)?)\\s*([a-zA-Z]+)");
  
  // Constants for conversion
  private static final long NANOS_PER_MILLI = 1_000_000L;
  private static final long NANOS_PER_SECOND = 1_000_000_000L;
  private static final long NANOS_PER_MINUTE = NANOS_PER_SECOND * 60L;
  private static final long NANOS_PER_HOUR = NANOS_PER_MINUTE * 60L;
  private static final long NANOS_PER_DAY = NANOS_PER_HOUR * 24L;
  
  private final long nanoseconds;
  private final String rawValue;

  /**
   * Creates a new TimeDuration object by parsing the given string value.
   *
   * @param value the string representation of time duration (e.g., "100ms", "5s", "2.5h")
   * @throws IllegalArgumentException if the value cannot be parsed or has an invalid unit
   */
  public TimeDuration(String value) {
    if (value == null || value.trim().isEmpty()) {
      throw new IllegalArgumentException("Time duration value cannot be null or empty");
    }
    
    this.rawValue = value.trim();
    
    Matcher matcher = TIME_PATTERN.matcher(rawValue);
    if (!matcher.matches()) {
      throw new IllegalArgumentException("Invalid time duration format: " + rawValue);
    }
    
    double number = Double.parseDouble(matcher.group(1));
    String unit = matcher.group(2).toLowerCase();
    
    if (number < 0) {
      throw new IllegalArgumentException("Time duration cannot be negative: " + rawValue);
    }
    
    switch (unit) {
      case "ns":
        nanoseconds = (long) number;
        break;
      case "ms":
        nanoseconds = (long) (number * NANOS_PER_MILLI);
        break;
      case "s":
        nanoseconds = (long) (number * NANOS_PER_SECOND);
        break;
      case "m":
      case "min":
        nanoseconds = (long) (number * NANOS_PER_MINUTE);
        break;
      case "h":
      case "hr":
        nanoseconds = (long) (number * NANOS_PER_HOUR);
        break;
      case "d":
        nanoseconds = (long) (number * NANOS_PER_DAY);
        break;
      default:
        throw new IllegalArgumentException("Invalid time unit: " + unit);
    }
  }

  /**
   * Returns the duration in nanoseconds.
   *
   * @return the duration in nanoseconds
   */
  public long getNanoseconds() {
    return nanoseconds;
  }
  
  /**
   * Returns the original string value.
   * 
   * @return the original string value
   */
  public String getRawValue() {
    return rawValue;
  }
  
  /**
   * Convert the time duration to a specific unit.
   * 
   * @param unit the unit to convert to ("ns", "ms", "s", "m", "h", "d")
   * @return the duration in the specified unit
   */
  public double to(String unit) {
    if (unit == null) {
      throw new IllegalArgumentException("Unit cannot be null");
    }
    
    switch (unit.toLowerCase()) {
      case "ns":
        return nanoseconds;
      case "ms":
        return nanoseconds / (double) NANOS_PER_MILLI;
      case "s":
      case "seconds":
        return nanoseconds / (double) NANOS_PER_SECOND;
      case "m":
      case "min":
      case "minutes":
        return nanoseconds / (double) NANOS_PER_MINUTE;
      case "h":
      case "hr":
      case "hours":
        return nanoseconds / (double) NANOS_PER_HOUR;
      case "d":
      case "days":
        return nanoseconds / (double) NANOS_PER_DAY;
      default:
        throw new IllegalArgumentException("Invalid time unit: " + unit);
    }
  }

  // Implement Token interface methods
  @Override
  public Object value() {
    return nanoseconds;
  }

  @Override
  public TokenType type() {
    return TokenType.TIME_DURATION;
  }

  @Override
  public JsonElement toJson() {
    JsonObject object = new JsonObject();
    object.addProperty("type", "TIME_DURATION");
    object.addProperty("value", rawValue);
    object.addProperty("nanoseconds", nanoseconds);
    return object;
  }
  
  @Override
  public String toString() {
    return rawValue;
  }
  
  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }
    TimeDuration other = (TimeDuration) obj;
    return nanoseconds == other.nanoseconds;
  }
  
  @Override
  public int hashCode() {
    return Long.hashCode(nanoseconds);
  }
}