package com.kmbbj.backend.matching.service.userroom;

import com.kmbbj.backend.auth.entity.User;
import com.kmbbj.backend.matching.entity.Room;
import com.kmbbj.backend.matching.entity.UserRoom;

public interface UserRoomService {
    void save(UserRoom userRoom);

    UserRoom findByUserAndRoom(User user, Room room);

    UserRoom deleteUserFromRoom(Long roomId);
}
