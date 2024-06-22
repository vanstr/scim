package com.scim.impl.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.util.List;

@Getter
public class ScimListResponseDto extends ScimResponseDto {

    @JsonProperty("schemas")
    private final List<String> schemas;
    @JsonProperty("totalResults")
    private final long totalResults;
    @JsonProperty("startIndex")
    private final int startIndex;
    @JsonProperty("itemsPerPage")
    private final int itemsPerPage;
    @JsonProperty("Resources")
    private final List Resources;

    public ScimListResponseDto(Integer startIndex,
                               Integer count,
                               Long totalResults,
                               List resources
    ) {
        this.schemas = List.of("urn:ietf:params:scim:api:messages:2.0:ListResponse");
        this.totalResults = totalResults;
        this.startIndex = startIndex;
        this.itemsPerPage = count;
        this.Resources = resources;
    }

}
