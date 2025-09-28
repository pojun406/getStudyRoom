package com.example.getstudyroom.repository;

import com.example.getstudyroom.entity.Reservation;
import com.example.getstudyroom.entity.Room;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    //비관적 락
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from Reservation r " +
            "where r.room.id = :roomId and r.startAt < :endAt and r.endAt > :startAt")
    List<Reservation> findOverlappingReservationsWithLock(@Param("roomId") Long roomId, @Param("startAt") LocalDateTime startAt, @Param("endAt") LocalDateTime endAt);

    @Query("SELECT r FROM Reservation r JOIN FETCH r.user WHERE r.startAt < :endOfDay AND r.endAt > :startOfDay")
    List<Reservation> findAllByDateRangeWithUser(
            @Param("startOfDay") LocalDateTime startOfDay,
            @Param("endOfDay") LocalDateTime endOfDay
    );
}
