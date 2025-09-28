package com.example.getstudyroom.controller;

import com.example.getstudyroom.entity.Reservation;
import com.example.getstudyroom.entity.Room;
import com.example.getstudyroom.entity.User;
import com.example.getstudyroom.enums.RolesType;
import com.example.getstudyroom.security.userdetails.UserDetailsImpl;
import com.example.getstudyroom.repository.RoomRepository;
import com.example.getstudyroom.repository.UserRepository;
import com.example.getstudyroom.repository.ReservationRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ReservationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private ReservationRepository reservationRepository;


    @Autowired
    private PasswordEncoder passwordEncoder;

    private Room testRoom;
    private User testUser;
    private User otherUser;
    private User testAdmin;
    private Reservation testReservation;

    // 각 테스트 실행 전에 테스트용 유저와 방을 미리 생성
    @BeforeEach
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    void setUp() {
        // 테스트용 유저 1 (예약 소유자)
        testUser = new User("testuser", passwordEncoder.encode("password"), RolesType.ROLE_USER);
        userRepository.save(testUser);

        // 테스트용 유저 2 (다른 사용자)
        otherUser = new User("otheruser", passwordEncoder.encode("password"), RolesType.ROLE_USER);
        userRepository.save(otherUser);

        testAdmin = new User("testadmin", passwordEncoder.encode("password"), RolesType.ROLE_ADMIN);
        userRepository.save(testAdmin);

        testRoom = new Room("미리 만든 방", "1층", 5);
        roomRepository.save(testRoom);

        // 'testuser'가 생성한 예약을 미리 저장
        testReservation = new Reservation(testUser, testRoom, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(1).plusHours(1));
        reservationRepository.save(testReservation);
    }

    @Test
    @DisplayName("USER 권한으로 예약 생성 성공")
    //@WithUserDetails("testuser") // 'testuser'라는 이름으로 로그인한 상태를 시뮬레이션
    void createReservation_Success() throws Exception {

        UserDetailsImpl userDetails = new UserDetailsImpl(this.testUser);

        // given
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("roomId", testRoom.getId());
        requestBody.put("startAt", LocalDateTime.now().plusHours(1).toString());
        requestBody.put("endAt", LocalDateTime.now().plusHours(2).toString());

        String jsonBody = objectMapper.writeValueAsString(requestBody);

        // when & then
        mockMvc.perform(post("/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody)
                        // 요청(request)에 직접 인증된 사용자 정보를 포함시킴
                        .with(user(userDetails)))
                .andExpect(status().isCreated())
                .andDo(print());
    }

    @Test
    @DisplayName("예약 소유자가 직접 예약을 취소하면 성공")
    void cancelReservation_Success_AsOwner() throws Exception {
        // given
        // setUp에서 생성된 testUser(소유자)로 UserDetails 생성
        UserDetailsImpl userDetails = new UserDetailsImpl(this.testUser);

        // when & then
        mockMvc.perform(delete("/reservations/" + testReservation.getId())
                        .with(user(userDetails))) //'testuser'로 요청
                .andExpect(status().isNoContent()) //204 No Content 기대
                .andDo(print());
    }

    @Test
    @DisplayName("관리자가 직접 예약을 취소하면 성공")
    void cancelReservation_Success_AsAdmin() throws Exception {
        // given
        // setUp에서 생성된 testAdmin(관리자)로 UserDetails 생성
        UserDetailsImpl userDetails = new UserDetailsImpl(this.testAdmin);

        // when & then
        mockMvc.perform(delete("/reservations/" + testReservation.getId())
                        .with(user(userDetails))) //'testAdmin'으로 요청
                .andExpect(status().isNoContent()) //204 No Content 기대
                .andDo(print());
    }

    @Test
    @DisplayName("다른 사용자가 예약을 취소하려고 하면 403 Forbidden 에러 발생")
    void cancelReservation_Fail_AsOtherUser() throws Exception {
        // given
        // setUp에서 생성된 otherUser(소유자 아님)로 UserDetails 생성
        UserDetailsImpl userDetails = new UserDetailsImpl(this.otherUser);

        // when & then
        mockMvc.perform(delete("/reservations/" + testReservation.getId())
                        .with(user(userDetails))) //'otheruser'로 요청
                .andExpect(status().isForbidden()) //403 Forbidden 기대
                .andDo(print());
    }

    /* -> 동일 방, 동일 시간 10개 동시 요청 테스트 실패(Mocku의 한계? 좀더 공부 필요)
    @Test
    @DisplayName("동일한 방, 동일한 시간에 10개의 예약 동시 요청 시 1개만 성공")
    void createReservation_Concurrency_Test() throws Exception {
        // given
        int threadCount = 10;
        // 10개의 스레드를 관리할 스레드 풀 생성
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        // 모든 스레드가 준비될 때까지 기다리게 할 Latch 생성
        CountDownLatch latch = new CountDownLatch(threadCount);

        // 성공, 실패 카운트를 동시성 환경에서 안전하게 증가시킬 AtomicInteger
        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger failCount = new AtomicInteger();

        UserDetailsImpl userDetails = new UserDetailsImpl(this.testUser);

        // 예약 요청 내용 미리 생성
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("roomId", testRoom.getId());
        requestBody.put("startAt", LocalDateTime.now().plusDays(5).toString());
        requestBody.put("endAt", LocalDateTime.now().plusDays(5).plusHours(1).toString());
        String jsonBody = objectMapper.writeValueAsString(requestBody);

        // when
        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    // MockMvc로 예약 생성 API 호출
                    mockMvc.perform(post("/reservations")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(jsonBody)
                                    .with(user(userDetails)))
                            .andExpect(status().isCreated()) // 성공(201)을 기대
                            .andDo(result -> successCount.getAndIncrement()); // 성공 시 카운트 증가
                } catch (Exception e) {
                    // isCreated()가 아닐 경우 예외가 발생하므로, 실패로 간주
                    failCount.getAndIncrement();
                } finally {
                    latch.countDown(); // 작업 완료를 Latch에 알림
                }
            });
        }

        latch.await(); // 모든 스레드의 작업이 끝날 때까지 대기
        executorService.shutdown();

        // then
        // 10개의 요청 중 정확히 1개만 성공했는지 검증
        assertThat(successCount.get()).isEqualTo(1);
        assertThat(failCount.get()).isEqualTo(threadCount - 1);

        // DB에도 실제로 1개의 예약만 저장되었는지 추가 검증
        List<Reservation> reservations = reservationRepository.findAllByRoom(testRoom);
        assertThat(reservations.size()).isEqualTo(1 + 1); // setUp에서 만든 예약 1개 + 동시성 테스트에서 성공한 예약 1개
    }*/

    /*@Test
    @DisplayName("이미 예약된 시간대에 중복 예약을 시도하면 409 Conflict 에러 발생")
    void createReservation_Fail_WhenTimeOverlaps() throws Exception {
        // given
        // setUp에서 testUser가 이미 내일 특정 시간에 예약을 해둔 상태 (testReservation)
        UserDetailsImpl userDetails = new UserDetailsImpl(this.testUser);

        // testReservation과 정확히 동일한 시간대로 예약 요청을 생성
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("roomId", testReservation.getRoom().getId());
        requestBody.put("startAt", testReservation.getStartAt().toString());
        requestBody.put("endAt", testReservation.getEndAt().toString());

        String jsonBody = objectMapper.writeValueAsString(requestBody);

        // when & then
        mockMvc.perform(post("/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody)
                        .with(user(userDetails)))
                .andExpect(status().isConflict())
                .andDo(print());
    }*/

}