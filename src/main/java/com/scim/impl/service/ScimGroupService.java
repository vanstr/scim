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

import static com.scim.impl.service.Helper.getCount;
import static com.scim.impl.service.Helper.getStartIndex;

@Component
@Slf4j
@RequiredArgsConstructor
public class ScimGroupService {

    private final GroupDatabase db;

    public ScimListResponseDto get(Map<String, String> requestParams) {

        int count = getCount(requestParams);
        int startIndex = getStartIndex(requestParams);
        PageRequest pageRequest = PageRequest.of(startIndex, count);

        // TODO add filtering
        Page<Group> groups = db.findAll(pageRequest);

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
                String.join(",", dto.getMembers())
        );
        db.save(newGroup);
        return new ScimGroupDto(newGroup);
    }

    public ScimResponseDto getById(String id) {
        return db.findById(id).map(ScimGroupDto::new)
                .orElseThrow(() -> new ScimException("Group not found by id " + id, HttpStatus.NOT_FOUND.value()));

    }
}
