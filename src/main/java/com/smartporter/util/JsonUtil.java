package com.smartporter.util;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.*;

/**
 * Lightweight, zero-dependency JSON utility for generating and parsing REST responses.
 */
public class JsonUtil {

    public static String toJson(Object obj) {
        if (obj == null) return "null";
        if (obj instanceof String) {
            return "\"" + escape((String) obj) + "\"";
        }
        if (obj instanceof Number || obj instanceof Boolean) {
            return obj.toString();
        }
        if (obj instanceof Timestamp) {
            return "\"" + obj.toString() + "\"";
        }
        if (obj instanceof Map) {
            Map<?, ?> map = (Map<?, ?>) obj;
            StringBuilder sb = new StringBuilder("{");
            boolean first = true;
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                if (!first) sb.append(",");
                first = false;
                sb.append("\"").append(escape(String.valueOf(entry.getKey()))).append("\":");
                sb.append(toJson(entry.getValue()));
            }
            sb.append("}");
            return sb.toString();
        }
        if (obj instanceof Collection) {
            Collection<?> col = (Collection<?>) obj;
            StringBuilder sb = new StringBuilder("[");
            boolean first = true;
            for (Object item : col) {
                if (!first) sb.append(",");
                first = false;
                sb.append(toJson(item));
            }
            sb.append("]");
            return sb.toString();
        }
        if (obj.getClass().isArray()) {
            Object[] arr = (Object[]) obj;
            return toJson(Arrays.asList(arr));
        }

        // Reflection fallback for POJOs
        try {
            java.lang.reflect.Method[] methods = obj.getClass().getMethods();
            Map<String, Object> map = new LinkedHashMap<>();
            for (java.lang.reflect.Method m : methods) {
                if (m.getName().startsWith("get") && m.getParameterCount() == 0 && !m.getName().equals("getClass")) {
                    String prop = Character.toLowerCase(m.getName().charAt(3)) + m.getName().substring(4);
                    Object val = m.invoke(obj);
                    map.put(prop, val);
                } else if (m.getName().startsWith("is") && m.getParameterCount() == 0) {
                    String prop = Character.toLowerCase(m.getName().charAt(2)) + m.getName().substring(3);
                    Object val = m.invoke(obj);
                    map.put(prop, val);
                }
            }
            return toJson(map);
        } catch (Exception e) {
            return "\"" + escape(obj.toString()) + "\"";
        }
    }

    private static String escape(String s) {
        if (s == null) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);
            switch (ch) {
                case '"': sb.append("\\\""); break;
                case '\\': sb.append("\\\\"); break;
                case '\b': sb.append("\\b"); break;
                case '\f': sb.append("\\f"); break;
                case '\n': sb.append("\\n"); break;
                case '\r': sb.append("\\r"); break;
                case '\t': sb.append("\\t"); break;
                default:
                    if (ch < ' ') {
                        String t = "000" + Integer.toHexString(ch);
                        sb.append("\\u").append(t.substring(t.length() - 4));
                    } else {
                        sb.append(ch);
                    }
                    break;
            }
        }
        return sb.toString();
    }

    /**
     * Simple JSON parser to Map for incoming POST request bodies.
     */
    public static Map<String, Object> parseJsonObject(String json) {
        Map<String, Object> map = new HashMap<>();
        if (json == null) return map;
        json = json.trim();
        if (json.startsWith("{") && json.endsWith("}")) {
            json = json.substring(1, json.length() - 1).trim();
        }
        if (json.isEmpty()) return map;

        // Basic tokenizer supporting nested values and escaped strings
        List<String> tokens = new ArrayList<>();
        StringBuilder cur = new StringBuilder();
        boolean inQuotes = false;
        int depth = 0;

        for (int i = 0; i < json.length(); i++) {
            char c = json.charAt(i);
            if (c == '"' && (i == 0 || json.charAt(i - 1) != '\\')) {
                inQuotes = !inQuotes;
                cur.append(c);
            } else if (!inQuotes && (c == '{' || c == '[')) {
                depth++;
                cur.append(c);
            } else if (!inQuotes && (c == '}' || c == ']')) {
                depth--;
                cur.append(c);
            } else if (c == ',' && !inQuotes && depth == 0) {
                tokens.add(cur.toString().trim());
                cur = new StringBuilder();
            } else {
                cur.append(c);
            }
        }
        if (cur.length() > 0) {
            tokens.add(cur.toString().trim());
        }

        for (String token : tokens) {
            int colon = token.indexOf(':');
            if (colon > 0) {
                String key = token.substring(0, colon).trim();
                String val = token.substring(colon + 1).trim();
                if (key.startsWith("\"") && key.endsWith("\"")) {
                    key = key.substring(1, key.length() - 1);
                }
                map.put(key, parseVal(val));
            }
        }
        return map;
    }

    private static Object parseVal(String v) {
        if (v == null || v.equals("null")) return null;
        if (v.equalsIgnoreCase("true")) return Boolean.TRUE;
        if (v.equalsIgnoreCase("false")) return Boolean.FALSE;
        if (v.startsWith("\"") && v.endsWith("\"")) {
            return v.substring(1, v.length() - 1)
                    .replace("\\\"", "\"")
                    .replace("\\\\", "\\")
                    .replace("\\n", "\n")
                    .replace("\\r", "\r");
        }
        try {
            if (v.contains(".")) {
                return Double.parseDouble(v);
            }
            return Long.parseLong(v);
        } catch (NumberFormatException e) {
            return v;
        }
    }
}
