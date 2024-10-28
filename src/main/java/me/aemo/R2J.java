package me.aemo;

import me.aemo.interfaces.ConvertListener;
import me.aemo.interfaces.ReadFileListener;
import me.aemo.interfaces.WriteFileListener;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class R2J {

    public static void readTextFile(String filePath, ReadFileListener listener) {
        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
        } catch (FileNotFoundException e) {
            if (listener != null){
                listener.onError(e.getMessage(), "FileNotFoundException");
            }
        } catch (IOException e) {
            if (listener != null){
                listener.onError(e.getMessage(), "IOException");
            }
        }
        if (listener != null){
            listener.onSuccess(content.toString());
        }
    }

    public static void writeJavaFile(String filePath, String content, WriteFileListener listener) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            writer.write(content);
            if (listener != null){
                listener.onSuccess();
            }
        } catch (IOException e) {
            if (listener != null){
                listener.onError(e.getMessage());
            }
        }
    }

    public static void convert(String content, String packageName, ConvertListener listener) {
        Map<String, Map<String, String>> data = new HashMap<>();
        BufferedReader reader = new BufferedReader(new StringReader(content));

        if (packageName.isEmpty()){
            if (listener != null){
                listener.onError("PackageName is empty..!");
            }
            return;
        }
        try {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(" ");
                if (parts.length >= 4) {
                    //String line = "int array exo_controls_playback_speeds 0x0";
                    String integer = parts[0];
                    String type = parts[1];
                    String name = parts[2];
                    String value = parts[3];
                    if (integer.equals("int[]")) {
                        int startIndex = line.indexOf("{");
                        int endIndex = line.indexOf("}", startIndex) + 1;
                        value = line.substring(startIndex, endIndex);
                    }
                    data.computeIfAbsent(type, k -> new HashMap<>()).put(name, value);
                }
            }

            StringBuilder rJavaBuilder = new StringBuilder();
            rJavaBuilder.append("package ").append(packageName).append(";\n\n");
            //rJavaBuilder.append("package"++" com.example.app;\n\n");
            rJavaBuilder.append("public final class R {\n");
            rJavaBuilder.append("    private R() {}\n\n");

            for (Map.Entry<String, Map<String, String>> entry : data.entrySet()) {
                String type = entry.getKey();
                Map<String, String> entries = entry.getValue();
                rJavaBuilder.append("    public static final class ").append(type).append(" {\n");
                for (Map.Entry<String, String> e : entries.entrySet()) {
                    boolean isIntegerList = e.getValue().contains("{");
                    rJavaBuilder.append("        public static final ")
                            .append(isIntegerList ? "int[] " : "int ")
                            .append(e.getKey())
                            .append(" = ").append(e.getValue()).append(";\n");
                }
                rJavaBuilder.append("    }\n");
            }

            rJavaBuilder.append("}\n");
            if (listener != null){
                listener.onSuccess(rJavaBuilder.toString());
            }
        } catch (IOException e) {
            if (listener != null){
                listener.onError(e.getMessage());
            }
        }

    }
}
