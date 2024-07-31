package com.kmbbj.backend.matching.repository;

import com.kmbbj.backend.matching.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {
    List<Room> findAllByIsDeleted(boolean isDeleted);
}
