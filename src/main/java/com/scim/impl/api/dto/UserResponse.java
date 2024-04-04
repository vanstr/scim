package com.scim.impl.api.dto;

import com.scim.impl.domain.User;
import lombok.Getter;

import java.util.*;
import java.util.stream.Collectors;

@Getter
public class UserResponse {
    private final List<User> list;
    private final int startIndex;
    private final int count;
    private final int totalResults;

    public UserResponse(List<User> list, Optional<Integer> startIndex, Optional<Integer> count, Optional<Integer> totalResults) {
        this.list = list;
        this.startIndex = startIndex.orElse(1);
        this.count = count.orElse(0);
        this.totalResults = totalResults.orElse(0);
    }

    public Map<String, Object> toScimResource() {
        Map<String, Object> returnValue = new HashMap<>();

        List<String> schemas = new ArrayList<>();
        schemas.add("urn:ietf:params:scim:api:messages:2.0:ListResponse");
        returnValue.put("schemas", schemas);
        returnValue.put("totalResults", this.totalResults);
        returnValue.put("startIndex", this.startIndex);

        List<Map<String, Object>> resources = this.list.stream().map(User::toScimResource).collect(Collectors.toList());

        if (this.count != 0) {
            returnValue.put("itemsPerPage", this.count);
        }
        returnValue.put("Resources", resources);

        return returnValue;
    }
}
