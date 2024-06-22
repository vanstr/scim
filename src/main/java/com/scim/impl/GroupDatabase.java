package com.scim.impl;

import com.scim.impl.domain.Group;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface GroupDatabase extends JpaRepository<Group, String> {

    Page<Group> findByNameIgnoreCase(@Param("name") String name, Pageable pageable);

}