package org.example.common.util;

import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * 파일 업로드 유틸리티 클래스
 * - 파일 경로 생성, 파일명 생성 등 순수 유틸리티 기능만 제공
 * - MultipartFile을 다루는 검증/저장 로직은 각 서비스에서 구현
 */
@Slf4j
public class FileUploadUtil {

    /**
     * 파일 업로드 경로 생성 (날짜 기반)
     *
     * @param folder 폴더명 (예: "company-logo", "resource-thumbnail")
     * @return 날짜 기반 경로 (예: "company-logo/2025/01/29/uuid")
     */
    public static String makeUploadPath(String folder) {
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        return folder + "/" + date + "/" + UUID.randomUUID();
    }

    /**
     * 파일 확장자 추출
     *
     * @param filename 파일명
     * @return 확장자 (예: "jpg", "png")
     */
    public static String getFileExtension(String filename) {
        if (filename == null || filename.isEmpty()) {
            return "";
        }
        int lastIndexOf = filename.lastIndexOf(".");
        if (lastIndexOf == -1) {
            return "";
        }
        return filename.substring(lastIndexOf + 1);
    }

    /**
     * 고유 파일명 생성 (UUID + 원본 파일명)
     *
     * @param originalFilename 원본 파일명 (예: "logo.png")
     * @return UUID_파일명.확장자 (예: "abc123_logo.png")
     */
    public static String generateUniqueFileName(String originalFilename) {
        if (originalFilename == null || originalFilename.isEmpty()) {
            return UUID.randomUUID().toString();
        }

        String extension = getFileExtension(originalFilename);
        String uuid = UUID.randomUUID().toString();

        // 원본 파일명에서 확장자 제거
        int lastDotIndex = originalFilename.lastIndexOf(".");
        String baseName = (lastDotIndex > 0)
                ? originalFilename.substring(0, lastDotIndex)
                : originalFilename;

        // 안전한 파일명으로 변환
        String sanitizedBaseName = sanitizeFileName(baseName);

        return uuid + "_" + sanitizedBaseName + "." + extension;
    }

    /**
     * 파일명을 안전하게 변환
     * - 특수문자 제거
     * - 공백을 언더스코어로 변경
     *
     * @param filename 원본 파일명
     * @return 안전한 파일명 (예: "my logo" → "my_logo")
     */
    public static String sanitizeFileName(String filename) {
        if (filename == null || filename.isEmpty()) {
            return "";
        }
        // 영문, 숫자, 한글만 허용, 나머지는 언더스코어로 변환
        return filename.replaceAll("[^a-zA-Z0-9가-힣]", "_");
    }
}