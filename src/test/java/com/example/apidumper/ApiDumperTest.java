package com.example.apidumper;

import org.junit.Test;
import static org.junit.Assert.*;
import java.lang.reflect.Method;

/**
 * Unit test for ApiDumper.generateSchemaReportJson method.
 * Tests the core functionality of schema report generation.
 */
public class ApiDumperTest {

    @Test
    public void testGenerateSchemaReportJson_SimpleObject() throws Exception {
        // Arrange
        String jsonResponse = "{\"name\": \"John Doe\", \"age\": 25, \"active\": true}";
        String dumpDistinctValues = null;
        
        // Act
        String result = invokeGenerateSchemaReportJson(jsonResponse, dumpDistinctValues);
        
        // Assert
        assertNotNull("Result should not be null", result);
        assertTrue("Result should contain schemaReport", result.contains("\"schemaReport\""));
        assertTrue("Result should contain name property", result.contains("\"property\": \"name\""));
        assertTrue("Result should contain age property", result.contains("\"property\": \"age\""));
        assertTrue("Result should contain active property", result.contains("\"property\": \"active\""));
        
        // Verify data types
        assertTrue("Result should contain string type for name", result.contains("\"type\": \"string\""));
        assertTrue("Result should contain number type for age", result.contains("\"type\": \"number\""));
        assertTrue("Result should contain boolean type for active", result.contains("\"type\": \"boolean\""));
        
        // Verify counts
        assertTrue("Result should contain count for name", result.contains("\"count\": 1"));
        assertTrue("Result should contain distinctValues", result.contains("\"distinctValues\": 1"));
    }

    @Test
    public void testGenerateSchemaReportJson_WithDistinctValues() throws Exception {
        // Arrange
        String jsonResponse = "{\"status\": \"active\", \"count\": 5}";
        String dumpDistinctValues = "status";
        
        // Act
        String result = invokeGenerateSchemaReportJson(jsonResponse, dumpDistinctValues);
        
        // Assert
        assertNotNull("Result should not be null", result);
        assertTrue("Result should contain distinctValuesArray for status", 
                   result.contains("\"distinctValuesArray\""));
        assertTrue("Result should contain active value", result.contains("\"active\""));
    }

    @Test
    public void testGenerateSchemaReportJson_NestedObject() throws Exception {
        // Arrange
        String jsonResponse = "{\"user\": {\"name\": \"John\", \"age\": 30}, \"settings\": {\"theme\": \"dark\"}}";
        String dumpDistinctValues = null;
        
        // Act
        String result = invokeGenerateSchemaReportJson(jsonResponse, dumpDistinctValues);
        
        // Assert
        assertNotNull("Result should not be null", result);
        assertTrue("Result should contain nested properties", 
                   result.contains("\"property\": \"user.name\""));
        assertTrue("Result should contain nested age property", 
                   result.contains("\"property\": \"user.age\""));
        assertTrue("Result should contain nested settings property", 
                   result.contains("\"property\": \"settings.theme\""));
    }

    @Test
    public void testGenerateSchemaReportJson_Array() throws Exception {
        // Arrange
        String jsonResponse = "{\"items\": [\"apple\", \"banana\", \"cherry\"], \"counts\": [1, 2, 3]}";
        String dumpDistinctValues = null;
        
        // Act
        String result = invokeGenerateSchemaReportJson(jsonResponse, dumpDistinctValues);
        
        // Assert
        assertNotNull("Result should not be null", result);
        assertTrue("Result should contain array type for items", 
                   result.contains("\"property\": \"items\"") && result.contains("\"type\": \"array\""));
        assertTrue("Result should contain array type for counts", 
                   result.contains("\"property\": \"counts\"") && result.contains("\"type\": \"array\""));
    }

    @Test
    public void testGenerateSchemaReportJson_EmptyResponse() throws Exception {
        // Arrange
        String jsonResponse = "";
        String dumpDistinctValues = null;
        
        // Act
        String result = invokeGenerateSchemaReportJson(jsonResponse, dumpDistinctValues);
        
        // Assert
        assertNull("Result should be null for empty response", result);
    }

    @Test
    public void testGenerateSchemaReportJson_NullResponse() throws Exception {
        // Arrange
        String jsonResponse = null;
        String dumpDistinctValues = null;
        
        // Act
        String result = invokeGenerateSchemaReportJson(jsonResponse, dumpDistinctValues);
        
        // Assert
        assertNull("Result should be null for null response", result);
    }

    @Test
    public void testGenerateSchemaReportJson_InvalidJson() throws Exception {
        // Arrange
        String jsonResponse = "invalid json {";
        String dumpDistinctValues = null;
        
        // Act
        String result = invokeGenerateSchemaReportJson(jsonResponse, dumpDistinctValues);
        
        // Assert
        assertNull("Result should be null for invalid JSON", result);
    }

    @Test
    public void testGenerateSchemaReportJson_ComplexStructure() throws Exception {
        // Arrange
        String jsonResponse = "{\"users\": [{\"name\": \"John\", \"age\": 25}, {\"name\": \"Jane\", \"age\": 30}], " +
                             "\"metadata\": {\"total\": 2, \"page\": 1}}";
        String dumpDistinctValues = "users.name";
        
        // Act
        String result = invokeGenerateSchemaReportJson(jsonResponse, dumpDistinctValues);
        
        // Assert
        assertNotNull("Result should not be null", result);
        assertTrue("Result should contain nested array properties", 
                   result.contains("\"property\": \"users.name\""));
        assertTrue("Result should contain nested array age properties", 
                   result.contains("\"property\": \"users.age\""));
        assertTrue("Result should contain metadata properties", 
                   result.contains("\"property\": \"metadata.total\""));
        assertTrue("Result should contain distinctValuesArray for users.name", 
                   result.contains("\"distinctValuesArray\""));
    }

    @Test
    public void testGenerateSchemaReportJson_WithTwoDistinctValues() throws Exception {
        // Arrange
        String jsonArrayResponse = "[{\"status\": \"active\", \"count\": 5}, {\"status\": \"inactive\", \"count\": 3}, {\"status\": \"pending\", \"count\": 2}]";
        String dumpDistinctValues = "status";
        
        // Act
        String result = invokeGenerateSchemaReportJson(jsonArrayResponse, dumpDistinctValues);
        
        // Assert
        assertNotNull("Result should not be null", result);
        
        // Check that the schema report contains distinct values information
        assertTrue("Result should contain distinctValuesArray", 
                   result.contains("\"distinctValuesArray\""));
        
        // Check that both distinct values are present in the response
        assertTrue("Result should contain 'active' value", result.contains("\"active\""));
        assertTrue("Result should contain 'inactive' value", result.contains("\"inactive\""));
        assertTrue("Result should contain 'pending' value", result.contains("\"pending\""));
        
        // Check that distinctValues count is correct (should be 3)
        assertTrue("Result should contain distinctValues count of 3", 
                   result.contains("\"distinctValues\": 3"));
        
        // Verify the property structure
        assertTrue("Result should contain status property", 
                   result.contains("\"property\": \"status\""));
        
        // Verify that the distinctValuesArray is a proper JSON array with all three values
        assertTrue("distinctValuesArray should contain all three values", 
                   result.contains("\"active\"") && result.contains("\"inactive\"") && result.contains("\"pending\""));
    }

    @Test
    public void testGenerateMissingPropertiesRule() throws Exception {
        // Arrange
        String jsonResponse = "{\"name\": \"John Doe\", \"age\": 25, \"active\": true}";
        String dumpDistinctValues = null;
        
        // Generate schema report first
        String schemaReport = invokeGenerateSchemaReportJson(jsonResponse, dumpDistinctValues);
        assertNotNull("Schema report should not be null", schemaReport);
        
        // Act - Test missing-properties rule by invoking the private method
        Method method = ApiDumper.class.getDeclaredMethod("generateMissingPropertiesOutput", 
            ApiDumper.SchemaReport.class, com.google.gson.Gson.class);
        method.setAccessible(true);
        
        // Parse the schema report to SchemaReport object
        com.google.gson.Gson gson = new com.google.gson.Gson();
        ApiDumper.SchemaReport schemaReportObj = gson.fromJson(schemaReport, ApiDumper.SchemaReport.class);
        
        // Capture output
        java.io.ByteArrayOutputStream outputStream = new java.io.ByteArrayOutputStream();
        java.io.PrintStream originalOut = System.out;
        System.setOut(new java.io.PrintStream(outputStream));
        
        try {
            method.invoke(null, schemaReportObj, gson);
            String output = outputStream.toString();
            
            // Assert
            assertTrue("Output should contain missing-properties rule name", 
                       output.contains("missing-properties"));
            assertTrue("Output should contain name property missing", 
                       output.contains("missing-properties.name"));
            assertTrue("Output should contain age property missing", 
                       output.contains("missing-properties.age"));
            assertTrue("Output should contain active property missing", 
                       output.contains("missing-properties.active"));
            
            // Verify JSON structure - should have properties but missing one
            assertTrue("Output should contain JSON structure", 
                       output.contains("{") && output.contains("}"));
            assertTrue("Output should contain some property values", 
                       output.contains("\"age\"") || output.contains("\"active\"") || output.contains("\"name\""));
        } finally {
            System.setOut(originalOut);
        }
    }

    @Test
    public void testGenerateMissingProperties() throws Exception {
        // Arrange
        String jsonResponse = "{\"name\": \"John Doe\", \"age\": 25, \"active\": true}";
        String dumpDistinctValues = null;
        
        // Generate schema report first
        String schemaReport = invokeGenerateSchemaReportJson(jsonResponse, dumpDistinctValues);
        assertNotNull("Schema report should not be null", schemaReport);
        
        // Parse the schema report to SchemaReport object
        com.google.gson.Gson gson = new com.google.gson.Gson();
        ApiDumper.SchemaReport schemaReportObj = gson.fromJson(schemaReport, ApiDumper.SchemaReport.class);
        
        // Act - Test generateMissingProperties method directly
        Method method = ApiDumper.class.getDeclaredMethod("generateMissingProperties", 
            ApiDumper.SchemaReport.class, String.class);
        method.setAccessible(true);
        
        // Test missing "name" property
        com.google.gson.JsonObject resultName = (com.google.gson.JsonObject) method.invoke(null, schemaReportObj, "name");
        
        // Assert - verify "name" property is missing
        assertNotNull("Result should not be null", resultName);
        assertFalse("JSON should not contain 'name' property", resultName.has("name"));
        assertTrue("JSON should contain 'age' property", resultName.has("age"));
        assertTrue("JSON should contain 'active' property", resultName.has("active"));
        assertEquals("Age value should be correct", 25, resultName.get("age").getAsInt());
        assertEquals("Active value should be correct", true, resultName.get("active").getAsBoolean());
        
        // Test missing "age" property
        com.google.gson.JsonObject resultAge = (com.google.gson.JsonObject) method.invoke(null, schemaReportObj, "age");
        
        // Assert - verify "age" property is missing
        assertNotNull("Result should not be null", resultAge);
        assertTrue("JSON should contain 'name' property", resultAge.has("name"));
        assertFalse("JSON should not contain 'age' property", resultAge.has("age"));
        assertTrue("JSON should contain 'active' property", resultAge.has("active"));
        assertEquals("Name value should be correct", "John Doe", resultAge.get("name").getAsString());
        assertEquals("Active value should be correct", true, resultAge.get("active").getAsBoolean());
        
        // Test missing "active" property
        com.google.gson.JsonObject resultActive = (com.google.gson.JsonObject) method.invoke(null, schemaReportObj, "active");
        
        // Assert - verify "active" property is missing
        assertNotNull("Result should not be null", resultActive);
        assertTrue("JSON should contain 'name' property", resultActive.has("name"));
        assertTrue("JSON should contain 'age' property", resultActive.has("age"));
        assertFalse("JSON should not contain 'active' property", resultActive.has("active"));
        assertEquals("Name value should be correct", "John Doe", resultActive.get("name").getAsString());
        assertEquals("Age value should be correct", 25, resultActive.get("age").getAsInt());
    }

    @Test
    public void testGenerateNullValues() throws Exception {
        // Arrange
        String jsonResponse = "{\"name\": \"John Doe\", \"age\": 25, \"active\": true}";
        String dumpDistinctValues = null;
        
        // Generate schema report first
        String schemaReport = invokeGenerateSchemaReportJson(jsonResponse, dumpDistinctValues);
        assertNotNull("Schema report should not be null", schemaReport);
        
        // Parse the schema report to SchemaReport object
        com.google.gson.Gson gson = new com.google.gson.GsonBuilder().serializeNulls().create();
        ApiDumper.SchemaReport schemaReportObj = gson.fromJson(schemaReport, ApiDumper.SchemaReport.class);
        
        // Act - Test generateNullValues method directly
        Method method = ApiDumper.class.getDeclaredMethod("generateNullValues", 
            ApiDumper.SchemaReport.class, String.class);
        method.setAccessible(true);
        
        // Test setting "name" property to null
        com.google.gson.JsonObject resultName = (com.google.gson.JsonObject) method.invoke(null, schemaReportObj, "name");
        
        // Assert - verify "name" property is set to null
        assertNotNull("Result should not be null", resultName);
        assertTrue("JSON should contain 'name' property", resultName.has("name"));
        assertTrue("JSON should contain 'age' property", resultName.has("age"));
        assertTrue("JSON should contain 'active' property", resultName.has("active"));
        assertTrue("Name property should be null", resultName.get("name").isJsonNull());
        assertEquals("Age value should be correct", 25, resultName.get("age").getAsInt());
        assertEquals("Active value should be correct", true, resultName.get("active").getAsBoolean());
        
        // Test setting "age" property to null
        com.google.gson.JsonObject resultAge = (com.google.gson.JsonObject) method.invoke(null, schemaReportObj, "age");
        
        // Assert - verify "age" property is set to null
        assertNotNull("Result should not be null", resultAge);
        assertTrue("JSON should contain 'name' property", resultAge.has("name"));
        assertTrue("JSON should contain 'age' property", resultAge.has("age"));
        assertTrue("JSON should contain 'active' property", resultAge.has("active"));
        assertEquals("Name value should be correct", "John Doe", resultAge.get("name").getAsString());
        assertTrue("Age property should be null", resultAge.get("age").isJsonNull());
        assertEquals("Active value should be correct", true, resultAge.get("active").getAsBoolean());
        
        // Test setting "active" property to null
        com.google.gson.JsonObject resultActive = (com.google.gson.JsonObject) method.invoke(null, schemaReportObj, "active");
        
        // Assert - verify "active" property is set to null
        assertNotNull("Result should not be null", resultActive);
        assertTrue("JSON should contain 'name' property", resultActive.has("name"));
        assertTrue("JSON should contain 'age' property", resultActive.has("age"));
        assertTrue("JSON should contain 'active' property", resultActive.has("active"));
        assertEquals("Name value should be correct", "John Doe", resultActive.get("name").getAsString());
        assertEquals("Age value should be correct", 25, resultActive.get("age").getAsInt());
        assertTrue("Active property should be null", resultActive.get("active").isJsonNull());
    }

    @Test
    public void testGenerateMinMaxValues_oneInstance() throws Exception {
        // Arrange
        String jsonResponse = "{\"age\": 25, \"score\": 85.5, \"name\": \"John Doe\"}";
        String dumpDistinctValues = null;
        
        // Generate schema report first
        String schemaReport = invokeGenerateSchemaReportJson(jsonResponse, dumpDistinctValues);
        assertNotNull("Schema report should not be null", schemaReport);
        
        // Parse the schema report to SchemaReport object
        com.google.gson.Gson gson = new com.google.gson.Gson();
        ApiDumper.SchemaReport schemaReportObj = gson.fromJson(schemaReport, ApiDumper.SchemaReport.class);
        
        // Act - Test generateMinMaxValues method directly
        Method method = ApiDumper.class.getDeclaredMethod("generateMinMaxValues", 
            ApiDumper.SchemaReport.class, String.class, String.class);
        method.setAccessible(true);
        
        // Test generating min value for "age" property
        com.google.gson.JsonObject resultAgeMin = (com.google.gson.JsonObject) method.invoke(null, schemaReportObj, "age", "min");
        
        // Assert - verify "age" property has min value
        assertNotNull("Result should not be null", resultAgeMin);
        assertTrue("JSON should contain 'age' property", resultAgeMin.has("age"));
        assertTrue("JSON should contain 'score' property", resultAgeMin.has("score"));
        assertTrue("JSON should contain 'name' property", resultAgeMin.has("name"));
        assertEquals("Age should have min value", 25, resultAgeMin.get("age").getAsInt());
        assertEquals("Score value should be correct", 85.5, resultAgeMin.get("score").getAsDouble(), 0.001);
        assertEquals("Name value should be correct", "John Doe", resultAgeMin.get("name").getAsString());
        
        // Test generating max value for "age" property
        com.google.gson.JsonObject resultAgeMax = (com.google.gson.JsonObject) method.invoke(null, schemaReportObj, "age", "max");
        
        // Assert - verify "age" property has max value
        assertNotNull("Result should not be null", resultAgeMax);
        assertTrue("JSON should contain 'age' property", resultAgeMax.has("age"));
        assertTrue("JSON should contain 'score' property", resultAgeMax.has("score"));
        assertTrue("JSON should contain 'name' property", resultAgeMax.has("name"));
        assertEquals("Age should have max value", 25, resultAgeMax.get("age").getAsInt());
        assertEquals("Score value should be correct", 85.5, resultAgeMax.get("score").getAsDouble(), 0.001);
        assertEquals("Name value should be correct", "John Doe", resultAgeMax.get("name").getAsString());
        
        // Test generating min value for "score" property
        com.google.gson.JsonObject resultScoreMin = (com.google.gson.JsonObject) method.invoke(null, schemaReportObj, "score", "min");
        
        // Assert - verify "score" property has min value
        assertNotNull("Result should not be null", resultScoreMin);
        assertTrue("JSON should contain 'age' property", resultScoreMin.has("age"));
        assertTrue("JSON should contain 'score' property", resultScoreMin.has("score"));
        assertTrue("JSON should contain 'name' property", resultScoreMin.has("name"));
        assertEquals("Age value should be correct", 25, resultScoreMin.get("age").getAsInt());
        assertEquals("Score should have min value", 85.5, resultScoreMin.get("score").getAsDouble(), 0.001);
        assertEquals("Name value should be correct", "John Doe", resultScoreMin.get("name").getAsString());
        
        // Test generating max value for "score" property
        com.google.gson.JsonObject resultScoreMax = (com.google.gson.JsonObject) method.invoke(null, schemaReportObj, "score", "max");
        
        // Assert - verify "score" property has max value
        assertNotNull("Result should not be null", resultScoreMax);
        assertTrue("JSON should contain 'age' property", resultScoreMax.has("age"));
        assertTrue("JSON should contain 'score' property", resultScoreMax.has("score"));
        assertTrue("JSON should contain 'name' property", resultScoreMax.has("name"));
        assertEquals("Age value should be correct", 25, resultScoreMax.get("age").getAsInt());
        assertEquals("Score should have max value", 85.5, resultScoreMax.get("score").getAsDouble(), 0.001);
        assertEquals("Name value should be correct", "John Doe", resultScoreMax.get("name").getAsString());
    }

    @Test
    public void testGenerateMinMaxValues_multipleInstances() throws Exception {
        // Arrange - Create array of objects with varying numeric values
        String jsonResponse = "[{\"age\": 25, \"score\": 85.5, \"name\": \"John\"}, " +
                             "{\"age\": 30, \"score\": 92.0, \"name\": \"Jane\"}, " +
                             "{\"age\": 18, \"score\": 78.3, \"name\": \"Bob\"}]";
        String dumpDistinctValues = null;
        
        // Generate schema report first
        String schemaReport = invokeGenerateSchemaReportJson(jsonResponse, dumpDistinctValues);
        assertNotNull("Schema report should not be null", schemaReport);
        
        // Parse the schema report to SchemaReport object
        com.google.gson.Gson gson = new com.google.gson.Gson();
        ApiDumper.SchemaReport schemaReportObj = gson.fromJson(schemaReport, ApiDumper.SchemaReport.class);
        
        // Act - Test generateMinMaxValues method directly
        Method method = ApiDumper.class.getDeclaredMethod("generateMinMaxValues", 
            ApiDumper.SchemaReport.class, String.class, String.class);
        method.setAccessible(true);
        
        // Test generating min value for "age" property (should be 18)
        com.google.gson.JsonObject resultAgeMin = (com.google.gson.JsonObject) method.invoke(null, schemaReportObj, "age", "min");
        
        // Assert - verify "age" property has correct min value from multiple instances
        assertNotNull("Result should not be null", resultAgeMin);
        assertTrue("JSON should contain 'age' property", resultAgeMin.has("age"));
        assertTrue("JSON should contain 'score' property", resultAgeMin.has("score"));
        assertTrue("JSON should contain 'name' property", resultAgeMin.has("name"));
        assertEquals("Age should have correct min value from multiple instances", 18, resultAgeMin.get("age").getAsInt());
        assertEquals("Score value should be correct", 85.5, resultAgeMin.get("score").getAsDouble(), 0.001);
        assertEquals("Name value should be correct", "John", resultAgeMin.get("name").getAsString());
        
        // Test generating max value for "age" property (should be 30)
        com.google.gson.JsonObject resultAgeMax = (com.google.gson.JsonObject) method.invoke(null, schemaReportObj, "age", "max");
        
        // Assert - verify "age" property has correct max value from multiple instances
        assertNotNull("Result should not be null", resultAgeMax);
        assertTrue("JSON should contain 'age' property", resultAgeMax.has("age"));
        assertTrue("JSON should contain 'score' property", resultAgeMax.has("score"));
        assertTrue("JSON should contain 'name' property", resultAgeMax.has("name"));
        assertEquals("Age should have correct max value from multiple instances", 30, resultAgeMax.get("age").getAsInt());
        assertEquals("Score value should be correct", 85.5, resultAgeMax.get("score").getAsDouble(), 0.001);
        assertEquals("Name value should be correct", "John", resultAgeMax.get("name").getAsString());
        
        // Test generating min value for "score" property (should be 78.3)
        com.google.gson.JsonObject resultScoreMin = (com.google.gson.JsonObject) method.invoke(null, schemaReportObj, "score", "min");
        
        // Assert - verify "score" property has correct min value from multiple instances
        assertNotNull("Result should not be null", resultScoreMin);
        assertTrue("JSON should contain 'age' property", resultScoreMin.has("age"));
        assertTrue("JSON should contain 'score' property", resultScoreMin.has("score"));
        assertTrue("JSON should contain 'name' property", resultScoreMin.has("name"));
        assertEquals("Age value should be correct", 25, resultScoreMin.get("age").getAsInt());
        assertEquals("Score should have correct min value from multiple instances", 78.3, resultScoreMin.get("score").getAsDouble(), 0.001);
        assertEquals("Name value should be correct", "John", resultScoreMin.get("name").getAsString());
        
        // Test generating max value for "score" property (should be 92.0)
        com.google.gson.JsonObject resultScoreMax = (com.google.gson.JsonObject) method.invoke(null, schemaReportObj, "score", "max");
        
        // Assert - verify "score" property has correct max value from multiple instances
        assertNotNull("Result should not be null", resultScoreMax);
        assertTrue("JSON should contain 'age' property", resultScoreMax.has("age"));
        assertTrue("JSON should contain 'score' property", resultScoreMax.has("score"));
        assertTrue("JSON should contain 'name' property", resultScoreMax.has("name"));
        assertEquals("Age value should be correct", 25, resultScoreMax.get("age").getAsInt());
        assertEquals("Score should have correct max value from multiple instances", 92.0, resultScoreMax.get("score").getAsDouble(), 0.001);
        assertEquals("Name value should be correct", "John", resultScoreMax.get("name").getAsString());
    }

    @Test
    public void testGenerateNullValuesRule() throws Exception {
        // Arrange
        String jsonResponse = "{\"name\": \"John Doe\", \"age\": 25, \"active\": true}";
        String dumpDistinctValues = null;
        
        // Generate schema report first
        String schemaReport = invokeGenerateSchemaReportJson(jsonResponse, dumpDistinctValues);
        assertNotNull("Schema report should not be null", schemaReport);
        
        // Act - Test nullValues rule by invoking the private method
        Method method = ApiDumper.class.getDeclaredMethod("generateNullValuesOutput", 
            ApiDumper.SchemaReport.class, com.google.gson.Gson.class);
        method.setAccessible(true);
        
        // Parse the schema report to SchemaReport object
        com.google.gson.Gson gson = new com.google.gson.GsonBuilder().serializeNulls().create();
        ApiDumper.SchemaReport schemaReportObj = gson.fromJson(schemaReport, ApiDumper.SchemaReport.class);
        
        // Capture output
        java.io.ByteArrayOutputStream outputStream = new java.io.ByteArrayOutputStream();
        java.io.PrintStream originalOut = System.out;
        System.setOut(new java.io.PrintStream(outputStream));
        
        try {
            method.invoke(null, schemaReportObj, gson);
            String output = outputStream.toString();
            
            // Assert
            assertTrue("Output should contain nullValues rule name", 
                       output.contains("nullValues"));
            assertTrue("Output should contain name property set to null", 
                       output.contains("nullValues.name"));
            assertTrue("Output should contain age property set to null", 
                       output.contains("nullValues.age"));
            assertTrue("Output should contain active property set to null", 
                       output.contains("nullValues.active"));
            
            // Verify JSON structure - should have null values
            assertTrue("Output should contain JSON structure", 
                       output.contains("{") && output.contains("}"));
            assertTrue("Output should contain null values", 
                       output.contains("null"));
        } finally {
            System.setOut(originalOut);
        }
    }

    @Test
    public void testGenerateMinMaxValueRule() throws Exception {
        // Arrange
        String jsonResponse = "{\"age\": 25, \"score\": 85.5, \"name\": \"John Doe\"}";
        String dumpDistinctValues = null;
        
        // Generate schema report first
        String schemaReport = invokeGenerateSchemaReportJson(jsonResponse, dumpDistinctValues);
        assertNotNull("Schema report should not be null", schemaReport);
        
        // Act - Test minmaxvalue rule by invoking the private method
        Method method = ApiDumper.class.getDeclaredMethod("generateMinMaxValueOutput", 
            ApiDumper.SchemaReport.class, com.google.gson.Gson.class);
        method.setAccessible(true);
        
        // Parse the schema report to SchemaReport object
        com.google.gson.Gson gson = new com.google.gson.Gson();
        ApiDumper.SchemaReport schemaReportObj = gson.fromJson(schemaReport, ApiDumper.SchemaReport.class);
        
        // Capture output
        java.io.ByteArrayOutputStream outputStream = new java.io.ByteArrayOutputStream();
        java.io.PrintStream originalOut = System.out;
        System.setOut(new java.io.PrintStream(outputStream));
        
        try {
            method.invoke(null, schemaReportObj, gson);
            String output = outputStream.toString();
            
            // Assert
            assertTrue("Output should contain minmaxvalue rule name", 
                       output.contains("minmaxvalue"));
            
            // Check for min/max values for numeric properties
            assertTrue("Output should contain min value for age", 
                       output.contains("minmaxvalue.min.age"));
            assertTrue("Output should contain max value for age", 
                       output.contains("minmaxvalue.max.age"));
            assertTrue("Output should contain min value for score", 
                       output.contains("minmaxvalue.min.score"));
            assertTrue("Output should contain max value for score", 
                       output.contains("minmaxvalue.max.score"));
            
            // Verify JSON structure contains min/max values
            assertTrue("Output should contain JSON structure", 
                       output.contains("{") && output.contains("}"));
            assertTrue("Output should contain numeric values", 
                       output.contains("\"age\"") || output.contains("\"score\""));
        } finally {
            System.setOut(originalOut);
        }
    }

    /**
     * Helper method to invoke the private generateSchemaReportJson method using reflection.
     */
    private String invokeGenerateSchemaReportJson(String jsonResponse, String dumpDistinctValues) throws Exception {
        Method method = ApiDumper.class.getDeclaredMethod("generateSchemaReportJson", String.class, String.class);
        method.setAccessible(true);
        return (String) method.invoke(null, jsonResponse, dumpDistinctValues);
    }
}
