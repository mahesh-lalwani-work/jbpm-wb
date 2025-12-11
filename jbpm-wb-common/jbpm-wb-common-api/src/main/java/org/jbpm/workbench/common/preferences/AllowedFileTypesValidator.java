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

import java.util.*;

import org.uberfire.preferences.shared.impl.validation.StringPropertyValidator;

/**
 * Validator for the Allowed File Types field in Manage Preferences.
 * 
 * <p>This validator ensures that only valid file extensions are entered,
 * provides suggestions for common typos, and prevents saving invalid values.
 * 
 * <p>Features:
 * <ul>
 *   <li><strong>Extension validation:</strong> Validates against predefined list of common extensions</li>
 *   <li><strong>Comma-separated lists:</strong> Properly handles "pdf,txt,jpg" format</li>
 *   <li><strong>Typo detection:</strong> Suggests corrections for common typos (e.g., "pfd" → "pdf")</li>
 *   <li><strong>Format validation:</strong> Ensures proper comma-separated format</li>
 *   <li><strong>Case insensitive:</strong> Accepts extensions in any case</li>
 *   <li><strong>Whitespace handling:</strong> Automatically trims spaces around extensions</li>
 * </ul>
 * 
 * @since 7.74.1
 */
public class AllowedFileTypesValidator extends StringPropertyValidator {

    /**
     * Predefined list of valid file extensions that users can configure.
     * This list includes common document, image, and data file types.
     */
    public static final List<String> VALID_FILE_EXTENSIONS = Arrays.asList(
        // Documents
        "pdf", "doc", "docx", "rtf", "txt", "odt", "pages",
        // Spreadsheets
        "xls", "xlsx", "csv", "ods", "numbers",
        // Presentations
        "ppt", "pptx", "odp", "key",
        // Images
        "jpg", "jpeg", "png", "gif", "bmp", "tiff", "tif", "svg", "webp", "ico",
        // Archives
        "zip", "rar", "7z", "tar", "gz",
        // Audio
        "mp3", "wav", "flac", "aac", "ogg", "m4a",
        // Video
        "mp4", "avi", "mov", "wmv", "flv", "webm", "mkv",
        // Code/Data
        "json", "xml", "yaml", "yml", "sql", "log",
        // Other common types
        "iso", "dmg", "exe", "msi", "deb", "rpm"
    );

    /**
     * Common typos and their corrections for file extensions.
     * This helps users who make typing mistakes.
     */
    private static final Map<String, String> COMMON_TYPOS = new HashMap<>();
    
    static {
        // Document typos
        COMMON_TYPOS.put("pfd", "pdf");
        COMMON_TYPOS.put("pdt", "pdf");
        
        // Image typos
        COMMON_TYPOS.put("jpge", "jpg");
        COMMON_TYPOS.put("pngg", "png");
        
        // Spreadsheet typos
        COMMON_TYPOS.put("xlxs", "xlsx");
        COMMON_TYPOS.put("cvs", "csv");
    }

    public AllowedFileTypesValidator() {
        super(AllowedFileTypesValidator::validateExtensionsString,
              "PropertyValidator.AllowedFileTypes.NotAllowed");
    }

    /**
     * Validate a comma-separated list of file extensions.
     * This is the validation function used by StringPropertyValidator.
     * Uses the shared FileExtensionsValidationUtil for consistent validation logic.
     */
    private static boolean validateExtensionsString(String value) {
        ValidationResult result = FileExtensionsValidationUtil.validateExtensionsWithResult(value);
        return result.isValid();
    }

    /**
     * Find similar extension using simple character matching.
     * This is used by FileExtensionsValidationUtil for providing suggestions.
     */
    public static String findSimilarExtension(String extension) {
        String bestMatch = null;
        int bestScore = Integer.MAX_VALUE;

        for (String validExt : VALID_FILE_EXTENSIONS) {
            int distance = calculateLevenshteinDistance(extension, validExt);
            if (distance <= 2 && distance < bestScore) {
                bestScore = distance;
                bestMatch = validExt;
            }
        }
        return bestMatch;
    }

    /**
     * Calculate Levenshtein distance between two strings.
     * Used for typo detection and suggestions.
     */
    private static int calculateLevenshteinDistance(String s1, String s2) {
        int[][] dp = new int[s1.length() + 1][s2.length() + 1];

        for (int i = 0; i <= s1.length(); i++) {
            for (int j = 0; j <= s2.length(); j++) {
                if (i == 0) {
                    dp[i][j] = j;
                } else if (j == 0) {
                    dp[i][j] = i;
                } else {
                    dp[i][j] = Math.min(
                        Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1),
                        dp[i - 1][j - 1] + (s1.charAt(i - 1) == s2.charAt(j - 1) ? 0 : 1)
                    );
                }
            }
        }
        return dp[s1.length()][s2.length()];
    }

    /**
     * Get the list of valid file extensions for display purposes.
     * 
     * @return unmodifiable list of valid file extensions
     */
    public static List<String> getValidFileExtensions() {
        return Collections.unmodifiableList(VALID_FILE_EXTENSIONS);
    }

    /**
     * Check if a specific extension is valid.
     * 
     * @param extension the extension to check (case insensitive)
     * @return true if the extension is valid, false otherwise
     */
    public static boolean isValidExtension(String extension) {
        if (extension == null || extension.trim().isEmpty()) {
            return false;
        }
        return VALID_FILE_EXTENSIONS.contains(extension.trim().toLowerCase());
    }

    /**
     * Get suggestion for a typo.
     * 
     * @param extension the potentially misspelled extension
     * @return suggested correction, or null if no suggestion available
     */
    public static String getSuggestion(String extension) {
        if (extension == null || extension.trim().isEmpty()) {
            return null;
        }
        return COMMON_TYPOS.get(extension.trim().toLowerCase());
    }

}
