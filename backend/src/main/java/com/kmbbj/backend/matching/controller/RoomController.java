package com.kmbbj.backend.matching.controller;


import com.kmbbj.backend.matching.dto.CreateRoomDTO;
import com.kmbbj.backend.matching.entity.Room;
import com.kmbbj.backend.matching.service.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/room")
@RequiredArgsConstructor
public class RoomController {

    private final RoomService roomService;

    @GetMapping("/list")
    public ResponseEntity<List<Room>> matchingRoomList() {
        List<Room> rooms= roomService.getRoomsByIsDeleted();
        return ResponseEntity.ok(rooms);
    }

    @PostMapping("/create")
    public ResponseEntity<Room> createRoom(@RequestBody CreateRoomDTO createRoomDTO) {
        Room room = roomService.createRoom(createRoomDTO);
        return ResponseEntity.ok(room);
    }

    @DeleteMapping("/delete/{roomId}")
    public ResponseEntity<?> deleteRoom(@PathVariable Long roomId) {
        roomService.deleteRoom(roomId);
        return ResponseEntity.ok().build();
    }

}
