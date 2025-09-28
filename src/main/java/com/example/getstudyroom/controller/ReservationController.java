package com.example.getstudyroom.controller;

import com.example.getstudyroom.dto.ReservationDto;
import com.example.getstudyroom.security.userdetails.UserDetailsImpl;
import com.example.getstudyroom.service.ReservationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequiredArgsConstructor
public class ReservationController {
    private final ReservationService reservationService;

    // 예약 생성
    @PostMapping("/reservations")
    public ResponseEntity<Void> createReservation(
            @Valid @RequestBody ReservationDto.Request requestDto,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        Long reservationId = reservationService.createReservation(requestDto, userDetails.getUser());
        return ResponseEntity.created(URI.create("/reservations/" + reservationId)).build();
    }

    @DeleteMapping("/reservations/{id}")
    public ResponseEntity<Void> cancelReservation(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        reservationService.cancelReservation(id, userDetails.getUser());
        return ResponseEntity.noContent().build();
    }

}
