package com.tech.ian.s3_failed_event_consumer.service;

import com.tech.ian.s3_failed_event_consumer.config.aws.S3Properties;
import com.tech.ian.s3_failed_event_consumer.config.dto.S3UploadFailDto;
import com.tech.ian.s3_failed_event_consumer.config.dto.S3UploadSuccessDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;

@Slf4j
@Service
public class ConsumerService {

    private final KafkaTemplate<String, S3UploadSuccessDto> template;
    private final S3Client client;
    private final S3Properties s3Properties;

    public ConsumerService(KafkaTemplate<String, S3UploadSuccessDto> template, S3Client client, S3Properties s3Properties) {
        this.template = template;
        this.client = client;
        this.s3Properties = s3Properties;
    }

    private void sendEvent(S3UploadSuccessDto successDto) {
        template.send("s3-success-topic", successDto);
    }

    @KafkaListener(topics = "s3-failed-dlq", groupId = "s3-failed-service-group")
    private void executeEvent(S3UploadFailDto s3UploadFailDto) {
        byte[] data;
        try {
            data = downloadFileFromS3(s3Properties.getQuarantineBucketName(), s3UploadFailDto.key());

            client.putObject(PutObjectRequest.builder()
                    .bucket(s3Properties.getBucketName())
                    .key(s3UploadFailDto.key())
                    .contentType(s3UploadFailDto.contentType())
                    .build(), RequestBody.fromBytes(data));

            S3UploadSuccessDto successDto = new S3UploadSuccessDto(s3UploadFailDto.email(), generateS3Url(s3UploadFailDto.key()));
            sendEvent(successDto);

            deleteObjectFromS3(s3Properties.getQuarantineBucketName(), s3UploadFailDto.key());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    private void deleteObjectFromS3(String s, String key) {
        DeleteObjectRequest request = DeleteObjectRequest.builder()
                .bucket(s)
                .key(key)
                .build();

        client.deleteObject(request);
    }

    private byte[] downloadFileFromS3(String bucketName, String key) throws IOException {
        GetObjectRequest request = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();
        ResponseInputStream<GetObjectResponse> object = client.getObject(request);

        return object.readAllBytes();
    }

    private String generateS3Url(String key) {
        return String.format("https://%s.s3.%s.amazonaws.com/%s",
                s3Properties.getBucketName(),
                s3Properties.getRegion(),
                key);
    }
}
