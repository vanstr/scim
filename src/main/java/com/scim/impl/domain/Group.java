package com.scim.impl.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "groups", uniqueConstraints = @UniqueConstraint(columnNames = {"name"}))
public class Group {
    @Column(length = 36)
    @Id
    public String id;

    @Column(length = 36)
    public String externalId;

    @Column
    public Boolean active = false;

    @Column(unique = true, nullable = false, length = 250)
    public String name;

    @Column
    public String members; // TODO add mapping

    public String created = LocalDateTime.now().toString();
    public String lastModified = LocalDateTime.now().toString();

    public Group() {
    }

    public Group(String id, String externalId, Boolean active, String name, String members) {
        this.id = id;
        this.externalId = externalId;
        this.active = active;
        this.name = name;
        this.members = members;
    }


}
