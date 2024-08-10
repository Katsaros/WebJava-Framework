package com.megadeploy.generators;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.megadeploy.annotations.core.Endpoint;
import com.megadeploy.annotations.request.Delete;
import com.megadeploy.annotations.request.Get;
import com.megadeploy.annotations.request.Post;
import com.megadeploy.annotations.request.Put;
import com.megadeploy.core.ClassFinder;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;

import java.io.File;
import java.io.FileWriter;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.logging.Logger;

public class OpenApiGenerator {
    private static final String API_TITLE = "WebJava API";
    private static final String API_VERSION = "1.0.0";
    private static final String API_DESCRIPTION = "API documentation for WebJava application";
    private static final Logger LOGGER = Logger.getLogger(OpenApiGenerator.class.getName());

    public static void generateOpenApiSpec(String outputPath, String basePackage) throws Exception {
        Map<String, Object> templateParams = new HashMap<>();
        templateParams.put("title", API_TITLE);
        templateParams.put("version", API_VERSION);
        templateParams.put("description", API_DESCRIPTION);

        List<Class<?>> classes = ClassFinder.findClasses(basePackage);
        List<String> classNames = classes.stream()
                .map(Class::getName)
                .collect(Collectors.toList());

        OpenAPI openAPI = new OpenAPI()
                .info(new Info()
                        .title((String) templateParams.get("title"))
                        .version((String) templateParams.get("version"))
                        .description((String) templateParams.get("description")))
                .paths(new Paths())
                .components(new Components());

        for (String className : classNames) {
            Class<?> clazz = Class.forName(className);
            if (clazz.isAnnotationPresent(Endpoint.class)) {
                Endpoint endpoint = clazz.getAnnotation(Endpoint.class);
                String basePath = endpoint.value();

                for (Method method : clazz.getDeclaredMethods()) {
                    String httpMethod = getHttpMethod(method);
                    if (httpMethod != null) {
                        String path = basePath + getPath(method);
                        PathItem pathItem = openAPI.getPaths().get(path);
                        if (pathItem == null) {
                            pathItem = new PathItem();
                            openAPI.getPaths().addPathItem(path, pathItem);
                        }

                        Operation operation = new Operation()
                                .operationId(method.getName())
                                .summary(method.getName())
                                .responses(new ApiResponses()
                                        .addApiResponse("200", new ApiResponse().description("OK"))
                                        .addApiResponse("404", new ApiResponse().description("Not Found"))
                                        .addApiResponse("500", new ApiResponse().description("Internal Server Error")));

                        switch (httpMethod) {
                            case "get":
                                pathItem.setGet(operation);
                                break;
                            case "post":
                                pathItem.setPost(operation);
                                break;
                            case "put":
                                pathItem.setPut(operation);
                                break;
                            case "delete":
                                pathItem.setDelete(operation);
                                break;
                        }
                    }
                }
            }
        }

        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        File outputFile = new File(outputPath);
        if (!outputFile.exists()) {
            outputFile.createNewFile();
        }
        try (FileWriter fw = new FileWriter(outputFile)) {
            mapper.writeValue(fw, openAPI);
        }
    }

    private static String getHttpMethod(Method method) {
        if (method.isAnnotationPresent(Get.class)) {
            return "get";
        } else if (method.isAnnotationPresent(Post.class)) {
            return "post";
        } else if (method.isAnnotationPresent(Put.class)) {
            return "put";
        } else if (method.isAnnotationPresent(Delete.class)) {
            return "delete";
        }
        return null;
    }

    private static String getPath(Method method) {
        if (method.isAnnotationPresent(Get.class)) {
            return method.getAnnotation(Get.class).value();
        } else if (method.isAnnotationPresent(Post.class)) {
            return method.getAnnotation(Post.class).value();
        } else if (method.isAnnotationPresent(Put.class)) {
            return method.getAnnotation(Put.class).value();
        } else if (method.isAnnotationPresent(Delete.class)) {
            return method.getAnnotation(Delete.class).value();
        }
        return "/";
    }
}
