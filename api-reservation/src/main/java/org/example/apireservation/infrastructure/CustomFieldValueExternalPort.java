package org.example.apireservation.infrastructure;

import org.example.apireservation.usecase.port.in.CustomFieldValueCommand;
import org.example.apireservation.usecase.port.out.CustomFieldValueDto;

import java.util.List;

public interface CustomFieldValueExternalPort {
    List<CustomFieldValueDto> getUserFieldValuesByReservation(Long reservationId);
    List<Object> register(Long reservatinId, List<CustomFieldValueCommand> dto);
}
