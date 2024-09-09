package com.kmbbj.backend.feature.matching.repository;

import com.kmbbj.backend.feature.auth.entity.User;
import com.kmbbj.backend.feature.matching.entity.Room;
import com.kmbbj.backend.feature.matching.entity.UserRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRoomRepository extends JpaRepository<UserRoom, Long> {
    Optional<UserRoom> findByUserAndRoom(User user, Room room);
    Optional<UserRoom> findByUserAndRoomAndIsPlayed(User user, Room room,boolean isPlayed);

    Optional<UserRoom> findByUserAndIsPlayed(User user, boolean isPlayed);

    List<UserRoom> findAllByRoomAndIsPlayed(Room room, boolean isPlayed);

    Optional<UserRoom> findByUserIdAndIsPlayed(Long userId, boolean isPlayed);
}
