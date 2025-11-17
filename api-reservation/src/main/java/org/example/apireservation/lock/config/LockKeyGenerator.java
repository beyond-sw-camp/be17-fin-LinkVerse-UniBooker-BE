package org.example.apireservation.lock.config;

import org.example.apireservation.domain.model.ServiceCategory;
import org.example.apireservation.usecase.port.in.ReservationCommand;
import org.example.common.base.BaseResponseStatus;
import org.example.common.exception.BaseException;

import java.time.format.DateTimeFormatter;

public final class LockKeyGenerator {

    private static final DateTimeFormatter SLOT_FMT = DateTimeFormatter.ofPattern("yyyyMMddHHmm");

    private LockKeyGenerator() {}

    public static String buildLockKey(ServiceCategory category, Long resourceId, ReservationCommand dto) {
        String base = "unibooker:lock:resource:" + resourceId;

        if (category == null) return base + ":global";

        return switch (category) {
            case RESERVATION -> base + ":slot:" + toSlot(dto);
            case SEAT -> base + ":slot:" + toSlot(dto) + ":seat:" + dto.getRow() + ":" + dto.getCol();
            case EVENT -> base + ":event";
            default -> throw new BaseException(BaseResponseStatus.INVALID_SERVICE_CATEGORY);
        };
    }

    private static String toSlot(ReservationCommand dto) {
        return dto.getDate().atTime(dto.getTime()).format(SLOT_FMT);
    }
}
