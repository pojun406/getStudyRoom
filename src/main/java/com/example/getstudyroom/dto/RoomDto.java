package com.example.getstudyroom.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.*;

public class RoomDto {

    // 방 생성을 위한 요청 DTO
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Request {
        @NotBlank(message = "방 이름은 필수입니다.")
        private String name;

        @NotBlank(message = "위치는 필수입니다.")
        private String location;

        @Positive(message = "수용인원은 1 이상이어야 합니다.")
        private int capacity;
    }
}
