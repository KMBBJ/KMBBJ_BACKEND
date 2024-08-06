package com.kmbbj.backend.matching.service;

import com.kmbbj.backend.auth.entity.User;
import com.kmbbj.backend.auth.service.UserService;
import com.kmbbj.backend.global.config.exception.ApiException;
import com.kmbbj.backend.global.config.exception.ExceptionEnum;
import com.kmbbj.backend.global.config.jwt.infrastructure.CustomUserDetails;
import com.kmbbj.backend.matching.dto.CreateRoomDTO;
import com.kmbbj.backend.matching.dto.SearchingRoomDTO;
import com.kmbbj.backend.matching.dto.SortedRoomDTO;
import com.kmbbj.backend.matching.entity.Room;
import com.kmbbj.backend.matching.entity.UserRoom;
import com.kmbbj.backend.matching.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RoomServiceImpl implements RoomService{

    private final RoomRepository roomRepository;
    private final UserService userService;
    private final UserRoomService userRoomService;


    /**
     *
     * @param createRoomDTO     방 생성시 필요한 정보(제목, 시작 시드머니, 마지막 라운드,
     *                                              생성일자, 삭제여부, 시작여부, 시작 딜레이)
     * @param authentication    인증정보
     * @return room     생성된 방
     */
    @Override
    @Transactional
    public Room createRoom(CreateRoomDTO createRoomDTO,Authentication authentication) {
        // 방 생성
        Room room = new Room();
        room.setTitle(createRoomDTO.getTitle());
        room.setStartSeedMoney(createRoomDTO.getStartSeedMoney());
        room.setEnd(createRoomDTO.getEnd());
        room.setCreateDate(createRoomDTO.getCreateDate());
        room.setIsDeleted(false);
        room.setIsStarted(false);
        room.setDelay(createRoomDTO.getDelay());

        // 방 정보를 데이터베이스에 저장
        room = roomRepository.save(room);

        // 현재 접속 유저
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Long userId = userDetails.getUserId();
        User user = userService.UserfindById(userId).orElseThrow(() -> new RuntimeException("유저를 찾지 못했습니다"));
        UserRoom userRoom = UserRoom.builder().user(user)
                .room(room)
                .isPlayed(true) // 방에 들어와 있는 거 체크
                .isManager(false) // 사용자를 방장으로 설정
                .build();

        // UserRoom 정보를 데이터베이스에 저장
        userRoomService.save(userRoom);

        return room;
    }



    /**
     *
     * @param roomId    삭제할 방 번호
     */
    @Override
    @Transactional
    public void deleteRoom(Long roomId) {
        // 방 삭제
        Room room = findById(roomId);
        room.setIsDeleted(true);
        roomRepository.save(room);
    }

    /**
     *
     * @param searchingRoomDTO     검색 기능 필요한 정보 (페이지, 찾는 방 제목)
     * @return rooms    찾는 키워드가 포함 되는 방 목록
     */
    @Override
    @Transactional(readOnly = true)
    public Page<Room> searchRoomsByTitle(SearchingRoomDTO searchingRoomDTO) {
        Pageable pageable = PageRequest.of(searchingRoomDTO.getPage(), 10);
        // 키워드 포함 목록 찾기
        Page<Room> rooms = roomRepository.findByTitleContainingIgnoreCase(searchingRoomDTO.getTitle(), pageable);
        return rooms;
    }

    /**
     *
     * @param sortedRoomDTO     정렬 기능 필요한 정보 (삭제 여부, 시작 여부, 페이지, 정렬 필드명, 정렬 기준)
     * @return rooms    정렬된 방 목록
     */

    @Override
    @Transactional(readOnly = true)
    public Page<Room> findAll(SortedRoomDTO sortedRoomDTO) {
        // 정렬 정보
        Sort sort = Sort.by(Sort.Direction.fromString(sortedRoomDTO.getSortDirection()), sortedRoomDTO.getSortField());
        // 페이지 마다 정렬기준이 풀리지 않도록
        Pageable pageable = PageRequest.of(sortedRoomDTO.getPage(), 10, sort);
        Page<Room> rooms = roomRepository.findAllByIsDeletedAndIsStarted(sortedRoomDTO.isDeleted(), sortedRoomDTO.isStarted(), pageable);
        return rooms;
    }

    /**
     *
     * @param roomId    현재 방 번호
     * @return room     현재 방 이름
     */
    @Override
    public Room findById(Long roomId) {
        return roomRepository.findById(roomId).orElseThrow(()->
                new ApiException(ExceptionEnum.ROOM_NOT_FOUND));
    }

    /**
     *
     * @param roomId    현재 방 번호
     */
    @Override
    @Transactional
    public void startGame(Long roomId) {
        Room room = findById(roomId);
        if (room.getUserRooms().size() >= 4) {
            room.setIsStarted(true);
            roomRepository.save(room);
        }
    }

    /**
     *
     * @param roomId    선택한 방 번호
     * @param authentication    현재 유저 정보
     */
    @Override
    @Transactional
    public void enterRoom(Long roomId,Authentication authentication) {
        Room room = findById(roomId);
        if (room.getUserRooms().size() < 10) {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            User user = userService.UserfindById(userDetails.getUserId()).orElseThrow(() -> new RuntimeException("유저를 찾지 못했습니다"));
            UserRoom userRoom = UserRoom.builder().user(user)
                    .room(room)
                    .isPlayed(true)
                    .isManager(false)
                    .build();

            userRoomService.save(userRoom);
        }

    }

}
