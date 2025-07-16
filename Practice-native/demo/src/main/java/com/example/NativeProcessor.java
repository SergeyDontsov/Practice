package com.example;

import java.util.List;
import java.util.Map;

public class NativeProcessor {
    static {
        System.load("C:\\Users\\1\\Documents\\Practice-native\\demo\\aggregation.dll"); // Загрузка нативной библиотеки
    }

    public native String calculateAverage(List<Map<String, Object>> jsonData, String field, List<String> groupFields);
    public native String calculateMax(List<Map<String, Object>> jsonData, String field, List<String> groupFields);
    public native String countUnique(List<Map<String, Object>> jsonData, String field, List<String> groupFields);
}