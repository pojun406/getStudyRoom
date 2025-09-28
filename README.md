Spring Boot와 JPA를 기반으로 한 간단한 스터디룸 예약 시스템

## ✨ 주요 기능

-   사용자, 스터디룸, 예약의 기본 CRUD 기능
-   특정 시간에 하나의 스터디룸은 한 명의 사용자만 예약 가능
-   **동시 예약 요청에 대한 데이터 정합성 보장 (Pessimistic Lock 활용)**

---

## 🛠️ 기술 스택

-   **Backend**: Java 17, Spring Boot 3.3.4, Spring Data JPA, Lombok
-   **Database**: MySQL 8.0
-   **Build Tool**: Gradle 8.7
-   **Containerization**: Docker, Docker Compose

### 사전 요구 사항

-   [Docker Desktop](https://www.docker.com/products/docker-desktop/)이 설치되어 있어야 합니다.

### 실행 방법

1.  **프로젝트 클론**
    ```bash
    git clone [https://github.com/pojun406/getStudyRoom.git](https://github.com/pojun406/getStudyRoom.git)
    ```

2.  **프로젝트 디렉토리로 이동**
    ```bash
    cd getStudyRoom
    ```

3.  **Docker Compose를 이용한 실행**
    아래 명령어를 실행하면 Gradle 빌드, Docker 이미지 생성, DB 컨테이너 및 애플리케이션 컨테이너 실행이 한번에 이루어집니다.

    ```bash
    docker-compose up --build
    ```
    정상적으로 실행되면 `study_room_app`과 `study_room_db` 두 개의 컨테이너가 실행됩니다.

4.  **애플리케이션 확인**
    -   애플리케이션은 `http://localhost:8080`에서 실행됩니다.
    -   데이터베이스는 `localhost:3306`에서 접속할 수 있습니다.
<img width="2014" height="111" alt="image" src="https://github.com/user-attachments/assets/21f447c6-1c0c-4c11-91f6-34b7f0c84e08" />
