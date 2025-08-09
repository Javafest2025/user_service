package org.solace.scholar_ai.user_service.config;

import java.net.URI;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@Configuration
@Slf4j
public class S3Config {

    @Value("${b2.endpoint:https://s3.ams5.backblazeb2.com}")
    private String b2Endpoint;

    @Value("${b2.key.id:}")
    private String b2KeyId;

    @Value("${b2.application.key:}")
    private String b2ApplicationKey;

    @Value("${b2.bucket.name:}")
    private String b2BucketName;

    @Bean
    public S3Client s3Client() {
        log.info("Configuring S3Client for B2 with endpoint: {}", b2Endpoint);

        if (b2KeyId == null || b2KeyId.isEmpty() || b2ApplicationKey == null || b2ApplicationKey.isEmpty()) {
            log.error(
                    "B2 credentials not configured. Please set B2_KEY_ID and B2_APPLICATION_KEY environment variables.");
            throw new IllegalStateException(
                    "B2 credentials not configured. Please set B2_KEY_ID and B2_APPLICATION_KEY environment variables.");
        }

        return S3Client.builder()
                .region(Region.EU_CENTRAL_1) // Arbitrary region required by SDK
                .endpointOverride(URI.create(b2Endpoint))
                .credentialsProvider(
                        StaticCredentialsProvider.create(AwsBasicCredentials.create(b2KeyId, b2ApplicationKey)))
                .serviceConfiguration(
                        S3Configuration.builder().pathStyleAccessEnabled(true).build())
                .build();
    }

    @Bean
    public S3Presigner s3Presigner() {
        log.info("Configuring S3Presigner for B2 with endpoint: {}", b2Endpoint);

        if (b2KeyId == null || b2KeyId.isEmpty() || b2ApplicationKey == null || b2ApplicationKey.isEmpty()) {
            log.error(
                    "B2 credentials not configured. Please set B2_KEY_ID and B2_APPLICATION_KEY environment variables.");
            throw new IllegalStateException(
                    "B2 credentials not configured. Please set B2_KEY_ID and B2_APPLICATION_KEY environment variables.");
        }

        return S3Presigner.builder()
                .region(Region.EU_CENTRAL_1) // Arbitrary region required by SDK
                .endpointOverride(URI.create(b2Endpoint))
                .credentialsProvider(
                        StaticCredentialsProvider.create(AwsBasicCredentials.create(b2KeyId, b2ApplicationKey)))
                .serviceConfiguration(
                        S3Configuration.builder().pathStyleAccessEnabled(true).build())
                .build();
    }
}
