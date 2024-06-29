package com.scim.impl;

import com.scim.impl.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface UserDatabase extends JpaRepository<User, String> {

    Page<User> findByUserNameIgnoreCase(@Param("name") String name, Pageable pagable);

    Page<User> findByActive(@Param("value") Boolean value, Pageable pagable);

    Page<User> findByFamilyNameIgnoreCase(@Param("name") String name, Pageable pagable);

    Page<User> findByGivenNameIgnoreCase(@Param("name") String name, Pageable pagable);

}