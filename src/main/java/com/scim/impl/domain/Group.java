package com.scim.impl.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Getter
@Setter
@Table(name = "groups", uniqueConstraints = @UniqueConstraint(columnNames = {"name"}))
public class Group {
    @Column(length = 36)
    @Id
    private String id;

    @Column(length = 36)
    private String externalId;

    @Column
    private Boolean active = false;

    @Column(unique = true, nullable = false, length = 250)
    private String name;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "user_group",
            joinColumns = @JoinColumn(name = "group_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> users = new HashSet<>();

    private String created = LocalDateTime.now().toString();
    private String lastModified = LocalDateTime.now().toString();

    public Group() {
    }

    public Group(String id, String externalId, Boolean active, String name) {
        this.id = id;
        this.externalId = externalId;
        this.active = active;
        this.name = name;
    }


}
