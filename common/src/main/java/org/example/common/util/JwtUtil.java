package org.example.common.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT 토큰 생성 및 검증 유틸리티
 * - Access Token 생성
 * - Refresh Token 생성
 * - 토큰 검증
 * - Claims 추출
 */
public class JwtUtil {

    private final Key key;
    private final long accessTokenValidityTime;
    private final long refreshTokenValidityTime;

    /**
     * JwtUtil 생성자
     *
     * @param secretKey JWT 시크릿 키
     * @param accessTokenValidityTime Access Token 유효 시간 (밀리초)
     * @param refreshTokenValidityTime Refresh Token 유효 시간 (밀리초)
     */
    public JwtUtil(String secretKey, long accessTokenValidityTime, long refreshTokenValidityTime) {
        this.key = Keys.hmacShaKeyFor(secretKey.getBytes());
        this.accessTokenValidityTime = accessTokenValidityTime;
        this.refreshTokenValidityTime = refreshTokenValidityTime;
    }

    /**
     * Access Token 생성
     */
    public String createAccessToken(Long userId, String email, String role, Long companyId, String userName) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("email", email);
        claims.put("role", role);
        claims.put("userName", userName);  // ← 추가
        if (companyId != null) {
            claims.put("companyId", companyId);
        }

        return createToken(claims, email, accessTokenValidityTime);
    }

    /**
     * Refresh Token 생성
     */
    public String createRefreshToken(Long userId, String email) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);

        return createToken(claims, email, refreshTokenValidityTime);
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
        return Jwts.parser()
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
        Object userId = claims.get("userId");
        if (userId instanceof Integer) {
            return ((Integer) userId).longValue();
        }
        return (Long) userId;
    }

    /**
     * 토큰에서 이메일 추출
     */
    public String getEmail(String token) {
        return getClaims(token).getSubject();
    }

    /**
     * 토큰에서 역할 추출
     */
    public String getRole(String token) {
        return getClaims(token).get("role", String.class);
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
        if (companyId instanceof Integer) {
            return ((Integer) companyId).longValue();
        }
        return (Long) companyId;
    }

    /**
     * 토큰에서 사용자 이름 추출
     */
    public String getUserName(String token) {
        return getClaims(token).get("userName", String.class);
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
}