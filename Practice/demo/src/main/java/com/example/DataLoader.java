package com.example;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class DataLoader {
    public static List<Map<String, Object>> loadJson(String filePath) throws IOException {
    ObjectMapper mapper = new ObjectMapper();
    List<Map<String, Object>> data = mapper.readValue(new File(filePath),
            new TypeReference<List<Map<String, Object>>>(){});
    return data;
}
}