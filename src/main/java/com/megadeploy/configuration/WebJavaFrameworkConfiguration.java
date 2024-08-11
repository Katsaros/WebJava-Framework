package com.megadeploy.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.megadeploy.utility.LogUtil;

import java.io.InputStream;
import java.util.logging.Logger;

public class WebJavaFrameworkConfiguration {
    private static final Logger LOGGER = Logger.getLogger(WebJavaFrameworkConfiguration.class.getName());

    private boolean enableInMemoryDatabase;

    public boolean isEnableInMemoryDatabase() {
        return enableInMemoryDatabase;
    }

    public static WebJavaFrameworkConfiguration loadConfig() {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        WebJavaFrameworkConfiguration config = null;

        try (InputStream inputStream = WebJavaFrameworkConfiguration.class.getResourceAsStream("/webjava-configuration.yml")) {
            if (inputStream != null) {
                config = mapper.readValue(inputStream, WebJavaFrameworkConfiguration.class);
            }
        } catch (Exception e) {
            LogUtil.logWebJava("Error loading webjava-configuration.yml. Loading default framework's configuration");
            config = getDefaultConfig();
        }
        return config;
    }

    private static WebJavaFrameworkConfiguration getDefaultConfig() {
        WebJavaFrameworkConfiguration defaultConfig = new WebJavaFrameworkConfiguration();
        defaultConfig.enableInMemoryDatabase = false;
        return defaultConfig;
    }
}
