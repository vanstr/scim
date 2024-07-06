package com.scim.impl.domain;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Entity
@Getter
@Setter
@Table(name = "users", uniqueConstraints = @UniqueConstraint(columnNames = {"userName"}))
public class User {
    @Column(length = 36)
    @Id
    public String id;

    @Column
    public Boolean active = false;

    @Column(unique = true, nullable = false, length = 250)
    public String userName;

    @Column(length = 250)
    public String familyName;

    @Column(length = 250)
    public String middleName;

    @Column(length = 250)
    public String givenName;

    public User() {
    }

    public User(Map<String, Object> resource) {
        this.update(resource);
    }

    public void update(Map<String, Object> resource) {
        try {
            if (resource.get("name") != null) {

                Map<String, Object> names = (Map<String, Object>) resource.get("name");
                for (String subName : names.keySet()) {
                    switch (subName) {
                        case "givenName" -> this.givenName = names.get(subName).toString();
                        case "familyName" -> this.familyName = names.get(subName).toString();
                        case "middleName" -> this.middleName = names.get(subName).toString();
                        default -> {
                        }
                    }
                }
            }
            this.userName = resource.get("userName").toString();
            this.active = (Boolean) resource.get("active");
        } catch (Exception e) {
            System.out.println(e);
        }
    }


    public Map<String, Object> toScimResource() {
        Map<String, Object> returnValue = new HashMap<>();
        List<String> schemas = new ArrayList<>();
        schemas.add("urn:ietf:params:scim:schemas:core:2.0:User");
        returnValue.put("schemas", schemas);
        returnValue.put("id", this.id);
        returnValue.put("active", this.active);
        returnValue.put("userName", this.userName);

        Map<String, Object> names = new HashMap<>();
        names.put("familyName", this.familyName);
        names.put("givenName", this.givenName);
        names.put("middleName", this.middleName);
        returnValue.put("name", names);

        Map<String, Object> meta = new HashMap<>();
        meta.put("resourceType", "User");
        meta.put("meta", ("/scim/Users/" + this.id));
        returnValue.put("meta", meta);

        return returnValue;
    }
}
