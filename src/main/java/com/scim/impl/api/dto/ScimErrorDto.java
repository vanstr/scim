package com.scim.impl.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.util.List;

@Getter
public class ScimErrorDto extends ScimResponseDto {

    @JsonProperty("schemas")
    private final List<String> schemas;
    @JsonProperty("detail")
    private final String detail;
    @JsonProperty("status")
    private final Integer status;

    public ScimErrorDto(String message, Integer statusCode) {
        this.schemas = List.of("urn:ietf:params:scim:api:messages:2.0:Error");
        this.detail = message;
        this.status = statusCode;
    }
}
