package com.tech.ian.user.repo;

import com.tech.ian.user.model.user.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, Long> {

    @Query(value = "SELECT * FROM users_tb WHERE user_email = :email", nativeQuery = true)
    Optional<UserEntity> findUserByEmail(@Param("email") String email);
}
