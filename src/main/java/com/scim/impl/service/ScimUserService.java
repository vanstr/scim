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

import java.lang.reflect.Field;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@Slf4j
@RequiredArgsConstructor
public class ScimUserService {

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

    public Map<String, Object> patch(Map<String, Object> payload, String id) {
        List schema = (List) payload.get("schemas");
        List<Map> operations = (List) payload.get("Operations");

        if (schema == null) {
            return scimError("Payload must contain schema attribute.", Optional.of(400));
        }
        if (operations == null) {
            return scimError("Payload must contain operations attribute.", Optional.of(400));
        }

        String schemaPatchOp = "urn:ietf:params:scim:api:messages:2.0:PatchOp";
        if (!schema.contains(schemaPatchOp)) {
            return scimError("The 'schemas' type in this request is not supported.", Optional.of(501));
        }

        User user = db.findById(id).get(0);

        for (Map map : operations) {
            if (map.get("op") == null && !map.get("op").equals("replace")) {
                continue;
            }
            Map<String, Object> value = (Map) map.get("value");

            if (value != null) {
                for (Map.Entry key : value.entrySet()) {
                    try {
                        Field field = user.getClass().getDeclaredField(key.getKey().toString());
                        field.set(user, key.getValue());
                    } catch (NoSuchFieldException | IllegalAccessException e) {
                        log.error("Failed o process patch", e);
                    }
                }
                db.save(user);
            }
        }
        return user.toScimResource();
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
        if(userName.isPresent()){
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
