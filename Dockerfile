# ===================
# 1. 빌드(Build) 단계
# ===================
# JDK 17 버전을 빌드 환경으로 사용
FROM eclipse-temurin:17-jdk-jammy as builder

# 작업 디렉토리 설정
WORKDIR /workspace

# 빌드에 필요한 파일들을 먼저 복사하여 캐시 활용
COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .

# 의존성 다운로드
RUN ./gradlew dependencies

# 소스 코드 전체 복사
COPY src src

# Gradle을 사용하여 애플리케이션 빌드
RUN ./gradlew build

# ===================
# 2. 실행(Run) 단계
# ===================
# 더 가벼운 JRE(Java 실행 환경) 이미지 사용
FROM eclipse-temurin:17-jre-jammy

USER root
# apt-get 업데이트 및 netcat-openbsd 설치 (-y 옵션으로 자동 승인)
RUN apt-get update && apt-get install -y netcat-openbsd

# 작업 디렉토리 설정
WORKDIR /app

# 빌드 단계에서 생성된 JAR 파일을 실행 단계로 복사
COPY --from=builder /workspace/build/libs/*.jar app.jar

# entrypoint.sh 스크립트 복사 및 실행 권한 부여
COPY entrypoint.sh .
RUN chmod +x entrypoint.sh

# entrypoint.sh 스크립트를 실행하여 컨테이너 시작
ENTRYPOINT ["./entrypoint.sh"]