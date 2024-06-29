package com.scim.impl.api.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ScimMember {
    private String $ref;
    private String value;
    private String type;

    public ScimMember(String $ref, String value) {
        this($ref, value, "User");
    }

    public ScimMember(String $ref, String value, String type) {
        this.$ref = $ref;
        this.value = value;
        this.type = type;
    }
}
