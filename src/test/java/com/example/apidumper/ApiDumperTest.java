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

    /**
     * Helper method to invoke the private generateSchemaReportJson method using reflection.
     */
    private String invokeGenerateSchemaReportJson(String jsonResponse, String dumpDistinctValues) throws Exception {
        Method method = ApiDumper.class.getDeclaredMethod("generateSchemaReportJson", String.class, String.class);
        method.setAccessible(true);
        return (String) method.invoke(null, jsonResponse, dumpDistinctValues);
    }
}
