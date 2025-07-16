package com.example;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DataConverter {
    public static class DataArrays {
        public int[] intValues;
        public double[] doubleValues;
        public String[] stringValues;
        public int[] typeCodes;  // 0 - NUL, 1 - INTEGER, 2 - DOUBLE, 3 - STRING, 4 - BOOLEAN
    }

    public static DataArrays convertToArrays(List<Map<String, Object>> data, String fieldName) {
        int size = data.size();
        int[] intValues = new int[size];
        double[] doubleValues = new double[size];
        String[] stringValues = new String[size];
        int[] typeCodes = new int[size];

        for (int i = 0; i < size; i++) {
            Map<String, Object> item = data.get(i);
            Object val = item.get(fieldName);
            if (val == null) {
                typeCodes[i] = 0; // NUL
            } else if (val instanceof Integer) {
                intValues[i] = (Integer) val;
                typeCodes[i] = 1; // INTEGER
            } else if (val instanceof Long) {
                intValues[i] = ((Long) val).intValue(); // аккуратнее, если есть риск потери данных
                typeCodes[i] = 1;
            } else if (val instanceof String) {
                stringValues[i] = (String) val;
                typeCodes[i] = 3; // STRING
            } else if (val instanceof Boolean) {
                Boolean[] boolValues = new Boolean[size];
                boolValues[i] = (Boolean) val;
                typeCodes[i] = 4; // BOOLEAN
            } else {
                typeCodes[i] = 0; // NUL
            }
        }
        DataArrays result = new DataArrays();
        result.intValues = intValues;
        result.doubleValues = doubleValues;
        result.stringValues = stringValues;
        result.typeCodes = typeCodes;
        return result;
    }
}