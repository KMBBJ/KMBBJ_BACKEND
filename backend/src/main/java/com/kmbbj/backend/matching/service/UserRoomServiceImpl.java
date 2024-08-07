package com.kmbbj.backend.matching.service;

import com.kmbbj.backend.auth.entity.User;
import com.kmbbj.backend.auth.service.UserService;
import com.kmbbj.backend.global.config.jwt.infrastructure.CustomUserDetails;
import com.kmbbj.backend.matching.entity.Room;
import com.kmbbj.backend.matching.entity.UserRoom;
import com.kmbbj.backend.matching.repository.UserRoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserRoomServiceImpl implements UserRoomService{
    private final UserRoomRepository userRoomRepository;
    private final UserService userService;

    @Override
    public void save(UserRoom userRoom) {
        userRoomRepository.save(userRoom);
    }

    /**
     *
     * @param user  접속 유저
     * @param room  현재 방 위치
     * @return userRoom
     */
    @Override
    public UserRoom findByUserAndRoom(User user, Room room) {
        return userRoomRepository.findByUserAndRoom(user, room);
    }

    /**
     *
     * @param roomId    현재 방 위치
     * @param authentication    인증정보
     */
    @Override
    public void deleteUserFromRoom(Long roomId,Authentication authentication) {

        // 유저가 들어가 있는 방
        UserRoom userRoom = findCurrentRoom(authentication);
        Room room = userRoom.getRoom();
        // 방에서 플레이 여부 false
        userRoom.setIsPlayed(false);
        room.setUserCount(room.getUserCount() - 1);
        save(userRoom);
    }

    public UserRoom findCurrentRoom(Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Long userId = userDetails.getUserId();
        User user = userService.UserfindById(userId).orElseThrow();
        UserRoom userRoom = userRoomRepository.findByUserAndIsPlayed(user, true);
        return userRoom;
    }
}
