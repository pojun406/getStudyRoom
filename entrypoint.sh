#!/bin/sh

# 데이터베이스 호스트와 포트를 환경 변수에서 읽어옴 (docker-compose.yml에서 설정)
DB_HOST=db
DB_PORT=3306

echo "Waiting for database to be ready at ${DB_HOST}:${DB_PORT}..."

# nc(netcat) 명령어를 사용하여 DB 포트가 열릴 때까지 1초마다 확인
while ! nc -z ${DB_HOST} ${DB_PORT}; do
  sleep 1
done

echo "Database is ready!"

# 데이터베이스가 준비되면 Spring Boot 애플리케이션 실행
exec java -jar app.jar