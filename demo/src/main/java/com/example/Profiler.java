package com.example;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;

public class Profiler {
            private long startTime;
            private long startMemory;
            private MemoryMXBean memoryBean;

            public Profiler() {
                memoryBean = ManagementFactory.getMemoryMXBean();
                startTime = System.nanoTime();
                startMemory = memoryBean.getHeapMemoryUsage().getUsed();
            }

            public void stopAndReport(String message) {
                long endTime = System.nanoTime();
                long endMemory = memoryBean.getHeapMemoryUsage().getUsed();
                System.out.println(message);
                System.out.printf("Время: %.3f сек\n", (endTime - startTime) / 1_000_000_000.0);
                System.out.printf("Использованная память: %d МБ\n", (endMemory - startMemory) / (1024 * 1024));
            }
        
}


