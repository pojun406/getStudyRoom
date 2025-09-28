package com.example.getstudyroom.repository;

import com.example.getstudyroom.entity.Reservation;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from Reservation r " +
            "where r.room.id = :roomId and r.startAt < :endAt and r.endAt > :startAt")
    List<Reservation> findOverlappingReservationsWithLock(@Param("roomId") Long roomId, @Param("startAt") LocalDateTime startAt, @Param("endAt") LocalDateTime endAt);
    // 비관적 락
}
