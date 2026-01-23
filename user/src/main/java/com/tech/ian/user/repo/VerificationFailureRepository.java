package com.tech.ian.user.repo;

import com.tech.ian.user.model.verification_failures.VerificationFailureEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface VerificationFailureRepository extends JpaRepository<VerificationFailureEntity, Long> {

    @Query(value = "SELECT * FROM verification_failure_tb ORDER BY failed_id ASC LIMIT 50", nativeQuery = true)
    List<VerificationFailureEntity> findAll();
}
