package com.kmbbj.backend.matching.service;

import com.kmbbj.backend.matching.dto.CreateRoomDTO;
import com.kmbbj.backend.matching.dto.SearchingRoomDTO;
import com.kmbbj.backend.matching.dto.SortedRoomDTO;
import com.kmbbj.backend.matching.entity.Room;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;

public interface RoomService {
    Room createRoom(CreateRoomDTO createRoomDTO, Authentication authentication);

    void deleteRoom(Long roomId);

    Page<Room> searchRoomsByTitle(SearchingRoomDTO searchingRoomDTO);

    Page<Room> findAll(SortedRoomDTO sortedRoomDTO);

    Room findById(Long roomId);

    void startGame(Long roomId);

    void enterRoom(Long roomId, Authentication authentication);
}
