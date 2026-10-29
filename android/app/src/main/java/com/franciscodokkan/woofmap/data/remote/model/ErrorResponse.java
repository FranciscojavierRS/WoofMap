package com.franciscodokkan.woofmap.data.remote.model;

import java.util.Map;

public class ErrorResponse {

    public int status;
    public String error;
    public String message;
    public String path;
    public Map<String, String> validationErrors;
}
