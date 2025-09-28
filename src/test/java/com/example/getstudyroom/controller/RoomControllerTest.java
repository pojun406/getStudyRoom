package com.example.getstudyroom.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class RoomControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("ADMIN 권한으로 회의실 생성 성공")
    @WithMockUser(roles = "ADMIN") // 'ROLE_ADMIN' 권한을 가진 가짜 사용자 설정
    void createRoom_Success() throws Exception {
        // given: 어떤 데이터가 주어졌을 때
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("name", "테스트 회의실");
        requestBody.put("location", "3층");
        requestBody.put("capacity", 10);

        String jsonBody = objectMapper.writeValueAsString(requestBody);

        // when: 이 API를 호출하면
        mockMvc.perform(post("/rooms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                // then: 이런 결과가 나와야 한다
                .andExpect(status().isCreated()) // 201 Created 상태 코드를 기대
                .andDo(print()); // 요청/응답 내용 출력
    }

    @Test
    @DisplayName("USER 권한으로 회의실 생성 시 403 Forbidden 에러 발생")
    @WithMockUser(roles = "USER") // 'ROLE_USER' 권한을 가진 가짜 사용자 설정
    void createRoom_Fail_Forbidden() throws Exception {
        // given
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("name", "권한 없는 회의실");
        requestBody.put("location", "1층");
        requestBody.put("capacity", 5);

        String jsonBody = objectMapper.writeValueAsString(requestBody);

        // when & then
        mockMvc.perform(post("/rooms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isForbidden()) // 403 Forbidden 상태 코드를 기대
                .andDo(print());
    }

    @Test
    @DisplayName("특정 날짜의 회의실 가용 시간 조회 성공")
    @WithMockUser // 인증된 사용자라면 누구나 조회 가능
    void getRoomAvailabilities_Success() throws Exception {
        // given
        String date = "2025-09-28";

        // when & then
        mockMvc.perform(get("/rooms")
                        .param("date", date))
                .andExpect(status().isOk()) // 200 OK 상태 코드를 기대
                .andExpect(jsonPath("$").isArray()) // 응답이 JSON 배열 형태인지 확인
                .andDo(print());
    }
}
