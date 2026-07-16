package com.tech.ian.user.service;

import com.tech.ian.user.config.dto.UserEmailVerificationDto;
import com.tech.ian.user.model.verification_failures.VerificationFailureEntity;
import com.tech.ian.user.repo.VerificationFailureRepository;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class VerificationFailureService {
    private final VerificationFailureRepository repository;
    private final KafkaTemplate<String, UserEmailVerificationDto> kafkaTemplateVerification;


    public VerificationFailureService(VerificationFailureRepository repository, KafkaTemplate<String, UserEmailVerificationDto> kafkaTemplateVerification) {
        this.repository = repository;
        this.kafkaTemplateVerification = kafkaTemplateVerification;
    }

    public void create(String email, int code) {
        repository.save(verificationFailureBuilder(email, code));
    }

    private static VerificationFailureEntity verificationFailureBuilder(String email, int code) {
        return VerificationFailureEntity.builder().email(email).code(code).build();
    }

    @Scheduled(fixedRate = 30000)
    private void worker() {
        List<VerificationFailureEntity> all = repository.findAll();
        if (all.isEmpty()) {
            return;
        }

        for (VerificationFailureEntity failure: all) {
            try {
                UserEmailVerificationDto dto = new UserEmailVerificationDto(failure.getEmail(), failure.getCode());
                kafkaTemplateVerification.send("email-verification-topic", dto);
                repository.delete(failure);
            }catch (Exception e) {
                // TODO create a error handling
            }
        }
    }
}
