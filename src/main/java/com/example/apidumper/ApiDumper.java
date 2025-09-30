package com.example.apidumper;

import org.apache.commons.cli.*;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import com.google.gson.*;

import java.io.IOException;
import java.util.*;

/**
 * ApiDumper - A command line tool for analyzing REST API responses
 */
public class ApiDumper {
    
    /**
     * Helper method to repeat a string n times (Java 8 compatible)
     */
    private static String repeat(String str, int times) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < times; i++) {
            sb.append(str);
        }
        return sb.toString();
    }
    
    public static void main(String[] args) {
        Options options = createOptions();
        CommandLineParser parser = new DefaultParser();
        
        try {
            CommandLine cmd = parser.parse(options, args);
            
            if (cmd.hasOption("help")) {
                printHelp(options);
                return;
            }
            
            String url = cmd.getOptionValue("url");
            if (url == null || url.trim().isEmpty()) {
                System.err.println("Error: URL is required");
                printHelp(options);
                System.exit(1);
            }
            
            boolean dumpSchemaReport = cmd.hasOption("dumpSchemaReport");
            boolean noDataDump = cmd.hasOption("noDataDump");
            
            callApiAndOutputResponse(url, dumpSchemaReport, noDataDump);
            
        } catch (ParseException e) {
            System.err.println("Error parsing command line arguments: " + e.getMessage());
            printHelp(options);
            System.exit(1);
        }
    }
    
    private static Options createOptions() {
        Options options = new Options();
        
        Option urlOption = Option.builder("u")
                .longOpt("url")
                .hasArg()
                .argName("URL")
                .desc("The URL of the REST API endpoint to call")
                .required()
                .build();
        
        Option helpOption = Option.builder("h")
                .longOpt("help")
                .desc("Display this help message")
                .build();
        
        Option schemaOption = Option.builder("s")
                .longOpt("dumpSchemaReport")
                .desc("Dump a schema report of the JSON response")
                .build();
        
        Option noDataDumpOption = Option.builder("n")
                .longOpt("noDataDump")
                .desc("Suppress output of the response body to console")
                .build();
        
        options.addOption(urlOption);
        options.addOption(helpOption);
        options.addOption(schemaOption);
        options.addOption(noDataDumpOption);
        
        return options;
    }
    
    private static void printHelp(Options options) {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("apidumper", 
                "A command line tool for analyzing REST API responses", 
                options, 
                "Example: java -jar apidumper.jar --url https://api.example.com/data");
    }
    
    private static void callApiAndOutputResponse(String url, boolean dumpSchemaReport, boolean noDataDump) {
        HttpClient client = HttpClients.createDefault();
        HttpGet request = new HttpGet(url);
        
        try {
            System.out.println("Calling API: " + url);
            System.out.println(repeat("-", 50));
            
            HttpResponse response = client.execute(request);
            
            // Output status information
            System.out.println("Status Code: " + response.getStatusLine().getStatusCode());
            System.out.println("Reason Phrase: " + response.getStatusLine().getReasonPhrase());
            System.out.println();
            
            // Get response body
            HttpEntity entity = response.getEntity();
            String responseBody = "";
            if (entity != null) {
                responseBody = EntityUtils.toString(entity);
                EntityUtils.consume(entity);
            }
            
            // Output response body (unless suppressed)
            if (!noDataDump) {
                System.out.println("Response Body:");
                System.out.println(repeat("-", 50));
                System.out.println(responseBody);
            }
            
            // Generate schema report if requested
            if (dumpSchemaReport) {
                System.out.println();
                System.out.println();
                generateSchemaReport(responseBody);
            }
            
        } catch (IOException e) {
            System.err.println("IO Error: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.err.println("Invalid URL: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
    
    private static void generateSchemaReport(String jsonResponse) {
        try {
            System.out.println("Schema Report:");
            System.out.println(repeat("=", 80));
            
            JsonElement element = JsonParser.parseString(jsonResponse);
            
            Map<String, PropertyInfo> propertyMap = new LinkedHashMap<>();
            analyzeJsonElement(element, "", propertyMap);
            
            // Print the report
            for (Map.Entry<String, PropertyInfo> entry : propertyMap.entrySet()) {
                PropertyInfo info = entry.getValue();
                System.out.println();
                System.out.println("Property: " + entry.getKey());
                System.out.println("  Count: " + info.count);
                System.out.println("  Distinct Values: " + info.distinctValues.size());
                System.out.println("  Data Types:");
                
                for (Map.Entry<String, Object> typeEntry : info.typeExamples.entrySet()) {
                    String dataType = typeEntry.getKey();
                    System.out.println("    - " + dataType);
                    System.out.println("      Example: " + formatExample(typeEntry.getValue()));
                    
                    // Show inferred type for strings
                    if (dataType.equals("string") && info.inferredTypesSet.containsKey(dataType)) {
                        Set<String> inferredTypes = info.inferredTypesSet.get(dataType);
                        String inferredTypesList = String.join(", ", inferredTypes);
                        System.out.println("      Inferred Type: " + inferredTypesList);
                    }
                }
            }
            
            System.out.println();
            System.out.println(repeat("=", 80));
            
        } catch (JsonSyntaxException e) {
            System.err.println("Error parsing JSON for schema report: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Error generating schema report: " + e.getMessage());
        }
    }
    
    private static void analyzeJsonElement(JsonElement element, String path, Map<String, PropertyInfo> propertyMap) {
        if (element.isJsonObject()) {
            JsonObject obj = element.getAsJsonObject();
            for (Map.Entry<String, JsonElement> entry : obj.entrySet()) {
                String key = entry.getKey();
                String newPath = path.isEmpty() ? key : path + "." + key;
                JsonElement value = entry.getValue();
                
                recordProperty(newPath, value, propertyMap);
                analyzeJsonElement(value, newPath, propertyMap);
            }
        } else if (element.isJsonArray()) {
            JsonArray array = element.getAsJsonArray();
            for (JsonElement item : array) {
                analyzeJsonElement(item, path, propertyMap);
            }
        }
    }
    
    private static void recordProperty(String propertyName, JsonElement value, Map<String, PropertyInfo> propertyMap) {
        PropertyInfo info = propertyMap.get(propertyName);
        if (info == null) {
            info = new PropertyInfo();
            propertyMap.put(propertyName, info);
        }
        
        info.count++;
        
        String dataType = getJsonType(value);
        if (!info.typeExamples.containsKey(dataType)) {
            info.typeExamples.put(dataType, getExampleValue(value));
        }
        
        // Track distinct values (convert to string representation for comparison)
        String valueStr = getValueAsString(value);
        info.distinctValues.add(valueStr);
        
        // Infer type for string values
        if (dataType.equals("string") && value.isJsonPrimitive()) {
            String strValue = value.getAsString();
            String inferredType = inferDataType(strValue);
            
            // Track all unique inferred types for this property
            if (!info.inferredTypesSet.containsKey(dataType)) {
                info.inferredTypesSet.put(dataType, new LinkedHashSet<>());
            }
            info.inferredTypesSet.get(dataType).add(inferredType);
        }
    }
    
    private static String getJsonType(JsonElement element) {
        if (element.isJsonNull()) {
            return "null";
        } else if (element.isJsonPrimitive()) {
            JsonPrimitive primitive = element.getAsJsonPrimitive();
            if (primitive.isBoolean()) {
                return "boolean";
            } else if (primitive.isNumber()) {
                return "number";
            } else if (primitive.isString()) {
                return "string";
            }
        } else if (element.isJsonArray()) {
            return "array";
        } else if (element.isJsonObject()) {
            return "object";
        }
        return "unknown";
    }
    
    private static Object getExampleValue(JsonElement element) {
        if (element.isJsonNull()) {
            return null;
        } else if (element.isJsonPrimitive()) {
            JsonPrimitive primitive = element.getAsJsonPrimitive();
            if (primitive.isBoolean()) {
                return primitive.getAsBoolean();
            } else if (primitive.isNumber()) {
                return primitive.getAsNumber();
            } else if (primitive.isString()) {
                return primitive.getAsString();
            }
        } else if (element.isJsonArray()) {
            return "[array]";
        } else if (element.isJsonObject()) {
            return "{object}";
        }
        return element.toString();
    }
    
    private static String formatExample(Object example) {
        if (example == null) {
            return "null";
        } else if (example instanceof String) {
            String str = (String) example;
            if (str.length() > 100) {
                return "\"" + str.substring(0, 97) + "...\"";
            }
            return "\"" + str + "\"";
        }
        return example.toString();
    }
    
    private static String getValueAsString(JsonElement element) {
        if (element.isJsonNull()) {
            return "null";
        } else if (element.isJsonPrimitive()) {
            return element.getAsString();
        } else if (element.isJsonArray()) {
            return element.toString();
        } else if (element.isJsonObject()) {
            return element.toString();
        }
        return element.toString();
    }
    
    private static String inferDataType(String value) {
        if (value == null || value.isEmpty()) {
            return "string";
        }
        
        // Check for boolean
        if (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false")) {
            return "boolean";
        }
        
        // Check for integer
        if (value.matches("-?\\d+")) {
            return "integer";
        }
        
        // Check for floating point
        if (value.matches("-?\\d*\\.\\d+([eE][+-]?\\d+)?") || value.matches("-?\\d+\\.\\d*([eE][+-]?\\d+)?")) {
            return "float";
        }
        
        // Check for date formats (ISO 8601 and common formats)
        // DateTime: YYYY-MM-DDTHH:MM:SS or YYYY-MM-DD HH:MM:SS
        if (value.matches("\\d{4}-\\d{2}-\\d{2}[T ]\\d{2}:\\d{2}:\\d{2}(\\.\\d+)?(Z|[+-]\\d{2}:\\d{2})?")) {
            return "datetime";
        }
        
        // Date: YYYY-MM-DD
        if (value.matches("\\d{4}-\\d{2}-\\d{2}")) {
            return "date";
        }
        
        // Date: MM/DD/YYYY or DD/MM/YYYY
        if (value.matches("\\d{1,2}/\\d{1,2}/\\d{4}")) {
            return "date";
        }
        
        // Time: HH:MM:SS or HH:MM
        if (value.matches("\\d{2}:\\d{2}(:\\d{2})?")) {
            return "time";
        }
        
        // Default to string
        return "string";
    }
    
    private static class PropertyInfo {
        int count = 0;
        Map<String, Object> typeExamples = new LinkedHashMap<>();
        Set<String> distinctValues = new HashSet<>();
        Map<String, Set<String>> inferredTypesSet = new LinkedHashMap<>();
    }
}

