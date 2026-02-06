package com.foodshop.config;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MinIO configuration for file storage.
 *
 * <p>Creates and configures the MinIO client for image uploads.
 */
@Configuration
public class MinioConfig {

  private static final Logger LOGGER = LoggerFactory.getLogger(MinioConfig.class);

  @Value("${app.minio.endpoint}")
  private String endpoint;

  @Value("${app.minio.access-key}")
  private String accessKey;

  @Value("${app.minio.secret-key}")
  private String secretKey;

  @Value("${app.minio.bucket-name}")
  private String bucketName;

  /**
   * Creates and configures the MinIO client.
   *
   * @return configured MinIO client
   */
  @Bean
  public MinioClient minioClient() {
    try {
      MinioClient minioClient = MinioClient.builder()
          .endpoint(endpoint)
          .credentials(accessKey, secretKey)
          .build();

      // Create bucket if it doesn't exist
      boolean bucketExists = minioClient.bucketExists(
          BucketExistsArgs.builder()
              .bucket(bucketName)
              .build()
      );

      if (!bucketExists) {
        minioClient.makeBucket(
            MakeBucketArgs.builder()
                .bucket(bucketName)
                .build()
        );
        LOGGER.info("Created MinIO bucket: {}", bucketName);
      }

      return minioClient;
    } catch (Exception e) {
      LOGGER.error("Failed to initialize MinIO client", e);
      throw new RuntimeException("Failed to initialize MinIO client", e);
    }
  }
}
