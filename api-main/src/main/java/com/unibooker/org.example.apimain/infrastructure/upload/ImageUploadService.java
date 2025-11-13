package com.unibooker.main.infrastructure.upload;

import com.unibooker.common.exception.BaseException;
import com.unibooker.common.exception.BaseResponseStatus;
import com.unibooker.common.util.FileUploadUtil;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;

/**
 * 이미지 업로드 서비스
 * - AWS S3 Presigned URL 생성
 * - 파일 검증 및 업로드 처리
 */
@Slf4j
@Service
public class ImageUploadService {

    // ========== AWS S3 설정 ==========

    @Value("${aws.s3.access-key}")
    private String accessKey;

    @Value("${aws.s3.secret-key}")
    private String secretKey;

    @Value("${aws.s3.bucket}")
    private String bucketName;

    @Value("${aws.s3.region}")
    private String region;

    @Value("${file.upload.max-size:5242880}") // 기본값: 5MB
    private long maxFileSize;

    private S3Presigner s3Presigner;

    // ========== 파일 검증 설정 ==========

    /** 허용 확장자 */
    private static final List<String> ALLOWED_EXTENSIONS =
            Arrays.asList("jpg", "jpeg", "png", "gif", "webp");

    /** 허용 Content-Type */
    private static final List<String> ALLOWED_CONTENT_TYPES = Arrays.asList(
            "image/jpeg",
            "image/jpg",
            "image/png",
            "image/gif",
            "image/webp"
    );

    /**
     * S3Presigner 초기화
     * - AWS 자격증명 설정
     * - 리전 설정
     */
    @PostConstruct
    public void init() {
        log.info("🚀 AWS S3 초기화 시작 - Region: {}, Bucket: {}", region, bucketName);

        try {
            AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKey, secretKey);
            s3Presigner = S3Presigner.builder()
                    .region(Region.of(region))
                    .credentialsProvider(StaticCredentialsProvider.create(credentials))
                    .build();

            log.info("✅ AWS S3 초기화 완료");
        } catch (Exception e) {
            log.error("❌ AWS S3 초기화 실패: {}", e.getMessage(), e);
            throw new RuntimeException("AWS S3 초기화 실패", e);
        }
    }

    // ========== 공개 API ==========

    /**
     * 이미지 업로드용 Presigned URL 생성 (MultipartFile 사용)
     *
     * @param file 업로드할 파일
     * @param imageType 이미지 타입 ("companyLogo", "serviceGroup", "service")
     * @return Presigned URL (10분 유효)
     * @throws BaseException 파일 검증 실패 시
     */
    public String getPresignedUrl(MultipartFile file, String imageType) {
        log.info("📤 Presigned URL 생성 요청 - imageType: {}, fileName: {}",
                imageType, file.getOriginalFilename());

        // 1. 파일 검증
        validateFile(file);

        // 2. S3 Key 생성 (업로드 경로 + 파일명)
        String s3Key = createS3Key(file.getOriginalFilename(), imageType);

        // 3. Presigned URL 생성 및 반환
        return generatePresignedUrl(s3Key, file.getContentType());
    }

    /**
     * 기업 로고용 Presigned URL 생성 (파일명만 사용)
     *
     * @param fileName 파일명
     * @param contentType Content-Type (예: "image/png")
     * @return Presigned URL (10분 유효)
     * @throws BaseException 검증 실패 시
     */
    public String getPresignedUrlForCompanyLogo(String fileName, String contentType) {
        log.info("📤 기업 로고 Presigned URL 생성 요청 - fileName: {}", fileName);

        // 1. Content-Type 검증
        validateContentType(contentType);

        // 2. 확장자 검증
        validateExtension(fileName);

        // 3. S3 Key 생성
        String s3Key = createS3Key(fileName, "companyLogo");

        // 4. Presigned URL 생성 및 반환
        return generatePresignedUrl(s3Key, contentType);
    }

    // ========== 내부 메서드 ==========

    /**
     * S3 Key 생성 (업로드 경로)
     *
     * @param originalFilename 원본 파일명
     * @param imageType 이미지 타입
     * @return S3 Key (예: "company-logo/2025/01/29/abc123/uuid_logo.png")
     */
    private String createS3Key(String originalFilename, String imageType) {
        // 1. imageType에 따라 폴더명 결정
        String folder = mapImageTypeToFolder(imageType);

        // 2. 날짜 기반 경로 생성 (Common 유틸 사용)
        String basePath = FileUploadUtil.makeUploadPath(folder);

        // 3. 고유 파일명 생성 (Common 유틸 사용)
        String uniqueFileName = FileUploadUtil.generateUniqueFileName(originalFilename);

        // 4. 최종 S3 Key 반환
        String s3Key = basePath + "/" + uniqueFileName;
        log.debug("S3 Key 생성: {}", s3Key);

        return s3Key;
    }

    /**
     * imageType을 폴더명으로 매핑
     *
     * @param imageType 이미지 타입
     * @return 폴더명
     * @throws BaseException 잘못된 imageType
     */
    private String mapImageTypeToFolder(String imageType) {
        return switch (imageType) {
            case "companyLogo" -> "company-logo";
            case "serviceGroup" -> "resource-group-thumbnail";
            case "service" -> "resource-thumbnail";
            default -> {
                log.error("알 수 없는 imageType: {}", imageType);
                throw new BaseException(BaseResponseStatus.BAD_REQUEST);
            }
        };
    }

    /**
     * Presigned URL 생성
     *
     * @param s3Key S3 Key (업로드 경로)
     * @param contentType Content-Type
     * @return Presigned URL (10분 유효)
     */
    private String generatePresignedUrl(String s3Key, String contentType) {
        try {
            // 1. PutObjectRequest 생성
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3Key)
                    .contentType(contentType)
                    .build();

            // 2. Presigned URL 생성 (10분 유효)
            PresignedPutObjectRequest presignedRequest = s3Presigner.presignPutObject(
                    builder -> builder
                            .putObjectRequest(putObjectRequest)
                            .signatureDuration(Duration.ofMinutes(10))
            );

            String url = presignedRequest.url().toString();
            log.info("✅ Presigned URL 생성 완료 - S3 Key: {}", s3Key);

            return url;

        } catch (Exception e) {
            log.error("❌ Presigned URL 생성 실패 - S3 Key: {}, error: {}", s3Key, e.getMessage(), e);
            throw new BaseException(BaseResponseStatus.FILE_UPLOAD_FAILED);
        }
    }

    // ========== 파일 검증 ==========

    /**
     * MultipartFile 검증
     * - 파일 존재 여부
     * - 파일 크기
     * - Content-Type
     * - 파일 확장자
     *
     * @param file MultipartFile
     * @throws BaseException 검증 실패 시
     */
    private void validateFile(MultipartFile file) {
        // 1. 파일이 비어있는지 확인
        if (file == null || file.isEmpty()) {
            log.error("파일이 비어있음");
            throw new BaseException(BaseResponseStatus.BAD_REQUEST);
        }

        // 2. 파일 크기 검증
        if (file.getSize() > maxFileSize) {
            log.error("파일 크기 초과: {} bytes (최대: {} bytes)", file.getSize(), maxFileSize);
            throw new BaseException(BaseResponseStatus.FILE_SIZE_EXCEEDED);
        }

        // 3. Content-Type 검증
        validateContentType(file.getContentType());

        // 4. 파일 확장자 검증
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            log.error("파일명이 null");
            throw new BaseException(BaseResponseStatus.INVALID_FILE_TYPE);
        }
        validateExtension(originalFilename);
    }

    /**
     * Content-Type 검증
     *
     * @param contentType Content-Type
     * @throws BaseException 지원하지 않는 Content-Type
     */
    private void validateContentType(String contentType) {
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            log.error("지원하지 않는 파일 형식: {}", contentType);
            throw new BaseException(BaseResponseStatus.INVALID_FILE_TYPE);
        }
    }

    /**
     * 파일 확장자 검증
     *
     * @param filename 파일명
     * @throws BaseException 지원하지 않는 확장자
     */
    private void validateExtension(String filename) {
        String extension = FileUploadUtil.getFileExtension(filename);
        if (!ALLOWED_EXTENSIONS.contains(extension.toLowerCase())) {
            log.error("지원하지 않는 파일 확장자: {}", extension);
            throw new BaseException(BaseResponseStatus.INVALID_FILE_TYPE);
        }
    }
}