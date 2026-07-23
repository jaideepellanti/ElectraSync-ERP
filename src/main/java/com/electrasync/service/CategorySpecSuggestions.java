package com.electrasync.service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// Provides a starting list of common specification field names for well-known
// electronics categories, so the "Add Product" form can suggest fields like
// RAM/Storage/Color for a phone, or Screen Size/Resolution for a TV.
//
// This is just a helper for the UI - it does not restrict what specs a product can have.
// The manager can always add a custom spec name that isn't in this list.
public class CategorySpecSuggestions {

    private static final Map<String, List<String>> SUGGESTIONS = new LinkedHashMap<>();

    static {
        SUGGESTIONS.put("mobile", List.of("RAM", "Storage", "Color", "Battery", "Processor"));
        SUGGESTIONS.put("phone", List.of("RAM", "Storage", "Color", "Battery", "Processor"));
        SUGGESTIONS.put("laptop", List.of("RAM", "Storage", "Processor", "Color", "Screen Size"));
        SUGGESTIONS.put("television", List.of("Screen Size", "Resolution", "Color", "Smart TV"));
        SUGGESTIONS.put("tv", List.of("Screen Size", "Resolution", "Color", "Smart TV"));
        SUGGESTIONS.put("refrigerator", List.of("Capacity", "Color", "Door Type", "Star Rating"));
        SUGGESTIONS.put("washing machine", List.of("Capacity", "Color", "Type", "Star Rating"));
        SUGGESTIONS.put("air conditioner", List.of("Capacity (Ton)", "Star Rating", "Type"));
        SUGGESTIONS.put("headphone", List.of("Color", "Connectivity", "Battery Life"));
        SUGGESTIONS.put("speaker", List.of("Color", "Connectivity", "Power Output"));
        SUGGESTIONS.put("camera", List.of("Megapixel", "Color", "Storage Type"));
        SUGGESTIONS.put("watch", List.of("Color", "Strap Material", "Battery Life"));
    }

    // Returns a suggested list of specification names for a given category name.
    // Matches loosely (case-insensitive, partial match) since category names are free text.
    // Falls back to a generic default list if the category isn't recognized.
    public static List<String> getSuggestedSpecNames(String categoryName) {
        if (categoryName == null) {
            return defaultSuggestions();
        }

        String lowerCaseCategory = categoryName.toLowerCase();

        for (Map.Entry<String, List<String>> entry : SUGGESTIONS.entrySet()) {
            if (lowerCaseCategory.contains(entry.getKey())) {
                return entry.getValue();
            }
        }

        return defaultSuggestions();
    }

    private static List<String> defaultSuggestions() {
        return List.of("Color", "Model", "Warranty");
    }
}
