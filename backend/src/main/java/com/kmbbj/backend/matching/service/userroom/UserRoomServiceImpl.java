package com.kmbbj.backend.matching.service.userroom;

import com.kmbbj.backend.auth.entity.User;
import com.kmbbj.backend.global.config.exception.ApiException;
import com.kmbbj.backend.global.config.exception.ExceptionEnum;
import com.kmbbj.backend.global.config.security.FindUserBySecurity;
import com.kmbbj.backend.matching.entity.Room;
import com.kmbbj.backend.matching.entity.UserRoom;
import com.kmbbj.backend.matching.repository.UserRoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserRoomServiceImpl implements UserRoomService{
    private final UserRoomRepository userRoomRepository;
    private final FindUserBySecurity findUserBySecurity;

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
        return userRoomRepository.findByUserAndRoom(user, room).orElseThrow(()-> new ApiException(ExceptionEnum.NOT_ENTRY_ROOM));
    }

    /**
     *
     * @param roomId    현재 방 위치
     */
    @Override
    public UserRoom deleteUserFromRoom(Long roomId) {
        // 해당 방에 들어가 있지 않을때 예외처리
        Long currentRoomId = findCurrentRoom().getRoom().getRoomId();
        if (currentRoomId == null || !currentRoomId.equals(roomId)) {
            throw new ApiException(ExceptionEnum.NOT_CURRENT_ROOM);
        }

        // 유저가 들어가 있는 방
        UserRoom userRoom = findCurrentRoom();
        // 방에서 플레이 여부 false
        userRoom.setIsPlayed(false);
        save(userRoom);
        return userRoom;
    }

    @Override
    public UserRoom findCurrentRoom() {
        User user = findUserBySecurity.getCurrentUser();
        UserRoom userRoom = userRoomRepository.findByUserAndIsPlayed(user, true).orElseThrow(()->new ApiException(ExceptionEnum.NOT_CURRENT_ROOM));
        return userRoom;
    }
}
