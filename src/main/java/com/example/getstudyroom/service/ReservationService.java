package com.example.getstudyroom.service;

import com.example.getstudyroom.dto.ReservationDto;
import com.example.getstudyroom.entity.Reservation;
import com.example.getstudyroom.entity.Room;
import com.example.getstudyroom.entity.User;
import com.example.getstudyroom.enums.RolesType;
import com.example.getstudyroom.repository.ReservationRepository;
import com.example.getstudyroom.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
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
    public Long createReservation(ReservationDto.Request dto, User user){
        // 방 존재 여부 확인
        Room room = roomRepository.findById(dto.getRoomId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 방 입니다."));

        // 예약 조회(겹치는 방 확인)
        List<Reservation> overlappingReservations = reservationRepository
                .findOverlappingReservationsWithLock(dto.getRoomId(), dto.getStartAt(), dto.getEndAt());

        // 겹친다면 예외 발생
        if(!overlappingReservations.isEmpty()){
            throw new IllegalArgumentException("해당시간에 이미 예약이 존재합니다.");
        }

        // 예약 성공 및 저장
        Reservation reservation = new Reservation(user, room, dto.getStartAt(), dto.getEndAt());

        Reservation savedReservation = reservationRepository.save(reservation);

        return savedReservation.getId();
    }

    @Transactional
    public void cancelReservation(Long reservationId, User currentUser) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 예약입니다."));

        // ADMIN이거나 예약 소유자인 경우에만 취소 가능
        if (currentUser.getRoles() == RolesType.ROLE_ADMIN || reservation.getUser().getId().equals(currentUser.getId())) {
            reservationRepository.delete(reservation);
        } else {
            throw new AccessDeniedException("예약을 취소할 권한이 없습니다.");
        }
    }
}
