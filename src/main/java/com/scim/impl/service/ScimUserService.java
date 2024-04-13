package com.scim.impl.service;

import com.scim.impl.Database;
import com.scim.impl.api.dto.UserResponse;
import com.scim.impl.domain.User;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@Slf4j
@RequiredArgsConstructor
public class ScimUserService {

    public static final int BAD_REQUEST = HttpStatus.BAD_REQUEST.value();
    private final Database db;

    public Map<String, Object> getScimUser(String id) {
        log.info("Get user by id {}", id);
        User user = db.findById(id).get(0);
        return user.toScimResource();
    }

    public Map<String, Object> update(Map<String, Object> payload, String id) {
        User user = db.findById(id).get(0);
        user.update(payload);
        return user.toScimResource();
    }

    public Map<String, Object> patch(Map<String, Object> payload, String id, HttpServletResponse response) {
        List schema = (List) payload.get("schemas");
        List<Map> operations = (List) payload.get("Operations");

        validation(schema, operations);

        User user = db.findById(id).get(0);

        for (Map map : operations) {
            if (map.get("op") == null && !map.get("op").equals("replace")) {
                continue;
            }

            if (map.containsKey("path")) {
                String path = map.get("path").toString();
                updateUserField(path, user, map.get("value"));
            } else {
                Map<String, Object> value = (Map) map.get("value");
                if (value != null) {
                    for (Map.Entry key : value.entrySet()) {
                        String fieldName = key.getKey().toString();
                        updateUserField(fieldName, user, key.getValue());
                    }
                }
            }
            db.save(user);
        }
        return user.toScimResource();
    }

    private void validation(List schema, List<Map> operations) {
        if (schema == null) {
            throw new ScimException("Payload must contain schema attribute.", BAD_REQUEST);
        }
        if (operations == null) {
            throw new ScimException("Payload must contain operations attribute.", BAD_REQUEST);
        }
        String schemaPatchOp = "urn:ietf:params:scim:api:messages:2.0:PatchOp";
        if (!schema.contains(schemaPatchOp)) {
            throw new ScimException("The 'schemas' type in this request is not supported.", 501);
        }
    }

    private static void updateUserField(String fieldName, User user, Object value) {
        switch (fieldName) {
            case "active" -> user.setActive((Boolean) value);
            case "userName" -> user.setUserName(value.toString());
            case "name.familyName" -> user.setFamilyName(value.toString());
            case "name.givenName" -> user.setGivenName(value.toString());
            default -> {
                throw new ScimException("Invalid field - " + fieldName, 501);
            }
        }
    }

    public Map<String, Object> get(Map<String, String> params) {
        Page<User> users;

        int count = getCount(params);

        int startIndex = getStartIndex(params);

        PageRequest pageRequest = PageRequest.of(startIndex, count);

        String filter = params.get("filter");
        if (filter != null && filter.contains("eq")) {
            String regex = "(\\w+) eq \"([^\"]*)\"";
            Pattern response = Pattern.compile(regex);

            Matcher match = response.matcher(filter);
            if (match.find()) {
                String searchKeyName = match.group(1);
                String searchValue = match.group(2);
                users = switch (searchKeyName) {
                    case "active" -> db.findByActive(Boolean.valueOf(searchValue), pageRequest);
                    case "faimlyName" -> db.findByFamilyNameIgnoreCase(searchValue, pageRequest);
                    case "givenName" -> db.findByGivenNameIgnoreCase(searchValue, pageRequest);
                    default -> db.findByUserNameIgnoreCase(searchValue, pageRequest);
                };
            } else {
                users = db.findAll(pageRequest);
            }
        } else {
            users = db.findAll(pageRequest);
        }

        List<User> foundUsers = users.getContent();
        int totalResults = foundUsers.size();

        return new UserResponse(
                foundUsers,
                Optional.of(startIndex),
                Optional.of(count),
                Optional.of(totalResults)
        ).toScimResource();
    }

    public Map<String, Object> deleteById(String id) {
        log.info("Delete user by id {}", id);
        User user = db.findById(id).get(0);
        user.active = false;
        User saved = db.save(user);
        return saved.toScimResource();
    }


    private int getCount(Map<String, String> params) {
        return (params.get("count") != null) ? Integer.parseInt(params.get("count")) : 100;
    }

    private int getStartIndex(Map<String, String> params) {
        int startIndex = (params.get("startIndex") != null) ? Integer.parseInt(params.get("startIndex")) : 1;

        if (startIndex < 1) {
            startIndex = 1;
        }
        startIndex -= 1;
        return startIndex;
    }

    public Map<String, Object> upsert(Map<String, Object> params, HttpServletResponse response) {
        Optional<User> userName = db.findByUserNameIgnoreCase(params.get("userName").toString(), PageRequest.of(0, 1)).get().findAny();
        if (userName.isPresent()) {
            response.setStatus(HttpStatus.CONFLICT.value());
            return scimError("User already exists", Optional.of(409));
        }
        User newUser = new User(params);
        newUser.id = UUID.randomUUID().toString();
        db.save(newUser);
        response.setStatus(HttpStatus.CREATED.value());
        return newUser.toScimResource();
    }

    public Map<String, Object> scimError(String message, Optional<Integer> statusCode) {

        Map<String, Object> returnValue = new HashMap<>();
        List<String> schemas = List.of("urn:ietf:params:scim:api:messages:2.0:Error");
        returnValue.put("schemas", schemas);
        returnValue.put("detail", message);

        returnValue.put("status", statusCode.orElse(500));
        return returnValue;
    }
}
