package org.example.unibooker.common;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Slf4j
@RestController
@Tag(name = "이미지 업로드", description = "AWS S3 기반 이미지 파일 업로드 기능을 제공합니다.")
@RequestMapping("/api/image-upload")
@RequiredArgsConstructor
public class ImageUploadController {
    private final ImageUploadService imageUploadService;

    @Operation(
            summary = "이미지 파일 업로드",
            description = "이미지 파일을 AWS S3에 업로드하고 접근 가능한 URL을 반환합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "업로드 성공"),
                    @ApiResponse(responseCode = "400", description = "잘못된 파일 형식"),
                    @ApiResponse(responseCode = "500", description = "서버 오류")
            }
    )
    @PostMapping
    public BaseResponse<String> getPresignedUrl(@RequestParam MultipartFile file, @RequestParam String imageType) throws IOException {
        String url = imageUploadService.getPresignedUrl(file, imageType);
        log.info("이미지 업로드 완료: URL = {}", url);
        return BaseResponse.success(url);
    }

    // ✅ 새로 추가할 엔드포인트 (companyLogo 전용)
    @Operation(
            summary = "기업 로고 Presigned URL 발급",
            description = "파일명과 타입 정보만 받아 S3 업로드용 임시 URL을 반환합니다."
    )
    @PostMapping("/presigned-url/company-logo")
    public BaseResponse<String> getPresignedUrlForCompanyLogo(
            @RequestParam String fileName,
            @RequestParam String contentType
    ) throws IOException {
        String presignedUrl = imageUploadService.getPresignedUrlForCompanyLogo(fileName, contentType);
        log.info("기업 로고 Presigned URL 발급 완료: fileName = {}", fileName);
        return BaseResponse.success(presignedUrl);
    }
}
