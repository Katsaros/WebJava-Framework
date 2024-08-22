package com.megadeploy.generators;

import com.megadeploy.annotations.core.Endpoint;
import com.megadeploy.annotations.core.Operator;
import com.megadeploy.annotations.request.Delete;
import com.megadeploy.annotations.request.Get;
import com.megadeploy.annotations.request.Post;
import com.megadeploy.annotations.request.Put;
import com.megadeploy.core.WebJavaServer;
import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.model.MutableGraph;
import guru.nidi.graphviz.model.Node;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.reflections.util.ConfigurationBuilder;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

import static com.megadeploy.utility.LogUtil.logWebJava;
import static com.megadeploy.utility.LogUtil.logWebJavaN;
import static guru.nidi.graphviz.model.Factory.*;

public class DiagramGenerator {

    public void generateDiagram(String outputPath) throws Exception {
        logWebJavaN("Starting process to generate a diagram with the connection of Endpoints-Operators");
        // Start building the graph
        MutableGraph graph = mutGraph("Endpoints-Operators Diagram").setDirected(true);

        // Get classes with the Endpoint and Operator annotations
        Set<Class<?>> endpointClasses = getClassesWithAnnotation(WebJavaServer.getAppBasePackage(), Endpoint.class);
        Set<Class<?>> operatorClasses = getClassesWithAnnotation(WebJavaServer.getAppBasePackage(), Operator.class);

        // Map to store endpoint to operator relationships
        Map<Class<?>, Set<Class<?>>> endpointToOperators = new HashMap<>();

        // Iterate through endpoints and add to the graph
        for (Class<?> endpoint : endpointClasses) {
            Node endpointNode = node(endpoint.getSimpleName()).with("shape", "box");
            graph.add(endpointNode);

            // Check if the endpoint uses any operators
            for (Method method : endpoint.getDeclaredMethods()) {
                if (method.isAnnotationPresent(Get.class) ||
                        method.isAnnotationPresent(Post.class) ||
                        method.isAnnotationPresent(Put.class) ||
                        method.isAnnotationPresent(Delete.class)) {

                    String methodName = method.getName();
                    Node methodNode = node(methodName).with("shape", "ellipse");

                    // Add edge between the endpoint and method
                    graph.add(endpointNode.link(methodNode));
                }
            }

            // Track which operators are used by this endpoint
            Set<Class<?>> operatorsUsed = getOperatorsUsedByEndpoint(endpoint, operatorClasses);
            if (!operatorsUsed.isEmpty()) {
                endpointToOperators.put(endpoint, operatorsUsed);
            }
        }

        // Add operators to the graph and link them to the endpoints
        for (Class<?> operator : operatorClasses) {
            Node operatorNode = node(operator.getSimpleName()).with("shape", "diamond");
            graph.add(operatorNode);

            // Add links from endpoints to this operator if applicable
            for (Map.Entry<Class<?>, Set<Class<?>>> entry : endpointToOperators.entrySet()) {
                Class<?> endpoint = entry.getKey();
                if (entry.getValue().contains(operator)) {
                    Node endpointNode = node(endpoint.getSimpleName()).with("shape", "box");
                    graph.add(endpointNode.link(operatorNode));
                }
            }
        }

        // Render the graph to a file
        Graphviz.fromGraph(graph).width(900).render(Format.PNG).toFile(new File(outputPath));
        logWebJava("The file diagram will be generated in src/main/resources/diagram.png. If you can't see it after run then click on 'Reload from Disk'");
    }

    private Set<Class<?>> getClassesWithAnnotation(String basePackage, Class<? extends Annotation> annotation) {
        Reflections reflections = new Reflections(basePackage);
        return reflections.getTypesAnnotatedWith(annotation);
    }

    private Set<Class<?>> getOperatorsUsedByEndpoint(Class<?> endpointClass, Set<Class<?>> operatorClasses) {
        Set<Class<?>> operatorsUsed = new HashSet<>();
        // Check constructor parameters
        for (Constructor<?> constructor : endpointClass.getDeclaredConstructors()) {
            for (Class<?> paramType : constructor.getParameterTypes()) {
                if (operatorClasses.contains(paramType)) {
                    operatorsUsed.add(paramType);
                }
            }
        }

        // Check fields
        for (Field field : endpointClass.getDeclaredFields()) {
            // Check if field name ends with "Operator"
            if (field.getName().endsWith("Operator")) {
                // Check if field type is in the set of operator classes
                if (operatorClasses.contains(field.getType())) {
                    operatorsUsed.add(field.getType());
                }
            }
        }

        // Check method parameters
        for (Method method : endpointClass.getDeclaredMethods()) {
            for (Class<?> paramType : method.getParameterTypes()) {
                if (operatorClasses.contains(paramType)) {
                    operatorsUsed.add(paramType);
                }
            }
        }

        return operatorsUsed;
    }

}
