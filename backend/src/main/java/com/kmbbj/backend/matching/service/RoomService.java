package com.kmbbj.backend.matching.service;

import com.kmbbj.backend.matching.dto.CreateRoomDTO;
import com.kmbbj.backend.matching.entity.Room;
import com.kmbbj.backend.matching.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RoomService {

    private final RoomRepository roomRepository;

    // 방 생성
    @Transactional
    public Room createRoom(CreateRoomDTO createRoomDTO) {
        Room room = new Room();
        room.setTitle(createRoomDTO.getTitle());
        room.setStartSeedMoney(createRoomDTO.getStartSeedMoney());
        room.setEnd(createRoomDTO.getEnd());
        room.setCreateDate(createRoomDTO.getCreateDate());
        room.setIsDeleted(false);
        room.setIsStarted(false);
        room.setDelay(createRoomDTO.getDelay());

        return roomRepository.save(room);
    }

    // 방 삭제
    @Transactional
    public void deleteRoom(Long roomId) {
        Room room = roomRepository.findById(roomId).orElseThrow(() -> new RuntimeException("방이 없습니다 : " + roomId));
        room.setIsDeleted(true);
        roomRepository.save(room);
    }

    public List<Room> getRoomsByIsDeleted() {
        return roomRepository.findAllByIsDeleted(false);
    }
}
