package org.example.unibooker.locust;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.io.FileWriter;
import java.io.IOException;
import java.security.Key;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Locust 성능 테스트용 더미 데이터 생성기
 * - SQL INSERT 쿼리 생성
 * - JWT 토큰 생성
 */
public class DummyDataGenerator {

    // ========== 설정 값 ==========
    private static final Long COMPANY_ID = 900L;
    private static final String COMPANY_SLUG = "testUB-001";
    private static final String COMPANY_NAME = "테스트 기업";
    private static final String BUSINESS_NUMBER = "123-45-67890";

    private static final int ADMIN_COUNT = 1;
    private static final int MANAGER_COUNT = 29;
    private static final int USER_COUNT = 100;
    private static final int TOTAL_USER_COUNT = ADMIN_COUNT + MANAGER_COUNT + USER_COUNT; // 130

    private static final Long START_USER_ID = 1001L;
    private static final int RESOURCE_GROUP_COUNT = 20;
    private static final int RESOURCE_PER_GROUP = 10;
    private static final int RESERVATION_PER_RESOURCE = 10;

    // JWT Secret Key (실제 값으로 변경 필요)
    private static final String JWT_SECRET = "your-secret-key-here-must-be-at-least-256-bits-long-for-HS256-algorithm-security";
    private static final long TOKEN_VALIDITY = 24 * 60 * 60 * 1000; // 24시간

    private static final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    private static final Key key = Keys.hmacShaKeyFor(JWT_SECRET.getBytes());
    private static final Random random = new Random();

    public static void main(String[] args) {
        try {
            System.out.println("=== 더미 데이터 생성 시작 ===\n");

            // 1. SQL 파일 생성
            generateSqlFile();

            // 2. JWT 토큰 파일 생성
            generateTokenFile();

            System.out.println("\n=== 더미 데이터 생성 완료 ===");
            System.out.println("생성된 파일:");
            System.out.println("  - dummy_data.sql");
            System.out.println("  - jwt_tokens.txt");

        } catch (IOException e) {
            System.err.println("파일 생성 실패: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ========== SQL 파일 생성 ==========
    private static void generateSqlFile() throws IOException {
        FileWriter writer = new FileWriter("dummy_data.sql");

        writer.write("-- ============================================\n");
        writer.write("-- Locust 성능 테스트용 더미 데이터\n");
        writer.write("-- 생성일시: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + "\n");
        writer.write("-- ============================================\n\n");

        writer.write("SET FOREIGN_KEY_CHECKS = 0;\n\n");

        // 1. Company 데이터
        writer.write("-- ========== 1. Company ==========\n");
        generateCompanyData(writer);

        // 2. Users 데이터
        writer.write("\n-- ========== 2. Users ==========\n");
        generateUserData(writer);

        // 3. ResourceGroups 데이터
        writer.write("\n-- ========== 3. Resource Groups ==========\n");
        List<Long> groupCreators = generateResourceGroupData(writer);

        // 4. Resources 데이터
        writer.write("\n-- ========== 4. Resources ==========\n");
        generateResourceData(writer, groupCreators);

        // 5. Reservations 데이터
        writer.write("\n-- ========== 5. Reservations ==========\n");
        generateReservationData(writer);

        writer.write("\nSET FOREIGN_KEY_CHECKS = 1;\n");
        writer.close();

        System.out.println("✅ SQL 파일 생성 완료");
    }

    // ========== Company INSERT ==========
    private static void generateCompanyData(FileWriter writer) throws IOException {
        String sql = String.format(
                "INSERT INTO companies (id, company_name, business_number, company_slug, status, approved_at, approved_by, created_at, updated_at) VALUES\n" +
                        "(%d, '%s', '%s', '%s', 'ACTIVE', NOW(), 1, NOW(), NOW());\n",
                COMPANY_ID, COMPANY_NAME, BUSINESS_NUMBER, COMPANY_SLUG
        );
        writer.write(sql);
    }

    // ========== Users INSERT ==========
    private static void generateUserData(FileWriter writer) throws IOException {
        writer.write("INSERT INTO users (id, email, password, name, role, status, company_id, is_first_login, suspended_by_company, created_at, updated_at) VALUES\n");

        List<String> values = new ArrayList<>();
        String hashedPassword = encoder.encode("test1234"); // 공통 비밀번호

        for (int i = 0; i < TOTAL_USER_COUNT; i++) {
            Long userId = START_USER_ID + i;
            String email = String.format("test%03d@test.com", i + 1);
            String name = String.format("test%03d", i + 1);
            String role;

            if (i < ADMIN_COUNT) {
                role = "ADMIN";
            } else if (i < ADMIN_COUNT + MANAGER_COUNT) {
                role = "MANAGER";
            } else {
                role = "USER";
            }

            values.add(String.format(
                    "(%d, '%s', '%s', '%s', '%s', 'ACTIVE', %d, false, false, NOW(), NOW())",
                    userId, email, hashedPassword, name, role, COMPANY_ID
            ));
        }

        writer.write(String.join(",\n", values));
        writer.write(";\n");
    }

    // ========== ResourceGroups INSERT ==========
    private static List<Long> generateResourceGroupData(FileWriter writer) throws IOException {
        writer.write("INSERT INTO resource_groups (id, name, group_code, description, category, is_always_available, is_active, company_id, created_by, updated_by, view_count, version, created_at, updated_at) VALUES\n");

        List<String> values = new ArrayList<>();
        List<Long> groupCreators = new ArrayList<>();
        Long adminAndManagerMax = START_USER_ID + ADMIN_COUNT + MANAGER_COUNT - 1; // 1030

        for (int i = 0; i < RESOURCE_GROUP_COUNT; i++) {
            Long groupId = 1L + i;
            String name = String.format("리소스그룹-%03d", i + 1);
            String groupCode = String.format("RG%03d", i + 1);
            String description = String.format("테스트용 리소스 그룹 %d", i + 1);

            // Admin 또는 Manager 랜덤 선택
            Long creatorId = START_USER_ID + random.nextInt(ADMIN_COUNT + MANAGER_COUNT);
            groupCreators.add(creatorId);

            values.add(String.format(
                    "(%d, '%s', '%s', '%s', 'RESERVATION', true, true, %d, %d, %d, %d, 0, NOW(), NOW())",
                    groupId, name, groupCode, description, COMPANY_ID, creatorId, creatorId, random.nextInt(1000)
            ));
        }

        writer.write(String.join(",\n", values));
        writer.write(";\n");

        return groupCreators;
    }

    // ========== Resources INSERT ==========
    private static void generateResourceData(FileWriter writer, List<Long> groupCreators) throws IOException {
        writer.write("INSERT INTO resources (id, name, description, is_active, start_date, end_date, time_interval, capacity, row, col, status, resource_group_id, created_by, updated_by, version, created_at, updated_at) VALUES\n");

        List<String> values = new ArrayList<>();
        Long resourceId = 1L;

        for (int groupIdx = 0; groupIdx < RESOURCE_GROUP_COUNT; groupIdx++) {
            Long groupId = 1L + groupIdx;
            Long creatorId = groupCreators.get(groupIdx);

            for (int resIdx = 0; resIdx < RESOURCE_PER_GROUP; resIdx++) {
                String name = String.format("리소스-%03d-%02d", groupIdx + 1, resIdx + 1);
                String description = String.format("테스트용 리소스 (그룹 %d)", groupIdx + 1);
                LocalDate startDate = LocalDate.now().minusDays(30);
                LocalDate endDate = LocalDate.now().plusDays(60);

                values.add(String.format(
                        "(%d, '%s', '%s', true, '%s', '%s', 30, %d, 10, 10, 'IN_PROGRESS', %d, %d, %d, 0, NOW(), NOW())",
                        resourceId++, name, description, startDate, endDate,
                        20 + random.nextInt(80), groupId, creatorId, creatorId
                ));
            }
        }

        writer.write(String.join(",\n", values));
        writer.write(";\n");
    }

    // ========== Reservations INSERT ==========
    private static void generateReservationData(FileWriter writer) throws IOException {
        writer.write("INSERT INTO reservations (id, user_id, resource_id, created_by, status, attendee_count, start_date, end_date, row, col, created_at, updated_at) VALUES\n");

        List<String> values = new ArrayList<>();
        Long reservationId = 1L;
        long totalResources = RESOURCE_GROUP_COUNT * RESOURCE_PER_GROUP;
        Long userStartId = START_USER_ID + ADMIN_COUNT + MANAGER_COUNT; // 1031
        Long userEndId = START_USER_ID + TOTAL_USER_COUNT - 1; // 1130

        for (Long resourceId = 1L; resourceId <= totalResources; resourceId++) {
            for (int i = 0; i < RESERVATION_PER_RESOURCE; i++) {
                Long userId = userStartId + random.nextInt((int)(userEndId - userStartId + 1));
                LocalDateTime startDate = LocalDateTime.now().plusDays(random.nextInt(30));
                LocalDateTime endDate = startDate.plusHours(1 + random.nextInt(3));

                values.add(String.format(
                        "(%d, %d, %d, %d, 'CONFIRMED', %d, '%s', '%s', %d, %d, NOW(), NOW())",
                        reservationId++, userId, resourceId, userId,
                        1 + random.nextInt(5),
                        startDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                        endDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                        random.nextInt(10), random.nextInt(10)
                ));
            }
        }

        writer.write(String.join(",\n", values));
        writer.write(";\n");
    }

    // ========== JWT 토큰 파일 생성 ==========
    private static void generateTokenFile() throws IOException {
        FileWriter writer = new FileWriter("jwt_tokens.txt");

        writer.write("# ============================================\n");
        writer.write("# JWT 토큰 목록 (24시간 유효)\n");
        writer.write("# 생성일시: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + "\n");
        writer.write("# ============================================\n\n");

        for (int i = 0; i < TOTAL_USER_COUNT; i++) {
            Long userId = START_USER_ID + i;
            String email = String.format("test%03d@test.com", i + 1);
            String role;

            if (i < ADMIN_COUNT) {
                role = "ADMIN";
            } else if (i < ADMIN_COUNT + MANAGER_COUNT) {
                role = "MANAGER";
            } else {
                role = "USER";
            }

            String token = generateToken(userId, email, role, COMPANY_ID);

            writer.write(String.format("# User ID: %d | Email: %s | Role: %s\n", userId, email, role));
            writer.write(token + "\n\n");
        }

        writer.close();
        System.out.println("✅ JWT 토큰 파일 생성 완료");
    }

    // ========== JWT 토큰 생성 ==========
    private static String generateToken(Long userId, String email, String role, Long companyId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("email", email);
        claims.put("role", role);
        claims.put("companyId", companyId);

        Date now = new Date();
        Date validity = new Date(now.getTime() + TOKEN_VALIDITY);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(email)
                .setIssuedAt(now)
                .setExpiration(validity)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }
}