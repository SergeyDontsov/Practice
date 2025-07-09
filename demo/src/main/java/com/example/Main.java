package com.example;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class Main {
    
    public static void main(String[] args) {
        CommandLineArgs cmdArgs = CommandLineArgs.parse(args);
        

        if (cmdArgs.dataFile == null || cmdArgs.aggregationName == null || cmdArgs.fieldName == null) {
            System.out.println("Использование: java -jar <app_name>.jar -a <aggregation_name> -f <field_name> -d <data.json> [-g <group_field> ...]");
            return;
        }

        // Профилирование времени
        long startTime = System.nanoTime();

        // Профилирование памяти
        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
        long startMemory = memoryBean.getHeapMemoryUsage().getUsed();

        try {
            // Загрузка данных
            List<Map<String, Object>> logs = DataLoader.loadJson(cmdArgs.dataFile);

            long endMemory = memoryBean.getHeapMemoryUsage().getUsed();
            long endTime = System.nanoTime();

            // Вывод профилей
            System.out.println("Загрузка данных завершена");
            System.out.printf("Время выполнения: %.3f сек%n", (endTime - startTime) / 1_000_000_000.0);
            System.out.printf("Использованная память: %d МБ%n", (endMemory - startMemory) / (1024 * 1024));

            // Создаем агрегатор и выполняем операцию
            Aggregator analyzer = new Aggregator(logs);

            if (cmdArgs.groupFields.isEmpty()) {
                // Без группировки
                performAggregation(analyzer, cmdArgs.aggregationName, cmdArgs.fieldName, Collections.emptyList());
            } else {
                // Группировка
                performGroupedAggregation(analyzer, cmdArgs.aggregationName, cmdArgs.fieldName, cmdArgs.groupFields);
            }

        } catch (Exception e) {
            System.err.println("Ошибка: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void performAggregation(Aggregator analyzer, String aggregationName, String fieldName, List<String> groupFields) {
        switch (aggregationName.toLowerCase()) {
            case "count":
                analyzer.printUniqueValuesCount(fieldName);
                break;
            case "sum":
                Double sum = analyzer.sumDoubleField(fieldName);
                System.out.println("Сумма по полю '" + fieldName + "': " + sum);
                break;
            case "average":
                Double average = analyzer.averageDoubleField(fieldName);
                System.out.println("Среднее по полю '" + fieldName + "': " + average);
                break;
            case "max":
                List<Map<String, Object>> maxResults = analyzer.getMaxByField(groupFields, fieldName);
                for (Map<String, Object> item : maxResults) {
                    System.out.println("Результат getMaxByField: " + item);
                }
                break;
            default:
                System.out.println("Неизвестная агрегационная функция");
        }
    }

     private static void performGroupedAggregation(Aggregator analyzer, String aggregationName, String fieldName, List<String> groupFields) {
        if (aggregationName.equalsIgnoreCase("max")) {
            List<Map<String, Object>> maxResults = analyzer.getMaxByField(groupFields, fieldName);
            for (Map<String, Object> item : maxResults) {
                System.out.println(item);
            }
        } else {
            performAggregation(analyzer, aggregationName, fieldName, groupFields);
        }
    }
}