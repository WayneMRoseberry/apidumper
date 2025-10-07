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
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.File;
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
            
            String generateJsonFile = cmd.getOptionValue("generateJson");
            String jsonFile = cmd.getOptionValue("jsonFile");
            String url = cmd.getOptionValue("url");
            
            // Check for mutual exclusivity between jsonFile and url
            if (jsonFile != null && url != null) {
                System.err.println("Error: --jsonFile and --url are mutually exclusive. Use only one of them.");
                printHelp(options);
                System.exit(1);
            }
            
            if (generateJsonFile != null && !generateJsonFile.trim().isEmpty()) {
                // Generate JSON from schema file - standalone mode
                String ruleName = cmd.getOptionValue("rule");
                generateJsonFromSchema(generateJsonFile, ruleName);
            } else if (jsonFile != null && !jsonFile.trim().isEmpty()) {
                // JSON file mode - read from file
                boolean dumpSchemaReport = cmd.hasOption("dumpSchemaReport");
                boolean noDataDump = cmd.hasOption("noDataDump");
                String dumpDistinctValues = cmd.getOptionValue("dumpDistinctValues", "");
                String reportFile = cmd.getOptionValue("reportFile");
                
                processJsonFile(jsonFile, dumpSchemaReport, noDataDump, dumpDistinctValues, reportFile);
            } else {
                // Normal API call mode - URL is required
                if (url == null || url.trim().isEmpty()) {
                    System.err.println("Error: URL is required for API mode, or use --jsonFile for file mode, or --generateJson for JSON generation mode");
                    printHelp(options);
                    System.exit(1);
                }
                
                boolean dumpSchemaReport = cmd.hasOption("dumpSchemaReport");
                boolean noDataDump = cmd.hasOption("noDataDump");
                String dumpDistinctValues = cmd.getOptionValue("dumpDistinctValues", "");
                String reportFile = cmd.getOptionValue("reportFile");
                
                callApiAndOutputResponse(url, dumpSchemaReport, noDataDump, dumpDistinctValues, reportFile);
            }
            
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
                .desc("The URL of the REST API endpoint to call (required for API mode)")
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
        
        Option distinctValuesOption = Option.builder("d")
                .longOpt("dumpDistinctValues")
                .hasArg()
                .argName("PROPERTIES")
                .desc("Comma-separated list of property names to dump distinct values for")
                .build();
        
        Option reportFileOption = Option.builder("f")
                .longOpt("reportFile")
                .hasArg()
                .argName("FILE")
                .desc("Write the schema report to the specified file instead of console")
                .build();
        
        Option generateJsonOption = Option.builder("g")
                .longOpt("generateJson")
                .hasArg()
                .argName("SCHEMA_FILE")
                .desc("Generate JSON data from a schema report file based on configurable rules")
                .build();
        
        Option ruleOption = Option.builder("r")
                .longOpt("rule")
                .hasArg()
                .argName("RULE_NAME")
                .desc("Specify which rule to use for JSON generation (default: generate-from-example)")
                .build();
        
        Option jsonFileOption = Option.builder("j")
                .longOpt("jsonFile")
                .hasArg()
                .argName("FILE")
                .desc("Read JSON from a file instead of making an API call")
                .build();
        
        options.addOption(urlOption);
        options.addOption(helpOption);
        options.addOption(schemaOption);
        options.addOption(noDataDumpOption);
        options.addOption(distinctValuesOption);
        options.addOption(reportFileOption);
        options.addOption(generateJsonOption);
        options.addOption(ruleOption);
        options.addOption(jsonFileOption);
        
        return options;
    }
    
    private static void printHelp(Options options) {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("apidumper", 
                "A command line tool for analyzing REST API responses", 
                options, 
                "Example: java -jar apidumper.jar --url https://api.example.com/data");
    }
    
    private static void callApiAndOutputResponse(String url, boolean dumpSchemaReport, boolean noDataDump, String dumpDistinctValues, String reportFile) {
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
                generateSchemaReport(responseBody, dumpDistinctValues, reportFile);
            }
            
        } catch (IOException e) {
            System.err.println("IO Error: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.err.println("Invalid URL: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
    
    private static void processJsonFile(String jsonFilePath, boolean dumpSchemaReport, boolean noDataDump, 
                                       String dumpDistinctValues, String reportFile) {
        try {
            System.out.println("Reading JSON from file: " + jsonFilePath);
            System.out.println(repeat("-", 50));
            
            // Read JSON content from file
            String jsonContent = readJsonFromFile(jsonFilePath);
            if (jsonContent == null) {
                System.err.println("Error: Could not read JSON content from file: " + jsonFilePath);
                return;
            }
            
            // Output JSON content (unless suppressed)
            if (!noDataDump) {
                System.out.println("JSON Content:");
                System.out.println(repeat("-", 50));
                System.out.println(jsonContent);
            }
            
            // Generate schema report if requested
            if (dumpSchemaReport) {
                System.out.println();
                System.out.println();
                generateSchemaReport(jsonContent, dumpDistinctValues, reportFile);
            }
            
        } catch (Exception e) {
            System.err.println("Error processing JSON file: " + e.getMessage());
        }
    }
    
    private static String readJsonFromFile(String filePath) {
        try {
            File file = new File(filePath);
            if (!file.exists()) {
                System.err.println("Error: File does not exist: " + filePath);
                return null;
            }
            
            if (!file.canRead()) {
                System.err.println("Error: Cannot read file: " + filePath);
                return null;
            }
            
            StringBuilder content = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    content.append(line).append("\n");
                }
            }
            
            // TODO: Remove this bug - always returning null to cause test failure
            // This should be removed to fix the file reading functionality
            return null;
            
            // This line will never be reached due to the bug above
            //return content.toString().trim();
            
        } catch (IOException e) {
            System.err.println("IO Error reading file: " + e.getMessage());
            return null;
        }
    }
    
    private static void generateSchemaReport(String jsonResponse, String dumpDistinctValues, String reportFile) {
        String schemaReportJson = generateSchemaReportJson(jsonResponse, dumpDistinctValues);
        outputSchemaReport(schemaReportJson, reportFile);
    }
    
    private static String generateSchemaReportJson(String jsonResponse, String dumpDistinctValues) {
        try {
            // Check if response is empty or null
            if (jsonResponse == null || jsonResponse.trim().isEmpty()) {
                System.err.println("Error: Response body is empty. Cannot generate schema report.");
                return null;
            }
            
            JsonElement element = JsonParser.parseString(jsonResponse);
            
            Map<String, PropertyInfo> propertyMap = new LinkedHashMap<>();
            analyzeJsonElement(element, "", propertyMap);
            
            // Parse the properties to dump distinct values for
            Set<String> distinctValueProps = new HashSet<>();
            if (dumpDistinctValues != null && !dumpDistinctValues.trim().isEmpty()) {
                String[] props = dumpDistinctValues.split(",");
                for (String prop : props) {
                    distinctValueProps.add(prop.trim());
                }
            }
            
            // Build the JSON report
            Map<String, Object> reportMap = new LinkedHashMap<>();
            List<Map<String, Object>> properties = new ArrayList<>();
            
            for (Map.Entry<String, PropertyInfo> entry : propertyMap.entrySet()) {
                PropertyInfo info = entry.getValue();
                String propertyName = entry.getKey();
                
                Map<String, Object> propertyReport = new LinkedHashMap<>();
                propertyReport.put("property", propertyName);
                propertyReport.put("count", info.count);
                propertyReport.put("distinctValues", info.distinctValues.size());
                
                // Add distinct values array if requested
                if (distinctValueProps.contains(propertyName)) {
                    List<String> sortedValues = new ArrayList<>(info.distinctValues);
                    Collections.sort(sortedValues);
                    propertyReport.put("distinctValuesArray", sortedValues);
                }
                
                // Build data types array
                List<Map<String, Object>> dataTypesList = new ArrayList<>();
                for (Map.Entry<String, Object> typeEntry : info.typeExamples.entrySet()) {
                    String dataType = typeEntry.getKey();
                    Map<String, Object> dataTypeInfo = new LinkedHashMap<>();
                    dataTypeInfo.put("type", dataType);
                    dataTypeInfo.put("count", info.typeCounts.getOrDefault(dataType, 0));
                    dataTypeInfo.put("example", typeEntry.getValue());
                    
                    // Add inferred types for strings with their counts
                    if (dataType.equals("string") && info.inferredTypesSet.containsKey(dataType)) {
                        Set<String> inferredTypes = info.inferredTypesSet.get(dataType);
                        Map<String, Integer> inferredCounts = info.inferredTypeCounts.get(dataType);
                        
                        // Build inferred types array with counts
                        List<Map<String, Object>> inferredTypesList = new ArrayList<>();
                        for (String inferredType : inferredTypes) {
                            Map<String, Object> inferredTypeInfo = new LinkedHashMap<>();
                            inferredTypeInfo.put("type", inferredType);
                            inferredTypeInfo.put("count", inferredCounts.getOrDefault(inferredType, 0));
                            inferredTypesList.add(inferredTypeInfo);
                        }
                        dataTypeInfo.put("inferredTypes", inferredTypesList);
                    }
                    
                    // Add min/max values
                    if (info.minValues.containsKey(dataType) && !info.minValues.get(dataType).isEmpty()) {
                        dataTypeInfo.put("minValues", info.minValues.get(dataType));
                    }
                    if (info.maxValues.containsKey(dataType) && !info.maxValues.get(dataType).isEmpty()) {
                        dataTypeInfo.put("maxValues", info.maxValues.get(dataType));
                    }
                    
                    dataTypesList.add(dataTypeInfo);
                }
                
                propertyReport.put("dataTypes", dataTypesList);
                properties.add(propertyReport);
            }
            
            reportMap.put("schemaReport", properties);
            
            // Output as formatted JSON
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            return gson.toJson(reportMap);
            
        } catch (JsonSyntaxException e) {
            System.err.println("Error parsing JSON for schema report: " + e.getMessage());
            System.err.println("Response preview (first 500 chars):");
            if (jsonResponse != null) {
                String preview = jsonResponse.length() > 500 ? jsonResponse.substring(0, 500) + "..." : jsonResponse;
                System.err.println(preview);
            }
            System.err.println("\nThe response may not be valid JSON. Common causes:");
            System.err.println("  - The API returned HTML (error page) instead of JSON");
            System.err.println("  - The API returned plain text");
            System.err.println("  - The response is malformed JSON");
            System.err.println("\nTip: Use --noDataDump to suppress response body and see only this error.");
            return null;
        } catch (Exception e) {
            System.err.println("Error generating schema report: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    private static void outputSchemaReport(String schemaReportJson, String reportFile) {
        if (schemaReportJson == null) {
            return; // Error already handled in generateSchemaReportJson
        }
        
        if (reportFile != null && !reportFile.trim().isEmpty()) {
            // Write to file
            try (PrintWriter writer = new PrintWriter(new FileWriter(reportFile))) {
                writer.println(schemaReportJson);
                System.out.println("Schema report written to: " + reportFile);
            } catch (IOException e) {
                System.err.println("Error writing schema report to file: " + e.getMessage());
                // Fall back to console output
                System.out.println();
                System.out.println("Schema Report:");
                System.out.println(schemaReportJson);
            }
        } else {
            // Output to console
            System.out.println();
            System.out.println("Schema Report:");
            System.out.println(schemaReportJson);
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
        
        // Track data type counts
        info.typeCounts.put(dataType, info.typeCounts.getOrDefault(dataType, 0) + 1);
        
        // Track distinct values (convert to string representation for comparison)
        String valueStr = getValueAsString(value);
        info.distinctValues.add(valueStr);
        
        // Track min/max values for all types
        if (!info.minValues.containsKey(dataType)) {
            info.minValues.put(dataType, new LinkedHashMap<>());
        }
        if (!info.maxValues.containsKey(dataType)) {
            info.maxValues.put(dataType, new LinkedHashMap<>());
        }
        
        // Handle string values with inferred types
        if (dataType.equals("string") && value.isJsonPrimitive()) {
            String strValue = value.getAsString();
            String inferredType = inferDataType(strValue);
            
            // Track all unique inferred types for this property
            if (!info.inferredTypesSet.containsKey(dataType)) {
                info.inferredTypesSet.put(dataType, new LinkedHashSet<>());
            }
            info.inferredTypesSet.get(dataType).add(inferredType);
            
            // Track inferred type counts
            if (!info.inferredTypeCounts.containsKey(dataType)) {
                info.inferredTypeCounts.put(dataType, new LinkedHashMap<>());
            }
            Map<String, Integer> inferredCounts = info.inferredTypeCounts.get(dataType);
            inferredCounts.put(inferredType, inferredCounts.getOrDefault(inferredType, 0) + 1);
            
            updateMinMaxValues(info.minValues.get(dataType), info.maxValues.get(dataType), inferredType, strValue);
        }
        // Handle numeric types directly
        else if (dataType.equals("number") && value.isJsonPrimitive()) {
            JsonPrimitive primitive = value.getAsJsonPrimitive();
            updateMinMaxValuesForNumber(info.minValues.get(dataType), info.maxValues.get(dataType), primitive);
        }
        // Handle boolean types
        else if (dataType.equals("boolean") && value.isJsonPrimitive()) {
            String boolValue = value.getAsString();
            updateMinMaxValues(info.minValues.get(dataType), info.maxValues.get(dataType), "boolean", boolValue);
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
    
    
    private static void updateMinMaxValuesForNumber(Map<String, String> minMap, Map<String, String> maxMap, JsonPrimitive primitive) {
        try {
            double doubleValue = primitive.getAsDouble();
            String valueStr = primitive.getAsString();
            
            if (!minMap.containsKey("number")) {
                minMap.put("number", valueStr);
                maxMap.put("number", valueStr);
            } else {
                double currentMin = Double.parseDouble(minMap.get("number"));
                double currentMax = Double.parseDouble(maxMap.get("number"));
                if (doubleValue < currentMin) {
                    minMap.put("number", valueStr);
                }
                if (doubleValue > currentMax) {
                    maxMap.put("number", valueStr);
                }
            }
        } catch (NumberFormatException e) {
            // Skip if parsing fails
        }
    }
    
    private static void updateMinMaxValues(Map<String, String> minMap, Map<String, String> maxMap, String inferredType, String value) {
        // Update min/max based on inferred type
        switch (inferredType) {
            case "integer":
                try {
                    long longValue = Long.parseLong(value);
                    if (!minMap.containsKey(inferredType)) {
                        minMap.put(inferredType, value);
                        maxMap.put(inferredType, value);
                    } else {
                        long currentMin = Long.parseLong(minMap.get(inferredType));
                        long currentMax = Long.parseLong(maxMap.get(inferredType));
                        if (longValue < currentMin) {
                            minMap.put(inferredType, value);
                        }
                        if (longValue > currentMax) {
                            maxMap.put(inferredType, value);
                        }
                    }
                } catch (NumberFormatException e) {
                    // Skip if parsing fails
                }
                break;
                
            case "float":
                try {
                    double doubleValue = Double.parseDouble(value);
                    if (!minMap.containsKey(inferredType)) {
                        minMap.put(inferredType, value);
                        maxMap.put(inferredType, value);
                    } else {
                        double currentMin = Double.parseDouble(minMap.get(inferredType));
                        double currentMax = Double.parseDouble(maxMap.get(inferredType));
                        if (doubleValue < currentMin) {
                            minMap.put(inferredType, value);
                        }
                        if (doubleValue > currentMax) {
                            maxMap.put(inferredType, value);
                        }
                    }
                } catch (NumberFormatException e) {
                    // Skip if parsing fails
                }
                break;
                
            case "date":
            case "datetime":
            case "time":
                // For date/time types, compare lexicographically (works for ISO 8601)
                if (!minMap.containsKey(inferredType)) {
                    minMap.put(inferredType, value);
                    maxMap.put(inferredType, value);
                } else {
                    String currentMin = minMap.get(inferredType);
                    String currentMax = maxMap.get(inferredType);
                    if (value.compareTo(currentMin) < 0) {
                        minMap.put(inferredType, value);
                    }
                    if (value.compareTo(currentMax) > 0) {
                        maxMap.put(inferredType, value);
                    }
                }
                break;
                
            case "guid":
                // For GUIDs, compare lexicographically
                if (!minMap.containsKey(inferredType)) {
                    minMap.put(inferredType, value);
                    maxMap.put(inferredType, value);
                } else {
                    String currentMin = minMap.get(inferredType);
                    String currentMax = maxMap.get(inferredType);
                    if (value.compareTo(currentMin) < 0) {
                        minMap.put(inferredType, value);
                    }
                    if (value.compareTo(currentMax) > 0) {
                        maxMap.put(inferredType, value);
                    }
                }
                break;
                
            case "string":
                // For strings, compare lexicographically
                if (!minMap.containsKey(inferredType)) {
                    minMap.put(inferredType, value);
                    maxMap.put(inferredType, value);
                } else {
                    String currentMin = minMap.get(inferredType);
                    String currentMax = maxMap.get(inferredType);
                    if (value.compareTo(currentMin) < 0) {
                        minMap.put(inferredType, value);
                    }
                    if (value.compareTo(currentMax) > 0) {
                        maxMap.put(inferredType, value);
                    }
                }
                break;
                
            case "boolean":
                // For boolean, min is false, max is true
                if (!minMap.containsKey(inferredType)) {
                    minMap.put(inferredType, value);
                    maxMap.put(inferredType, value);
                } else {
                    if (value.equalsIgnoreCase("false")) {
                        minMap.put(inferredType, value);
                    }
                    if (value.equalsIgnoreCase("true")) {
                        maxMap.put(inferredType, value);
                    }
                }
                break;
        }
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
        
        // Check for GUID/UUID (with or without hyphens, case-insensitive)
        // Standard format: xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx
        // Compact format: xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
        if (value.matches("(?i)[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}")) {
            return "guid";
        }
        if (value.matches("(?i)[0-9a-f]{32}")) {
            return "guid";
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
        Map<String, Integer> typeCounts = new LinkedHashMap<>();
        Set<String> distinctValues = new HashSet<>();
        Map<String, Set<String>> inferredTypesSet = new LinkedHashMap<>();
        Map<String, Map<String, Integer>> inferredTypeCounts = new LinkedHashMap<>();
        Map<String, Map<String, String>> minValues = new LinkedHashMap<>();
        Map<String, Map<String, String>> maxValues = new LinkedHashMap<>();
    }
    
    // Methods for JSON generation from schema
    private static void generateJsonFromSchema(String schemaFile, String ruleName) {
        try {
            // Read schema report
            String schemaJson = readFile(schemaFile);
            SchemaReport schemaReport = parseSchemaReport(schemaJson);
            
            // Read configuration
            Map<String, RuleConfig> rules = readConfig("apidumper.config");
            
            // Output result
            Gson gson = new GsonBuilder().setPrettyPrinting().serializeNulls().create();
            
            if (ruleName != null && !ruleName.trim().isEmpty()) {
                // Apply specified rule
                if (!rules.containsKey(ruleName)) {
                    System.err.println("Error: Rule '" + ruleName + "' not found in configuration");
                    System.err.println("Available rules: " + String.join(", ", rules.keySet()));
                    System.exit(1);
                }
                
                RuleConfig rule = rules.get(ruleName);
                executeRule(schemaReport, rule, ruleName, gson);
            } else {
                // Execute all rules
                for (Map.Entry<String, RuleConfig> entry : rules.entrySet()) {
                    String currentRuleName = entry.getKey();
                    RuleConfig rule = entry.getValue();
                    executeRule(schemaReport, rule, currentRuleName, gson);
                }
            }
            
        } catch (IOException e) {
            System.err.println("Error reading schema file: " + e.getMessage());
            System.exit(1);
        } catch (Exception e) {
            System.err.println("Error generating JSON: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
    
    private static void executeRule(SchemaReport schemaReport, RuleConfig rule, String ruleName, Gson gson) {
        if ("missing-properties".equals(rule.type.toLowerCase())) {
            // Special handling for missing-properties rule
            generateMissingPropertiesOutput(schemaReport, gson);
        } else if ("nullvalues".equals(rule.type.toLowerCase())) {
            // Special handling for nullValues rule
            generateNullValuesOutput(schemaReport, gson);
        } else if ("emptyvalues".equals(rule.type.toLowerCase())) {
            // Special handling for emptyValues rule
            generateEmptyValuesOutput(schemaReport, gson);
        } else if ("minmaxvalue".equals(rule.type.toLowerCase())) {
            // Special handling for minmaxvalue rule
            generateMinMaxValueOutput(schemaReport, gson);
        } else if ("distinctvalues".equals(rule.type.toLowerCase())) {
            // Special handling for distinctValues rule
            generateDistinctValuesOutput(schemaReport, gson);
        } else {
            // Standard single JSON output
            JsonObject result = applyRule(schemaReport, rule);
            System.out.println(ruleName);
            System.out.println();
            System.out.println(gson.toJson(result));
        }
    }
    
    private static String readFile(String filename) throws IOException {
        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
        }
        return content.toString();
    }
    
    private static SchemaReport parseSchemaReport(String json) {
        Gson gson = new Gson();
        JsonObject jsonObject = JsonParser.parseString(json).getAsJsonObject();
        return gson.fromJson(jsonObject, SchemaReport.class);
    }
    
    private static Map<String, RuleConfig> readConfig(String configFile) throws IOException {
        Map<String, RuleConfig> rules = new LinkedHashMap<>();
        
        // Default generate-from-example rule
        RuleConfig generateFromExampleRule = new RuleConfig();
        generateFromExampleRule.type = "generate-from-example";
        generateFromExampleRule.description = "Generate JSON using example values from schema report";
        rules.put("generate-from-example", generateFromExampleRule);
        
        // Default missing-properties rule
        RuleConfig missingPropertiesRule = new RuleConfig();
        missingPropertiesRule.type = "missing-properties";
        missingPropertiesRule.description = "Generate JSON examples with each property missing individually";
        rules.put("missing-properties", missingPropertiesRule);
        
        // Default nullValues rule
        RuleConfig nullValuesRule = new RuleConfig();
        nullValuesRule.type = "nullValues";
        nullValuesRule.description = "Generate JSON examples with each property set to null";
        rules.put("nullValues", nullValuesRule);
        
        // Default emptyValues rule
        RuleConfig emptyValuesRule = new RuleConfig();
        emptyValuesRule.type = "emptyValues";
        emptyValuesRule.description = "Generate JSON examples with each property set to empty";
        rules.put("emptyValues", emptyValuesRule);
        
        // Default minmaxvalue rule
        RuleConfig minmaxvalueRule = new RuleConfig();
        minmaxvalueRule.type = "minmaxvalue";
        minmaxvalueRule.description = "Generate JSON examples using minimum and maximum values from schema report";
        rules.put("minmaxvalue", minmaxvalueRule);
        
        // Default distinctValues rule
        RuleConfig distinctValuesRule = new RuleConfig();
        distinctValuesRule.type = "distinctValues";
        distinctValuesRule.description = "Generate JSON examples using each distinct value for each property";
        rules.put("distinctValues", distinctValuesRule);
        
        // Try to read config file if it exists
        File config = new File(configFile);
        if (config.exists()) {
            String configContent = readFile(configFile);
            parseConfigFile(configContent, rules);
        }
        
        return rules;
    }
    
    private static void parseConfigFile(String configContent, Map<String, RuleConfig> rules) {
        // Simple config parser - one rule per line
        // Format: ruleName=ruleType:description
        String[] lines = configContent.split("\n");
        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty() || line.startsWith("#")) {
                continue; // Skip empty lines and comments
            }
            
            if (line.contains("=")) {
                String[] parts = line.split("=", 2);
                if (parts.length == 2) {
                    String ruleName = parts[0].trim();
                    String ruleDef = parts[1].trim();
                    
                    RuleConfig rule = new RuleConfig();
                    if (ruleDef.contains(":")) {
                        String[] ruleParts = ruleDef.split(":", 2);
                        rule.type = ruleParts[0].trim();
                        rule.description = ruleParts[1].trim();
                    } else {
                        rule.type = ruleDef;
                        rule.description = "Custom rule: " + ruleDef;
                    }
                    rules.put(ruleName, rule);
                }
            }
        }
    }
    
    private static JsonObject applyRule(SchemaReport schemaReport, RuleConfig rule) {
        switch (rule.type.toLowerCase()) {
            case "generate-from-example":
                return generateFromExample(schemaReport);
            default:
                throw new IllegalArgumentException("Unknown rule type: " + rule.type);
        }
    }
    
    private static JsonObject generateFromExample(SchemaReport schemaReport) {
        JsonObject result = new JsonObject();
        
        for (SchemaProperty property : schemaReport.schemaReport) {
            setPropertyValue(result, property.property, property);
        }
        
        return result;
    }
    
    private static void generateMissingPropertiesOutput(SchemaReport schemaReport, Gson gson) {
        for (SchemaProperty property : schemaReport.schemaReport) {
            // Generate JSON with this property missing
            JsonObject result = generateMissingProperties(schemaReport, property.property);
            
            // Output format: newline, rule.propertyName, newline, JSON
            System.out.println();
            System.out.println("missing-properties." + property.property);
            System.out.println();
            System.out.println(gson.toJson(result));
        }
    }
    
    private static void generateNullValuesOutput(SchemaReport schemaReport, Gson gson) {
        for (SchemaProperty property : schemaReport.schemaReport) {
            // Generate JSON with this property set to null
            JsonObject result = generateNullValues(schemaReport, property.property);
            
            // Output format: newline, rule.propertyName, newline, JSON
            System.out.println();
            System.out.println("nullValues." + property.property);
            System.out.println();
            System.out.println(gson.toJson(result));
        }
    }
    
    private static JsonObject generateNullValues(SchemaReport schemaReport, String nullProperty) {
        // First generate the complete JSON with all example values
        JsonObject result = generateFromExample(schemaReport);
        
        // Then set the specific property to null
        setPropertyToNull(result, nullProperty);
        
        return result;
    }
    
    private static void setPropertyToNull(JsonObject obj, String propertyPath) {
        String[] pathParts = propertyPath.split("\\.");
        JsonObject current = obj;
        
        // Navigate/create nested objects
        for (int i = 0; i < pathParts.length - 1; i++) {
            String part = pathParts[i];
            if (!current.has(part)) {
                current.add(part, new JsonObject());
            } else {
                // Check if the existing value is already a JsonObject
                JsonElement existingElement = current.get(part);
                if (existingElement.isJsonObject()) {
                    current = existingElement.getAsJsonObject();
                } else if (existingElement.isJsonArray()) {
                    // If it's a JsonArray, we need to navigate into the first element for nested properties
                    JsonArray array = existingElement.getAsJsonArray();
                    
                    // Ensure the array has at least one element
                    if (array.size() == 0) {
                        // Add an empty object to the array
                        JsonObject emptyObject = new JsonObject();
                        array.add(emptyObject);
                    }
                    
                    // Navigate into the first element of the array
                    JsonElement firstElement = array.get(0);
                    if (firstElement.isJsonObject()) {
                        current = firstElement.getAsJsonObject();
                    } else {
                        // If the first element is not an object, replace it with an object
                        JsonObject newObject = new JsonObject();
                        array.set(0, newObject);
                        current = newObject;
                    }
                } else {
                    // If it's not a JsonObject or JsonArray, replace it with a new JsonObject
                    current.add(part, new JsonObject());
                    current = current.getAsJsonObject(part);
                }
            }
        }
        
        // Set the final value to null
        String finalProperty = pathParts[pathParts.length - 1];
        // Remove existing property if it exists, then add null
        if (current.has(finalProperty)) {
            current.remove(finalProperty);
        }
        current.add(finalProperty, JsonNull.INSTANCE);
    }
    
    private static void generateEmptyValuesOutput(SchemaReport schemaReport, Gson gson) {
        for (SchemaProperty property : schemaReport.schemaReport) {
            // Check if this property can be empty
            if (canBeEmpty(property)) {
                // Generate JSON with this property set to empty
                JsonObject result = generateEmptyValues(schemaReport, property.property);
                
                // Output format: newline, rule.propertyName, newline, JSON
                System.out.println();
                System.out.println("emptyValues." + property.property);
                System.out.println();
                System.out.println(gson.toJson(result));
            }
        }
    }
    
    private static boolean canBeEmpty(SchemaProperty property) {
        if (property.dataTypes.isEmpty()) {
            return false;
        }
        
        // Check if any data type can be empty
        for (DataTypeInfo dataType : property.dataTypes) {
            String type = dataType.type.toLowerCase();
            if ("array".equals(type) || "string".equals(type) || "object".equals(type)) {
                return true;
            }
        }
        
        return false;
    }
    
    private static JsonObject generateEmptyValues(SchemaReport schemaReport, String emptyProperty) {
        // First generate the complete JSON with all example values
        JsonObject result = generateFromExample(schemaReport);
        
        // Then set the specific property to empty
        setPropertyToEmpty(result, emptyProperty, schemaReport);
        
        return result;
    }
    
    private static void setPropertyToEmpty(JsonObject obj, String propertyPath, SchemaReport schemaReport) {
        String[] pathParts = propertyPath.split("\\.");
        JsonObject current = obj;
        
        // Navigate/create nested objects
        for (int i = 0; i < pathParts.length - 1; i++) {
            String part = pathParts[i];
            if (!current.has(part)) {
                current.add(part, new JsonObject());
            } else {
                // Check if the existing value is already a JsonObject
                JsonElement existingElement = current.get(part);
                if (existingElement.isJsonObject()) {
                    current = existingElement.getAsJsonObject();
                } else if (existingElement.isJsonArray()) {
                    // If it's a JsonArray, we need to navigate into the first element for nested properties
                    JsonArray array = existingElement.getAsJsonArray();
                    
                    // Ensure the array has at least one element
                    if (array.size() == 0) {
                        // Add an empty object to the array
                        JsonObject emptyObject = new JsonObject();
                        array.add(emptyObject);
                    }
                    
                    // Navigate into the first element of the array
                    JsonElement firstElement = array.get(0);
                    if (firstElement.isJsonObject()) {
                        current = firstElement.getAsJsonObject();
                    } else {
                        // If the first element is not an object, replace it with an object
                        JsonObject newObject = new JsonObject();
                        array.set(0, newObject);
                        current = newObject;
                    }
                } else {
                    // If it's not a JsonObject or JsonArray, replace it with a new JsonObject
                    current.add(part, new JsonObject());
                    current = current.getAsJsonObject(part);
                }
            }
        }
        
        // Set the final value to empty based on the property's data type
        String finalProperty = pathParts[pathParts.length - 1];
        JsonElement emptyValue = getEmptyValueForProperty(propertyPath, schemaReport);
        
        // Remove existing property if it exists, then add empty value
        if (current.has(finalProperty)) {
            current.remove(finalProperty);
        }
        current.add(finalProperty, emptyValue);
    }
    
    private static JsonElement getEmptyValueForProperty(String propertyPath, SchemaReport schemaReport) {
        // Find the property in the schema report
        for (SchemaProperty property : schemaReport.schemaReport) {
            if (property.property.equals(propertyPath)) {
                if (property.dataTypes.isEmpty()) {
                    return JsonNull.INSTANCE;
                }
                
                // Use the first data type to determine empty value
                DataTypeInfo dataType = property.dataTypes.get(0);
                String type = dataType.type.toLowerCase();
                
                switch (type) {
                    case "array":
                        return new JsonArray(); // Empty array
                    case "string":
                        return new JsonPrimitive(""); // Empty string
                    case "object":
                        return new JsonObject(); // Empty object
                    default:
                        return JsonNull.INSTANCE; // For other types, use null
                }
            }
        }
        
        return JsonNull.INSTANCE;
    }
    
    private static void generateMinMaxValueOutput(SchemaReport schemaReport, Gson gson) {
        for (SchemaProperty property : schemaReport.schemaReport) {
            // Check if this property has min/max values
            if (hasMinMaxValues(property)) {
                // Generate JSON with min value
                JsonObject minResult = generateMinMaxValues(schemaReport, property.property, "min");
                System.out.println();
                System.out.println("minmaxvalue.min." + property.property);
                System.out.println();
                System.out.println(gson.toJson(minResult));
                
                // Generate JSON with max value
                JsonObject maxResult = generateMinMaxValues(schemaReport, property.property, "max");
                System.out.println();
                System.out.println("minmaxvalue.max." + property.property);
                System.out.println();
                System.out.println(gson.toJson(maxResult));
            }
        }
    }
    
    private static boolean hasMinMaxValues(SchemaProperty property) {
        if (property.dataTypes.isEmpty()) {
            return false;
        }
        
        // Check if any data type has min/max values
        for (DataTypeInfo dataType : property.dataTypes) {
            if (dataType.minValues != null && !dataType.minValues.isEmpty() && 
                dataType.maxValues != null && !dataType.maxValues.isEmpty()) {
                return true;
            }
        }
        
        return false;
    }
    
    private static JsonObject generateMinMaxValues(SchemaReport schemaReport, String targetProperty, String minOrMax) {
        // First generate the complete JSON with all example values
        JsonObject result = generateFromExample(schemaReport);
        
        // Then set the specific property to min/max value
        setPropertyToMinMax(result, targetProperty, minOrMax, schemaReport);
        
        return result;
    }
    
    private static void setPropertyToMinMax(JsonObject obj, String propertyPath, String minOrMax, SchemaReport schemaReport) {
        String[] pathParts = propertyPath.split("\\.");
        JsonObject current = obj;
        
        // Navigate/create nested objects
        for (int i = 0; i < pathParts.length - 1; i++) {
            String part = pathParts[i];
            if (!current.has(part)) {
                current.add(part, new JsonObject());
            } else {
                // Check if the existing value is already a JsonObject
                JsonElement existingElement = current.get(part);
                if (existingElement.isJsonObject()) {
                    current = existingElement.getAsJsonObject();
                } else if (existingElement.isJsonArray()) {
                    // If it's a JsonArray, we need to navigate into the first element for nested properties
                    JsonArray array = existingElement.getAsJsonArray();
                    
                    // Ensure the array has at least one element
                    if (array.size() == 0) {
                        // Add an empty object to the array
                        JsonObject emptyObject = new JsonObject();
                        array.add(emptyObject);
                    }
                    
                    // Navigate into the first element of the array
                    JsonElement firstElement = array.get(0);
                    if (firstElement.isJsonObject()) {
                        current = firstElement.getAsJsonObject();
                    } else {
                        // If the first element is not an object, replace it with an object
                        JsonObject newObject = new JsonObject();
                        array.set(0, newObject);
                        current = newObject;
                    }
                } else {
                    // If it's not a JsonObject or JsonArray, replace it with a new JsonObject
                    current.add(part, new JsonObject());
                    current = current.getAsJsonObject(part);
                }
            }
        }
        
        // Set the final value to min/max based on the property's data type
        String finalProperty = pathParts[pathParts.length - 1];
        JsonElement minMaxValue = getMinMaxValueForProperty(propertyPath, minOrMax, schemaReport);
        
        // Remove existing property if it exists, then add min/max value
        if (current.has(finalProperty)) {
            current.remove(finalProperty);
        }
        current.add(finalProperty, minMaxValue);
    }
    
    private static JsonElement getMinMaxValueForProperty(String propertyPath, String minOrMax, SchemaReport schemaReport) {
        // Find the property in the schema report
        for (SchemaProperty property : schemaReport.schemaReport) {
            if (property.property.equals(propertyPath)) {
                if (property.dataTypes.isEmpty()) {
                    return JsonNull.INSTANCE;
                }
                
                // Use the first data type to get min/max value
                DataTypeInfo dataType = property.dataTypes.get(0);
                Map<String, String> valuesMap = "min".equals(minOrMax) ? dataType.minValues : dataType.maxValues;
                
                if (valuesMap == null || valuesMap.isEmpty()) {
                    return JsonNull.INSTANCE;
                }
                
                // Get the first value from the map (since there might be multiple inferred types)
                String value = valuesMap.values().iterator().next();
                
                if (value == null || value.isEmpty()) {
                    return JsonNull.INSTANCE;
                }
                
                // Convert the string value to appropriate JSON element based on data type
                String type = dataType.type.toLowerCase();
                switch (type) {
                    case "number":
                        try {
                            // Try to parse as double first
                            return new JsonPrimitive(Double.parseDouble(value));
                        } catch (NumberFormatException e) {
                            try {
                                // Try to parse as integer
                                return new JsonPrimitive(Integer.parseInt(value));
                            } catch (NumberFormatException e2) {
                                // If both fail, return as string
                                return new JsonPrimitive(value);
                            }
                        }
                    case "string":
                        return new JsonPrimitive(value);
                    case "boolean":
                        return new JsonPrimitive(Boolean.parseBoolean(value));
                    default:
                        return new JsonPrimitive(value);
                }
            }
        }
        
        return JsonNull.INSTANCE;
    }
    
    /**
     * Generates output for the distinctValues rule
     */
    private static void generateDistinctValuesOutput(SchemaReport schemaReport, Gson gson) {
        for (SchemaProperty property : schemaReport.schemaReport) {
            if (hasDistinctValues(property)) {
                generateDistinctValues(schemaReport, property.property, gson);
            }
        }
    }
    
    /**
     * Checks if a property has distinct values available
     */
    private static boolean hasDistinctValues(SchemaProperty property) {
        return property.distinctValuesArray != null && !property.distinctValuesArray.isEmpty();
    }
    
    /**
     * Generates JSON examples for each distinct value of a property
     */
    private static void generateDistinctValues(SchemaReport schemaReport, String targetProperty, Gson gson) {
        // Find the property in the schema report
        SchemaProperty property = null;
        for (SchemaProperty prop : schemaReport.schemaReport) {
            if (prop.property.equals(targetProperty)) {
                property = prop;
                break;
            }
        }
        
        if (property == null || property.distinctValuesArray == null || property.distinctValuesArray.isEmpty()) {
            return;
        }
        
        // Generate JSON for each distinct value
        for (String distinctValue : property.distinctValuesArray) {
            // Generate base JSON with all example values
            JsonObject result = generateFromExample(schemaReport);
            
            // Set the target property to the distinct value
            setPropertyToDistinctValue(result, targetProperty, new JsonPrimitive(distinctValue));
            
            // Output the result
            System.out.println();
            System.out.println("distinctValues." + targetProperty + "." + distinctValue);
            System.out.println();
            System.out.println(gson.toJson(result));
        }
    }
    
    /**
     * Sets a property to a specific distinct value
     */
    private static void setPropertyToDistinctValue(JsonObject obj, String propertyPath, JsonElement distinctValue) {
        String[] pathParts = propertyPath.split("\\.");
        JsonObject current = obj;
        
        // Navigate/create nested objects
        for (int i = 0; i < pathParts.length - 1; i++) {
            String part = pathParts[i];
            if (!current.has(part)) {
                current.add(part, new JsonObject());
            } else {
                // Check if the existing value is already a JsonObject
                JsonElement existingElement = current.get(part);
                if (existingElement.isJsonObject()) {
                    current = existingElement.getAsJsonObject();
                } else if (existingElement.isJsonArray()) {
                    // If it's a JsonArray, we need to navigate into the first element for nested properties
                    JsonArray array = existingElement.getAsJsonArray();
                    
                    // Ensure the array has at least one element
                    if (array.size() == 0) {
                        // Add an empty object to the array
                        JsonObject emptyObject = new JsonObject();
                        array.add(emptyObject);
                    }
                    
                    // Navigate into the first element of the array
                    JsonElement firstElement = array.get(0);
                    if (firstElement.isJsonObject()) {
                        current = firstElement.getAsJsonObject();
                    } else {
                        // If the first element is not an object, replace it with an object
                        JsonObject newObject = new JsonObject();
                        array.set(0, newObject);
                        current = newObject;
                    }
                } else {
                    // If it's not a JsonObject or JsonArray, replace it with a new JsonObject
                    current.add(part, new JsonObject());
                    current = current.getAsJsonObject(part);
                }
            }
        }
        
        // Set the final value to the distinct value
        String finalProperty = pathParts[pathParts.length - 1];
        
        // Remove existing property if it exists, then add distinct value
        if (current.has(finalProperty)) {
            current.remove(finalProperty);
        }
        current.add(finalProperty, distinctValue);
    }
    
    private static JsonObject generateMissingProperties(SchemaReport schemaReport, String excludedProperty) {
        JsonObject result = new JsonObject();
        
        for (SchemaProperty property : schemaReport.schemaReport) {
            // Skip the excluded property
            if (!property.property.equals(excludedProperty)) {
                setPropertyValue(result, property.property, property);
            }
        }
        
        return result;
    }
    
    private static void setPropertyValue(JsonObject obj, String propertyPath, SchemaProperty property) {
        String[] pathParts = propertyPath.split("\\.");
        JsonObject current = obj;
        
        // Navigate/create nested objects
        for (int i = 0; i < pathParts.length - 1; i++) {
            String part = pathParts[i];
            if (!current.has(part)) {
                current.add(part, new JsonObject());
            } else {
                // Check if the existing value is already a JsonObject
                JsonElement existingElement = current.get(part);
                if (existingElement.isJsonObject()) {
                    current = existingElement.getAsJsonObject();
                } else if (existingElement.isJsonArray()) {
                    // If it's a JsonArray, we need to navigate into the first element for nested properties
                    JsonArray array = existingElement.getAsJsonArray();
                    
                    // Ensure the array has at least one element
                    if (array.size() == 0) {
                        // Add an empty object to the array
                        JsonObject emptyObject = new JsonObject();
                        array.add(emptyObject);
                    }
                    
                    // Navigate into the first element of the array
                    JsonElement firstElement = array.get(0);
                    if (firstElement.isJsonObject()) {
                        current = firstElement.getAsJsonObject();
                    } else {
                        // If the first element is not an object, replace it with an object
                        JsonObject newObject = new JsonObject();
                        array.set(0, newObject);
                        current = newObject;
                    }
                } else {
                    // If it's not a JsonObject or JsonArray, replace it with a new JsonObject
                    current.add(part, new JsonObject());
                    current = current.getAsJsonObject(part);
                }
            }
        }
        
        // Set the final value
        String finalProperty = pathParts[pathParts.length - 1];
        JsonElement value = getExampleValue(property);
        current.add(finalProperty, value);
    }
    
    private static void setPropertyValue(JsonObject obj, String propertyPath, Object value) {
        String[] pathParts = propertyPath.split("\\.");
        JsonObject current = obj;
        
        // Navigate/create nested objects
        for (int i = 0; i < pathParts.length - 1; i++) {
            String part = pathParts[i];
            if (!current.has(part)) {
                current.add(part, new JsonObject());
            } else {
                // Check if the existing value is already a JsonObject
                JsonElement existingElement = current.get(part);
                if (existingElement.isJsonObject()) {
                    current = existingElement.getAsJsonObject();
                } else if (existingElement.isJsonArray()) {
                    // If it's a JsonArray, we need to navigate into the first element for nested properties
                    JsonArray array = existingElement.getAsJsonArray();
                    
                    // Ensure the array has at least one element
                    if (array.size() == 0) {
                        // Add an empty object to the array
                        JsonObject emptyObject = new JsonObject();
                        array.add(emptyObject);
                    }
                    
                    // Navigate into the first element of the array
                    JsonElement firstElement = array.get(0);
                    if (firstElement.isJsonObject()) {
                        current = firstElement.getAsJsonObject();
                    } else {
                        // If the first element is not an object, replace it with an object
                        JsonObject newObject = new JsonObject();
                        array.set(0, newObject);
                        current = newObject;
                    }
                } else {
                    // If it's not a JsonObject or JsonArray, replace it with a new JsonObject
                    current.add(part, new JsonObject());
                    current = current.getAsJsonObject(part);
                }
            }
        }
        
        // Set the final value
        String finalProperty = pathParts[pathParts.length - 1];
        JsonElement jsonValue = convertObjectToJsonElement(value);
        current.add(finalProperty, jsonValue);
    }
    
    private static JsonElement getExampleValue(SchemaProperty property) {
        if (property.dataTypes.isEmpty()) {
            return JsonNull.INSTANCE;
        }
        
        DataTypeInfo dataType = property.dataTypes.get(0); // Use first data type
        Object example = dataType.example;
        
        if (example == null) {
            return JsonNull.INSTANCE;
        } else if ("array".equals(dataType.type)) {
            // Handle array data type
            if (example instanceof String && "[array]".equals(((String) example).trim())) {
                // Special case: generate an array containing an empty object
                JsonArray jsonArray = new JsonArray();
                JsonObject emptyObject = new JsonObject();
                jsonArray.add(emptyObject);
                return jsonArray;
            } else {
                return convertToJsonArray(example);
            }
        } else if (example instanceof String) {
            return new JsonPrimitive((String) example);
        } else if (example instanceof Number) {
            return new JsonPrimitive((Number) example);
        } else if (example instanceof Boolean) {
            return new JsonPrimitive((Boolean) example);
        } else {
            return new JsonPrimitive(example.toString());
        }
    }
    
    private static JsonArray convertToJsonArray(Object example) {
        JsonArray jsonArray = new JsonArray();
        
        if (example instanceof List) {
            List<?> list = (List<?>) example;
            for (Object item : list) {
                jsonArray.add(convertObjectToJsonElement(item));
            }
        } else if (example instanceof Object[]) {
            Object[] array = (Object[]) example;
            for (Object item : array) {
                jsonArray.add(convertObjectToJsonElement(item));
            }
        } else {
            // If it's not a recognized array type, try to parse it as a string representation
            String str = example.toString();
            if (str.startsWith("[") && str.endsWith("]")) {
                // Try to parse as JSON array string
                try {
                    JsonElement parsed = JsonParser.parseString(str);
                    if (parsed.isJsonArray()) {
                        return parsed.getAsJsonArray();
                    }
                } catch (Exception e) {
                    // If parsing fails, handle special cases
                    String trimmedStr = str.trim();
                    if ("[array]".equals(trimmedStr)) {
                        // Special case: generate an array containing an empty object
                        JsonObject emptyObject = new JsonObject();
                        jsonArray.add(emptyObject);
                    } else if ("[object]".equals(trimmedStr)) {
                        // Special case: generate an array containing an empty object
                        JsonObject emptyObject = new JsonObject();
                        jsonArray.add(emptyObject);
                    } else {
                        // For other invalid JSON, treat as single element array
                        jsonArray.add(new JsonPrimitive(str));
                    }
                }
            } else {
                // Single value, wrap in array
                jsonArray.add(convertObjectToJsonElement(example));
            }
        }
        
        return jsonArray;
    }
    
    private static JsonElement convertObjectToJsonElement(Object obj) {
        if (obj == null) {
            return JsonNull.INSTANCE;
        } else if (obj instanceof String) {
            return new JsonPrimitive((String) obj);
        } else if (obj instanceof Number) {
            return new JsonPrimitive((Number) obj);
        } else if (obj instanceof Boolean) {
            return new JsonPrimitive((Boolean) obj);
        } else {
            return new JsonPrimitive(obj.toString());
        }
    }
    
    // Data classes for JSON parsing
    public static class SchemaReport {
        public List<SchemaProperty> schemaReport;
    }
    
    public static class SchemaProperty {
        public String property;
        public int count;
        public int distinctValues;
        public List<String> distinctValuesArray;
        public List<DataTypeInfo> dataTypes;
    }
    
    public static class DataTypeInfo {
        public String type;
        public int count;
        public Object example;
        public List<InferredTypeInfo> inferredTypes;
        public Map<String, String> minValues;
        public Map<String, String> maxValues;
    }
    
    public static class InferredTypeInfo {
        public String type;
        public int count;
    }
    
    public static class RuleConfig {
        public String type;
        public String description;
    }
}

