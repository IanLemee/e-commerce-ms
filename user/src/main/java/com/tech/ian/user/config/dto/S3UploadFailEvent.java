package com.tech.ian.user.config.dto;

public record S3UploadFailEvent(String key, String contentType, String email) {
}
