package com.foodshop.service;

import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;
import javax.imageio.ImageIO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * Service for managing file storage using MinIO.
 *
 * <p>Handles image uploads, thumbnail generation, and file deletion.
 */
@Service
public class FileStorageService {

  private static final Logger LOGGER = LoggerFactory.getLogger(FileStorageService.class);
  private static final int THUMBNAIL_WIDTH = 300;
  private static final int THUMBNAIL_HEIGHT = 300;

  private final MinioClient minioClient;
  private final String bucketName;
  private final String endpoint;

  public FileStorageService(
      MinioClient minioClient,
      @Value("${app.minio.bucket-name}") String bucketName,
      @Value("${app.minio.endpoint}") String endpoint) {
    this.minioClient = minioClient;
    this.bucketName = bucketName;
    this.endpoint = endpoint;
  }

  /**
   * Uploads an image file and returns the URL.
   *
   * @param file the image file to upload
   * @return the URL of the uploaded image
   * @throws IOException if upload fails
   */
  public String uploadImage(MultipartFile file) throws IOException {
    String fileName = generateFileName(file.getOriginalFilename());
    String contentType = file.getContentType();

    try {
      minioClient.putObject(
          PutObjectArgs.builder()
              .bucket(bucketName)
              .object(fileName)
              .stream(file.getInputStream(), file.getSize(), -1)
              .contentType(contentType)
              .build()
      );

      return getFileUrl(fileName);
    } catch (Exception e) {
      LOGGER.error("Failed to upload image: {}", fileName, e);
      throw new IOException("Failed to upload image", e);
    }
  }

  /**
   * Uploads an image and generates a thumbnail.
   *
   * @param file the image file to upload
   * @return array containing [originalUrl, thumbnailUrl]
   * @throws IOException if upload or thumbnail generation fails
   */
  public String[] uploadImageWithThumbnail(MultipartFile file) throws IOException {
    String originalUrl = uploadImage(file);

    try {
      // Generate thumbnail
      BufferedImage originalImage = ImageIO.read(file.getInputStream());
      BufferedImage thumbnail = createThumbnail(originalImage);

      // Upload thumbnail
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      ImageIO.write(thumbnail, "jpg", baos);
      byte[] thumbnailBytes = baos.toByteArray();

      String thumbnailFileName = "thumb_" + generateFileName(file.getOriginalFilename());
      minioClient.putObject(
          PutObjectArgs.builder()
              .bucket(bucketName)
              .object(thumbnailFileName)
              .stream(new ByteArrayInputStream(thumbnailBytes), thumbnailBytes.length, -1)
              .contentType("image/jpeg")
              .build()
      );

      String thumbnailUrl = getFileUrl(thumbnailFileName);
      return new String[]{originalUrl, thumbnailUrl};
    } catch (Exception e) {
      LOGGER.error("Failed to generate thumbnail", e);
      // If thumbnail generation fails, return original as thumbnail
      return new String[]{originalUrl, originalUrl};
    }
  }

  /**
   * Deletes a file from storage.
   *
   * @param fileUrl the URL of the file to delete
   */
  public void deleteFile(String fileUrl) {
    try {
      String fileName = extractFileNameFromUrl(fileUrl);
      minioClient.removeObject(
          RemoveObjectArgs.builder()
              .bucket(bucketName)
              .object(fileName)
              .build()
      );
    } catch (Exception e) {
      LOGGER.error("Failed to delete file: {}", fileUrl, e);
    }
  }

  /**
   * Downloads a file from storage.
   *
   * @param fileName the name of the file to download
   * @return input stream of the file
   * @throws IOException if download fails
   */
  public InputStream downloadFile(String fileName) throws IOException {
    try {
      return minioClient.getObject(
          GetObjectArgs.builder()
              .bucket(bucketName)
              .object(fileName)
              .build()
      );
    } catch (Exception e) {
      LOGGER.error("Failed to download file: {}", fileName, e);
      throw new IOException("Failed to download file", e);
    }
  }

  private String generateFileName(String originalFileName) {
    String extension = "";
    if (originalFileName != null && originalFileName.contains(".")) {
      extension = originalFileName.substring(originalFileName.lastIndexOf("."));
    }
    return UUID.randomUUID().toString() + extension;
  }

  private String getFileUrl(String fileName) {
    return endpoint + "/" + bucketName + "/" + fileName;
  }

  private String extractFileNameFromUrl(String fileUrl) {
    return fileUrl.substring(fileUrl.lastIndexOf("/") + 1);
  }

  private BufferedImage createThumbnail(BufferedImage original) {
    int originalWidth = original.getWidth();
    int originalHeight = original.getHeight();

    // Calculate scaling to maintain aspect ratio
    double scale = Math.min(
        (double) THUMBNAIL_WIDTH / originalWidth,
        (double) THUMBNAIL_HEIGHT / originalHeight
    );

    int scaledWidth = (int) (originalWidth * scale);
    int scaledHeight = (int) (originalHeight * scale);

    BufferedImage thumbnail = new BufferedImage(scaledWidth, scaledHeight,
        BufferedImage.TYPE_INT_RGB);
    Graphics2D g = thumbnail.createGraphics();
    g.drawImage(original, 0, 0, scaledWidth, scaledHeight, null);
    g.dispose();

    return thumbnail;
  }
}
