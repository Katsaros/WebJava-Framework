package com.megadeploy.core;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

public class OpenApiServlet extends HttpServlet {
    private static final String OPENAPI_JSON_PATH = WebJavaServer.getOutputPath();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        File file = new File(OPENAPI_JSON_PATH);
        resp.setContentType("application/json");
        resp.setHeader("Content-Disposition", "inline; filename=\"openapi.json\"");
        try (FileInputStream fis = new FileInputStream(file);
             OutputStream os = resp.getOutputStream()) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
        }
    }
}