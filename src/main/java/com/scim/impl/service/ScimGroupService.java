package com.scim.impl.service;

import com.scim.impl.GroupDatabase;
import com.scim.impl.api.dto.ScimGroupDto;
import com.scim.impl.api.dto.ScimListResponseDto;
import com.scim.impl.api.dto.ScimResponseDto;
import com.scim.impl.domain.Group;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.scim.impl.service.Helper.getCount;
import static com.scim.impl.service.Helper.getStartIndex;

@Component
@Slf4j
@RequiredArgsConstructor
public class ScimGroupService {

    private final GroupDatabase db;

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
                }else{
                    throw new ScimException("Unsupported filter key '" + searchKeyName + "'", HttpStatus.BAD_REQUEST.value());
                }
            } else {
                groups = db.findAll(pageRequest);
            }
        } else {
            groups = db.findAll(pageRequest);
        }


        List<ScimGroupDto> foundUsers = groups.getContent().stream().map(ScimGroupDto::new).toList();
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
                dto.getDisplayName(),
                ""
        );
        db.save(newGroup);
        return new ScimGroupDto(newGroup);
    }

    public ScimResponseDto getById(String id) {
        return db.findById(id).map(ScimGroupDto::new)
                .orElseThrow(() -> new ScimException("Group not found by id " + id, HttpStatus.NOT_FOUND.value()));

    }
}
