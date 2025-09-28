package com.example.getstudyroom.dto;

import com.example.getstudyroom.entity.Reservation;
import com.example.getstudyroom.entity.Room;

import java.util.List;
import java.util.stream.Collectors;

public class RoomAvailabilityDto {
    private Long roomId;
    private String roomName;
    private List<ReservationDto.Response> reservations;

    public static RoomAvailabilityDto fromEntity(Room room, List<Reservation> reservations) {
        RoomAvailabilityDto dto = new RoomAvailabilityDto();
        dto.roomId = room.getId();
        dto.roomName = room.getName();
        dto.reservations = reservations.stream()
                .map(ReservationDto.Response::fromEntity)
                .collect(Collectors.toList());
        return dto;
    }
}
