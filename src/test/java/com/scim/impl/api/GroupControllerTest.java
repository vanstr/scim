package com.scim.impl.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import com.scim.impl.api.dto.ScimGroupDto;
import com.scim.impl.api.dto.ScimPatchDto;
import com.scim.impl.domain.Group;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

import static com.scim.impl.service.ScimGroupService.PATCH_OP;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class GroupControllerTest {

    public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    @Autowired
    private MockMvc mockMvc;

    @Transactional
    @Test
    public void crud() throws Exception {

        Group adminGroup = prepareGroup("Administrators", "12345");
        ResultActions resultActions = createGroup(adminGroup);
        resultActions
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.externalId").value("12345"))
                .andExpect(jsonPath("$.displayName").value("Administrators"))
                .andExpect(jsonPath("$.meta.lastModified").exists())
                .andExpect(jsonPath("$.meta.location").exists())
                .andExpect(jsonPath("$.meta.resourceType").value("Group"))
                .andExpect(jsonPath("$.meta.created").exists());

        String contentAsString = resultActions.andReturn().getResponse().getContentAsString();
        String adminGroupId = JsonPath.parse(contentAsString).read("$.id", String.class);

        Group testersGroup = prepareGroup("Testers", "5432");
        createGroup(testersGroup);

        mockMvc.perform(get("/scim/v2/Groups")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalResults").value(2))
                .andExpect(jsonPath("$.Resources[0].id").exists())
                .andExpect(jsonPath("$.Resources[0].externalId").value("12345"))
                .andExpect(jsonPath("$.Resources[0].displayName").value("Administrators"))
                .andExpect(jsonPath("$.Resources[0].meta.lastModified").exists())

                .andExpect(jsonPath("$.Resources[1].id").exists())
                .andExpect(jsonPath("$.Resources[1].externalId").value("5432"))
                .andExpect(jsonPath("$.Resources[1].displayName").value("Testers"))
                .andExpect(jsonPath("$.Resources[1].meta.lastModified").exists());

        ScimPatchDto scimPatchDto = new ScimPatchDto(
                List.of(PATCH_OP),
                List.of(Map.of(
                        "op", "Replace",
                        "path", "displayName",
                        "value", "Admins")
                )
        );
        String updatePayload = OBJECT_MAPPER.writeValueAsString(scimPatchDto);
        mockMvc.perform(patch("/scim/v2/Groups/" + adminGroupId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updatePayload))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.externalId").value("12345"))
                .andExpect(jsonPath("$.displayName").value("Admins"))
                .andExpect(jsonPath("$.meta.lastModified").exists());
    }

    private static Group prepareGroup(String name, String externalId) {
        Group groupEntity = new Group();
        groupEntity.setName(name);
        groupEntity.setActive(true);
        groupEntity.setExternalId(externalId);
        return groupEntity;
    }

    private ResultActions createGroup(Group groupEntity) throws Exception {
        ScimGroupDto group = new ScimGroupDto(groupEntity);
        String groupjson = OBJECT_MAPPER.writeValueAsString(group);
        ResultActions resultActions = mockMvc.perform(post("/scim/v2/Groups")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(groupjson))
                .andExpect(status().isOk());
        return resultActions;
    }

}