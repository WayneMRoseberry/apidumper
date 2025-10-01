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

ApiDumper operates in two distinct modes:

### Mode 1: API Analysis (Default)
Analyzes REST API responses and generates schema reports.

### Mode 2: JSON Generation
Generates JSON data from existing schema report files.

### Run with Maven

```bash
# API Analysis Mode
mvn exec:java -Dexec.mainClass="com.example.apidumper.ApiDumper" -Dexec.args="--url https://api.example.com/data"

# JSON Generation Mode  
mvn exec:java -Dexec.mainClass="com.example.apidumper.ApiDumper" -Dexec.args="--generateJson schema-report.json"
```

### Run the JAR file

```bash
# API Analysis Mode
java -jar target/apidumper-1.0.0.jar --url "https://api.example.com/data"

# JSON Generation Mode
java -jar target/apidumper-1.0.0.jar --generateJson schema-report.json
```

### Command Line Options

- `--url` or `-u`: The URL of the REST API endpoint to call (required for API mode only)
- `--dumpSchemaReport` or `-s`: Generate a detailed schema report analyzing the JSON response structure (optional, API mode only)
- `--noDataDump` or `-n`: Suppress output of the response body to console (optional, API mode only)
- `--dumpDistinctValues` or `-d`: Comma-separated list of property names to show all distinct values as JSON arrays (optional, requires `--dumpSchemaReport`)
- `--reportFile` or `-f`: Write the schema report to the specified file instead of console (optional, requires `--dumpSchemaReport`)
- `--generateJson` or `-g`: Generate JSON data from a schema report file based on configurable rules (standalone mode, alternative to API mode)
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
  - Inferred data types for string properties (detects integer, float, boolean, date, time, datetime, guid)
  - Shows comma-separated list when a property contains multiple inferred types
  - Min and max values for all data types (numbers, strings with inferred types, booleans)
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
{
  "schemaReport": [
    {
      "property": "amiibo",
      "count": 1,
      "distinctValues": 1,
      "dataTypes": [
        {
          "type": "array",
          "count": 1,
          "example": "[array]"
        }
      ]
    },
    {
      "property": "amiibo.amiiboSeries",
      "count": 800,
      "distinctValues": 45,
      "dataTypes": [
        {
          "type": "string",
          "count": 800,
          "example": "Animal Crossing",
          "inferredTypes": [
            {
              "type": "string",
              "count": 800
            }
          ]
        }
      ]
    },
    {
      "property": "amiibo.character",
      "count": 800,
      "distinctValues": 350,
      "dataTypes": [
        {
          "type": "string",
          "count": 800,
          "example": "Sandy",
          "inferredTypes": [
            {
              "type": "string",
              "count": 800
            }
          ]
        }
      ]
    },
    {
      "property": "amiibo.type",
      "count": 800,
      "distinctValues": 2,
      "distinctValuesArray": ["Card", "Figure"],
      "dataTypes": [
        {
          "type": "string",
          "count": 800,
          "example": "Card",
          "inferredTypes": [
            {
              "type": "string",
              "count": 800
            }
          ]
        }
      ]
    },
    {
      "property": "someNumericField",
      "count": 500,
      "distinctValues": 250,
      "dataTypes": [
        {
          "type": "number",
          "count": 500,
          "example": 42.5,
          "minValues": {
            "number": "1.0"
          },
          "maxValues": {
            "number": "999.99"
          }
        }
      ]
    },
    {
      "property": "amiibo.release.au",
      "count": 800,
      "distinctValues": 125,
      "dataTypes": [
        {
          "type": "string",
          "count": 775,
          "example": "2016-11-10",
          "inferredTypes": [
            {
              "type": "date",
              "count": 775
            }
          ],
          "minValues": {
            "date": "2015-01-22"
          },
          "maxValues": {
            "date": "2021-11-05"
          }
        },
        {
          "type": "null",
          "count": 25,
          "example": null
        }
      ]
    },
    {
      "property": "userId",
      "count": 1000,
      "distinctValues": 1000,
      "dataTypes": [
        {
          "type": "string",
          "count": 1000,
          "example": "550e8400-e29b-41d4-a716-446655440000",
          "inferredTypes": [
            {
              "type": "guid",
              "count": 1000
            }
          ],
          "minValues": {
            "guid": "00000000-0000-0000-0000-000000000001"
          },
          "maxValues": {
            "guid": "ffffffff-ffff-ffff-ffff-ffffffffffff"
          }
        }
      ]
    },
    {
      "property": "mixedField",
      "count": 100,
      "distinctValues": 100,
      "dataTypes": [
        {
          "type": "string",
          "count": 100,
          "example": "123",
          "inferredTypes": [
            {
              "type": "integer",
              "count": 40
            },
            {
              "type": "date",
              "count": 30
            },
            {
              "type": "string",
              "count": 30
            }
          ],
          "minValues": {
            "integer": "1",
            "date": "2020-01-01",
            "string": "apple"
          },
          "maxValues": {
            "integer": "999",
            "date": "2024-12-31",
            "string": "zebra"
          }
        }
      ]
    }
  ]
}
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
{
  "schemaReport": [
    {
      "property": "amiibo",
      "count": 1,
      "distinctValues": 1,
      "dataTypes": [
        {
          "type": "array",
          "count": 1,
          "example": "[array]"
        }
      ]
    },
    {
      "property": "amiibo.release.au",
      "count": 800,
      "distinctValues": 125,
      "dataTypes": [
        {
          "type": "string",
          "count": 775,
          "example": "2016-11-10",
          "inferredTypes": [
            {
              "type": "date",
              "count": 775
            }
          ],
          "minValues": {
            "date": "2015-01-22"
          },
          "maxValues": {
            "date": "2021-11-05"
          }
        }
      ]
    }
  ]
}
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
{
  "schemaReport": [
    {
      "property": "amiibo.type",
      "count": 800,
      "distinctValues": 2,
      "distinctValuesArray": ["Card", "Figure"],
      "dataTypes": [
        {
          "type": "string",
          "count": 800,
          "example": "Card",
          "inferredTypes": [
            {
              "type": "string",
              "count": 800
            }
          ]
        }
      ]
    },
    {
      "property": "amiibo.gameSeries",
      "count": 800,
      "distinctValues": 35,
      "distinctValuesArray": ["Animal Crossing", "Chibi-Robo!", "Darkstalkers", "Diablo", "Fire Emblem", "Kirby", "Mario Sports Superstars", "Mega Man", "Metroid", "Mii", "Monster Hunter Stories", "Pikmin", "Pokémon", "Shovel Knight", "Splatoon", "Star Fox", "Super Mario", "Super Smash Bros.", "The Legend of Zelda", "Xenoblade Chronicles", "Yo-kai Watch", "Yoshi"],
      "dataTypes": [
        {
          "type": "string",
          "count": 800,
          "example": "Animal Crossing",
          "inferredTypes": [
            {
              "type": "string",
              "count": 800
            }
          ]
        }
      ]
    }
  ]
}
```

### Schema Report to File

```bash
java -jar target/apidumper-1.0.0.jar --url "https://www.amiiboapi.com/api/amiibo/" --dumpSchemaReport --noDataDump --reportFile schema-report.json
```

```
Calling API: https://www.amiiboapi.com/api/amiibo/
--------------------------------------------------
Status Code: 200
Reason Phrase: OK

Schema report written to: schema-report.json
```

The schema report will be written to the specified file instead of being displayed on the console. If there's an error writing to the file, the report will fall back to console output.

### Generate JSON from Schema Report

```bash
java -jar target/apidumper-1.0.0.jar --generateJson schema-report.json
```

This mode generates JSON data from a previously created schema report file. The tool uses configurable rules defined in `apidumper.config` to determine how to generate the data.

#### Configuration File (apidumper.config)

The configuration file defines rules for data generation:

```
# Comments start with #
ruleName=ruleType:description

# Example:
generate-from-example=generate-from-example:Generate JSON using example values from schema report
```

#### Default Rule: generate-from-example

The default rule creates JSON by:
- Using the first data type's example value for each property
- Preserving nested structure (e.g., `user.address.street` becomes nested objects)
- Handling different data types (string, number, boolean, array) appropriately
- Converting array data types to JSON arrays
- Special handling for `"[array]"` and `"[object]"` examples (generates arrays containing empty objects)

**Example:**

Input schema report:
```json
{
  "schemaReport": [
    {
      "property": "user.name",
      "dataTypes": [
        {
          "type": "string",
          "example": "John Doe"
        }
      ]
    },
    {
      "property": "user.age",
      "dataTypes": [
        {
          "type": "number",
          "example": 25
        }
      ]
    },
    {
      "property": "user.hobbies",
      "dataTypes": [
        {
          "type": "array",
          "example": ["reading", "swimming", "coding"]
        }
      ]
    }
  ]
}
```

Output JSON:
```json
{
  "user": {
    "name": "John Doe",
    "age": 25,
    "hobbies": ["reading", "swimming", "coding"]
  }
}
```

#### Complete Workflow Example

1. **Generate schema report:**
```bash
java -jar target/apidumper-1.0.0.jar --url "https://api.example.com/data" --dumpSchemaReport --noDataDump --reportFile schema.json
```

2. **Generate sample data:**
```bash
java -jar target/apidumper-1.0.0.jar --generateJson schema.json > sample-data.json
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