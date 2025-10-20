package org.example.unibooker.domain.reservation.repository;

import org.example.unibooker.domain.reservation.model.entity.Reservations;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ReservationRepository extends JpaRepository<Reservations, Long> {
    List<Reservations> findAllByUsers_Id(Long userId);
}
