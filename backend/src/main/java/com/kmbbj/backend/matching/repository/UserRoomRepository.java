package com.kmbbj.backend.matching.repository;

import com.kmbbj.backend.auth.entity.User;
import com.kmbbj.backend.matching.entity.Room;
import com.kmbbj.backend.matching.entity.UserRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRoomRepository extends JpaRepository<UserRoom, Long> {
    Optional<UserRoom> findByUserAndRoom(User user, Room room);

    Optional<UserRoom> findByUserAndIsPlayed(User user, boolean isPlayed);
}
