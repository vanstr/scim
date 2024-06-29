package com.scim.impl.api.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.beans.ConstructorProperties;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class ScimPatchDto {
    @JsonProperty("schemas")
    private final List<String> schemas;
    @JsonProperty("Operations")
    private final List<Map<String, Object>> operations;

    @JsonCreator
    @ConstructorProperties(value = {"schemas", "Operations"})
    public ScimPatchDto(List<String> schemas, List<Map<String, Object>> operations) {
        this.schemas = schemas;
        this.operations = operations;
    }

}
