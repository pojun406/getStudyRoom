package com.example.getstudyroom.service;

import com.example.getstudyroom.entity.Reservation;
import com.example.getstudyroom.entity.Room;
import com.example.getstudyroom.entity.User;
import com.example.getstudyroom.repository.ReservationRepository;
import com.example.getstudyroom.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReservationService {
    private final ReservationRepository reservationRepository;
    private final RoomRepository roomRepository;

    @Transactional
    public Long createReservation(Long roomId, LocalDateTime startAt, LocalDateTime endAt, User user){
        // 방 존재 여부 확인
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 방 입니다."));

        // 예약 조회(겹치는 방 확인)
        List<Reservation> overlappingReservations = reservationRepository
                .findOverlappingReservationsWithLock(roomId, startAt, endAt);

        // 겹친다면 예외 발생
        if(!overlappingReservations.isEmpty()){
            throw new IllegalArgumentException("해당시간에 이미 예약이 존재합니다.");
        }

        // 예약 성공 및 저장
        Reservation reservation = new Reservation(user, room, startAt, endAt);

        Reservation savedReservation = reservationRepository.save(reservation);

        return savedReservation.getId();
    }
}
