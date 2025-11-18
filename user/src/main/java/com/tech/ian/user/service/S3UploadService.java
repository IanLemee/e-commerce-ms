package com.tech.ian.user.service;

import com.tech.ian.user.config.aws.S3Properties;
import com.tech.ian.user.config.dto.S3UploadFailEvent;
import com.tech.ian.user.config.dto.S3UploadSuccessEvent;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;

@Service
public class S3UploadService {

    private final S3Client client;
    private final S3Properties properties;
    @Qualifier("kafkaTemplateS3FailEvent")
    private final KafkaTemplate<String, S3UploadFailEvent> templateS3FailEvent;
    @Qualifier("producerFactoryS3SuccessEvent")
    private final KafkaTemplate<String, S3UploadSuccessEvent> templateS3SuccessEvent;

    public S3UploadService(S3Client client, S3Properties properties, KafkaTemplate<String, S3UploadFailEvent> templateS3FailEvent, KafkaTemplate<String, S3UploadSuccessEvent> templateS3SuccessEvent) {
        this.client = client;
        this.properties = properties;
        this.templateS3FailEvent = templateS3FailEvent;
        this.templateS3SuccessEvent = templateS3SuccessEvent;
    }

    @Async
    public void uploadFileAsync(String key, byte[] data, String contentType, String email) {
        try {
            uploadFileToS3(key, data, contentType, properties.getBucketName());

            String url = generateS3Url(key);
            S3UploadSuccessEvent s3UploadSuccessEvent = new S3UploadSuccessEvent(email, url);
            templateS3SuccessEvent.send("s3-success-topic", s3UploadSuccessEvent);
        } catch (Exception exception) {
            try {
                uploadFileToS3(key, data, contentType, properties.getQuarantineBucketName());
                S3UploadFailEvent event = new S3UploadFailEvent(key, contentType, email);

                templateS3FailEvent.send("s3-failed-dlq", event);
            } catch (IOException e) {
                throw new RuntimeException("Failed to upload file to S3", exception);
            }

        }
    }

    private void uploadFileToS3(String key, byte[] data, String contentType, String bucketName) throws IOException {
        client.putObject(
                PutObjectRequest.builder()
                        .bucket(bucketName)
                        .key(key)
                        .contentType(contentType).
                        build(),
                RequestBody.fromBytes(data));
    }

    private String generateS3Url(String key) {
        return String.format("https://%s.s3.%s.amazonaws.com/%s",
                properties.getBucketName(),
                properties.getRegion(),
                key);
    }
}
