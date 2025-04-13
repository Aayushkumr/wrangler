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
import io.cdap.wrangler.api.DirectiveExecutionException;
import io.cdap.wrangler.api.ExecutorContext;
import io.cdap.wrangler.api.Row;
import io.cdap.wrangler.api.parser.ByteSize;
import io.cdap.wrangler.api.parser.ColumnName;
import io.cdap.wrangler.api.parser.Text;
import io.cdap.wrangler.api.parser.TimeDuration;
import io.cdap.wrangler.api.parser.TokenType;
import io.cdap.wrangler.api.parser.UsageDefinition;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.List;

public class AggregateStatsTest {
    private AggregateStats directive;
    private ExecutorContext context;
    
    @Before
    public void setup() {
        directive = new AggregateStats();
        context = Mockito.mock(ExecutorContext.class);
    }
    
    @Test
    public void testAggregation() throws Exception {
        List<Row> rows = Arrays.asList(
            new Row("data_transfer_size", new ByteSize("10KB")).add("response_time", new TimeDuration("100ms")),
            new Row("data_transfer_size", new ByteSize("5MB")).add("response_time", new TimeDuration("2s"))
        );
        
        Arguments args = createArguments(
            "data_transfer_size",
            "response_time", "total_size", "total_time",
            "MB", "seconds", "sum"
         );
        directive.initialize(args);
        
        List<Row> results = directive.execute(rows, context);
        
        // Check if there's one result row
        Assert.assertEquals(1, results.size());
        
        // Check sum aggregation results (converted to MB and seconds)
        Row resultRow = results.get(0);
        Assert.assertEquals(5.01, (Double) resultRow.getValue("total_size"), 0.01); // 10KB + 5MB ≈ 5.01MB
        Assert.assertEquals(2.1, (Double) resultRow.getValue("total_time"), 0.01);  // 100ms + 2s = 2.1s
    }
    
    @Test
    public void testAverageAggregation() throws Exception {
        List<Row> rows = Arrays.asList(
            new Row("data_transfer_size", new ByteSize("10KB"))
            .add("response_time", new TimeDuration("100ms")),
            new Row("data_transfer_size", new ByteSize("5MB"))
            .add("response_time", new TimeDuration("2s"))
        );
        
        Arguments args = createArguments(
            "data_transfer_size", "response_time",
            "avg_size", "avg_time",
            "KB", "ms", "average"
        );
        directive.initialize(args);
        
        List<Row> results = directive.execute(rows, context);
        
        // Check average aggregation results (converted to KB and ms)
        Row resultRow = results.get(0);
        Assert.assertEquals(2566.0, (Double) resultRow.getValue("avg_size"), 1.0); // Average of 10KB and 5MB in KB
        Assert.assertEquals(1050.0, (Double) resultRow.getValue("avg_time"), 0.1); // Average of 100ms and 2s in ms
    }
    
    @Test
    public void testEmptyRows() throws Exception {
        List<Row> rows = Arrays.asList();
        
        Arguments args = createArguments(
            "data_transfer_size", "response_time",
            "total_size", "total_time",
            "MB", "s", "sum"
        );
        directive.initialize(args);
        
        List<Row> results = directive.execute(rows, context);
        
        // Should return empty list
        Assert.assertEquals(0, results.size());
    }
    
    @Test(expected = DirectiveExecutionException.class)
    public void testInvalidTypeValues() throws Exception {
        List<Row> rows = Arrays.asList(
            new Row("data_transfer_size", "NotAByteSize").add("response_time", new TimeDuration("100ms"))
        );
        
        Arguments args = createArguments(
            "data_transfer_size", "response_time",
            "total_size", "total_time",
            "MB", "s", "sum"
        );
        directive.initialize(args);
        
        directive.execute(rows, context);
    }
    
    /**
     * Helper method to create Arguments object for testing
     */
    private Arguments createArguments(String sizeCol, String timeCol, String sizeTarget, String timeTarget, 
                                     String sizeUnit, String timeUnit, String aggType) {
        // Create a mock Arguments object with the expected values
        Arguments args = Mockito.mock(Arguments.class);
        
        // Set up column names
        ColumnName sizeColName = new ColumnName(sizeCol);
        ColumnName timeColName = new ColumnName(timeCol);
        ColumnName sizeTargetName = new ColumnName(sizeTarget);
        ColumnName timeTargetName = new ColumnName(timeTarget);
        
        // Set up text values
        Text sizeUnitText = new Text(sizeUnit);
        Text timeUnitText = new Text(timeUnit);
        Text aggTypeText = new Text(aggType);
        
        // Configure the mock
        Mockito.when(args.value("sizeSourceCol")).thenReturn(sizeColName);
        Mockito.when(args.value("timeSourceCol")).thenReturn(timeColName);
        Mockito.when(args.value("sizeTargetCol")).thenReturn(sizeTargetName);
        Mockito.when(args.value("timeTargetCol")).thenReturn(timeTargetName);
        Mockito.when(args.value("sizeOutputUnit")).thenReturn(sizeUnitText);
        Mockito.when(args.value("timeOutputUnit")).thenReturn(timeUnitText);
        Mockito.when(args.value("aggregationType")).thenReturn(aggTypeText);
        
        Mockito.when(args.contains("sizeOutputUnit")).thenReturn(true);
        Mockito.when(args.contains("timeOutputUnit")).thenReturn(true);
        Mockito.when(args.contains("aggregationType")).thenReturn(true);
        
        return args;
    }
}
