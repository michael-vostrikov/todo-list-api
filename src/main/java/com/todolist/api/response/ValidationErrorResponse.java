package com.todolist.api.response;

import java.util.Map;
import lombok.Data;

@Data
public class ValidationErrorResponse {
    // field -> error message
    public final Map<String, String> errors;
}
