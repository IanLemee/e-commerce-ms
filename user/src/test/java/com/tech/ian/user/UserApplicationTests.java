package com.tech.ian.user;

import com.tech.ian.user.repo.UserRepository;
import com.tech.ian.user.repo.VerificationFailureRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
@EnableAutoConfiguration(exclude = {DataSourceAutoConfiguration.class, HibernateJpaAutoConfiguration.class})
class UserApplicationTests {

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private VerificationFailureRepository verificationFailureRepository;

    @Test
    void contextLoads() {
    }

}
