package com.example.getstudyroom.repository;

import com.example.getstudyroom.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoomRepository extends JpaRepository<Room,Long> {

}
