package quickbite.boundary.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiError(String code, String message, Map<String, String> errors) {

    public ApiError(String code, String message) {
        this(code, message, null);
    }
}
