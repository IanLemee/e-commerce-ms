package com.tech.ian.user.repo;

import com.tech.ian.user.model.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, Long> {

    @Query(value = "SELECT * FROM users_tb WHERE EMAIL = :email", nativeQuery = true)
    Optional<UserEntity> loadUserByUsername(@Param("email") String email);
}
