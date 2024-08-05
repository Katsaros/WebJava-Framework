package com.megadeploy.endpoints;

import com.megadeploy.annotations.*;

@Endpoint("/status")
public class StatusEndpoint {
    @Get("/")
    public String getStatus() {
        String msg = "WebJava GET is up.";
        System.out.println(msg);
        return msg;
    }

    @Post("/")
    public String getPostStatus() {
        String msg = "WebJava POST is up.";
        System.out.println(msg);
        return msg;
    }

    @Put("/")
    public String getPutStatus() {
        String msg = "WebJava PUT is up.";
        System.out.println(msg);
        return msg;
    }

    @Delete("/")
    public String getDeleteStatus() {
        String msg = "WebJava DELETE is up.";
        System.out.println(msg);
        return msg;
    }
}