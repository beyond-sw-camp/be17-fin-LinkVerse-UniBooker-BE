package org.example.apireservation.domain.model;

import lombok.*;

import java.time.LocalDate;

@Getter
@Builder
public class Resource {
    private Long id;                        // 리소스 아이디
    private String name;                    // 리소스명
    private ServiceCategory category;       // 리소스 카테고리
    private LocalDate startDate;            // 시작 날짜
    private LocalDate endDate;              // 종료 날짜
    private Integer row;
    private Integer col;
    private Integer timeInterval;           // 시간 간격
    private Integer capacity;               // 인원수
    private String resourceImage;           // 리소스 이미지
    private String status;                  // 리소스 상태
    private Long resourceGroupId;           // 리소스 그룹 아이디
    private String resourceGroupName;       // 리소스 그룹명
    private Boolean isAlwaysAvailable;      // 상시 모집 여부
}