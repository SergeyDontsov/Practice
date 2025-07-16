package com.example;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.example.Profiler;


public class Main {
    static {
        // Загрузка нативной библиотеки
        System.load("C:\\Users\\1\\Documents\\Practice-native\\demo\\aggregation.dll");
    }

    // Объявление нативных методов
    public native String calculateAverage(String jsonData, String field, List<String> groupFields);
    public native String calculateMax(String jsonData, String field, List<String> groupFields);
    public native String countUnique(String jsonData, String field, List<String> groupFields);

    public static void main(String[] args) throws Exception {
        
        Profiler profiler = new Profiler();
        String aggregation = null;
        String field = null;
        String[] groupFields = null;
        String dataFile = null;

        // Обработка аргументов командной строки
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "-a":
                    aggregation = args[++i];
                    break;
                case "-f":
                    field = args[++i];
                    break;
                case "-g":
                    // Обработка нескольких группировочных полей
                    int gCount = 0;
                    int startIdx = i + 1;
                    while (startIdx < args.length && !args[startIdx].startsWith("-")) {
                        gCount++;
                        startIdx++;
                    }
                    groupFields = new String[gCount];
                    for (int j = 0; j < gCount; j++) {
                        groupFields[j] = args[i + 1 + j];
                    }
                    i += gCount; // пропускаем обработанные аргументы
                    break;
                case "-d":
                    dataFile = args[++i];
                    break;
                default:
                    break;
            }
        }

        if (aggregation == null || field == null || dataFile == null) {
            System.err.println("Usage: java -jar <app_name>.jar -a <aggregation> -f <field> [-g <group_fields>] -d <data.json>");
            return;
        }

        profiler.stopAndReport("Профилирование 1:");

        // Чтение JSON файла
        String jsonData = readFileAsString(dataFile);


        profiler.stopAndReport("Профилирование 2:");

        // Вызов нативных методов
        Main nativeLib = new Main();
        String resultStr = null;

        // Перед условными блоками подготовьте список groupFields
        List<String> groupFieldsList = (groupFields != null) ? Arrays.asList(groupFields) : Collections.emptyList();


        // Используйте switch-case для выбора метода
        switch (aggregation.toLowerCase()) {
            case "max":
                resultStr = nativeLib.calculateMax(jsonData, field, groupFieldsList);
                break;
            case "average":
            case "avg":
                resultStr = nativeLib.calculateAverage(jsonData, field, groupFieldsList);
                break;
            case "count_unique":
            case "unique":
                resultStr = nativeLib.countUnique(jsonData, field, groupFieldsList);
                break;
            default:
                System.err.println("Unknown aggregation type");
                return;
        }
        profiler.stopAndReport("Профилирование 3:");

        // Обратный парсинг результата
        Gson gson1 = new Gson();
        Type listType1 = new TypeToken<List<Map<String, Object>>>(){}.getType();
        List<Map<String, Object>> results = gson1.fromJson(resultStr, listType1);

        profiler.stopAndReport("Профилирование 4:");

        // Вывод результатов
        System.out.println("Результат агрегации:");
        for (Map<String, Object> item : results) {
            System.out.printf("{max=%s, name=%s}\n", item.get("result"), item.get("group"));
        }

        profiler.stopAndReport("Профилирование 5:");
    }

    
    // Чтение файла
    private static String readFileAsString(String filename) throws IOException {
        byte[] fileContent = Files.readAllBytes(Paths.get(filename)); // Быстрое чтение всего файла в байтовый массив
        return new String(fileContent, StandardCharsets.UTF_8);       // Конвертируем байты в строку UTF-8
    }
}