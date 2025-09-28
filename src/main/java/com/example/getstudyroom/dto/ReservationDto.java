package com.example.getstudyroom.dto;

import com.example.getstudyroom.entity.Reservation;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.time.LocalDateTime;

public class ReservationDto {

    @Getter
    // 예약 생성을 위한 요청 DTO
    public static class Request {
        @NotNull(message = "방 ID는 필수입니다.")
        private Long roomId;

        @NotNull(message = "시작 시간은 필수입니다.")
        @Future(message = "예약 시간은 현재보다 미래여야 합니다.")
        private LocalDateTime startAt;

        @NotNull(message = "종료 시간은 필수입니다.")
        @Future(message = "예약 시간은 현재보다 미래여야 합니다.")
        private LocalDateTime endAt;

        @AssertTrue(message = "종료 시간은 시작 시간보다 이후여야 합니다.")
        private boolean isEndAfterStart() {
            if (startAt == null || endAt == null) return false;
            return endAt.isAfter(startAt);
        }
    }

    // 가용성 조회 시 반환할 예약 정보 응답 DTO
    public static class Response {
        private Long reservationId;
        private String username;
        private LocalDateTime startAt;
        private LocalDateTime endAt;

        public static Response fromEntity(Reservation reservation) {
            Response dto = new Response();
            dto.reservationId = reservation.getId();
            dto.username = reservation.getUser().getName(); // User 정보 접근
            dto.startAt = reservation.getStartAt();
            dto.endAt = reservation.getEndAt();
            return dto;
        }
    }
}
