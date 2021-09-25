package com.oath.maven.plugin.freemarker;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * @author yrc
 * @date 2021/9/25
 */
public class JsonUtil {
    private static final Gson gson = new GsonBuilder().setLenient().create();
    private static final Type stringObjectMap = new TypeToken<Map<String, Object>>() {
    }.getType();

    public static Map<String, Object> parseJson(File jsonDataFile) {
        try (JsonReader reader = new JsonReader(new InputStreamReader(new FileInputStream(jsonDataFile), StandardCharsets.UTF_8))) {
            return gson.fromJson(reader, stringObjectMap);
        } catch (Throwable t) {
            throw new RuntimeException("Could not parse json data file: " + jsonDataFile, t);
        }
    }
}
