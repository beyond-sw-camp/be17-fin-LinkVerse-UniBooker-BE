package org.example.unibooker.domain.resource.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.unibooker.common.BaseEntity;
import org.example.unibooker.domain.user.model.entity.Users;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 리소스(시설/공간/장비 등) 엔티티
 * - 예약 가능한 리소스의 기본 정보 관리
 * - 리소스 그룹 및 이미지와 연관관계 설정
 */
@Getter
@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class Resources extends BaseEntity {

    /** 리소스명 */
    @Column(nullable = false, length = 100)
    private String name;

    /** 리소스 설명 */
    @Column(length = 500)
    private String description;

    /** 활성화 여부 */
    @Column(nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    /** 예약 시작일 */
    @Column(nullable = false)
    private LocalDate startDate;

    /** 예약 종료일 */
    @Column(nullable = false)
    private LocalDate endDate;

    /** 예약 시작 시간 */
    private LocalTime startTime;

    /** 예약 종료 시간 */
    private LocalTime endTime;

    /** 수용 인원 */
    @Column(nullable = false)
    private Integer capacity;

    /** 좌석 행 개수 */
    private Integer row;

    /** 좌석 열 개수 */
    private Integer column;

    /** 리소스 상태 */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ResourceStatus status;

    /** 리소스 그룹 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resource_group_id")
    private ResourceGroups resourceGroup;

    /** 리소스 이미지 목록 */
    @OneToMany(mappedBy = "resource", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ResourceImages> resourceImages = new ArrayList<>();

    /** 생성자 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private Users createdBy;

    /** 수정자 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_by")
    private Users updatedBy;
}