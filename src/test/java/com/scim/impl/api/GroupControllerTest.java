package com.scim.impl.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.javafaker.Faker;
import com.jayway.jsonpath.JsonPath;
import com.scim.impl.UserDatabase;
import com.scim.impl.api.dto.ScimGroupDto;
import com.scim.impl.api.dto.ScimMember;
import com.scim.impl.api.dto.ScimPatchDto;
import com.scim.impl.domain.Group;
import com.scim.impl.domain.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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

    @Autowired
    private UserDatabase userDatabase;

    private Faker faker;

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
                .andExpect(jsonPath("$.Resources[0].members").isEmpty())
                .andExpect(jsonPath("$.Resources[0].meta.lastModified").exists())

                .andExpect(jsonPath("$.Resources[1].id").exists())
                .andExpect(jsonPath("$.Resources[1].externalId").value("5432"))
                .andExpect(jsonPath("$.Resources[1].displayName").value("Testers"))
                .andExpect(jsonPath("$.Resources[1].members").isEmpty())
                .andExpect(jsonPath("$.Resources[1].meta.lastModified").exists());

        String updateNameRequest = getUpdateNameRequest();
        mockMvc.perform(patch("/scim/v2/Groups/" + adminGroupId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateNameRequest))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.externalId").value("12345"))
                .andExpect(jsonPath("$.displayName").value("Admins"))
                .andExpect(jsonPath("$.meta.lastModified").exists());

        User user1 = createUser();
        User user2 = createUser();
        User user3 = createUser();

        mockMvc.perform(patch("/scim/v2/Groups/" + adminGroupId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(getAddMemberRequest(user1.getId(), user2.getId())))
                .andExpect(status().isOk());

        mockMvc.perform(get("/scim/v2/Groups/" + adminGroupId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.externalId").value("12345"))
                .andExpect(jsonPath("$.displayName").value("Admins"))
                .andExpect(jsonPath("$.members.length()").value(2))
                .andExpect(jsonPath("$.members[?(@.value == '" + user1.getId()+"')].type").value("User"))
                .andExpect(jsonPath("$.members[?(@.value == '" + user2.getId()+"')].type").value("User"))
                .andExpect(jsonPath("$.meta.lastModified").exists());
    }

    private String getAddMemberRequest(String... userIds) throws JsonProcessingException {
        List<ScimMember> scimMembers = Arrays.stream(userIds)
                .map( userId->new ScimMember(null, userId))
                .toList();
        ScimPatchDto scimPatchDto = new ScimPatchDto(
                List.of(PATCH_OP),
                List.of(Map.of(
                        "op", "Add",
                        "path", "members",
                        "value", scimMembers
                )
        ));
        return OBJECT_MAPPER.writeValueAsString(scimPatchDto);

    }

    private static String getUpdateNameRequest() throws JsonProcessingException {
        ScimPatchDto scimPatchDto = new ScimPatchDto(
                List.of(PATCH_OP),
                List.of(Map.of(
                        "op", "Replace",
                        "path", "displayName",
                        "value", "Admins")
                )
        );
        return OBJECT_MAPPER.writeValueAsString(scimPatchDto);
    }

    private User createUser() {
        faker = new Faker();
        User entity = new User();
        entity.setId(UUID.randomUUID().toString());
        entity.setUserName(faker.name().username());
        entity.setGivenName(faker.name().firstName());
        entity.setFamilyName(faker.name().lastName());
        entity.setActive(true);
        return userDatabase.save(entity);
    }

    private static Group prepareGroup(String name, String externalId) {
        Group groupEntity = new Group();
        groupEntity.setName(name);
        groupEntity.setActive(true);
        groupEntity.setExternalId(externalId);
        return groupEntity;
    }

    private ResultActions createGroup(Group groupEntity) throws Exception {
        ScimGroupDto group = new ScimGroupDto(groupEntity, false);
        String groupjson = OBJECT_MAPPER.writeValueAsString(group);
        ResultActions resultActions = mockMvc.perform(post("/scim/v2/Groups")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(groupjson))
                .andExpect(status().isOk());
        return resultActions;
    }

}