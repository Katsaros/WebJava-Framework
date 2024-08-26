package com.megadeploy.utility;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

public class JsonResponseUtil {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static String toJson(Object obj) throws Exception {
        return objectMapper.writeValueAsString(obj);
    }

    public static <T> T fromJson(String json, Class<T> clazz) throws IOException {
        return objectMapper.readValue(json, clazz);
    }
}