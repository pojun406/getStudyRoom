-- init/init.sql

-- 데이터베이스가 없다면 생성
CREATE DATABASE IF NOT EXISTS study_room;
USE study_room;

-- room 테이블 생성
CREATE TABLE room (
                      id BIGINT NOT NULL AUTO_INCREMENT,
                      name VARCHAR(255),
                      location VARCHAR(255),
                      capacity INT,
                      PRIMARY KEY (id)
);

-- users 테이블 생성
CREATE TABLE users (
                       id BIGINT NOT NULL AUTO_INCREMENT,
                       name VARCHAR(255) NOT NULL UNIQUE,
                       password VARCHAR(255) NOT NULL,
                       roles VARCHAR(255) NOT NULL,
                       PRIMARY KEY (id)
);

-- reservation 테이블 생성
CREATE TABLE reservation (
                             id BIGINT NOT NULL AUTO_INCREMENT,
                             start_at DATETIME(6),
                             end_at DATETIME(6),
                             uid BIGINT,
                             rid BIGINT,
                             PRIMARY KEY (id),
    -- 외래 키(Foreign Key) 제약 조건 설정
                             CONSTRAINT fk_reservation_to_users FOREIGN KEY (uid) REFERENCES users (id),
                             CONSTRAINT fk_reservation_to_room FOREIGN KEY (rid) REFERENCES room (id)
);