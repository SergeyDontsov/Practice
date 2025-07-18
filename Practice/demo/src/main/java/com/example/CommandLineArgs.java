package com.example;

import java.util.ArrayList;
import java.util.List;

public class CommandLineArgs {
    public String aggregationName;
    public String fieldName;
    public List<String> groupFields = new ArrayList<>();
    public String dataFile;

    public static CommandLineArgs parse(String[] args) {
        CommandLineArgs cmdArgs = new CommandLineArgs();

        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "-a":
                    if (i + 1 < args.length) {
                        cmdArgs.aggregationName = args[++i];
                    }
                    break;
                case "-f":
                    if (i + 1 < args.length) {
                        cmdArgs.fieldName = args[++i];
                    }
                    break;
                case "-g":
                    if (i + 1 < args.length) {
                        String[] fields = args[++i].split(",");
                        for (String field : fields) {
                            cmdArgs.groupFields.add(field.trim());
                        }
                    }
                    break;
                case "-d":
                    if (i + 1 < args.length) {
                        cmdArgs.dataFile = args[++i];
                    }
                    break;
                default:
                    // игнорируем или логируем
            }
        }
        return cmdArgs;
    }
}