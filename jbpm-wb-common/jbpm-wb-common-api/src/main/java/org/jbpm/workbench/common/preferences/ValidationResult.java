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
import java.util.Collections;
import java.util.List;

/**
 * Result of a file extension validation operation.
 * 
 * <p>This class encapsulates the result of validating file extensions,
 * including whether the validation passed, any error codes, messages,
 * and suggestions for fixing the errors.
 * 
 * <p>Error codes follow the format: EXT_VAL_XXX where:
 * <ul>
 *   <li><strong>001-099:</strong> Design-time validation errors (form designer, preferences)</li>
 *   <li><strong>101-199:</strong> Runtime validation errors (file uploads)</li>
 *   <li><strong>201-299:</strong> Process execution validation errors</li>
 *   <li><strong>301-399:</strong> API validation errors</li>
 * </ul>
 * 
 * @since 7.74.1
 */
public class ValidationResult {
    
    private final boolean valid;
    private final String errorCode;
    private final String message;
    private final List<String> invalidExtensions;
    private final List<String> suggestions;
    
    private ValidationResult(boolean valid, String errorCode, String message, 
                            List<String> invalidExtensions, List<String> suggestions) {
        this.valid = valid;
        this.errorCode = errorCode;
        this.message = message;
        this.invalidExtensions = invalidExtensions != null ? new ArrayList<>(invalidExtensions) : new ArrayList<>();
        this.suggestions = suggestions != null ? new ArrayList<>(suggestions) : new ArrayList<>();
    }
    
    /**
     * Creates a successful validation result.
     * 
     * @return a valid ValidationResult
     */
    public static ValidationResult valid() {
        return new ValidationResult(true, null, null, null, null);
    }
    
    /**
     * Creates a failed validation result with an error code and message.
     * 
     * @param errorCode the error code (e.g., "EXT_VAL_001")
     * @param message the error message
     * @return an invalid ValidationResult
     */
    public static ValidationResult invalid(String errorCode, String message) {
        return new ValidationResult(false, errorCode, message, null, null);
    }
    
    /**
     * Creates a failed validation result with detailed information.
     * 
     * @param errorCode the error code (e.g., "EXT_VAL_001")
     * @param message the error message
     * @param invalidExtensions list of invalid extensions
     * @param suggestions list of suggestions for fixing the error
     * @return an invalid ValidationResult
     */
    public static ValidationResult invalid(String errorCode, String message, 
                                          List<String> invalidExtensions, List<String> suggestions) {
        return new ValidationResult(false, errorCode, message, invalidExtensions, suggestions);
    }
    
    /**
     * Returns whether the validation passed.
     * 
     * @return true if valid, false otherwise
     */
    public boolean isValid() {
        return valid;
    }
    
    /**
     * Returns the error code if validation failed.
     * 
     * @return the error code (e.g., "EXT_VAL_001"), or null if valid
     */
    public String getErrorCode() {
        return errorCode;
    }
    
    /**
     * Returns the error message if validation failed.
     * 
     * @return the error message, or null if valid
     */
    public String getMessage() {
        return message;
    }
    
    /**
     * Returns the list of invalid extensions that caused the validation to fail.
     * 
     * @return unmodifiable list of invalid extensions
     */
    public List<String> getInvalidExtensions() {
        return Collections.unmodifiableList(invalidExtensions);
    }
    
    /**
     * Returns suggestions for fixing the validation error.
     * 
     * @return unmodifiable list of suggestions
     */
    public List<String> getSuggestions() {
        return Collections.unmodifiableList(suggestions);
    }
    
    /**
     * Returns a formatted user-friendly error message.
     * 
     * @return formatted error message with code, message, and suggestions
     */
    public String getFormattedMessage() {
        if (valid) {
            return "Validation passed";
        }
        
        StringBuilder sb = new StringBuilder();
        if (errorCode != null) {
            sb.append("[").append(errorCode).append("] ");
        }
        if (message != null) {
            sb.append(message);
        }
        if (!suggestions.isEmpty()) {
            sb.append(" Suggestions: ").append(String.join(", ", suggestions));
        }
        return sb.toString();
    }
    
    @Override
    public String toString() {
        return "ValidationResult{" +
                "valid=" + valid +
                ", errorCode='" + errorCode + '\'' +
                ", message='" + message + '\'' +
                ", invalidExtensions=" + invalidExtensions +
                ", suggestions=" + suggestions +
                '}';
    }
}

