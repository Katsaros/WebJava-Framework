package com.megadeploy.core.servlets;

import com.megadeploy.core.WebJavaServer;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.webjars.WebJarAssetLocator;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import static com.megadeploy.utility.LogUtil.logWebJava;

public class SwaggerUiServlet extends HttpServlet {
    private static final WebJarAssetLocator assetLocator = new WebJarAssetLocator();

    public SwaggerUiServlet(int mainPort) {
        logWebJava("The file openapi.json will be generated in " + WebJavaServer.getOpenapiJson() + ". If you can't see it after run then click on 'Reload from Disk'");
        logWebJava("File openapi.json exposed at →", "http://localhost:" + mainPort + "/openapi.json");
        logWebJava("Swagger API exposed at →", "http://localhost:" + mainPort + "/swagger-ui/index.html");
    }


    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String path = req.getPathInfo();
        if (path == null || path.equals("/")) {
            path = "/index.html";
        }

        String fullPath = assetLocator.getFullPath("swagger-ui", path);

        resp.setContentType(getServletContext().getMimeType(fullPath));

        try (InputStream is = getClass().getClassLoader().getResourceAsStream(fullPath);
             OutputStream os = resp.getOutputStream()) {

            if (is == null) {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Resource not found: " + fullPath);
                return;
            }

            if ("/index.html".equals(path)) {
                String indexHtml = new String(is.readAllBytes(), StandardCharsets.UTF_8);
                os.write(indexHtml.getBytes(StandardCharsets.UTF_8));
            } else if ("/swagger-initializer.js".equals(path)) {
                String initializerJs = new String(is.readAllBytes(), StandardCharsets.UTF_8);
                String modifiedJs = initializerJs.replace(
                        "url: \"https://petstore.swagger.io/v2/swagger.json\"",
                        "url: \"/openapi.json\""
                );
                os.write(modifiedJs.getBytes(StandardCharsets.UTF_8));
            } else {
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = is.read(buffer)) != -1) {
                    os.write(buffer, 0, bytesRead);
                }
            }
        }
    }
}