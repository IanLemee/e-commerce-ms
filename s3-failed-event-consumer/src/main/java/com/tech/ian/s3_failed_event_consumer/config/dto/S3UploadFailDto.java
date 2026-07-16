package com.tech.ian.s3_failed_event_consumer.config.dto;

public record S3UploadFailDto(String key, String contentType, String email) {
}
