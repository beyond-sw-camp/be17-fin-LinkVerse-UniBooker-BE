package org.example.unibooker.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.example.unibooker.domain.user.model.entity.Users;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class JwtUtil {

    private final Key key;
    private final long accessTokenValidityTime;
    private final long refreshTokenValidityTime;

    public JwtUtil(
            @Value("${jwt.secret}") String secretKey,
            @Value("${jwt.access-token-validity}") long accessTokenValidityTime,
            @Value("${jwt.refresh-token-validity}") long refreshTokenValidityTime) {

        this.key = Keys.hmacShaKeyFor(secretKey.getBytes());
        this.accessTokenValidityTime = accessTokenValidityTime;
        this.refreshTokenValidityTime = refreshTokenValidityTime;
    }

    /**
     * Access Token 생성
     */
    public String createAccessToken(Users user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("email", user.getEmail());
        claims.put("role", user.getRole().name());

        if (user.getCompanyId() != null) {
            claims.put("companyId", user.getCompanyId());
        }

        return createToken(claims, user.getEmail(), accessTokenValidityTime);
    }

    /**
     * Refresh Token 생성
     * - 7일 유효기간
     * - userId만 포함 (최소 정보 원칙)
     * - Access Token 재발급에 사용
     */
    public String createRefreshToken(Users user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());

        return createToken(claims, user.getEmail(), refreshTokenValidityTime);
    }

    /**
     * JWT 토큰 생성
     */
    private String createToken(Map<String, Object> claims, String subject, long validityTime) {
        Date now = new Date();
        Date validity = new Date(now.getTime() + validityTime);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(now)
                .setExpiration(validity)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 토큰에서 Claims 추출
     */
    public Claims getClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * 토큰에서 사용자 ID 추출
     */
    public Long getUserId(String token) {
        Claims claims = getClaims(token);
        return claims.get("userId", Long.class);
    }

    /**
     * 토큰에서 이메일 추출
     */
    public String getEmail(String token) {
        return getClaims(token).getSubject();
    }

    /**
     * 토큰에서 역할(Role) 추출
     */
    public String getRole(String token) {
        Claims claims = getClaims(token);
        return claims.get("role", String.class);
    }

    /**
     * 토큰에서 기업 ID 추출
     */
    public Long getCompanyId(String token) {
        Claims claims = getClaims(token);
        Object companyId = claims.get("companyId");
        if (companyId == null) {
            return null;
        }
        // Integer로 저장되었을 수 있으므로 Long으로 변환
        if (companyId instanceof Integer) {
            return ((Integer) companyId).longValue();
        }
        return (Long) companyId;
    }

    /**
     * 토큰 유효성 검증
     */
    public boolean validateToken(String token) {
        try {
            getClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 토큰 만료 여부 확인
     */
    public boolean isTokenExpired(String token) {
        try {
            Date expiration = getClaims(token).getExpiration();
            return expiration.before(new Date());
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * Refresh Token 유효성 검증
     * - 토큰 형식 검증
     * - 만료 여부 확인
     */
    public boolean validateRefreshToken(String refreshToken) {
        try {
            Claims claims = getClaims(refreshToken);

            // userId가 포함되어 있는지 확인
            if (claims.get("userId") == null) {
                return false;
            }

            // 만료 여부 확인
            return !isTokenExpired(refreshToken);

        } catch (Exception e) {
            log.error("Refresh Token 검증 실패: {}", e.getMessage());
            return false;
        }
    }
}