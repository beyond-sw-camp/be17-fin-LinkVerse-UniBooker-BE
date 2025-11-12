package org.example.apireservation.infrastructure;

import org.example.apireservation.domain.model.CustomFieldValue;
import org.example.apireservation.usecase.port.in.CustomFieldValueCommand;

import java.util.List;

public interface CustomFieldValueExternalPort {
    List<CustomFieldValue> getUserFieldValuesByReservation(Long reservationId);
    List<CustomFieldValue> register(Long reservatinId, List<CustomFieldValueCommand> dto);
}
