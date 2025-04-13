# Wrangler Assignment - Data Aggregation Extensions

## Overview

This assignment implements new data aggregation capabilities for the CDAP Wrangler framework, focusing on storage size and time duration values processing with unit conversion support.

## Features Implemented

### 1. New Token Types

#### ByteSize

A new token type for representing storage capacity values with unit support:

```java
// Examples of ByteSize values
ByteSize size1 = new ByteSize("10KB");  // 10 kilobytes
ByteSize size2 = new ByteSize("1.5MB"); // 1.5 megabytes
ByteSize size3 = new ByteSize("2 GB");  // 2 gigabytes
```

Supported units:
- B (bytes)
- KB (kilobytes)
- MB (megabytes)
- GB (gigabytes)
- TB (terabytes)

#### TimeDuration

A new token type for representing time interval values with unit support:

```java
// Examples of TimeDuration values
TimeDuration time1 = new TimeDuration("100ms");  // 100 milliseconds
TimeDuration time2 = new TimeDuration("2.5s");   // 2.5 seconds
TimeDuration time3 = new TimeDuration("1 min");  // 1 minute
TimeDuration time4 = new TimeDuration("0.5 hr"); // half an hour
```

Supported units:
- ns (nanoseconds)
- ms (milliseconds)
- s/seconds
- m/min/minutes
- h/hr/hours
- d/days

### 2. AggregateStats Directive

Implemented a new directive for aggregating ByteSize and TimeDuration values across rows with unit conversion support.

#### Syntax

```
aggregate-stats :size_column :time_column :size_target :time_target [size_unit] [time_unit] [aggregation_type]
```

Parameters:
- `size_column`: Source column containing ByteSize values
- `time_column`: Source column containing TimeDuration values
- `size_target`: Target column for aggregated size result
- `time_target`: Target column for aggregated time result
- `size_unit`: (Optional) Output unit for size (B, KB, MB, GB, TB), defaults to MB
- `time_unit`: (Optional) Output unit for time (ns, ms, s, min, hr, days), defaults to seconds
- `aggregation_type`: (Optional) Type of aggregation (sum or average), defaults to sum

### 3. Examples

#### Example 1: Sum Aggregation

Input data:
| data_transfer_size | response_time |
|--------------------|---------------|
| 10KB               | 100ms         |
| 5MB                | 2s            |
| 100KB              | 300ms         |

Directive:
```
aggregate-stats :data_transfer_size :response_time :total_size :total_time MB seconds sum
```

Output:
| total_size | total_time |
|------------|------------|
| 5.11       | 2.4        |

#### Example 2: Average Aggregation

Using the same input data:

Directive:
```
aggregate-stats :data_transfer_size :response_time :avg_size :avg_time KB ms average
```

Output:
| avg_size | avg_time |
|----------|----------|
| 1736.7   | 800      |

## Testing

Comprehensive test suites were developed for each component:

### ByteSizeTest

- Tests for parsing all supported units (B, KB, MB, GB, TB)
- Unit conversion tests
- Edge case testing (zero values, decimals, large numbers)
- Error handling tests (invalid format, negative values, invalid units)
- Object equality and toString tests

### TimeDurationTest

- Tests for all supported time units
- Time unit conversion tests
- Edge case coverage
- Error handling for invalid inputs
- Object comparison tests

### AggregateStatsTest

- Sum aggregation tests
- Average aggregation tests
- Empty input handling
- Invalid input type handling

## Implementation Details

1. **ByteSize and TimeDuration Classes**
   - Parse string representations with units
   - Store standardized values (bytes and nanoseconds)
   - Provide unit conversion methods
   - Support mathematical operations

2. **AggregateStats Directive**
   - Validates input data types
   - Aggregates values across rows
   - Converts results to requested output units
   - Handles both sum and average aggregations

## Usage in Real-World Scenarios

This directive is particularly useful for:

- Analyzing file transfer statistics
- Processing API response metrics
- System monitoring data aggregation
- Performance testing result analysis
- Network traffic analysis
- Database operation timing summaries