/*
 * Copyright © 2017-2024 Cask Data, Inc.
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
 * Represents a byte size with unit (B, KB, MB, GB, TB).
 * Parses values like "1KB", "5 MB", "2.5GB", etc.
 */
@PublicEvolving
public class ByteSize implements Token { 
  private static final Pattern BYTE_PATTERN = Pattern.compile("(\\d+(?:\\.\\d+)?)\\s*([a-zA-Z]+)");
  
  // Constants for conversion
  private static final long KB = 1024L;
  private static final long MB = KB * 1024L;
  private static final long GB = MB * 1024L;
  private static final long TB = GB * 1024L;
  
  private final long bytes;
  private final String rawValue; 

  /**
   * Creates a new ByteSize object by parsing the given string value.
   *
   * @param value the string representation of byte size (e.g., "1KB", "5 MB", "2.5GB")
   * @throws IllegalArgumentException if the value cannot be parsed or has an invalid unit
   */
  public ByteSize(String value) {
    if (value == null || value.trim().isEmpty()) {
      throw new IllegalArgumentException("Byte size value cannot be null or empty");
    }
    
    this.rawValue = value.trim();
    
    Matcher matcher = BYTE_PATTERN.matcher(rawValue);
    if (!matcher.matches()) {
      throw new IllegalArgumentException("Invalid byte size format: " + rawValue);
    }
    
    double number = Double.parseDouble(matcher.group(1));
    String unit = matcher.group(2).toLowerCase();
    
    if (number < 0) {
      throw new IllegalArgumentException("Byte size cannot be negative: " + rawValue);
    }
    
    switch (unit) {
      case "b":  
        bytes = (long) number; 
        break;
      case "kb": 
        bytes = (long) (number * KB); 
        break;
      case "mb": 
        bytes = (long) (number * MB); 
        break;
      case "gb": 
        bytes = (long) (number * GB); 
        break;
      case "tb": 
        bytes = (long) (number * TB); 
        break;
      default: 
        throw new IllegalArgumentException("Invalid byte unit: " + unit);
    }
  }

  /**
   * Returns the size in bytes.
   *
   * @return the size in bytes
   */
  public long getBytes() {
    return bytes;
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
   * Convert the byte size to a specific unit.
   * 
   * @param unit the unit to convert to ("b", "kb", "mb", "gb", "tb")
   * @return the size in the specified unit
   */
  public double to(String unit) {
    if (unit == null) {
      throw new IllegalArgumentException("Unit cannot be null");
    }
    
    switch (unit.toLowerCase()) {
      case "b":  return bytes;
      case "kb": return bytes / (double) KB;
      case "mb": return bytes / (double) MB;
      case "gb": return bytes / (double) GB;
      case "tb": return bytes / (double) TB;
      default: throw new IllegalArgumentException("Invalid byte unit: " + unit);
    }
  }

  // Implement Token interface methods
  @Override
  public Object value() {
    return bytes;
  }

  @Override
  public TokenType type() {
    return TokenType.BYTE_SIZE;
  }

  @Override
  public JsonElement toJson() {
    JsonObject object = new JsonObject();
    object.addProperty("type", "BYTE_SIZE");
    object.addProperty("value", rawValue);
    object.addProperty("bytes", bytes);
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
    ByteSize other = (ByteSize) obj;
    return bytes == other.bytes;
  }
  
  @Override
  public int hashCode() {
    return Long.hashCode(bytes);
  }
}
