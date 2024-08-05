package com.megadeploy.utility;

import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonResponseUtil {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static String toJson(Object obj) throws Exception {
        return objectMapper.writeValueAsString(obj);
    }
}