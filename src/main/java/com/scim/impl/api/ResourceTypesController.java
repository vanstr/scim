package com.scim.impl.api;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/scim/ResourceTypes")
public class ResourceTypesController {
    private static final String scimBaseUrl = "https://careful-logical-hippo.ngrok-free.app/scim";

    // Example: https://scim.dev/playground/resource-types.html
    private static String userResourceType = """
            {
                "schemas": [
                    "urn:ietf:params:scim:schemas:core:2.0:ResourceType"
                ],
                "id": "User",
                "name": "Users",
                "endpoint": "{{BASE_URL}}/Users",
                "description": "User Account",
                "schema": "urn:ietf:params:scim:schemas:core:2.0:User",
                "schemaExtensions": [],
                "meta": {
                    "location": "{{BASE_URL}}/ResourceTypes/User",
                    "resourceType": "ResourceType"
                }
            }
            """.replace("{{BASE_URL}}", scimBaseUrl);
    private static String response = """
            {
              "totalResults": 1,
              "itemsPerPage": 1,
              "startIndex": 1,
              "schemas": [
                "urn:ietf:params:scim:api:messages:2.0:ListResponse"
              ],
              "Resources": [
                    {{USER_RESOURCE_TYPE}}
              ]
            }          
            """.replace("{{USER_RESOURCE_TYPE}}", userResourceType);

    @GetMapping(produces = "application/json")
    public @ResponseBody String getResourceTypes() {
        return response;
    }

    @GetMapping(value = "/User", produces = "application/json")
    public  @ResponseBody String getUsersResourceTypes() {
        return userResourceType;
    }
}
