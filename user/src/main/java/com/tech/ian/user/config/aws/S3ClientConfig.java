package com.tech.ian.user.config.aws;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
public class S3ClientConfig {

    private final S3Properties s3Properties;

    public S3ClientConfig(S3Properties s3Properties) {
        this.s3Properties = s3Properties;
    }


    @Bean
    public S3Client client() {
        return S3Client.builder()
                .region(Region.of(s3Properties.getRegion()))
                .build();
    }
}
