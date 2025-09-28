package com.example.getstudyroom.service;

import com.example.getstudyroom.dto.ReservationDto;
import com.example.getstudyroom.entity.Reservation;
import com.example.getstudyroom.entity.Room;
import com.example.getstudyroom.entity.User;
import com.example.getstudyroom.enums.RolesType;
import com.example.getstudyroom.repository.ReservationRepository;
import com.example.getstudyroom.repository.RoomRepository;
import com.example.getstudyroom.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReservationService {
    private final ReservationRepository reservationRepository;
    private final UserRepository userRepository;
    private final RoomRepository roomRepository;

    @Transactional
    public Long createReservation(ReservationDto.Request requestDto, User userFromController) {
        // Controller에서 받은 user는 Detached 상태일 수 있으므로,
        // DB에서 다시 조회하여 영속 상태(Managed)의 User 객체를 가져옵니다.
        User managedUser = userRepository.findById(userFromController.getId())
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다."));

        Room room = roomRepository.findById(requestDto.getRoomId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 방입니다."));

        // 비관적 락을 통해 겹치는 예약 조회
        List<Reservation> overlappingReservations = reservationRepository
                .findOverlappingReservationsWithLock(requestDto.getRoomId(), requestDto.getStartAt(), requestDto.getEndAt());

        if (!overlappingReservations.isEmpty()) {
            throw new IllegalStateException("해당 시간에 이미 예약이 존재합니다.");
        }

        // 새로 조회한 영속 상태의 User 객체를 사용하여 Reservation 생성
        Reservation reservation = new Reservation(managedUser, room, requestDto.getStartAt(), requestDto.getEndAt());
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
