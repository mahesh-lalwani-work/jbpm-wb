/*
 * Copyright 2025 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jbpm.workbench.common.preferences;

import org.junit.Before;
import org.junit.Test;
import org.uberfire.preferences.shared.impl.validation.ValidationResult;

import static org.junit.Assert.*;

public class AllowedFileTypesValidatorTest {

    private AllowedFileTypesValidator validator;

    @Before
    public void setup() {
        validator = new AllowedFileTypesValidator();
    }

    @Test
    public void testValidate_ValidExtensions() {
        // Test valid extensions - ConstrainedValuesValidator validates individual values
        ValidationResult result = validator.validate("pdf");
        assertTrue("Valid extension should pass validation", result.isValid());
        
        result = validator.validate("docx");
        assertTrue("Valid extension should pass validation", result.isValid());
        
        result = validator.validate("jpg");
        assertTrue("Valid image extension should pass validation", result.isValid());
    }

    @Test
    public void testValidate_ValidExtensionsWithSpaces() {
        // Test valid extensions with spaces (should be handled gracefully)
        ValidationResult result = validator.validate(" pdf ");
        assertTrue("Valid extension with spaces should pass validation", result.isValid());
        
        result = validator.validate(" docx ");
        assertTrue("Valid extension with extra spaces should pass validation", result.isValid());
    }

    @Test
    public void testValidate_ValidExtensionsCaseInsensitive() {
        // Test case insensitive validation
        ValidationResult result = validator.validate("PDF");
        assertTrue("Valid extension in uppercase should pass validation", result.isValid());
        
        result = validator.validate("DocX");
        assertTrue("Valid extension in mixed case should pass validation", result.isValid());
    }

    @Test
    public void testValidate_InvalidExtensions() {
        // Test invalid extensions
        ValidationResult result = validator.validate("invalid");
        assertFalse("Invalid extension should fail validation", result.isValid());
        
        result = validator.validate("pfd");
        assertFalse("Typo should fail validation", result.isValid());
    }

    @Test
    public void testValidate_EmptyAndNull() {
        // Test empty and null values - these should be VALID (means use defaults)
        ValidationResult result = validator.validate(null);
        assertTrue("Null value should be valid (uses defaults)", result.isValid());
        
        result = validator.validate("");
        assertTrue("Empty string should be valid (uses defaults)", result.isValid());
        
        result = validator.validate("   ");
        assertTrue("Whitespace-only string should be valid (uses defaults)", result.isValid());
    }

    @Test
    public void testValidate_CommonTypos() {
        // Test common typos that should be caught
        ValidationResult result = validator.validate("pfd"); // typo for pdf
        assertFalse("Common typo should fail validation", result.isValid());
    }

    @Test
    public void testGetValidFileExtensions() {
        // Test that we can get the list of valid extensions
        assertNotNull("getValidFileExtensions should not return null", 
                     AllowedFileTypesValidator.getValidFileExtensions());
        assertFalse("getValidFileExtensions should not be empty", 
                   AllowedFileTypesValidator.getValidFileExtensions().isEmpty());
        assertTrue("getValidFileExtensions should contain common extensions", 
                  AllowedFileTypesValidator.getValidFileExtensions().contains("pdf"));
        assertTrue("getValidFileExtensions should contain common extensions", 
                  AllowedFileTypesValidator.getValidFileExtensions().contains("jpg"));
    }

    @Test
    public void testIsValidExtension() {
        // Test individual extension validation
        assertTrue("pdf should be valid", AllowedFileTypesValidator.isValidExtension("pdf"));
        assertTrue("PDF should be valid (case insensitive)", AllowedFileTypesValidator.isValidExtension("PDF"));
        assertTrue("jpg should be valid", AllowedFileTypesValidator.isValidExtension("jpg"));
        assertFalse("invalid should not be valid", AllowedFileTypesValidator.isValidExtension("invalid"));
        assertFalse("null should not be valid", AllowedFileTypesValidator.isValidExtension(null));
        assertFalse("empty string should not be valid", AllowedFileTypesValidator.isValidExtension(""));
    }

    @Test
    public void testValidate_RealWorldScenarios() {
        // Test real-world scenarios that users might encounter
        
        // Scenario 1: User enters common document types
        ValidationResult result = validator.validate("pdf");
        assertTrue("Common document type should be valid", result.isValid());
        
        // Scenario 2: User makes typos
        result = validator.validate("pfd");
        assertFalse("Typo should be caught", result.isValid());
        
        // Scenario 3: User enters random text
        result = validator.validate("hhh");
        assertFalse("Random text should be invalid", result.isValid());
        
        // Scenario 4: User enters valid extension with extra spaces
        result = validator.validate(" pdf ");
        assertTrue("Valid extension with spaces should be valid", result.isValid());
    }
}
