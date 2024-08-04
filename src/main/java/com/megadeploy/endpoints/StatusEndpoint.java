package com.megadeploy.endpoints;

import com.megadeploy.annotations.*;

@Endpoint("/status")
public class StatusEndpoint {

    @Get("/")
    public void getStatus() {
        System.out.println("WebJava GET is up.");
    }

    @Post("/")
    public void getPostStatus() {
        System.out.println("WebJava POST is up.");
    }

    @Put("/")
    public void getPutStatus() {
        System.out.println("WebJava PUT is up.");
    }

    @Delete("/")
    public void getDeleteStatus() {
        System.out.println("WebJava DELETE is up.");
    }
}