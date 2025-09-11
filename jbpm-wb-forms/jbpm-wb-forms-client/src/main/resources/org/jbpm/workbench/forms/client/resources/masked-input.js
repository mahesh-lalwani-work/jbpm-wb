/*
 * Copyright 2024 Red Hat, Inc. and/or its affiliates.
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

/**
 * MaskedInputText field handler
 * Provides client-side masking functionality for MaskedInputText fields
 */
(function() {
    'use strict';

    // Initialize masked input fields when the page loads
    document.addEventListener('DOMContentLoaded', function() {
        try {
            console.log('MaskedTextBox field initialized successfully');
        } catch (e) {}
        try {
            renamePaletteItems();
        } catch (e) {}
        initializeMaskedInputFields();
        
        // Debug: Check if MaskedInputText appears anywhere
        setTimeout(function() {
            debugFieldTypes();
        }, 2000);
    });

    // Also initialize when new content is added dynamically
    if (typeof MutationObserver !== 'undefined') {
        var observer = new MutationObserver(function(mutations) {
            mutations.forEach(function(mutation) {
                if (mutation.type === 'childList') {
                    mutation.addedNodes.forEach(function(node) {
                        if (node.nodeType === 1) { // Element node
                            try { renamePaletteItems(node); } catch (e) {}
                            initializeMaskedInputFields(node);
                        }
                    });
                }
            });
        });
        
        observer.observe(document.body, {
            childList: true,
            subtree: true
        });
    }

    function renamePaletteItems(container) {
        console.log('renamePaletteItems called.');
        container = container || document;
        var targets = [
            { find: 'MaskedInputText', replace: 'MaskedTextBox' },
            { find: 'maskedinputtext', replace: 'MaskedTextBox' },
            { find: 'MASKEDINPUTTEXT', replace: 'MaskedTextBox' }
        ];
        
        // Check all text nodes, not just specific elements
        var nodes = container.querySelectorAll('*');
        for (var i = 0; i < nodes.length; i++) {
            var el = nodes[i];
            if (el.getAttribute && el.getAttribute('data-renamed') === 'true') continue;
            
            var txt = (el.textContent || '').trim();
            for (var t = 0; t < targets.length; t++) {
                if (txt === targets[t].find) {
                    el.textContent = targets[t].replace;
                    el.setAttribute('data-renamed', 'true');
                    console.log('Renamed "' + targets[t].find + '" to "' + targets[t].replace + '"');
                    break;
                }
            }
            
            // Also check option elements in select dropdowns
            if (el.tagName === 'OPTION' || el.tagName === 'SELECT') {
                for (var t = 0; t < targets.length; t++) {
                    if (el.value === targets[t].find) {
                        el.value = targets[t].replace;
                        console.log('Renamed option value "' + targets[t].find + '" to "' + targets[t].replace + '"');
                    }
                    if (txt === targets[t].find) {
                        el.textContent = targets[t].replace;
                        console.log('Renamed option text "' + targets[t].find + '" to "' + targets[t].replace + '"');
                    }
                }
            }
        }
        
        // Run again after a delay to catch dynamically loaded content
        setTimeout(function() {
            renamePaletteItemsDelayed(container);
        }, 500);
    }
    
    function renamePaletteItemsDelayed(container) {
        container = container || document;
        var targets = [
            { find: 'MaskedInputText', replace: 'MaskedTextBox' },
            { find: 'maskedinputtext', replace: 'MaskedTextBox' },
            { find: 'MASKEDINPUTTEXT', replace: 'MaskedTextBox' }
        ];
        
        var nodes = container.querySelectorAll('*');
        for (var i = 0; i < nodes.length; i++) {
            var el = nodes[i];
            if (el.getAttribute && el.getAttribute('data-renamed') === 'true') continue;
            
            var txt = (el.textContent || '').trim();
            for (var t = 0; t < targets.length; t++) {
                if (txt === targets[t].find) {
                    el.textContent = targets[t].replace;
                    el.setAttribute('data-renamed', 'true');
                    console.log('Delayed rename: "' + targets[t].find + '" to "' + targets[t].replace + '"');
                    break;
                }
            }
        }
    }

    function initializeMaskedInputFields(container) {
        container = container || document;
        
        // Find all input fields that should be masked
        var inputs = container.querySelectorAll('input[data-field-type="MaskedInputText"], input.masked-input-text-field');
        
        inputs.forEach(function(input) {
            if (!input.hasAttribute('data-masked-initialized')) {
                setupMaskedInput(input);
                input.setAttribute('data-masked-initialized', 'true');
            }
        });
    }

    function setupMaskedInput(input) {
        var originalValue = input.value || '';
        var isMasked = false;
        
        // Get masking configuration from data attributes
        var maskingCharacter = input.getAttribute('data-masking-character') || '*';
        var maskingStartIndex = parseInt(input.getAttribute('data-masking-start-index')) || null;
        var maskingFromStartLength = parseInt(input.getAttribute('data-masking-from-start-length')) || null;
        var maskingFromEndLength = parseInt(input.getAttribute('data-masking-from-end-length')) || null;
        var isMaskedInDB = input.getAttribute('data-is-masked-in-db') === 'true';
        
        // Apply initial masking if configured
        if (originalValue && (isMaskedInDB || input.readOnly)) {
            input.value = applyMasking(originalValue, maskingCharacter, maskingStartIndex, maskingFromStartLength, maskingFromEndLength);
            isMasked = true;
            input.classList.add('masked');
        }
        
        // Store original value
        input.setAttribute('data-original-value', originalValue);
        
        // Add event listeners
        input.addEventListener('focus', function() {
            if (!input.readOnly) {
                showOriginalValue();
            }
        });
        
        input.addEventListener('blur', function() {
            if (!input.readOnly) {
                applyMaskingToInput();
            }
        });
        
        input.addEventListener('input', function() {
            if (!input.readOnly) {
                // Update stored original value
                input.setAttribute('data-original-value', input.value);
            }
        });
        
        function showOriginalValue() {
            var original = input.getAttribute('data-original-value');
            if (original) {
                input.value = original;
                isMasked = false;
                input.classList.remove('masked');
                input.classList.add('unmasked');
            }
        }
        
        function applyMaskingToInput() {
            var original = input.getAttribute('data-original-value');
            if (original) {
                input.value = applyMasking(original, maskingCharacter, maskingStartIndex, maskingFromStartLength, maskingFromEndLength);
                isMasked = true;
                input.classList.add('masked');
                input.classList.remove('unmasked');
            }
        }
    }

    function applyMasking(value, maskingCharacter, maskingStartIndex, maskingFromStartLength, maskingFromEndLength) {
        if (!value) {
            return value;
        }
        
        var maskedValue = value;
        
        // Apply masking from start index
        if (maskingStartIndex !== null && maskingFromStartLength !== null) {
            var startIndex = Math.min(maskingStartIndex, value.length);
            var endIndex = Math.min(startIndex + maskingFromStartLength, value.length);
            
            maskedValue = value.substring(0, startIndex) + 
                         maskingCharacter.repeat(endIndex - startIndex) + 
                         value.substring(endIndex);
        }
        
        // Apply masking from end
        if (maskingFromEndLength !== null) {
            var startIndex = Math.max(0, maskedValue.length - maskingFromEndLength);
            maskedValue = maskedValue.substring(0, startIndex) + 
                         maskingCharacter.repeat(maskedValue.length - startIndex);
        }
        
        return maskedValue;
    }

    function debugFieldTypes() {
        console.log('=== DEBUG: Checking for MaskedInputText/MaskedTextBox ===');
        
        // Check palette items
        var paletteItems = document.querySelectorAll('.palette-item, .form-control-item, .field-type-item');
        console.log('Found ' + paletteItems.length + ' palette items');
        for (var i = 0; i < paletteItems.length; i++) {
            var item = paletteItems[i];
            var text = (item.textContent || '').trim();
            if (text.toLowerCase().indexOf('masked') !== -1) {
                console.log('Palette item found:', text, item);
            }
        }
        
        // Check select options
        var selects = document.querySelectorAll('select');
        console.log('Found ' + selects.length + ' select elements');
        for (var i = 0; i < selects.length; i++) {
            var select = selects[i];
            var options = select.querySelectorAll('option');
            for (var j = 0; j < options.length; j++) {
                var option = options[j];
                var text = (option.textContent || '').trim();
                var value = option.value || '';
                if (text.toLowerCase().indexOf('masked') !== -1 || value.toLowerCase().indexOf('masked') !== -1) {
                    console.log('Select option found:', text, value, option);
                }
            }
        }
        
        // Check for any element containing "masked"
        var allElements = document.querySelectorAll('*');
        var maskedElements = [];
        for (var i = 0; i < allElements.length; i++) {
            var el = allElements[i];
            var text = (el.textContent || '').trim();
            if (text.toLowerCase().indexOf('maskedinputtext') !== -1 || text.toLowerCase().indexOf('maskedtextbox') !== -1) {
                maskedElements.push({element: el, text: text});
            }
        }
        console.log('Found ' + maskedElements.length + ' elements with "masked" text:', maskedElements);
        
        console.log('=== END DEBUG ===');
    }

    // Expose the function globally for manual initialization
    window.initializeMaskedInputFields = initializeMaskedInputFields;
    window.debugFieldTypes = debugFieldTypes;

})();
