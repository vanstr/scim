package com.scim.impl.api;

import com.scim.impl.api.dto.ScimErrorDto;
import com.scim.impl.api.dto.ScimGroupDto;
import com.scim.impl.api.dto.ScimPatchDto;
import com.scim.impl.api.dto.ScimResponseDto;
import com.scim.impl.service.ScimException;
import com.scim.impl.service.ScimGroupService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/scim/v2/Groups")
public class GroupController {

    public final ScimGroupService groupService;

    @GetMapping("/{id}")
    public @ResponseBody ScimResponseDto getGroupById(
            @PathVariable String id,
            @RequestParam Map<String, String> params,
            HttpServletResponse response
    ) {
        try {
            String excludedAttributes = params.get("excludedAttributes");
            boolean populateMembers = excludedAttributes == null || !excludedAttributes.equals("members");
            return groupService.getById(id, populateMembers);
        } catch (ScimException se) {
            response.setStatus(se.getErrorCode());
            log.error("Error ", se);
            return new ScimErrorDto(se.getMessage(), se.getErrorCode());
        } catch (Exception e) {
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            log.error("Error ", e);
            return new ScimErrorDto("Error: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    @GetMapping
    public @ResponseBody ScimResponseDto getGroups(
            @RequestParam Map<String, String> params,
            HttpServletResponse response
    ) {
        try {
            return groupService.get(params);
        } catch (ScimException se) {
            response.setStatus(se.getErrorCode());
            log.error("Error ", se);
            return new ScimErrorDto(se.getMessage(), se.getErrorCode());
        } catch (Exception e) {
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            log.error("Error ", e);
            return new ScimErrorDto("Error: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    @PostMapping
    public @ResponseBody ScimResponseDto createGroup(
            @RequestBody ScimGroupDto groupCreationRequest,
            HttpServletResponse response
    ) {
        try {
            return groupService.create(groupCreationRequest);
        } catch (ScimException se) {
            response.setStatus(se.getErrorCode());
            log.error("Error ", se);
            return new ScimErrorDto(se.getMessage(), se.getErrorCode());
        } catch (Exception e) {
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            log.error("Error ", e);
            return new ScimErrorDto("Error: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    @PatchMapping("/{id}")
    public @ResponseBody ScimResponseDto patchGroup(
            @RequestBody ScimPatchDto payload,
            @PathVariable String id,
            HttpServletResponse response
    ) {
        try {
             return groupService.patch(payload, id);
        } catch (ScimException se) {
            response.setStatus(se.getErrorCode());
            return new ScimErrorDto(se.getMessage(), se.getErrorCode());
        } catch (Exception e) {
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            return new ScimErrorDto("Error" + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    @DeleteMapping("/{id}")
    public @ResponseBody ScimResponseDto delete(
            @PathVariable String id,
            HttpServletResponse response
    ) {
        try {
            groupService.deleteById(id);
            return null; // TODO
        } catch (ScimException se) {
            response.setStatus(se.getErrorCode());
            log.error("Error ", se);
            return new ScimErrorDto(se.getMessage(), se.getErrorCode());
        } catch (Exception e) {
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            log.error("Error ", e);
            return new ScimErrorDto("Error: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }
}
