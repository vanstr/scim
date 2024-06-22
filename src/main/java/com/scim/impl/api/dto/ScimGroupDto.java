package com.scim.impl.api.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.scim.impl.domain.Group;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ScimGroupDto extends ScimResponseDto {

    @JsonProperty("schemas")
    private final List<String> schemas;
    @JsonProperty("id")
    private final String id;
    @JsonProperty("externalId")
    private final String externalId;
    @JsonProperty("displayName")
    private final String displayName;
    @JsonProperty("members")
    private final List<String> members;
    @JsonProperty("meta")
    private final Map<String, Object> meta;


    public ScimGroupDto(Group group) {
        this.schemas = List.of("urn:ietf:params:scim:schemas:core:2.0:User");
        this.id = group.getId();
        this.externalId = group.getExternalId();
        this.displayName = group.getName();
        this.members = Arrays.stream(group.getMembers().split(",")).toList();
        this.meta = new HashMap<>();
        meta.put("resourceType", "Group");
        meta.put("created", group.getCreated());
        meta.put("lastModified", group.getLastModified());
        meta.put("meta", ("/scim/v2/Groups/" + group.getId()));
    }
}
