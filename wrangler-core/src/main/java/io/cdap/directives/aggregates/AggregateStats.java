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
package io.cdap.directives.aggregates;

import io.cdap.wrangler.api.Arguments;
import io.cdap.wrangler.api.Directive;
import io.cdap.wrangler.api.DirectiveExecutionException;
import io.cdap.wrangler.api.ExecutorContext;
import io.cdap.wrangler.api.Row;
import io.cdap.wrangler.api.annotations.Categories;
import io.cdap.wrangler.api.annotations.Usage;
import io.cdap.wrangler.api.parser.ByteSize;
import io.cdap.wrangler.api.parser.ColumnName;
import io.cdap.wrangler.api.parser.Text;
import io.cdap.wrangler.api.parser.TimeDuration;
import io.cdap.wrangler.api.parser.TokenType;
import io.cdap.wrangler.api.parser.UsageDefinition;

import java.util.Collections;
import java.util.List;


/**
 * A directive for aggregating byte size and time duration values.
 * 
 * This directive calculates statistics on columns containing byte size and time
 * duration values, with support for unit conversion.
 */
@Categories(categories = {"aggregate"})
@Usage("Aggregates byte size and time duration values with unit conversion")
public class AggregateStats implements Directive {
    // Byte conversion constants
    private static final double KB = 1024.0;
    private static final double MB = KB * 1024.0;
    private static final double GB = MB * 1024.0;
    private static final double TB = GB * 1024.0;

    // Time conversion constants
    private static final double NANOS_PER_MILLI = 1_000_000.0;
    private static final double NANOS_PER_SECOND = 1_000_000_000.0;
    private static final double NANOS_PER_MINUTE = NANOS_PER_SECOND * 60.0;
    private static final double NANOS_PER_HOUR = NANOS_PER_MINUTE * 60.0;
    private static final double NANOS_PER_DAY = NANOS_PER_HOUR * 24.0;

    private String sizeSourceCol;
    private String timeSourceCol;
    private String sizeTargetCol;
    private String timeTargetCol;
    private String sizeOutputUnit;
    private String timeOutputUnit;
    private String aggregationType;

    @Override
    public UsageDefinition define() {
        UsageDefinition.Builder builder = UsageDefinition.builder("aggregate-stats");
        builder.define("sizeSourceCol", TokenType.COLUMN_NAME);
        builder.define("timeSourceCol", TokenType.COLUMN_NAME);
        builder.define("sizeTargetCol", TokenType.COLUMN_NAME);
        builder.define("timeTargetCol", TokenType.COLUMN_NAME);
        builder.define("sizeOutputUnit", TokenType.TEXT);
        builder.define("timeOutputUnit", TokenType.TEXT);
        builder.define("aggregationType", TokenType.TEXT);
        return builder.build();
    }

    @Override
    public void initialize(Arguments args) {
        try {
            this.sizeSourceCol = ((ColumnName) args.value("sizeSourceCol")).value();
            this.timeSourceCol = ((ColumnName) args.value("timeSourceCol")).value();
            this.sizeTargetCol = ((ColumnName) args.value("sizeTargetCol")).value();
            this.timeTargetCol = ((ColumnName) args.value("timeTargetCol")).value();
            
            if (args.contains("sizeOutputUnit")) {
                this.sizeOutputUnit = ((Text) args.value("sizeOutputUnit")).value();
            } else {
                this.sizeOutputUnit = "MB";
            }
            
            if (args.contains("timeOutputUnit")) {
                this.timeOutputUnit = ((Text) args.value("timeOutputUnit")).value();
            } else {
                this.timeOutputUnit = "seconds";
            }
            
            if (args.contains("aggregationType")) {
                this.aggregationType = ((Text) args.value("aggregationType")).value();
            } else {
                this.aggregationType = "sum";
            }
        } catch (Exception e) {
            // Handle or convert exceptions as appropriate
            throw new IllegalArgumentException("Failed to initialize directive: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Row> execute(List<Row> rows, ExecutorContext context) throws DirectiveExecutionException {
        if (rows.isEmpty()) {
            return rows;
        }
        
        // Aggregate values
        long totalBytes = 0;
        long totalNanos = 0;
        int rowCount = 0;
        
        for (Row row : rows) {
            // Process byte size values
            Object sizeValue = row.getValue(sizeSourceCol);
            if (sizeValue != null && !(sizeValue instanceof ByteSize)) {
                throw new DirectiveExecutionException(
                    String.format("Column '%s' should contain byte size values", sizeSourceCol)
                );
            }
            
            if (sizeValue instanceof ByteSize) {
                totalBytes += ((ByteSize) sizeValue).getBytes();
            }
            
            // Process time duration values
            Object timeValue = row.getValue(timeSourceCol);
            if (timeValue != null && !(timeValue instanceof TimeDuration)) {
                throw new DirectiveExecutionException(
                    String.format("Column '%s' should contain time duration values", timeSourceCol)
                );
            }
            
            if (timeValue instanceof TimeDuration) {
                totalNanos += ((TimeDuration) timeValue).getNanoseconds();
            }
            
            rowCount++;
        }
        
        // Calculate average if requested and have at least one row
        if ("average".equalsIgnoreCase(aggregationType) && rowCount > 0) {
            totalBytes = totalBytes / rowCount;
            totalNanos = totalNanos / rowCount;
        }

        // Convert to requested output units
        double sizeResult = convertBytes(totalBytes, sizeOutputUnit);
        double timeResult = convertNanos(totalNanos, timeOutputUnit);

        // Create result row with aggregated values
        Row result = new Row();
        result.add(sizeTargetCol, sizeResult);
        result.add(timeTargetCol, timeResult);
        
        // Return just the aggregated result row
        return Collections.singletonList(result);
    }

    /**
     * Converts byte count to the specified unit.
     *
     * @param bytes raw byte count
     * @param unit target unit (B, KB, MB, GB, TB)
     * @return converted value in the specified unit
     */
    private double convertBytes(long bytes, String unit) {
        switch (unit.toLowerCase()) {
            case "b": return bytes;
            case "kb": return bytes / KB;
            case "mb": return bytes / MB;
            case "gb": return bytes / GB;
            case "tb": return bytes / TB;
            default: 
                // Default to MB if unit not recognized
                return bytes / MB;
        }
    }

    /**
     * Converts nanosecond count to the specified time unit.
     *
     * @param nanos raw nanosecond count
     * @param unit target unit (ns, ms, s, m/min, h/hr, d)
     * @return converted value in the specified unit
     */
    private double convertNanos(long nanos, String unit) {
        switch (unit.toLowerCase()) {
            case "ns": return nanos;
            case "ms": return nanos / NANOS_PER_MILLI;
            case "s":
            case "seconds": return nanos / NANOS_PER_SECOND;
            case "m":
            case "min":
            case "minutes": return nanos / NANOS_PER_MINUTE;
            case "h":
            case "hr":
            case "hours": return nanos / NANOS_PER_HOUR;
            case "d":
            case "days": return nanos / NANOS_PER_DAY;
            default:
                // Default to seconds if unit not recognized
                return nanos / NANOS_PER_SECOND;
        }
    }
    
    @Override
    public void destroy() {
        // No resources to clean up
    }
}