package org.example.unibooker.common;

import jakarta.annotation.PostConstruct;
import org.example.unibooker.utils.FileUploadUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;

import java.io.IOException;
import java.time.Duration;
import java.util.UUID;

@Service
public class ImageUploadService {

    @Value("${S3_ACCESS_KEY}")
    private String accessKey;

    @Value("${S3_SECRET_KEY}")
    private String secretKey;

    @Value("${MY_BUCKET}")
    private String bucketName;

    @Value("${AWS_REGION}")
    private String region;

    private S3Presigner s3Presigner;

    // 초기화
    @PostConstruct
    public void init() {
        System.out.println("AWS Region: " + region);
        AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKey, secretKey);
        s3Presigner = S3Presigner.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .build();
    }

    public String getPresignedUrl(MultipartFile file, String imageType) throws IOException {
        String dirPath = switch (imageType) {
            case "companyLogo" -> FileUploadUtil.makeUploadPath("company-logo");
            case "serviceGroup" -> FileUploadUtil.makeUploadPath("resource-group-thumbnail");
            case "service" -> FileUploadUtil.makeUploadPath("resource-thumbnail");
            default -> throw new IllegalArgumentException("알 수 없는 imageType: " + imageType);
        };

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(dirPath)
                .contentType(file.getContentType())
                .build();

        PresignedPutObjectRequest presignedRequest = s3Presigner.presignPutObject(
                b -> b.putObjectRequest(putObjectRequest)
                        .signatureDuration(Duration.ofMinutes(10)) // URL 만료 시간
        );

        // 생성된 URL 반환 (클라이언트가 직접 S3에 PUT 요청 가능)
        return presignedRequest.url().toString();
    }

    // ✅ 새로 추가할 메서드 (companyLogo 전용)
    public String getPresignedUrlForCompanyLogo(String fileName, String contentType) throws IOException {
        // 파일 확장자 추출
        String extension = fileName.substring(fileName.lastIndexOf("."));

        // UUID + 확장자로 고유한 파일명 생성
        String uniqueFileName = UUID.randomUUID() + extension;

        // S3 경로 생성: company-logo/2025/01/29/uuid.jpg
        String dirPath = FileUploadUtil.makeUploadPath("company-logo") + uniqueFileName;

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(dirPath)
                .contentType(contentType)
                .build();

        PresignedPutObjectRequest presignedRequest = s3Presigner.presignPutObject(
                b -> b.putObjectRequest(putObjectRequest)
                        .signatureDuration(Duration.ofMinutes(10))
        );

        return presignedRequest.url().toString();
    }
}