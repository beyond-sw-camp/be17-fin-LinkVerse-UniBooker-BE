package org.example.unibooker.query.model;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

public class ResourceReservationDto {

    @Getter
    @Builder
    public static class ResourceReservationCountRes {
        private Long resourceId;
        private String resourceName;
        private List<SlotCount> slots;

        @Getter
        @Builder
        public static class SlotCount {
            private LocalDateTime startAt;
            private LocalDateTime endAt;
            private int count;
        }
    }
}
