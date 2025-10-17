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

import java.util.ArrayList;
import java.util.List;

/**
 * Shared utility for validating file extensions with structured error codes.
 * 
 * <p>This utility provides common validation logic used by both:
 * <ul>
 *   <li>Manage Preferences (via AllowedFileTypesValidator)</li>
 *   <li>Form Designer fields (DocumentFieldDefinition, DocumentCollectionFieldDefinition)</li>
 * </ul>
 * 
 * <p>Returns structured ValidationResult objects with error codes following the format:
 * <ul>
 *   <li><strong>EXT_VAL_001:</strong> Invalid format (spaces, dots, wrong separators)</li>
 *   <li><strong>EXT_VAL_002:</strong> Extensions not in allowed list</li>
 *   <li><strong>EXT_VAL_004:</strong> Duplicate extensions</li>
 * </ul>
 * 
 * @since 7.74.1
 */
public class FileExtensionsValidationUtil {
    
    // Error codes
    private static final String ERROR_INVALID_FORMAT = "EXT_VAL_001";
    private static final String ERROR_INVALID_EXTENSIONS = "EXT_VAL_002";
    private static final String ERROR_DUPLICATE_EXTENSIONS = "EXT_VAL_004";
    
    /**
     * Validate a comma-separated list of file extensions and return structured result.
     * 
     * @param extensionsString comma-separated file extensions (e.g., "pdf,txt,jpg")
     * @return ValidationResult with error code and details if invalid
     */
    public static ValidationResult validateExtensionsWithResult(String extensionsString) {
        // Empty or null is valid
        if (extensionsString == null || extensionsString.trim().isEmpty()) {
            return ValidationResult.valid();
        }
        
        String trimmed = extensionsString.trim();
        
        // Check for format issues (dots, semicolons, leading/trailing commas, double commas)
        // Note: Spaces after commas are allowed and will be trimmed (e.g., "pdf, txt, jpg" is valid)
        if (trimmed.contains(".") || trimmed.contains(";") || 
            trimmed.startsWith(",") || trimmed.endsWith(",") || trimmed.contains(",,")) {
            String message = "Invalid format for file extensions. Use comma-separated values without dots (e.g., pdf,docx,xlsx)";
            return ValidationResult.invalid(ERROR_INVALID_FORMAT, message);
        }
        
        String[] extensions = trimmed.split(",");
        List<String> invalidExtensions = new ArrayList<>();
        List<String> suggestions = new ArrayList<>();
        List<String> seenExtensions = new ArrayList<>();
        List<String> duplicates = new ArrayList<>();
        
        for (String extension : extensions) {
            String ext = extension.trim().toLowerCase();
            
            // Skip empty extensions
            if (ext.isEmpty()) {
                continue;
            }
            
            // Check for duplicates
            if (seenExtensions.contains(ext)) {
                if (!duplicates.contains(ext)) {
                    duplicates.add(ext);
                }
            } else {
                seenExtensions.add(ext);
            }
            
            // Check if extension is valid
            if (!AllowedFileTypesValidator.isValidExtension(ext)) {
                invalidExtensions.add(extension.trim());
                
                // Check for typo suggestions
                String suggestion = AllowedFileTypesValidator.getSuggestion(ext);
                if (suggestion != null) {
                    suggestions.add(extension.trim() + " → " + suggestion);
                } else {
                    // Try to find similar extension using Levenshtein distance
                    String similar = AllowedFileTypesValidator.findSimilarExtension(ext);
                    if (similar != null) {
                        suggestions.add(extension.trim() + " → " + similar);
                    }
                }
            }
        }
        
        // Check for duplicates first (warning, but still valid)
        if (!duplicates.isEmpty()) {
            String message = "Duplicate file extensions found: " + String.join(", ", duplicates);
            // Note: This is a warning, not an error, so we return valid but with a message
            // For now, we'll treat it as an error for consistency
            return ValidationResult.invalid(ERROR_DUPLICATE_EXTENSIONS, message, duplicates, null);
        }
        
        // If there are invalid extensions, return error
        if (!invalidExtensions.isEmpty()) {
            StringBuilder errorMessage = new StringBuilder();
            errorMessage.append("Invalid file extension");
            if (invalidExtensions.size() > 1) {
                errorMessage.append("s");
            }
            errorMessage.append(": ").append(String.join(", ", invalidExtensions));
            errorMessage.append(".");
            
            if (!suggestions.isEmpty()) {
                errorMessage.append(" Did you mean: ").append(String.join(", ", suggestions)).append("?");
            }
            
            errorMessage.append(" Valid extensions include: ");
            List<String> validExtensions = AllowedFileTypesValidator.getValidFileExtensions();
            errorMessage.append(String.join(", ", validExtensions.subList(0, Math.min(10, validExtensions.size()))));
            if (validExtensions.size() > 10) {
                errorMessage.append(", and ").append(validExtensions.size() - 10).append(" more");
            }
            errorMessage.append(".");
            
            return ValidationResult.invalid(ERROR_INVALID_EXTENSIONS, errorMessage.toString(), 
                                           invalidExtensions, suggestions);
        }
        
        return ValidationResult.valid();
    }
    
    /**
     * Validate a comma-separated list of file extensions (legacy method for backward compatibility).
     * Throws IllegalArgumentException if validation fails.
     * 
     * @param extensionsString comma-separated file extensions (e.g., "pdf,txt,jpg")
     * @throws IllegalArgumentException if any extension is invalid
     */
    public static void validateExtensions(String extensionsString) {
        ValidationResult result = validateExtensionsWithResult(extensionsString);
        if (!result.isValid()) {
            throw new IllegalArgumentException(result.getMessage());
        }
    }
    
    /**
     * Check if a specific extension is valid.
     * 
     * @param extension the extension to check
     * @return true if valid, false otherwise
     */
    public static boolean isValidExtension(String extension) {
        return AllowedFileTypesValidator.isValidExtension(extension);
    }
    
    /**
     * Get the list of all valid file extensions.
     * 
     * @return list of valid extensions
     */
    public static List<String> getValidExtensions() {
        return AllowedFileTypesValidator.getValidFileExtensions();
    }
}

