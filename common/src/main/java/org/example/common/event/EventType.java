package org.example.common.event;

/**
 * Kafka 이벤트 타입
 */
public enum EventType {
    /**
     * 생성 이벤트
     */
    CREATED,

    /**
     * 수정 이벤트
     */
    UPDATED,

    /**
     * 삭제 이벤트 (Soft Delete)
     */
    DELETED
}