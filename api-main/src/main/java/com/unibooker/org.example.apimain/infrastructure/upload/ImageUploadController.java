package com.unibooker.main.infrastructure.upload;

import com.unibooker.common.response.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 이미지 업로드 컨트롤러
 * - AWS S3 Presigned URL 발급
 * - 기업 로고, 리소스 그룹, 리소스 썸네일 업로드
 */
@Slf4j
@Tag(name = "Image Upload API", description = "이미지 업로드 API (AWS S3 Presigned URL)")
@RestController
@RequestMapping("/api/image-upload")
@RequiredArgsConstructor
public class ImageUploadController {

    private final ImageUploadService imageUploadService;

    /**
     * 이미지 파일 업로드 (MultipartFile)
     * - 파일을 직접 받아 검증 후 Presigned URL 생성
     * - 클라이언트는 반환된 URL로 직접 S3에 PUT 요청
     */
    @Operation(
            summary = "이미지 업로드 Presigned URL 발급",
            description = """
                    이미지 파일을 업로드하기 위한 Presigned URL을 발급합니다.
                    
                    **처리 흐름:**
                    1. 파일 검증 (크기, 형식, 확장자)
                    2. S3 경로 생성 (imageType에 따라 분기)
                    3. Presigned URL 생성 (10분 유효)
                    4. 클라이언트는 반환된 URL로 PUT 요청하여 업로드
                    
                    **지원 형식:** JPG, JPEG, PNG, GIF, WEBP
                    **최대 크기:** 5MB (설정 가능)
                    """,
            responses = {
                    @ApiResponse(responseCode = "200", description = "Presigned URL 생성 성공"),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청 (파일 검증 실패)"),
                    @ApiResponse(responseCode = "500", description = "서버 오류 (S3 연결 실패)")
            }
    )
    @PostMapping
    public BaseResponse<String> uploadImage(
            @Parameter(description = "업로드할 이미지 파일", required = true)
            @RequestParam("file") MultipartFile file,

            @Parameter(
                    description = "이미지 타입 (업로드 경로 결정)",
                    required = true,
                    example = "companyLogo"
            )
            @RequestParam("imageType") String imageType) {

        log.info("📤 이미지 업로드 요청 - imageType: {}, fileName: {}", imageType, file.getOriginalFilename());

        // Presigned URL 생성
        String presignedUrl = imageUploadService.getPresignedUrl(file, imageType);

        log.info("✅ Presigned URL 생성 완료 - imageType: {}", imageType);

        return BaseResponse.success(presignedUrl);
    }

    /**
     * 기업 로고 Presigned URL 발급 (파일명만 사용)
     * - 파일을 직접 받지 않고 파일명과 Content-Type만으로 URL 생성
     * - 프론트엔드에서 파일 선택 시 미리 URL 발급받는 용도
     */
    @Operation(
            summary = "기업 로고 Presigned URL 발급 (파일명 기반)",
            description = """
                    기업 로고 업로드용 Presigned URL을 파일명 정보만으로 발급합니다.
                    
                    **사용 시나리오:**
                    - 프론트엔드에서 파일 선택 시 즉시 URL 발급
                    - 파일 전송 없이 빠른 URL 생성
                    - 클라이언트가 직접 S3에 업로드
                    
                    **처리 흐름:**
                    1. 파일명과 Content-Type 검증
                    2. 고유 파일명 생성 (UUID 추가)
                    3. S3 경로 생성 (company-logo/날짜/UUID_파일명)
                    4. Presigned URL 생성 (10분 유효)
                    
                    **지원 형식:** JPG, JPEG, PNG, GIF, WEBP
                    """,
            responses = {
                    @ApiResponse(responseCode = "200", description = "Presigned URL 생성 성공"),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청 (파일명 또는 Content-Type 오류)"),
                    @ApiResponse(responseCode = "500", description = "서버 오류 (S3 연결 실패)")
            }
    )
    @PostMapping("/presigned-url/company-logo")
    public BaseResponse<String> getPresignedUrlForCompanyLogo(
            @Parameter(
                    description = "파일명 (확장자 포함)",
                    required = true,
                    example = "logo.png"
            )
            @RequestParam("fileName") String fileName,

            @Parameter(
                    description = "파일 Content-Type",
                    required = true,
                    example = "image/png"
            )
            @RequestParam("contentType") String contentType) {

        log.info("📤 기업 로고 Presigned URL 발급 요청 - fileName: {}, contentType: {}", fileName, contentType);

        // Presigned URL 생성
        String presignedUrl = imageUploadService.getPresignedUrlForCompanyLogo(fileName, contentType);

        log.info("✅ 기업 로고 Presigned URL 발급 완료 - fileName: {}", fileName);

        return BaseResponse.success(presignedUrl);
    }
}