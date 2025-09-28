package com.example.getstudyroom.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
public class Reservation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uid")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rid")
    private Room room;

    private LocalDateTime startAt; // 예약 시작 날짜
    private LocalDateTime endAt; // 예약 종료 날짜

    public Reservation(User user, Room room, LocalDateTime startAt, LocalDateTime endAt) {
        this.user = user;
        this.room = room;
        this.startAt = startAt;
        this.endAt = endAt;
    }
}
