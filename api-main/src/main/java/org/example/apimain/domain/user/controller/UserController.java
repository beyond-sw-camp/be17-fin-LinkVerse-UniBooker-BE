package org.example.apimain.domain.user.controller;

import org.example.common.util.CookieUtil;
import org.example.common.model.UserRole;
import jakarta.servlet.http.HttpServletResponse;
import org.example.apimain.domain.user.model.dto.AuthDto;
import org.example.apimain.domain.user.model.dto.UserDto;
import org.example.common.base.BaseResponse;
import org.example.apimain.domain.user.service.AuthService;
import org.example.apimain.domain.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 일반 사용자 컨트롤러
 * - 회원가입, 로그인, 프로필 관리, 비밀번호 관리, 회원 탈퇴
 */
@Slf4j
@Tag(name = "User API", description = "일반 사용자 회원 관리 API")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final AuthService authService;

    // ========== 회원가입 ==========

    /**
     * 1. 일반 사용자 회원가입
     */
    @Operation(
            summary = "일반 사용자 회원가입",
            description = "일반 사용자(고객) 회원가입을 진행합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "회원가입 성공"),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청"),
                    @ApiResponse(responseCode = "409", description = "이메일 중복")
            }
    )
    @PostMapping("/signup")
    public BaseResponse<UserDto.SignUpResponse> signUp(
            @RequestBody @Valid UserDto.SignUpRequest request) {

        log.info("일반 사용자 회원가입 - email: {}, companyId: {}", request.getEmail(), request.getCompanyId());
        UserDto.SignUpResponse response = userService.signUpUser(request);
        return BaseResponse.success(response);
    }

    // ========== 로그인 ==========

    /**
     * 2. 일반 사용자 로그인
     */
    @Operation(
            summary = "일반 사용자 로그인",
            description = "일반 사용자 계정으로 로그인합니다. JWT 토큰을 반환합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "로그인 성공"),
                    @ApiResponse(responseCode = "401", description = "인증 실패"),
                    @ApiResponse(responseCode = "403", description = "계정 비활성화")
            }
    )
    @PostMapping("/login")
    public BaseResponse<UserDto.LoginResponse> login(
            @RequestBody @Valid UserDto.LoginRequest request,
            HttpServletResponse response) {  // ← HttpServletResponse 추가

        log.info("일반 사용자 로그인 시도 - email: {}, companyId: {}", request.getEmail(), request.getCompanyId());

        // 1. 로그인 처리 (토큰 포함)
        UserDto.LoginResponseWithToken loginResponseWithToken = userService.login(request);

        // 2. 단일 세션 정책: 모든 역할의 기존 쿠키 삭제
        CookieUtil.deleteAllRolesCookies(response);

        // 3. 현재 역할의 토큰을 HttpOnly Cookie에 저장
        response.addCookie(CookieUtil.createAccessTokenCookie(
                loginResponseWithToken.getAccessToken(),
                loginResponseWithToken.getRole()
        ));
        response.addCookie(CookieUtil.createRefreshTokenCookie(
                loginResponseWithToken.getRefreshToken(),
                loginResponseWithToken.getRole()
        ));

        // 4. Response Body에는 토큰 제외한 정보만 반환
        UserDto.LoginResponse loginResponse = UserDto.LoginResponse.builder()
                .userId(loginResponseWithToken.getUserId())
                .email(loginResponseWithToken.getEmail())
                .name(loginResponseWithToken.getName())
                .role(loginResponseWithToken.getRole())
                .companyId(loginResponseWithToken.getCompanyId())
                .companySlug(loginResponseWithToken.getCompanySlug())
                .build();

        return BaseResponse.success(loginResponse);
    }

    // ========== 로그아웃 ==========

    /**
     * 2-1. 로그아웃
     */
    @Operation(
            summary = "로그아웃",
            description = "현재 로그인 세션을 종료하고 Refresh Token을 삭제합니다. " +
                    "API Gateway에서 쿠키를 자동으로 삭제합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "로그아웃 성공"),
                    @ApiResponse(responseCode = "401", description = "인증 실패"),
                    @ApiResponse(responseCode = "404", description = "Refresh Token을 찾을 수 없음")
            }
    )
    @PostMapping("/logout")
    public BaseResponse<AuthDto.LogoutResponse> logout(
            @Parameter(description = "사용자 ID (JWT에서 추출)", required = true)
            @RequestHeader("X-User-Id") Long userId,
            @Parameter(description = "사용자 권한 (JWT에서 추출)", required = true)
            @RequestHeader("X-User-Role") String userRole,
            HttpServletResponse response) {  // ← HttpServletResponse 추가

        log.info("일반 사용자 로그아웃 - userId: {}, role: {}", userId, userRole);

        // 1. Refresh Token 삭제 (DB 또는 Redis)
        AuthDto.LogoutResponse logoutResponse = authService.logout(userId);

        // 2. 쿠키 삭제
        UserRole role = UserRole.valueOf(userRole);
        CookieUtil.deleteAllTokenCookies(response, role);

        return BaseResponse.success(logoutResponse);
    }

    // ========== 현재 사용자 정보 ==========

    /**
     * 3. 현재 로그인한 사용자 정보 조회
     */
    @Operation(
            summary = "현재 사용자 정보 조회",
            description = "JWT 토큰 기반으로 현재 로그인한 사용자의 기본 정보를 반환합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "조회 성공"),
                    @ApiResponse(responseCode = "401", description = "인증 실패"),
                    @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음")
            }
    )
    @GetMapping("/me")
    public BaseResponse<UserDto.CurrentUserResponse> getCurrentUser(
            @Parameter(description = "사용자 ID (JWT에서 추출)", required = true)
            @RequestHeader("X-User-Id") Long userId) {

        log.info("현재 사용자 정보 조회 - userId: {}", userId);
        UserDto.CurrentUserResponse response = userService.getCurrentUserInfo(userId);
        return BaseResponse.success(response);
    }

    // ========== 비밀번호 관리 ==========

    /**
     * 4. 비밀번호 변경
     */
    @Operation(
            summary = "비밀번호 변경",
            description = "사용자 비밀번호를 변경합니다. 현재 비밀번호 확인이 필요합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "비밀번호 변경 성공"),
                    @ApiResponse(responseCode = "400", description = "현재 비밀번호 불일치"),
                    @ApiResponse(responseCode = "401", description = "인증 실패")
            }
    )
    @PutMapping("/password")
    public BaseResponse<String> changePassword(
            @RequestBody @Valid UserDto.PasswordChangeRequest request,
            @Parameter(description = "사용자 ID (JWT에서 추출)", required = true)
            @RequestHeader("X-User-Id") Long userId) {

        log.info("비밀번호 변경 - userId: {}", userId);
        userService.changePassword(userId, request);
        return BaseResponse.success("비밀번호가 성공적으로 변경되었습니다.");
    }

    /**
     * 5. 비밀번호 찾기 - 임시 비밀번호 발급
     */
    @Operation(
            summary = "비밀번호 찾기",
            description = "이메일과 기업ID로 사용자를 확인하고 임시 비밀번호를 이메일로 발송합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "임시 비밀번호 발송 성공"),
                    @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음")
            }
    )
    @PostMapping("/reset-password")
    public BaseResponse<String> resetPassword(
            @Parameter(description = "이메일", required = true, example = "user@example.com")
            @RequestParam @Email(message = "올바른 이메일 형식이 아닙니다") String email,
            @Parameter(description = "기업 ID", required = true, example = "1")
            @RequestParam Long companyId) {

        log.info("비밀번호 찾기 - email: {}, companyId: {}", email, companyId);
        userService.resetPassword(email, companyId);
        return BaseResponse.success("임시 비밀번호가 이메일로 발송되었습니다. 이메일을 확인해주세요.");
    }

    // ========== 프로필 관리 ==========

    /**
     * 6. 내 프로필 조회
     */
    @Operation(
            summary = "내 프로필 조회",
            description = "현재 로그인한 사용자의 프로필 정보를 조회합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "조회 성공"),
                    @ApiResponse(responseCode = "401", description = "인증 실패"),
                    @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음")
            }
    )
    @GetMapping("/profile")
    public BaseResponse<UserDto.ProfileResponse> getMyProfile(
            @Parameter(description = "사용자 ID (JWT에서 추출)", required = true)
            @RequestHeader("X-User-Id") Long userId) {

        log.info("프로필 조회 - userId: {}", userId);
        UserDto.ProfileResponse response = userService.getMyProfile(userId);
        return BaseResponse.success(response);
    }

    /**
     * 7. 내 프로필 수정
     */
    @Operation(
            summary = "내 프로필 수정",
            description = "현재 로그인한 사용자의 프로필 정보를 수정합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "수정 성공"),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청"),
                    @ApiResponse(responseCode = "401", description = "인증 실패"),
                    @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음")
            }
    )
    @PatchMapping("/profile")
    public BaseResponse<UserDto.ProfileResponse> updateMyProfile(
            @RequestBody @Valid UserDto.ProfileUpdateRequest request,
            @Parameter(description = "사용자 ID (JWT에서 추출)", required = true)
            @RequestHeader("X-User-Id") Long userId) {

        log.info("프로필 수정 - userId: {}", userId);
        UserDto.ProfileResponse response = userService.updateMyProfile(userId, request);
        return BaseResponse.success(response);
    }

    // ========== 회원 탈퇴 ==========

    /**
     * 8. 회원 탈퇴
     */
    @Operation(
            summary = "회원 탈퇴",
            description = "현재 로그인한 사용자의 계정을 탈퇴 처리합니다. 계정 상태가 DELETED로 변경됩니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "탈퇴 성공"),
                    @ApiResponse(responseCode = "400", description = "비밀번호 불일치"),
                    @ApiResponse(responseCode = "401", description = "인증 실패"),
                    @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음")
            }
    )
    @DeleteMapping("/profile")
    public BaseResponse<UserDto.WithdrawResponse> withdraw(
            @RequestBody @Valid UserDto.WithdrawRequest request,
            @Parameter(description = "사용자 ID (JWT에서 추출)", required = true)
            @RequestHeader("X-User-Id") Long userId) {

        log.info("회원 탈퇴 - userId: {}", userId);
        UserDto.WithdrawResponse response = userService.withdraw(userId, request);
        return BaseResponse.success(response);
    }

    // ========== 이메일 관리 ==========

    /**
     * 9. 이메일 중복 확인 (기업별)
     */
    @Operation(
            summary = "이메일 중복 확인 (기업별)",
            description = "특정 기업 내에서 이메일이 이미 사용 중인지 확인합니다. true: 사용 중, false: 사용 가능",
            responses = {
                    @ApiResponse(responseCode = "200", description = "조회 성공")
            }
    )
    @GetMapping("/check-email")
    public BaseResponse<Boolean> checkEmail(
            @Parameter(description = "이메일", required = true, example = "user@example.com")
            @RequestParam @Email(message = "올바른 이메일 형식이 아닙니다") String email,
            @Parameter(description = "기업 ID", required = true, example = "1")
            @RequestParam Long companyId) {

        log.info("이메일 중복 확인 - email: {}, companyId: {}", email, companyId);
        boolean exists = userService.existsByEmailAndCompany(email, companyId);
        return BaseResponse.success(exists);
    }

    /**
     * 10. 이메일로 가입한 기업 목록 조회
     */
    @Operation(
            summary = "이메일로 가입한 기업 목록 조회",
            description = "해당 이메일로 가입한 모든 기업의 계정 정보를 조회합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "조회 성공"),
                    @ApiResponse(responseCode = "404", description = "가입 정보를 찾을 수 없음")
            }
    )
    @GetMapping("/accounts")
    public BaseResponse<List<UserDto.AccountInfo>> getAccountsByEmail(
            @Parameter(description = "이메일", required = true, example = "user@example.com")
            @RequestParam @Email(message = "올바른 이메일 형식이 아닙니다") String email) {

        log.info("이메일로 가입한 기업 목록 조회 - email: {}", email);
        List<UserDto.AccountInfo> accounts = userService.getAccountsByEmail(email);
        return BaseResponse.success(accounts);
    }

    /**
     * 11. 아이디 찾기 (이메일 조회)
     */
    @Operation(
            summary = "아이디 찾기",
            description = "이름과 전화번호 또는 생년월일로 가입한 이메일을 찾습니다. 이메일은 마스킹 처리되어 반환됩니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "조회 성공"),
                    @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음")
            }
    )
    @PostMapping("/find-email")
    public BaseResponse<UserDto.FindEmailResponse> findEmail(
            @RequestBody @Valid UserDto.FindEmailRequest request) {

        log.info("아이디 찾기 - name: {}, phone: {}", request.getName(), request.getPhone());
        UserDto.FindEmailResponse response = userService.findEmail(request);
        return BaseResponse.success(response);
    }
}