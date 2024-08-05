package com.megadeploy.dataObjects;

import com.megadeploy.utility.JsonResponseUtil;

public class ApiResponse<T> {

    private T data;
    private String message;
    private int status;

    public ApiResponse() {}

    public ApiResponse(T data, String message, int status) {
        this.data = data;
        this.message = message;
        this.status = status;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String toJson() throws Exception {
        return JsonResponseUtil.toJson(this);
    }

    @Override
    public String toString() {
        return "ApiResponse{" +
                "data=" + data +
                ", message='" + message + '\'' +
                ", status=" + status +
                '}';
    }
}