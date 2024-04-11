package com.scim.impl.api;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

@Controller
@RequestMapping("/scim/Schemas")
public class SchemaController {
    private static final String scimBaseUrl = "https://careful-logical-hippo.ngrok-free.app/scim";

    private static String schema = """
            {
              "totalResults": 1,
              "itemsPerPage": 1,
              "startIndex": 1,
              "schemas": [
                "urn:ietf:params:scim:api:messages:2.0:ListResponse"
              ],
              "Resources": [
                    {{USER_SCHEMA}}
              ]
             }
            """;
    @GetMapping(value = "/urn:ietf:params:scim:schemas:core:2.0:User", produces = "application/scim+json")
    public @ResponseBody String getUserSchema() throws Exception {
        URI path = getClass().getResource("/UserSchema.json").toURI();
        return Files.readString(Paths.get(path), StandardCharsets.UTF_8).replace("{{BASE_URL}}", scimBaseUrl);
    }

    @GetMapping( produces = "application/scim+json")
    public  @ResponseBody String getSchema() throws Exception {
        return schema.replace("{{USER_SCHEMA}}", this.getUserSchema());
    }

}
