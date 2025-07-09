package com.example;
import java.util.*;
import java.util.stream.Collectors;

public class Aggregator {
    private final List<Map<String, Object>> data;

    public Aggregator(List<Map<String, Object>> data) {
        this.data = data;
    }

    // Подсчет уникальных значений и их количества по полю
    /*
    public void printUniqueValuesCount(String fieldName) {
        Map<Object, Long> counts = data.stream()
                .map(entry -> entry.get(fieldName))
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(e -> e, Collectors.counting()));

        counts.forEach((key, count) -> System.out.println(key + ": " + count));
    }
    */  
    public void printUniqueValuesCount(String fieldName) {
    long uniqueCount = data.stream()
            .map(entry -> entry.get(fieldName))
            .filter(Objects::nonNull) // исключает null
            .filter(value -> {
                if (value instanceof String) {
                    return !((String) value).isEmpty();
                }
                return true; // для чисел или других типов, можно оставить
            })
            .distinct() // оставляет только уникальные значения
            .count();

    System.out.println("Количество уникальных значений по полю '" + fieldName + "': " + uniqueCount);
    }

    // Сумма по числовому полю (Double)
    public Double sumDoubleField(String fieldName) {
        return data.stream()
                .map(entry -> entry.get(fieldName))
                .filter(Objects::nonNull)
                .filter(value -> value instanceof Number)
                .mapToDouble(value -> ((Number) value).doubleValue())
                .sum();
    }

    // Среднее по числовому полю
    public Double averageDoubleField(String fieldName) {
        DoubleSummaryStatistics stats = data.stream()
                .map(entry -> entry.get(fieldName))
                .filter(Objects::nonNull)
                .filter(value -> value instanceof Number)
                .mapToDouble(value -> ((Number) value).doubleValue())
                .summaryStatistics();

        if (stats.getCount() == 0) {
            return null;
        }
        return stats.getAverage();
    }


    public List<Map<String, Object>> getMaxByField(List<String> groupFields, String maxField) {
    Map<List<Object>, Map<String, Object>> maxRecords = new HashMap<>();

    for (Map<String, Object> record : data) {
        List<Object> key = new ArrayList<>();
        if (groupFields.isEmpty()) {
            key.add("ALL"); // Для случаев без группировки
        } else {
            for (String gf : groupFields) {
                key.add(record.get(gf));
            }
        }

        Map<String, Object> currentMaxRecord = maxRecords.get(key);
        Object currentMaxObj = currentMaxRecord != null ? currentMaxRecord.get(maxField) : null;

        Double currentMax = (currentMaxObj instanceof Number) ? ((Number) currentMaxObj).doubleValue() : null;
        Object recordMaxObj = record.get(maxField);
        Double recordMax = (recordMaxObj instanceof Number) ? ((Number) recordMaxObj).doubleValue() : null;

        if (recordMax != null && (currentMax == null || recordMax > currentMax)) {
            maxRecords.put(key, record);
        }
    }

    List<Map<String, Object>> result = new ArrayList<>();
    for (Map<String, Object> maxRecord : maxRecords.values()) {
        Map<String, Object> resItem = new HashMap<>();
        resItem.put("max", maxRecord.get(maxField));
        if (maxRecord.containsKey("name")) {
            resItem.put("name", maxRecord.get("name"));
        }
        result.add(resItem);
    }
    return result;
}
}