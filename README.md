# ApiDumper

A Java command line tool for analyzing REST API responses.

## Prerequisites

- Java 8 or higher (JDK recommended for compilation)
- Maven 3.6 or higher (optional - see manual compilation below)

## Building

### Option 1: Using Maven (if available)

```bash
mvn clean package
```

This will create a standalone JAR file in the `target` directory.

### Option 2: Manual Compilation (if Maven is not available)

1. Download the required JAR files:
   - [Apache Commons CLI 1.4](https://mvnrepository.com/artifact/commons-cli/commons-cli/1.4)
   - [Apache HttpClient 4.5.14](https://mvnrepository.com/artifact/org.apache.httpcomponents/httpclient/4.5.14)
   - [Apache HttpCore 4.4.16](https://mvnrepository.com/artifact/org.apache.httpcomponents/httpcore/4.4.16)
   - [Google Gson 2.10.1](https://mvnrepository.com/artifact/com.google.code.gson/gson/2.10.1)

2. Compile the code:
```bash
javac -cp "commons-cli-1.4.jar:httpclient-4.5.14.jar:httpcore-4.4.16.jar:gson-2.10.1.jar" -d . src/main/java/com/example/apidumper/ApiDumper.java
```

3. Create a JAR file:
```bash
jar cfe apidumper.jar com.example.apidumper.ApiDumper -C . com/ -C . META-INF/
```

## Usage

### Run with Maven

```bash
mvn exec:java -Dexec.mainClass="com.example.apidumper.ApiDumper" -Dexec.args="--url https://api.example.com/data"
```

### Run the JAR file

```bash
java -jar target/apidumper-1.0.0.jar --url "https://api.example.com/data"
```

### Command Line Options

- `--url` or `-u`: The URL of the REST API endpoint to call (required)
- `--dumpSchemaReport` or `-s`: Generate a detailed schema report analyzing the JSON response structure (optional)
- `--noDataDump` or `-n`: Suppress output of the response body to console (optional)
- `--dumpDistinctValues` or `-d`: Comma-separated list of property names to show all distinct values as JSON arrays (optional, requires `--dumpSchemaReport`)
- `--help` or `-h`: Display help message

## Features

- Makes GET requests to specified REST API endpoints
- Displays HTTP status code and reason phrase
- Outputs the complete response body to console (can be suppressed with `--noDataDump`)
- **Schema Report Analysis**: Optional detailed analysis of JSON response structure including:
  - Property names and paths
  - Data types for each property
  - Occurrence count for each property
  - Number of distinct values
  - Example values for each data type
  - Inferred data types for string properties (detects integer, float, boolean, date, time, datetime)
  - Shows comma-separated list when a property contains multiple inferred types
  - Min and max values for each inferred data type
  - Optional distinct values dump for specified properties (as JSON arrays)
- Includes comprehensive error handling for various HTTP and network errors
- Uses Apache HttpClient 4.5.14 for HTTP client functionality (Java 8 compatible)
- Proper resource cleanup with EntityUtils

## Example Output

### Basic Usage

```bash
java -jar target/apidumper-1.0.0.jar --url "https://api.example.com/data"
```

```
Calling API: https://api.example.com/data
--------------------------------------------------
Status Code: 200
Reason Phrase: OK

Response Body:
--------------------------------------------------
{"data": "example response", "status": "success"}
```

### With Schema Report

```bash
java -jar target/apidumper-1.0.0.jar --url "https://www.amiiboapi.com/api/amiibo/" --dumpSchemaReport
```

```
Calling API: https://www.amiiboapi.com/api/amiibo/
--------------------------------------------------
Status Code: 200
Reason Phrase: OK

Response Body:
--------------------------------------------------
{"amiibo": [{"amiiboSeries": "Animal Crossing", "character": "Sandy", ...}]}


Schema Report:
================================================================================

Property: amiibo
  Count: 1
  Distinct Values: 1
  Data Types:
    - array
      Example: [array]

Property: amiibo.amiiboSeries
  Count: 800
  Distinct Values: 45
  Data Types:
    - string
      Example: "Animal Crossing"
      Inferred Type: string

Property: amiibo.character
  Count: 800
  Distinct Values: 350
  Data Types:
    - string
      Example: "Sandy"
      Inferred Type: string

Property: amiibo.type
  Count: 800
  Distinct Values: 2
  Distinct Values Array: ["Card","Figure"]
  Data Types:
    - string
      Example: "Card"
      Inferred Type: string

Property: amiibo.release.au
  Count: 800
  Distinct Values: 125
  Data Types:
    - string
      Example: "2016-11-10"
      Inferred Type: date
      Min Value: date: "2015-01-22"
      Max Value: date: "2021-11-05"
    - null
      Example: null

Property: amiibo.head
  Count: 800
  Distinct Values: 450
  Data Types:
    - string
      Example: "04380001"
      Inferred Type: string

Property: mixedField
  Count: 100
  Distinct Values: 100
  Data Types:
    - string
      Example: "123"
      Inferred Type: integer, date, string
      Min Value: integer: "1", date: "2020-01-01", string: "apple"
      Max Value: integer: "999", date: "2024-12-31", string: "zebra"

... (additional properties)

================================================================================
```

### Schema Report Only (Suppress Response Body)

```bash
java -jar target/apidumper-1.0.0.jar --url "https://www.amiiboapi.com/api/amiibo/" --dumpSchemaReport --noDataDump
```

```
Calling API: https://www.amiiboapi.com/api/amiibo/
--------------------------------------------------
Status Code: 200
Reason Phrase: OK


Schema Report:
================================================================================

Property: amiibo
  Count: 1
  Distinct Values: 1
  Data Types:
    - array
      Example: [array]

Property: amiibo.amiiboSeries
  Count: 800
  Distinct Values: 45
  Data Types:
    - string
      Example: "Animal Crossing"
      Inferred Type: string

Property: amiibo.release.au
  Count: 800
  Distinct Values: 125
  Data Types:
    - string
      Example: "2016-11-10"
      Inferred Type: date
      Min Value: date: "2015-01-22"
      Max Value: date: "2021-11-05"

... (all properties shown without the response body)

================================================================================
```

### With Distinct Values Dump

```bash
java -jar target/apidumper-1.0.0.jar --url "https://www.amiiboapi.com/api/amiibo/" --dumpSchemaReport --noDataDump --dumpDistinctValues "amiibo.type,amiibo.gameSeries"
```

```
Calling API: https://www.amiiboapi.com/api/amiibo/
--------------------------------------------------
Status Code: 200
Reason Phrase: OK


Schema Report:
================================================================================

Property: amiibo.type
  Count: 800
  Distinct Values: 2
  Distinct Values Array: ["Card","Figure"]
  Data Types:
    - string
      Example: "Card"
      Inferred Type: string

Property: amiibo.gameSeries
  Count: 800
  Distinct Values: 35
  Distinct Values Array: ["Animal Crossing","Chibi-Robo!","Darkstalkers","Diablo","Fire Emblem","Kirby","Mario Sports Superstars","Mega Man","Metroid","Mii","Monster Hunter Stories","Pikmin","Pokémon","Shovel Knight","Splatoon","Star Fox","Super Mario","Super Smash Bros.","The Legend of Zelda","Xenoblade Chronicles","Yo-kai Watch","Yoshi"]
  Data Types:
    - string
      Example: "Animal Crossing"
      Inferred Type: string

================================================================================
```

## Error Handling

The tool handles various types of errors:
- IO errors (network issues)
- Interrupted requests
- Invalid URLs
- General exceptions

## Dependencies

- Apache Commons CLI 1.4 for command line argument parsing
- Apache HttpClient 4.5.14 for HTTP requests
- Apache HttpCore 4.4.16 (transitive dependency of HttpClient)
- Google Gson 2.10.1 for JSON parsing and schema analysis