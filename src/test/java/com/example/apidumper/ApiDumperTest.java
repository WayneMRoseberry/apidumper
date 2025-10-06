package com.example.apidumper;

import org.junit.Test;
import static org.junit.Assert.*;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

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
        
        // Verify the result can be deserialized into a valid SchemaReport object
        com.google.gson.Gson gson = new com.google.gson.Gson();
        ApiDumper.SchemaReport schemaReport = gson.fromJson(result, ApiDumper.SchemaReport.class);
        assertNotNull("SchemaReport should be successfully deserialized", schemaReport);
        assertNotNull("SchemaReport schemaReport should not be null", schemaReport.schemaReport);
        
        // Verify all properties using helper function
        verifySchemaProperty(schemaReport, "name", "string", 1);
        verifySchemaProperty(schemaReport, "age", "number", 1);
        verifySchemaProperty(schemaReport, "active", "boolean", 1);
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
        assertTrue("Result should contain schemaReport", result.contains("\"schemaReport\""));
        assertTrue("Result should contain distinctValuesArray for status", 
                   result.contains("\"distinctValuesArray\""));
        assertTrue("Result should contain active value", result.contains("\"active\""));
        
        // Verify the result can be deserialized into a valid SchemaReport object
        com.google.gson.Gson gson = new com.google.gson.Gson();
        ApiDumper.SchemaReport schemaReport = gson.fromJson(result, ApiDumper.SchemaReport.class);
        assertNotNull("SchemaReport should be successfully deserialized", schemaReport);
        assertNotNull("SchemaReport schemaReport should not be null", schemaReport.schemaReport);
        
        // Verify properties using helper function
        verifySchemaProperty(schemaReport, "status", "string", 1);
        verifySchemaProperty(schemaReport, "count", "number", 1);
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
        assertTrue("Result should contain schemaReport", result.contains("\"schemaReport\""));
        
        // Verify the result can be deserialized into a valid SchemaReport object
        com.google.gson.Gson gson = new com.google.gson.Gson();
        ApiDumper.SchemaReport schemaReport = gson.fromJson(result, ApiDumper.SchemaReport.class);
        assertNotNull("SchemaReport should be successfully deserialized", schemaReport);
        assertNotNull("SchemaReport schemaReport should not be null", schemaReport.schemaReport);
        
        // Verify all nested properties using helper function
        verifySchemaProperty(schemaReport, "user", "object", 1);
        verifySchemaProperty(schemaReport, "user.name", "string", 1);
        verifySchemaProperty(schemaReport, "user.age", "number", 1);
        verifySchemaProperty(schemaReport, "settings", "object", 1);
        verifySchemaProperty(schemaReport, "settings.theme", "string", 1);
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
        assertTrue("Result should contain schemaReport", result.contains("\"schemaReport\""));
        
        // Verify the result can be deserialized into a valid SchemaReport object
        com.google.gson.Gson gson = new com.google.gson.Gson();
        ApiDumper.SchemaReport schemaReport = gson.fromJson(result, ApiDumper.SchemaReport.class);
        assertNotNull("SchemaReport should be successfully deserialized", schemaReport);
        assertNotNull("SchemaReport schemaReport should not be null", schemaReport.schemaReport);
        
        // Verify array properties using helper function
        verifySchemaProperty(schemaReport, "items", "array", 1);
        verifySchemaProperty(schemaReport, "counts", "array", 1);
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
        assertTrue("Result should contain schemaReport", result.contains("\"schemaReport\""));
        assertTrue("Result should contain distinctValuesArray for users.name", 
                   result.contains("\"distinctValuesArray\""));
        
        // Verify the result can be deserialized into a valid SchemaReport object
        com.google.gson.Gson gson = new com.google.gson.Gson();
        ApiDumper.SchemaReport schemaReport = gson.fromJson(result, ApiDumper.SchemaReport.class);
        assertNotNull("SchemaReport should be successfully deserialized", schemaReport);
        assertNotNull("SchemaReport schemaReport should not be null", schemaReport.schemaReport);
        
        // Verify all properties using helper function
        verifySchemaProperty(schemaReport, "users", "array", 1);
        verifySchemaProperty(schemaReport, "users.name", "string", 2);
        verifySchemaProperty(schemaReport, "users.age", "number", 2);
        verifySchemaProperty(schemaReport, "metadata", "object", 1);
        verifySchemaProperty(schemaReport, "metadata.total", "number", 1);
        verifySchemaProperty(schemaReport, "metadata.page", "number", 1);
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
        assertTrue("Result should contain schemaReport", result.contains("\"schemaReport\""));
        
        // Check that the schema report contains distinct values information
        assertTrue("Result should contain distinctValuesArray", 
                   result.contains("\"distinctValuesArray\""));
        
        // Check that distinct values are present in the response
        assertTrue("Result should contain 'active' value", result.contains("\"active\""));
        assertTrue("Result should contain 'inactive' value", result.contains("\"inactive\""));
        assertTrue("Result should contain 'pending' value", result.contains("\"pending\""));
        
        // Verify the result can be deserialized into a valid SchemaReport object
        com.google.gson.Gson gson = new com.google.gson.Gson();
        ApiDumper.SchemaReport schemaReport = gson.fromJson(result, ApiDumper.SchemaReport.class);
        assertNotNull("SchemaReport should be successfully deserialized", schemaReport);
        assertNotNull("SchemaReport schemaReport should not be null", schemaReport.schemaReport);
        
        // Verify properties using helper function
        verifySchemaProperty(schemaReport, "status", "string", 3);
        verifySchemaProperty(schemaReport, "count", "number", 3);
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

    @Test
    public void testGenerateSchemaReportJson_twoobjectswithpropertiesofdifferentintegervalue_checkminandmax() throws Exception {
        // Arrange
        String jsonResponse = "[{\"age\": 25, \"score\": 100}, {\"age\": 30, \"score\": 85}]";
        String dumpDistinctValues = null;
        
        // Act
        String result = invokeGenerateSchemaReportJson(jsonResponse, dumpDistinctValues);
        
        // Assert
        assertNotNull("Result should not be null", result);
        assertTrue("Result should contain schemaReport", result.contains("\"schemaReport\""));
        
        // Verify the result can be deserialized into a valid SchemaReport object
        com.google.gson.Gson gson = new com.google.gson.Gson();
        ApiDumper.SchemaReport schemaReport = gson.fromJson(result, ApiDumper.SchemaReport.class);
        assertNotNull("SchemaReport should be successfully deserialized", schemaReport);
        assertNotNull("SchemaReport schemaReport should not be null", schemaReport.schemaReport);
        
        // Verify age property min/max values (25, 30)
        verifyNumericPropertyMinMax(schemaReport, "age", "25", "30");
        
        // Verify score property min/max values (85, 100)
        verifyNumericPropertyMinMax(schemaReport, "score", "85", "100");
    }

    @Test
    public void testGenerateSchemaReportJson_twoobjectswithpropertiesofdifferentstringvalue_checkminandmax() throws Exception {
        // Arrange
        String jsonResponse = "[{\"name\": \"Alice\", \"status\": \"active\"}, {\"name\": \"Bob\", \"status\": \"inactive\"}, {\"name\": \"Dennis\", \"status\": \"inactive\"}]";
        String dumpDistinctValues = null;
        
        // Act
        String result = invokeGenerateSchemaReportJson(jsonResponse, dumpDistinctValues);
        
        // Assert
        assertNotNull("Result should not be null", result);
        assertTrue("Result should contain schemaReport", result.contains("\"schemaReport\""));
        
        // Verify the result can be deserialized into a valid SchemaReport object
        com.google.gson.Gson gson = new com.google.gson.Gson();
        ApiDumper.SchemaReport schemaReport = gson.fromJson(result, ApiDumper.SchemaReport.class);
        assertNotNull("SchemaReport should be successfully deserialized", schemaReport);
        assertNotNull("SchemaReport schemaReport should not be null", schemaReport.schemaReport);
        
        // Verify name property min/max values (Alice, Dennis - lexicographically)
        verifyStringPropertyMinMax(schemaReport, "name", "Alice", "Dennis");
        
        // Verify status property min/max values (active, pending - lexicographically)
        verifyStringPropertyMinMax(schemaReport, "status", "active", "inactive");
    }

    @Test
    public void testGenerateSchemaReportJson_twoobjectswithpropertiesofdifferentdatevalue_checkminandmax() throws Exception {
        // Arrange
        String jsonResponse = "[{\"created\": \"2023-01-15\", \"updated\": \"2023-02-20\"}, {\"created\": \"2023-03-10\", \"updated\": \"2023-04-05\"}]";
        String dumpDistinctValues = null;
        
        // Act
        String result = invokeGenerateSchemaReportJson(jsonResponse, dumpDistinctValues);
        
        // Assert
        assertNotNull("Result should not be null", result);
        assertTrue("Result should contain schemaReport", result.contains("\"schemaReport\""));
        
        // Verify the result can be deserialized into a valid SchemaReport object
        com.google.gson.Gson gson = new com.google.gson.Gson();
        ApiDumper.SchemaReport schemaReport = gson.fromJson(result, ApiDumper.SchemaReport.class);
        assertNotNull("SchemaReport should be successfully deserialized", schemaReport);
        assertNotNull("SchemaReport schemaReport should not be null", schemaReport.schemaReport);
        
        // Verify created property min/max values (2023-01-15, 2023-03-10 - chronologically)
        verifyStringPropertyMinMax(schemaReport, "created", "2023-01-15", "2023-03-10");
        
        // Verify updated property min/max values (2023-02-20, 2023-04-05 - chronologically)
        verifyStringPropertyMinMax(schemaReport, "updated", "2023-02-20", "2023-04-05");
    }

    @Test
    public void testGenerateSchemaReportJson_twoobjectswithpropertiesofdifferentfloatvalue_checkminandmax() throws Exception {
        // Arrange
        String jsonResponse = "[{\"price\": 19.99, \"rating\": 4.5}, {\"price\": 29.99, \"rating\": 3.8}]";
        String dumpDistinctValues = null;
        
        // Act
        String result = invokeGenerateSchemaReportJson(jsonResponse, dumpDistinctValues);
        
        // Assert
        assertNotNull("Result should not be null", result);
        assertTrue("Result should contain schemaReport", result.contains("\"schemaReport\""));
        
        // Verify the result can be deserialized into a valid SchemaReport object
        com.google.gson.Gson gson = new com.google.gson.Gson();
        ApiDumper.SchemaReport schemaReport = gson.fromJson(result, ApiDumper.SchemaReport.class);
        assertNotNull("SchemaReport should be successfully deserialized", schemaReport);
        assertNotNull("SchemaReport schemaReport should not be null", schemaReport.schemaReport);
        
        // Verify price property min/max values (19.99, 29.99)
        verifyNumericPropertyMinMax(schemaReport, "price", "19.99", "29.99");
        
        // Verify rating property min/max values (3.8, 4.5)
        verifyNumericPropertyMinMax(schemaReport, "rating", "3.8", "4.5");
    }

    @Test
    public void testGenerateSchemaReportJson_fourobjectswithpropertyofmixedtype_checkminandmax() throws Exception {
        // Arrange
        String jsonResponse = "[{\"value\": 10, \"type\": \"number\"}, {\"value\": \"hello\", \"type\": \"string\"}, " +
                             "{\"value\": true, \"type\": \"boolean\"}, {\"value\": 25.5, \"type\": \"float\"}]";
        String dumpDistinctValues = null;
        
        // Act
        String result = invokeGenerateSchemaReportJson(jsonResponse, dumpDistinctValues);
        
        // Assert
        assertNotNull("Result should not be null", result);
        assertTrue("Result should contain schemaReport", result.contains("\"schemaReport\""));
        
        // Verify the result can be deserialized into a valid SchemaReport object
        com.google.gson.Gson gson = new com.google.gson.Gson();
        ApiDumper.SchemaReport schemaReport = gson.fromJson(result, ApiDumper.SchemaReport.class);
        assertNotNull("SchemaReport should be successfully deserialized", schemaReport);
        assertNotNull("SchemaReport schemaReport should not be null", schemaReport.schemaReport);
        
        // Verify value property min/max values for numeric portion (10, 25.5)
        // Note: This property has mixed types, but we verify the numeric min/max values
        verifyNumericPropertyMinMax(schemaReport, "value", "10", "25.5");
        
        // Verify type property min/max values for string portion (boolean, string - lexicographically)
        verifyStringPropertyMinMax(schemaReport, "type", "boolean", "string");
    }

    @Test
    public void testGenerateSchemaReportJson_arrayandobjectnestedthreedeep_checkpropertiesallreportedwithexpectedvalues() throws Exception {
        // Arrange
        String jsonResponse = "{\"users\": [{\"profile\": {\"personal\": {\"name\": \"John\", \"age\": 25}}}, " +
                             "{\"profile\": {\"personal\": {\"name\": \"Jane\", \"age\": 30}}}]}";
        String dumpDistinctValues = null;
        
        // Act
        String result = invokeGenerateSchemaReportJson(jsonResponse, dumpDistinctValues);
        
        // Assert
        assertNotNull("Result should not be null", result);
        assertTrue("Result should contain schemaReport", result.contains("\"schemaReport\""));
        
        // Verify the result can be deserialized into a valid SchemaReport object
        com.google.gson.Gson gson = new com.google.gson.Gson();
        ApiDumper.SchemaReport schemaReport = gson.fromJson(result, ApiDumper.SchemaReport.class);
        assertNotNull("SchemaReport should be successfully deserialized", schemaReport);
        assertNotNull("SchemaReport schemaReport should not be null", schemaReport.schemaReport);
        
        // Verify all nested properties using helper function
        verifySchemaProperty(schemaReport, "users", "array", 1);
        verifySchemaProperty(schemaReport, "users.profile", "object", 2);
        verifySchemaProperty(schemaReport, "users.profile.personal", "object", 2);
        verifySchemaProperty(schemaReport, "users.profile.personal.name", "string", 2);
        verifySchemaProperty(schemaReport, "users.profile.personal.age", "number", 2);
    }

    @Test
    public void testGenerateSchemaReportJson_multipleobjectswithnotallpropertynamesmatchbetweenthem() throws Exception {
        // Arrange
        String jsonResponse = "[{\"name\": \"John\", \"age\": 25, \"city\": \"New York\"}, " +
                             "{\"name\": \"Jane\", \"salary\": 50000, \"department\": \"IT\"}, " +
                             "{\"name\": \"Bob\", \"age\": 30, \"city\": \"Boston\", \"salary\": 60000}]";
        String dumpDistinctValues = null;
        
        // Act
        String result = invokeGenerateSchemaReportJson(jsonResponse, dumpDistinctValues);
        
        // Assert
        assertNotNull("Result should not be null", result);
        assertTrue("Result should contain schemaReport", result.contains("\"schemaReport\""));
        
        // Verify the result can be deserialized into a valid SchemaReport object
        com.google.gson.Gson gson = new com.google.gson.Gson();
        ApiDumper.SchemaReport schemaReport = gson.fromJson(result, ApiDumper.SchemaReport.class);
        assertNotNull("SchemaReport should be successfully deserialized", schemaReport);
        assertNotNull("SchemaReport schemaReport should not be null", schemaReport.schemaReport);
        
        // Find all properties in the schema report
        Map<String, ApiDumper.SchemaProperty> propertyMap = new HashMap<>();
        for (ApiDumper.SchemaProperty prop : schemaReport.schemaReport) {
            propertyMap.put(prop.property, prop);
        }
        
        // Check that all properties are reported, even if not present in all objects
        assertTrue("Result should contain name property", propertyMap.containsKey("name"));
        assertTrue("Result should contain age property", propertyMap.containsKey("age"));
        assertTrue("Result should contain city property", propertyMap.containsKey("city"));
        assertTrue("Result should contain salary property", propertyMap.containsKey("salary"));
        assertTrue("Result should contain department property", propertyMap.containsKey("department"));
        
        // Verify data types
        assertTrue("Result should contain string type for name", 
                   propertyMap.get("name").dataTypes.stream().anyMatch(dt -> "string".equals(dt.type)));
        assertTrue("Result should contain number type for age", 
                   propertyMap.get("age").dataTypes.stream().anyMatch(dt -> "number".equals(dt.type)));
        assertTrue("Result should contain string type for city", 
                   propertyMap.get("city").dataTypes.stream().anyMatch(dt -> "string".equals(dt.type)));
        assertTrue("Result should contain number type for salary", 
                   propertyMap.get("salary").dataTypes.stream().anyMatch(dt -> "number".equals(dt.type)));
        assertTrue("Result should contain string type for department", 
                   propertyMap.get("department").dataTypes.stream().anyMatch(dt -> "string".equals(dt.type)));
        
        // Verify counts - some properties appear in fewer objects
        assertEquals("Count for name should be 3", 3, propertyMap.get("name").count);
        assertEquals("Count for age should be 2", 2, propertyMap.get("age").count);
        assertEquals("Count for city should be 2", 2, propertyMap.get("city").count);
        assertEquals("Count for salary should be 2", 2, propertyMap.get("salary").count);
        assertEquals("Count for department should be 1", 1, propertyMap.get("department").count);
        
        // Verify distinct values
        assertEquals("Distinct values for name should be 3", 3, propertyMap.get("name").distinctValues);
        assertEquals("Distinct values for age should be 2", 2, propertyMap.get("age").distinctValues);
        assertEquals("Distinct values for city should be 2", 2, propertyMap.get("city").distinctValues);
        assertEquals("Distinct values for salary should be 2", 2, propertyMap.get("salary").distinctValues);
        assertEquals("Distinct values for department should be 1", 1, propertyMap.get("department").distinctValues);
    }

    @Test
    public void testGenerateSchemaReportJson_multipleobjectssamepropertyinferredtypeisbothstringandinteger() throws Exception {
        // Arrange
        String jsonResponse = "[{\"id\": \"123\", \"value\": \"hello\"}, " +
                             "{\"id\": 456, \"value\": 42}, " +
                             "{\"id\": \"789\", \"value\": \"world\"}, " +
                             "{\"id\": 101, \"value\": 3.14}]";
        String dumpDistinctValues = null;
        
        // Act
        String result = invokeGenerateSchemaReportJson(jsonResponse, dumpDistinctValues);
        
        // Assert
        assertNotNull("Result should not be null", result);
        assertTrue("Result should contain schemaReport", result.contains("\"schemaReport\""));
        
        // Verify the result can be deserialized into a valid SchemaReport object
        com.google.gson.Gson gson = new com.google.gson.Gson();
        ApiDumper.SchemaReport schemaReport = gson.fromJson(result, ApiDumper.SchemaReport.class);
        assertNotNull("SchemaReport should be successfully deserialized", schemaReport);
        assertNotNull("SchemaReport schemaReport should not be null", schemaReport.schemaReport);
        
        // Verify properties with mixed types using helper function
        verifySchemaPropertyWithMixedTypes(schemaReport, "id", new String[]{"string", "number"}, 4, 4);
        verifySchemaPropertyWithMixedTypes(schemaReport, "value", new String[]{"string", "number"}, 4, 4);
    }

    @Test
    public void testGenerateSchemaReportJson_fiveobjectstwohavepropertieswithsamevaluerestaredistinct_checkdistinctvaluetotal() throws Exception {
        // Arrange
        String jsonResponse = "[{\"status\": \"active\", \"priority\": \"high\"}, " +
                             "{\"status\": \"inactive\", \"priority\": \"low\"}, " +
                             "{\"status\": \"active\", \"priority\": \"medium\"}, " +
                             "{\"status\": \"pending\", \"priority\": \"high\"}, " +
                             "{\"status\": \"inactive\", \"priority\": \"low\"}]";
        String dumpDistinctValues = null;
        
        // Act
        String result = invokeGenerateSchemaReportJson(jsonResponse, dumpDistinctValues);
        
        // Assert
        assertNotNull("Result should not be null", result);
        assertTrue("Result should contain schemaReport", result.contains("\"schemaReport\""));
        
        // Verify the result can be deserialized into a valid SchemaReport object
        com.google.gson.Gson gson = new com.google.gson.Gson();
        ApiDumper.SchemaReport schemaReport = gson.fromJson(result, ApiDumper.SchemaReport.class);
        assertNotNull("SchemaReport should be successfully deserialized", schemaReport);
        assertNotNull("SchemaReport schemaReport should not be null", schemaReport.schemaReport);
        
        // Find all properties in the schema report
        Map<String, ApiDumper.SchemaProperty> propertyMap = new HashMap<>();
        for (ApiDumper.SchemaProperty prop : schemaReport.schemaReport) {
            propertyMap.put(prop.property, prop);
        }
        
        // Check that properties are reported
        assertTrue("Result should contain status property", propertyMap.containsKey("status"));
        assertTrue("Result should contain priority property", propertyMap.containsKey("priority"));
        
        // Verify data types
        assertTrue("Result should contain string type for status", 
                   propertyMap.get("status").dataTypes.stream().anyMatch(dt -> "string".equals(dt.type)));
        assertTrue("Result should contain string type for priority", 
                   propertyMap.get("priority").dataTypes.stream().anyMatch(dt -> "string".equals(dt.type)));
        
        // Verify counts
        assertEquals("Count for status should be 5", 5, propertyMap.get("status").count);
        assertEquals("Count for priority should be 5", 5, propertyMap.get("priority").count);
        
        // Verify distinct values - status has 3 distinct values (active, inactive, pending)
        // priority has 3 distinct values (high, low, medium)
        assertEquals("Distinct values for status should be 3", 3, propertyMap.get("status").distinctValues);
        assertEquals("Distinct values for priority should be 3", 3, propertyMap.get("priority").distinctValues);
        
        // Do not assert schema min/max here; distinct values and counts are already verified
    }

    /**
     * Helper method to invoke the private generateSchemaReportJson method using reflection.
     */
    private String invokeGenerateSchemaReportJson(String jsonResponse, String dumpDistinctValues) throws Exception {
        Method method = ApiDumper.class.getDeclaredMethod("generateSchemaReportJson", String.class, String.class);
        method.setAccessible(true);
        return (String) method.invoke(null, jsonResponse, dumpDistinctValues);
    }

    /**
     * Helper method to verify numeric property min/max values in schema report.
     */
    private void verifyNumericPropertyMinMax(ApiDumper.SchemaReport schemaReport, String propertyName, 
                                           String expectedMinValue, String expectedMaxValue) {
        // Find the property in the schema report
        ApiDumper.SchemaProperty property = null;
        for (ApiDumper.SchemaProperty prop : schemaReport.schemaReport) {
            if (propertyName.equals(prop.property)) {
                property = prop;
                break;
            }
        }
        
        assertNotNull(propertyName + " property should be found", property);
        assertEquals(propertyName + " property name should be correct", propertyName, property.property);
        
        // Verify minValues and maxValues for the property
        for (ApiDumper.DataTypeInfo dataType : property.dataTypes) {
            if ("number".equals(dataType.type)) {
                assertNotNull(propertyName + " should have minValues", dataType.minValues);
                assertNotNull(propertyName + " should have maxValues", dataType.maxValues);
                assertTrue(propertyName + " minValues should not be empty", !dataType.minValues.isEmpty());
                assertTrue(propertyName + " maxValues should not be empty", !dataType.maxValues.isEmpty());
                
                // Check specific min/max values
                assertTrue(propertyName + " minValues should contain " + expectedMinValue, 
                           dataType.minValues.containsValue(expectedMinValue));
                assertTrue(propertyName + " maxValues should contain " + expectedMaxValue, 
                           dataType.maxValues.containsValue(expectedMaxValue));
                break; // Only check the first number type found
            }
        }
    }

    /**
     * Helper method to verify string property min/max values in schema report.
     */
    private void verifyStringPropertyMinMax(ApiDumper.SchemaReport schemaReport, String propertyName, 
                                          String expectedMinValue, String expectedMaxValue) {
        // Find the property in the schema report
        ApiDumper.SchemaProperty property = null;
        for (ApiDumper.SchemaProperty prop : schemaReport.schemaReport) {
            if (propertyName.equals(prop.property)) {
                property = prop;
                break;
            }
        }
        
        assertNotNull(propertyName + " property should be found", property);
        assertEquals(propertyName + " property name should be correct", propertyName, property.property);
        
        // Verify minValues and maxValues for the property
        for (ApiDumper.DataTypeInfo dataType : property.dataTypes) {
            if ("string".equals(dataType.type)) {
                assertNotNull(propertyName + " should have minValues", dataType.minValues);
                assertNotNull(propertyName + " should have maxValues", dataType.maxValues);
                assertTrue(propertyName + " minValues should not be empty", !dataType.minValues.isEmpty());
                assertTrue(propertyName + " maxValues should not be empty", !dataType.maxValues.isEmpty());
                
                // Check specific min/max values
                assertTrue(propertyName + " minValues should contain " + expectedMinValue, 
                           dataType.minValues.containsValue(expectedMinValue));
                assertTrue(propertyName + " maxValues should contain " + expectedMaxValue, 
                           dataType.maxValues.containsValue(expectedMaxValue));
                break; // Only check the first string type found
            }
        }
    }

    /**
     * Helper method to verify a property exists in schema report and has expected values.
     */
    private void verifySchemaProperty(ApiDumper.SchemaReport schemaReport, String propertyName, 
                                     String expectedDataType, int expectedCount) {
        // Find the property in the schema report
        ApiDumper.SchemaProperty property = null;
        for (ApiDumper.SchemaProperty prop : schemaReport.schemaReport) {
            if (propertyName.equals(prop.property)) {
                property = prop;
                break;
            }
        }
        
        assertNotNull(propertyName + " property should be found", property);
        assertEquals(propertyName + " property name should be correct", propertyName, property.property);
        
        // Verify data type
        assertTrue(propertyName + " should have expected data type " + expectedDataType, 
                   property.dataTypes.stream().anyMatch(dt -> expectedDataType.equals(dt.type)));
        
        // Verify count
        assertEquals(propertyName + " should have expected count", expectedCount, property.count);
    }

    /**
     * Helper method to verify a property exists in schema report and has expected values with mixed types.
     */
    private void verifySchemaPropertyWithMixedTypes(ApiDumper.SchemaReport schemaReport, String propertyName, 
                                                   String[] expectedDataTypes, int expectedCount, int expectedDistinctValues) {
        // Find the property in the schema report
        ApiDumper.SchemaProperty property = null;
        for (ApiDumper.SchemaProperty prop : schemaReport.schemaReport) {
            if (propertyName.equals(prop.property)) {
                property = prop;
                break;
            }
        }
        
        assertNotNull(propertyName + " property should be found", property);
        assertEquals(propertyName + " property name should be correct", propertyName, property.property);
        
        // Verify data types (should have all expected types)
        for (String expectedType : expectedDataTypes) {
            assertTrue(propertyName + " should have expected data type " + expectedType, 
                       property.dataTypes.stream().anyMatch(dt -> expectedType.equals(dt.type)));
        }
        
        // Verify count
        assertEquals(propertyName + " should have expected count", expectedCount, property.count);
        
        // Verify distinct values
        assertEquals(propertyName + " should have expected distinct values", expectedDistinctValues, property.distinctValues);
    }
}
