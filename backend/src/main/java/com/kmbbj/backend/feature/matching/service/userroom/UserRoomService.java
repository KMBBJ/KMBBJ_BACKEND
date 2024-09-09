package com.kmbbj.backend.feature.matching.service.userroom;

import com.kmbbj.backend.feature.auth.entity.User;
import com.kmbbj.backend.feature.matching.entity.Room;
import com.kmbbj.backend.feature.matching.entity.UserRoom;

import java.util.List;
import java.util.Optional;

public interface UserRoomService {
    void save(UserRoom userRoom);

    Optional<UserRoom> findByUserAndRoom(User user, Room room);

    Optional<UserRoom> findByUserAndRoomAndIsPlayed(User user, Room room);

    UserRoom deleteUserFromRoom(Long roomId);

    UserRoom findCurrentRoom();

    List<UserRoom> findUserRooms(Room room);

    Optional<UserRoom> findUserRoomByUserAndIsPlayed();
}
