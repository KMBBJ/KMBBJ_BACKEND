package com.kmbbj.backend.feature.matching.service.room;

import com.kmbbj.backend.feature.auth.entity.User;
import com.kmbbj.backend.feature.matching.dto.*;
import com.kmbbj.backend.feature.matching.entity.Room;
import org.springframework.data.domain.Page;

import java.util.List;

public interface RoomService {
    Room createRoom(CreateRoomDTO createRoomDTO, User user);

    void editRoom(Long roomId, EditRoomDTO editRoomDTO);

    void deleteRoom(Long roomId);

    Page<RoomListDTO> searchRoomsByTitle(SearchingRoomDTO searchingRoomDTO);

    Page<RoomListDTO> findAll(SortConditionDTO sortConditionDTO);

    Room findById(Long roomId);

    void startGame(Long roomId);

    void enterRoom(User user,Long roomId);

    void quitRoom(Long roomId);

    List<Room> findRoomsWithinAssetRange(Long maxAsset);

    Room findRoomByLatestCreateDate();

    EnterRoomDTO getEnterRoomDto(Room room);

    int beforeStart(Long roomId);

    Room findRoomById(Long roomId);
}
