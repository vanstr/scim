package com.scim.impl.api;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/scim/ResourceTypes")
public class ResourceTypesController {

    // Example: https://scim.dev/playground/resource-types.html
    private static String userResourceType = """
            {
                "schemas": [
                    "urn:ietf:params:scim:schemas:core:2.0:ResourceType"
                ],
                "id": "User",
                "name": "Users",
                "endpoint": "/scim/v2/Users",
                "description": "User Account",
                "schema": "urn:ietf:params:scim:schemas:core:2.0:User",
                "schemaExtensions": [],
                "meta": {
                    "location": "/scim/v2/ResourceTypes/User",
                    "resourceType": "ResourceType"
                }
            })
            """;
    private static String groupResourceType = """
            {
                "schemas": [
                    "urn:ietf:params:scim:schemas:core:2.0:ResourceType"
                ],
                "id": "User",
                "name": "Users",
                "endpoint": "/scim/v2/Groups",
                "description": "User groups",
                "schema": "urn:ietf:params:scim:schemas:core:2.0:Group",
                "schemaExtensions": [],
                "meta": {
                    "location": "/scim/v2/ResourceTypes/Group",
                    "resourceType": "ResourceType"
                }
            })
            """;
    private static String response = """
            {
              "totalResults": 2,
              "itemsPerPage": 2,
              "startIndex": 1,
              "schemas": [
                "urn:ietf:params:scim:api:messages:2.0:ListResponse"
              ],
              "Resources": [
                    {{USER_RESOURCE_TYPE}},
                    {{GROUP_RESOURCE_TYPE}}
              ]
            }          
            """.replace("{{USER_RESOURCE_TYPE}}", userResourceType)
            .replace("{{GROUP_RESOURCE_TYPE}}", groupResourceType);

    @GetMapping(produces = "application/json")
    public @ResponseBody String getResourceTypes() {
        return response;
    }

    @GetMapping(value = "/User", produces = "application/json")
    public  @ResponseBody String getUsersResourceTypes() {
        return userResourceType;
    }

    @GetMapping(value = "/Group", produces = "application/json")
    public  @ResponseBody String getGroupsResourceTypes() {
        return groupResourceType;
    }
}
