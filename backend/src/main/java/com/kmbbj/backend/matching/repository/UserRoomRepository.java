package com.kmbbj.backend.matching.repository;

import com.kmbbj.backend.auth.entity.User;
import com.kmbbj.backend.matching.entity.Room;
import com.kmbbj.backend.matching.entity.UserRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRoomRepository extends JpaRepository<UserRoom, Long> {
    UserRoom findByUserAndRoom(User user, Room room);

    UserRoom findByUserAndIsPlayed(User user, boolean isPlayed);
}
