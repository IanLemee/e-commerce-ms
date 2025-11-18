package com.tech.ian.user.config.aws;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "aws.s3")
@Getter
@Setter
public class S3Properties {

    private String bucketName;
    private String quarantineBucketName;
    private String region;
}
