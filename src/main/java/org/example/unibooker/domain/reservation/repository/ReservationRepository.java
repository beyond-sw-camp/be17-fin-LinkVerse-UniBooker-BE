package org.example.unibooker.domain.reservation.repository;

import org.example.unibooker.domain.reservation.model.entity.Reservations;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservationRepository extends JpaRepository<Reservations, Integer> {
}
