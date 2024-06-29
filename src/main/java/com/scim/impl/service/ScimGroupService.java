package com.scim.impl.service;

import com.scim.impl.GroupDatabase;
import com.scim.impl.UserDatabase;
import com.scim.impl.api.dto.ScimGroupDto;
import com.scim.impl.api.dto.ScimListResponseDto;
import com.scim.impl.api.dto.ScimPatchDto;
import com.scim.impl.api.dto.ScimResponseDto;
import com.scim.impl.domain.Group;
import com.scim.impl.domain.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.scim.impl.service.Helper.getCount;
import static com.scim.impl.service.Helper.getStartIndex;

@Component
@Slf4j
@RequiredArgsConstructor
public class ScimGroupService {

    public static final String PATCH_OP = "urn:ietf:params:scim:api:messages:2.0:PatchOp";
    private final GroupDatabase db;
    private final UserDatabase userDb;

    public ScimListResponseDto get(Map<String, String> params) {

        int count = getCount(params);
        int startIndex = getStartIndex(params);
        PageRequest pageRequest = PageRequest.of(startIndex, count);

        Page<Group> groups;
        String filter = params.get("filter");
        if (filter != null && filter.contains("eq")) {
            String regex = "(\\w+) eq \"([^\"]*)\"";
            Pattern response = Pattern.compile(regex);

            Matcher match = response.matcher(filter);
            if (match.find()) {
                String searchKeyName = match.group(1);
                String searchValue = match.group(2);
                if (searchKeyName.equalsIgnoreCase("displayName")) {
                    groups = db.findByNameIgnoreCase(searchValue, pageRequest);
                } else {
                    throw new ScimException("Unsupported filter key '" + searchKeyName + "'", HttpStatus.BAD_REQUEST.value());
                }
            } else {
                groups = db.findAll(pageRequest);
            }
        } else {
            groups = db.findAll(pageRequest);
        }


        List<ScimGroupDto> foundUsers = groups.getContent().stream()
                .sorted(Comparator.comparing(Group::getCreated))
                .map(ScimGroupDto::new).toList();
        long totalResults = foundUsers.size();

        return new ScimListResponseDto(
                startIndex,
                count,
                totalResults,
                foundUsers
        );
    }


    public ScimResponseDto create(ScimGroupDto dto) {
        PageRequest pageable = PageRequest.of(0, 1);
        Optional<Group> group = db.findByNameIgnoreCase(dto.getDisplayName(), pageable).get().findAny();
        if (group.isPresent()) {
            throw new ScimException("Group already exists", HttpStatus.CONFLICT.value());
        }
        Group newGroup = new Group(
                UUID.randomUUID().toString(),
                dto.getExternalId(),
                true,
                dto.getDisplayName()
        );
        db.save(newGroup);
        return new ScimGroupDto(newGroup);
    }

    public ScimGroupDto patch(ScimPatchDto payload, String id) {
        validate(payload);

        Group group = getValidGroupById(id);
        for (Map<String, Object> map : payload.getOperations()) {
            Object operation = map.get("op");
            if (operation != null && map.get("path") != null && map.get("value") != null) {
                String path = map.get("path").toString();
                String value = map.get("value").toString();
                if(Objects.equals(operation, "Add") && Objects.equals(path, "members")){
                    User user = getValidUser(value);
                    group.getUsers().add(user);
                } else if (Objects.equals(operation, "Remove") && Objects.equals(path, "members")) {
                        group.getUsers().stream()
                                .filter(user -> user.getId().equals(value))
                                .findFirst().ifPresent(group.getUsers()::remove);

                }else if ( operation.equals("Replace")) {
                    if (Objects.equals(path, "displayName")) {
                        group.setName( value);
                    } else if (Objects.equals(path, "externalId")) {
                        group.setExternalId(value);
                    } else {
                        throw new ScimException("Unsupported path '" + path + "'", HttpStatus.BAD_REQUEST.value());
                    }
                } else {
                    throw new ScimException("Unsupported operation '" + operation + "'", HttpStatus.BAD_REQUEST.value());
                }

                group.setLastModified(LocalDateTime.now().toString());
                db.save(group);
            }
        }
        return new ScimGroupDto(group);
    }

    private User getValidUser(String value) {
        return userDb.findById(value).orElseThrow(() -> new ScimException("User not found by id " + value, HttpStatus.NOT_FOUND.value()));
    }

    private void validate(ScimPatchDto payload) {
        if (!payload.getSchemas().contains(PATCH_OP)) {
            throw new ScimException("Request must contain correct schema PatchOp.", HttpStatus.BAD_REQUEST.value());
        }
        if (CollectionUtils.isEmpty(payload.getOperations())) {
            throw new ScimException("Payload must contain operations attribute.", HttpStatus.BAD_REQUEST.value());
        }
    }

    public ScimResponseDto getById(String id) {
        return new ScimGroupDto(getValidGroupById(id));
    }

    private Group getValidGroupById(String id) {
        return db.findById(id)
                .orElseThrow(() -> new ScimException("Group not found by id " + id, HttpStatus.NOT_FOUND.value()));
    }
}
