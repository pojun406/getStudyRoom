package com.example.getstudyroom.service;

import com.example.getstudyroom.dto.RoomAvailabilityDto;
import com.example.getstudyroom.dto.RoomAvailableSlotsDto;
import com.example.getstudyroom.dto.RoomDto;
import com.example.getstudyroom.entity.Reservation;
import com.example.getstudyroom.entity.Room;
import com.example.getstudyroom.repository.ReservationRepository;
import com.example.getstudyroom.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RoomService {
    private final RoomRepository roomRepository;
    private final ReservationRepository reservationRepository;

    // 회의실 등록
    @Transactional
    public Long createRoom(RoomDto.Request requestDto) {
        Room room = new Room(requestDto.getName(), requestDto.getLocation(), requestDto.getCapacity());
        Room savedRoom = roomRepository.save(room);
        return savedRoom.getId();
    }

    public List<RoomAvailableSlotsDto> getRoomAvailabilities(LocalDate date) {
        List<Room> allRooms = roomRepository.findAll();
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.plusDays(1).atStartOfDay();
        List<Reservation> allReservationsForDate = reservationRepository.findAllByDateRangeWithUser(startOfDay, endOfDay);

        Map<Long, List<Reservation>> reservationsByRoomId = allReservationsForDate.stream()
                .collect(Collectors.groupingBy(reservation -> reservation.getRoom().getId()));

        return allRooms.stream().map(room -> {
            List<Reservation> bookedReservations = reservationsByRoomId.getOrDefault(room.getId(), List.of());
            List<LocalTime> availableSlots = calculateAvailableSlots(date, bookedReservations);
            return RoomAvailableSlotsDto.from(room, availableSlots);
        }).collect(Collectors.toList());
    }

    private List<LocalTime> calculateAvailableSlots(LocalDate date, List<Reservation> bookedReservations) {
        // 운영 시간 정의
        final LocalTime OPENING_TIME = LocalTime.of(9, 0);
        final LocalTime CLOSING_TIME = LocalTime.of(21, 0);
        final int SLOT_DURATION_HOURS = 1;

        List<LocalTime> availableSlots = new ArrayList<>();
        LocalTime potentialSlot = OPENING_TIME;

        // 운영 시간 내에서 1시간 단위로 모든 슬롯을 순회
        while (potentialSlot.isBefore(CLOSING_TIME)) {
            LocalDateTime slotStart = date.atTime(potentialSlot);
            LocalDateTime slotEnd = slotStart.plusHours(SLOT_DURATION_HOURS);

            boolean isBooked = false;
            // 해당 슬롯이 기존 예약과 겹치는지 확인
            for (Reservation booked : bookedReservations) {
                // [기존 예약 시작 < 슬롯 종료] AND [기존 예약 종료 > 슬롯 시작] -> 겹침
                if (booked.getStartAt().isBefore(slotEnd) && booked.getEndAt().isAfter(slotStart)) {
                    isBooked = true;
                    break;
                }
            }

            if (!isBooked) {
                availableSlots.add(potentialSlot);
            }
            potentialSlot = potentialSlot.plusHours(SLOT_DURATION_HOURS);
        }
        return availableSlots;
    }
}
