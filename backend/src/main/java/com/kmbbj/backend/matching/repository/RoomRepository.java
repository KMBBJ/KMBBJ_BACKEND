package com.kmbbj.backend.matching.repository;

import com.kmbbj.backend.matching.entity.Room;
import com.kmbbj.backend.matching.entity.StartSeedMoney;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {
    Page<Room> findAllByIsDeleted(boolean isDeleted, Pageable pageable);

    Page<Room> findByTitleContainingIgnoreCase(String title, Pageable pageable);

    Page<Room> findAllByIsDeletedAndIsStarted(boolean isDeleted, boolean isStarted, Pageable pageable);


    List<Room> findRoomsByStartSeedMoneyAndIsStarted(StartSeedMoney startSeedMoney,boolean isStarted);


    List<Room> findAllByOrderByCreateDateDesc();
}
