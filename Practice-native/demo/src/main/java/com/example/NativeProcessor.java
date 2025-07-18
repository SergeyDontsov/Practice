package com.example;

import java.util.List;
import java.util.Map;

public class NativeProcessor {
    static {
        System.load("C:\\Users\\1\\Documents\\Practice-native\\demo\\aggregation.dll"); // Загрузка нативной библиотеки
    }


    // Вызов нативных функций
    static native void initNativeLib();

    static native double calcAverage(double[] data, int length);

    static native double calcMax(double[] data, int length);

    static native int countUnique(String[] data, int length);

}