package com.tech.ian.user.model.verification_failures;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "verification_failure_tb")
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class VerificationFailureEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "failed_id")
    private Long id;
    @Column(name = "failed_user_email")
    private String email;
    @Column(name = "failed_user_code")
    private int code;
}
