package com.ecommerce.project.helper;

import java.util.Map;

public class JsonPatchUtils {

    public static boolean containsAnyIdKey(Map<String, Object> payload) {
        return payload.keySet().stream()
                .anyMatch(key -> key.toLowerCase().endsWith("id"));
    }
}
