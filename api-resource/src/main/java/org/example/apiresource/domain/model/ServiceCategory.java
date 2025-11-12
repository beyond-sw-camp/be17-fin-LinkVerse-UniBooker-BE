package org.example.apiresource.domain.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ServiceCategory {
    RESERVATION("예약"),
    SEAT("좌석 예매"),
    EVENT("이벤트 신청"),
    ALL("전체");

    private final String label; // 한글 라벨

}