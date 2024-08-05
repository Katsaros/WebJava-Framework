package com.megadeploy.endpoints;

import com.megadeploy.annotations.core.Endpoint;
import com.megadeploy.annotations.request.Delete;
import com.megadeploy.annotations.request.Get;
import com.megadeploy.annotations.request.Post;
import com.megadeploy.annotations.request.Put;

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