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
    @Column(nullable = true)
    private LocalDate startDate;

    /** 예약 종료일 */
    @Column(nullable = true)
    private LocalDate endDate;

    /** 예약 시작 시간 */
    @Column(nullable = true)
    private LocalTime startTime;

    /** 예약 종료 시간 */
    @Column(nullable = true)
    private LocalTime endTime;

    /** 시간 간격 */
    @Column(nullable = true)
    private TimeIntervalType timeInterval; // private int timeInterval;

    /** 수용 인원 */
    @Column(nullable = false)
    private Integer capacity;

    /** 좌석 행 개수 */
    private Integer row;

    /** 좌석 열 개수 */
    private Integer col;

    /** 리소스 상태 */
    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
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

    /** 타임 슬롯 */
    @OneToMany(mappedBy = "resources",  cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ResourceTimeSlots> timeSlots = new ArrayList<>();

    /** 예외 타임 슬롯 */
    @OneToMany(mappedBy = "resources", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ResourceTimeSlotExceptions> timeSlotExceptions = new ArrayList<>();

    @Version
    @Column(nullable = false)
    private Long version = 0L;

    /*
    public void setTimeInterval(int minutes) {
        this.timeInterval = TimeIntervalType.fromMinutes(minutes).getMinutes();
    }

    public TimeIntervalType getTimeIntervalEnum() {
        return TimeIntervalType.fromMinutes(this.timeInterval);
    }
    */

    // 서비스 수정 함수
    public void update(ResourceDto.ResourceUpdateReq dto) {

        // 종료일 체크
        if (dto.getEndDate() != null && dto.getEndDate().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("종료일이 오늘 이전인 리소스는 수정할 수 없습니다.");
        }

        // 시작일/종료일 체크
        if (dto.getStartDate() != null && dto.getEndDate() != null
                && dto.getEndDate().isBefore(dto.getStartDate())) {
            throw new IllegalArgumentException("종료일은 시작일보다 빠를 수 없습니다.");
        }

        // 시작시간/종료시간 체크
        if (dto.getStartTime() != null && dto.getEndTime() != null
                && !dto.getEndTime().isAfter(dto.getStartTime())) {
            throw new IllegalArgumentException("종료시간은 시작시간보다 늦어야 합니다.");
        }

        // 상태 결정
        if (resourceGroup.getIsAlwaysAvailable() || (dto.getStartDate() != null && !dto.getStartDate().isAfter(LocalDate.now()))) {
            this.status = ResourceStatus.IN_PROGRESS;
        } else {
            this.status = ResourceStatus.PROGRESS_BEFORE;
        }

        // 필드 업데이트 (null 체크)
        if (dto.getName() != null) this.name = dto.getName();
        if (dto.getDescription() != null) this.description = dto.getDescription();
        if (dto.getStartDate() != null) this.startDate = dto.getStartDate();
        if (dto.getEndDate() != null) this.endDate = dto.getEndDate();
        if (dto.getStartTime() != null) this.startTime = dto.getStartTime();
        if (dto.getEndTime() != null) this.endTime = dto.getEndTime();
        if (dto.getTimeInterval() != null) this.timeInterval = dto.getTimeInterval(); // if (dto.getTimeInterval() != null) this.timeInterval = dto.getTimeInterval();
        if (dto.getCapacity() != null) this.capacity = dto.getCapacity();
        if (dto.getRow() != null) this.row = dto.getRow();
        if (dto.getCol() != null) this.col = dto.getCol();
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public void setUpdateStatus(ResourceStatus resourceStatus) {
        this.status = resourceStatus;
    }
}