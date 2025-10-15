package org.example.unibooker.domain.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import lombok.RequiredArgsConstructor;
import org.example.unibooker.common.BaseResponse;
import org.example.unibooker.domain.user.model.AdminDto;
import org.example.unibooker.domain.user.model.UserDto;
import org.example.unibooker.domain.user.service.AdminService;
import org.example.unibooker.domain.user.service.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "User", description = "사용자 API")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final AdminService adminService;

    // ========== 일반 사용자 ==========

    @Operation(summary = "일반 사용자 회원가입", description = "일반 사용자(고객) 회원가입을 진행합니다.")
    @PostMapping("/signup")
    public BaseResponse<UserDto.SignUpResponse> signUp(
            @RequestBody @Valid UserDto.SignUpRequest request) {

        UserDto.SignUpResponse response = userService.signUpUser(request);
        return BaseResponse.success(response);
    }

    @Operation(summary = "비밀번호 변경", description = "사용자 비밀번호를 변경합니다. 첫 로그인 시 필수입니다.")
    @PutMapping("/password")
    public BaseResponse<String> changePassword(
            @RequestBody @Valid UserDto.PasswordChangeRequest request,
            @AuthenticationPrincipal Long userId) {

        userService.changePassword(userId, request);
        return BaseResponse.success("비밀번호가 성공적으로 변경되었습니다.");
    }

    // ========== 관리자 ==========

    @Operation(summary = "관리자 회원가입 신청", description = "기업 관리자 회원가입을 신청합니다. 슈퍼 관리자의 승인이 필요합니다.")
    @PostMapping("/admin/signup")
    public BaseResponse<AdminDto.SignUpResponse> adminSignUp(
            @RequestPart("data") @Valid AdminDto.SignUpRequest request,
            @RequestPart(value = "logoFile", required = false) MultipartFile logoFile) {

        AdminDto.SignUpResponse response = adminService.signUpAdmin(request, logoFile);
        return BaseResponse.success(response);
    }

    @Operation(summary = "관리자 회원가입 승인 상태 조회", description = "이메일로 관리자 회원가입 승인 상태를 조회합니다.")
    @GetMapping("/admin/status")
    public BaseResponse<AdminDto.StatusResponse> checkAdminStatus(
            @RequestParam @Email(message = "올바른 이메일 형식이 아닙니다") String email) {

        AdminDto.StatusResponse response = adminService.checkSignUpStatus(email);
        return BaseResponse.success(response);
    }
}