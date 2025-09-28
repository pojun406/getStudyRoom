package com.example.getstudyroom.controller;

import com.example.getstudyroom.dto.RoomAvailableSlotsDto;
import com.example.getstudyroom.dto.RoomDto;
import com.example.getstudyroom.service.RoomService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class RoomController {

    private final RoomService roomService;

    @PostMapping("/rooms")
    public ResponseEntity<Void> createRoom(@Valid @RequestBody RoomDto.Request requestDto) {
        Long roomId = roomService.createRoom(requestDto);
        return ResponseEntity.created(URI.create("/rooms/" + roomId)).build();
    }

    @GetMapping("/rooms")
    public ResponseEntity<List<RoomAvailableSlotsDto>> getRoomAvailableSlotsDto(@RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date){
        List<RoomAvailableSlotsDto> availableSlot = roomService.getRoomAvailabilities(date);

        return ResponseEntity.ok(availableSlot);
    }
}
