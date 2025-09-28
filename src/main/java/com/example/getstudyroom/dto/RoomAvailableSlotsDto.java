package com.example.getstudyroom.dto;

import com.example.getstudyroom.entity.Room;
import lombok.Getter;

import java.time.LocalTime;
import java.util.List;

@Getter
public class RoomAvailableSlotsDto {
    private Long roomId;
    private String roomName;
    private List<LocalTime> availableStartTimes; // 예약 가능한 시작 시간 목록

    public static RoomAvailableSlotsDto from(Room room, List<LocalTime> availableStartTimes) {
        RoomAvailableSlotsDto dto = new RoomAvailableSlotsDto();
        dto.roomId = room.getId();
        dto.roomName = room.getName();
        dto.availableStartTimes = availableStartTimes;
        return dto;
    }
}
