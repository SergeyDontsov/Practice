package com.example;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.google.gson.stream.JsonReader;

public class JsonParsery {
    private static final Gson gson = new Gson();

        public static void processJsonStream(String filename, DataProcessor processor) throws IOException {
        try (JsonReader reader = new JsonReader(new FileReader(filename))) {
            reader.beginArray();
            while (reader.hasNext()) {
                Map<String, Object> obj = gson.fromJson(reader, new TypeToken<Map<String, Object>>(){}.getType());
                processor.process(obj);
            }
            reader.endArray();
        }
    }


    // Интерфейс для обработки каждого объекта
    public interface DataProcessor {
        void process(Map<String, Object> data);
    }
}