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

import static org.junit.Assert.*;

public class ManagePreferencesTest {

    private ManagePreferences managePreferences;

    @Before
    public void setup() {
        managePreferences = new ManagePreferences();
    }

    @Test
    public void testDefaultValue_DoesNotOverrideAllowedFileTypes() {
        // CRITICAL: This tests the bug fix
        // defaultValue() should set allowedFileTypes to null, not DEFAULT_ALLOWED_FILE_TYPES
        
        ManagePreferences defaultPrefs = new ManagePreferences();
        defaultPrefs.setAllowedFileTypes("pdf,docx"); // User configured value
        
        // Call defaultValue() - this should NOT override the user value
        ManagePreferences result = managePreferences.defaultValue(defaultPrefs);
        
        // The allowedFileTypes should be null (not overridden to defaults)
        assertNull("defaultValue() should set allowedFileTypes to null to allow proper fallback mechanism",
                  result.getAllowedFileTypes());
    }

    @Test
    public void testGetSetAllowedFileTypes() {
        // Test basic getter/setter
        String testValue = "pdf,docx,xlsx";
        
        managePreferences.setAllowedFileTypes(testValue);
        
        assertEquals("getAllowedFileTypes should return the set value", 
                    testValue, 
                    managePreferences.getAllowedFileTypes());
    }

    @Test
    public void testAllowedFileTypes_NullValue() {
        // Test null handling
        managePreferences.setAllowedFileTypes(null);
        
        assertNull("getAllowedFileTypes should return null when set to null",
                  managePreferences.getAllowedFileTypes());
    }

    @Test
    public void testAllowedFileTypes_EmptyString() {
        // Test empty string handling
        managePreferences.setAllowedFileTypes("");
        
        assertEquals("getAllowedFileTypes should return empty string when set to empty",
                    "", 
                    managePreferences.getAllowedFileTypes());
    }

    @Test
    public void testAllowedFileTypes_WithSpaces() {
        // Test handling of extensions with spaces (should be preserved as-is)
        String valueWithSpaces = "pdf, docx, xlsx";
        managePreferences.setAllowedFileTypes(valueWithSpaces);
        
        assertEquals("getAllowedFileTypes should preserve spaces (trimming handled in validation logic)",
                    valueWithSpaces, 
                    managePreferences.getAllowedFileTypes());
    }

    @Test
    public void testDefaultAllowedFileTypesConstant() {
        // Verify the default constant value
        assertEquals("DEFAULT_ALLOWED_FILE_TYPES should be 'pdf,docx,xlsx,txt,jpg,png'",
                    "pdf,docx,xlsx,txt,jpg,png",
                    ManagePreferences.DEFAULT_ALLOWED_FILE_TYPES);
    }

    @Test
    public void testDefaultValue_SetsItemsPerPage() {
        // Verify that defaultValue() still sets other defaults correctly
        ManagePreferences defaultPrefs = new ManagePreferences();
        
        ManagePreferences result = managePreferences.defaultValue(defaultPrefs);
        
        // itemsPerPage should be set to default
        assertEquals("defaultValue() should set itemsPerPage to DEFAULT_PAGINATION_OPTION",
                    ManagePreferences.DEFAULT_PAGINATION_OPTION,
                    result.getItemsPerPage());
    }

    @Test
    public void testMultipleSetGetCycles() {
        // Test multiple set/get cycles
        managePreferences.setAllowedFileTypes("pdf");
        assertEquals("pdf", managePreferences.getAllowedFileTypes());
        
        managePreferences.setAllowedFileTypes("docx,xlsx");
        assertEquals("docx,xlsx", managePreferences.getAllowedFileTypes());
        
        managePreferences.setAllowedFileTypes(null);
        assertNull(managePreferences.getAllowedFileTypes());
        
        managePreferences.setAllowedFileTypes("");
        assertEquals("", managePreferences.getAllowedFileTypes());
    }
}

