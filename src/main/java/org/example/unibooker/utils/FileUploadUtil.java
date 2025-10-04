package org.example.unibooker.utils;

import lombok.extern.slf4j.Slf4j;
import org.example.unibooker.common.BaseResponseStatus;
import org.example.unibooker.common.exception.BaseException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
public class FileUploadUtil {

    @Value("${file.upload.path}")
    private String uploadPath;

    @Value("${file.upload.company-logo-path}")
    private String companyLogoPath;

    @Value("${file.upload.max-size}")
    private long maxFileSize;

    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList("jpg", "jpeg", "png");
    private static final List<String> ALLOWED_CONTENT_TYPES = Arrays.asList(
            "image/jpeg",
            "image/jpg",
            "image/png"
    );

    /**
     * 기업 로고 업로드
     */
    public String uploadCompanyLogo(MultipartFile file) {
        // 파일 검증
        validateFile(file);

        // 고유 파일명 생성
        String uniqueFileName = generateUniqueFileName(file.getOriginalFilename());

        // 저장 경로 생성
        String fullPath = uploadPath + companyLogoPath;

        // 파일 저장
        String savedFilePath = saveFile(file, uniqueFileName, fullPath);

        // URL 반환 (DB 저장용)
        return "/" + savedFilePath;
    }

    /**
     * 파일 검증
     */
    private void validateFile(MultipartFile file) {
        // 파일이 비어있는지 확인
        if (file == null || file.isEmpty()) {
            throw new BaseException(BaseResponseStatus.BAD_REQUEST);
        }

        // 파일 크기 검증
        if (file.getSize() > maxFileSize) {
            log.error("파일 크기 초과: {} bytes (최대: {} bytes)", file.getSize(), maxFileSize);
            throw new BaseException(BaseResponseStatus.FILE_SIZE_EXCEEDED);
        }

        // Content-Type 검증
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            log.error("지원하지 않는 파일 형식: {}", contentType);
            throw new BaseException(BaseResponseStatus.INVALID_FILE_TYPE);
        }

        // 파일 확장자 검증
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !hasValidExtension(originalFilename)) {
            log.error("지원하지 않는 파일 확장자: {}", originalFilename);
            throw new BaseException(BaseResponseStatus.INVALID_FILE_TYPE);
        }
    }

    /**
     * 파일 확장자 검증
     */
    private boolean hasValidExtension(String filename) {
        String extension = getFileExtension(filename);
        return ALLOWED_EXTENSIONS.contains(extension.toLowerCase());
    }

    /**
     * 파일 확장자 추출
     */
    private String getFileExtension(String filename) {
        int lastIndexOf = filename.lastIndexOf(".");
        if (lastIndexOf == -1) {
            return "";
        }
        return filename.substring(lastIndexOf + 1);
    }

    /**
     * 고유 파일명 생성
     */
    private String generateUniqueFileName(String originalFilename) {
        String extension = getFileExtension(originalFilename);
        String uuid = UUID.randomUUID().toString();

        // 원본 파일명에서 확장자 제거 및 안전한 파일명으로 변환
        String baseName = originalFilename.substring(0, originalFilename.lastIndexOf("."));
        String sanitizedBaseName = sanitizeFileName(baseName);

        return uuid + "_" + sanitizedBaseName + "." + extension;
    }

    /**
     * 파일명 안전하게 변환
     */
    private String sanitizeFileName(String filename) {
        // 특수문자 제거, 공백을 언더스코어로 변경
        return filename.replaceAll("[^a-zA-Z0-9가-힣]", "_");
    }

    /**
     * 파일 저장
     */
    private String saveFile(MultipartFile file, String fileName, String uploadPath) {
        try {
            // 디렉토리 생성
            Path directoryPath = Paths.get(uploadPath);
            if (!Files.exists(directoryPath)) {
                Files.createDirectories(directoryPath);
                log.info("디렉토리 생성: {}", directoryPath.toAbsolutePath());
            }

            // 파일 저장
            Path filePath = directoryPath.resolve(fileName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            log.info("파일 저장 성공: {}", filePath.toAbsolutePath());

            // 상대 경로 반환
            return uploadPath + fileName;

        } catch (IOException e) {
            log.error("파일 저장 실패: {}", e.getMessage(), e);
            throw new BaseException(BaseResponseStatus.FILE_UPLOAD_FAILED);
        }
    }

    /**
     * 파일 삭제 (향후 사용)
     */
    public void deleteFile(String fileUrl) {
        try {
            if (fileUrl == null || fileUrl.isEmpty()) {
                return;
            }

            // URL에서 "/" 제거
            String filePath = fileUrl.startsWith("/") ? fileUrl.substring(1) : fileUrl;
            Path path = Paths.get(filePath);

            if (Files.exists(path)) {
                Files.delete(path);
                log.info("파일 삭제 성공: {}", path.toAbsolutePath());
            }
        } catch (IOException e) {
            log.error("파일 삭제 실패: {}", e.getMessage(), e);
            // 파일 삭제 실패는 예외를 던지지 않고 로그만 기록
        }
    }
}