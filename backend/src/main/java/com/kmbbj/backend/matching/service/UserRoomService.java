package com.kmbbj.backend.matching.service;

import com.kmbbj.backend.auth.entity.User;
import com.kmbbj.backend.matching.entity.Room;
import com.kmbbj.backend.matching.entity.UserRoom;
import org.springframework.security.core.Authentication;

public interface UserRoomService {
    void save(UserRoom userRoom);

    UserRoom findByUserAndRoom(User user, Room room);

    void deleteUserFromRoom(Long roomId, Authentication authentication);


}
