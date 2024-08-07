package com.kmbbj.backend.matching.service;

import com.kmbbj.backend.matching.dto.CreateRoomDTO;
import com.kmbbj.backend.matching.dto.SearchingRoomDTO;
import com.kmbbj.backend.matching.dto.SortConditionDTO;
import com.kmbbj.backend.matching.dto.RoomListDTO;
import com.kmbbj.backend.matching.entity.Room;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;

public interface RoomService {
    Room createRoom(CreateRoomDTO createRoomDTO, Authentication authentication);

    void deleteRoom(Long roomId);

    Page<RoomListDTO> searchRoomsByTitle(SearchingRoomDTO searchingRoomDTO);

    Page<RoomListDTO> findAll(SortConditionDTO sortConditionDTO);

    Room findById(Long roomId);

    void startGame(Long roomId);

    void enterRoom(Long roomId, Authentication authentication);
}
