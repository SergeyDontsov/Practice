package com.example;

import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.Reader;
import java.util.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class Main {
    static class ResultItem {
        long result;
        String name;
        
        ResultItem(long result, String name) {
            this.result = result;
            this.name = name;
        }

        @Override
        public String toString() {
            return String.format("{result=%d, name=%s}", result, name);
        }
    }
    
    static JsonArray loadJsonArray(String filename) throws IOException {
        try (Reader reader = Files.newBufferedReader(Paths.get(filename))) {
            JsonElement jsonElement = JsonParser.parseReader(reader);
            if (!jsonElement.isJsonArray())
                throw new IllegalArgumentException("Файл JSON должен содержать массив объектов.");
            
            return jsonElement.getAsJsonArray();
        }
    }


        static void processJsonStream(String filename, Map<String, List<Double>> dataByName) throws IOException {
        try (JsonReader reader = new JsonReader(new FileReader(filename))) {
            reader.beginArray();
            while (reader.hasNext()) {
                reader.beginObject();
                String name = null;
                Double number = null;

                while (reader.hasNext()) {
                    String key = reader.nextName();
                    switch (key) {
                        case "name":
                            name = reader.nextString();
                            break;
                        case "number":
                            if (reader.peek() == JsonToken.NULL) {
                                reader.nextNull();
                            } else {
                                number = reader.nextDouble();
                            }
                            break;
                        default:
                            reader.skipValue(); // Пропускаем неизвестные поля
                    }
                }
                reader.endObject();

                if (name != null && number != null) {
                    dataByName.computeIfAbsent(name, k -> new ArrayList<>()).add(number);
                }
            }
            reader.endArray();
        }
    }

    static Map<String, double[]> loadData(String filename) throws IOException {
        Map<String, List<Double>> tempDataByName = new HashMap<>(); // Временная мапа для сбора списков
        processJsonStream(filename, tempDataByName); // Читаем JSON в списки
        Map<String, double[]> dataByName = new HashMap<>();

        for (Map.Entry<String, List<Double>> entry : tempDataByName.entrySet()) {
            String name = entry.getKey();
            List<Double> values = entry.getValue();
            double[] arrayValues = new double[values.size()];
            for (int j = 0; j < values.size(); j++) {
                arrayValues[j] = values.get(j);
            }
            dataByName.put(name, arrayValues);
        }

        return dataByName;
    }

    static native void initNativeLib();
    static native double calcAverage(double[] data, int length);
    static native double calcMax(double[] data, int length);
    static native int countUnique(double[] data, int length);  // Изменено на массив примитивов

    static {
        System.load("C:\\Users\\1\\Documents\\Practice-native\\demo\\aggregation.dll");
    }

 public static void main(String[] args) throws Exception {
        String filename = null;
        String aggregation = null;
        String field = null;
        Profiler profiler = new Profiler();

        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "-d": filename = args[++i]; break;
                case "-a": aggregation = args[++i]; break;
                case "-f": field = args[++i]; break;
                case "-g": // Группировка не обрабатывается
                    break;
            }
        }

        if (filename == null || aggregation == null || field == null) {
            System.err.println("Ошибка! Используйте параметры: -d <файл> -a <агрегация> -f <поле> [-g <группировка>]");
            return;
        }

        // Замените loadJsonArray вызовом loadData
        Map<String, double[]> dataByName = loadData(filename);

        initNativeLib();
        List<ResultItem> results = new ArrayList<>();

        for (Map.Entry<String, double[]> entry : dataByName.entrySet()) {
            String name = entry.getKey();
            double[] arrayValues = entry.getValue();

            switch (aggregation) {
                case "max":
                    double maxVal = calcMax(arrayValues, arrayValues.length);
                    results.add(new ResultItem((long) Math.round(maxVal), name));
                    break;

                case "average":
                    double average = calcAverage(arrayValues, arrayValues.length);
                    results.add(new ResultItem((long) Math.round(average), name));
                    break;

                case "unique":
                    int uniqueCount = countUnique(arrayValues, arrayValues.length);
                    results.add(new ResultItem(uniqueCount, name));
                    break;

                default:
                    System.err.println("Агрегация '" + aggregation + "' неизвестна!");
            }
        }

        for (ResultItem item : results) {
            System.out.println(item);
        }

        profiler.stopAndReport("Профилирование 3:");
    }
}