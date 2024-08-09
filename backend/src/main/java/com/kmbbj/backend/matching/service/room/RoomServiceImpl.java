package com.kmbbj.backend.matching.service.room;

import com.kmbbj.backend.auth.entity.Authority;
import com.kmbbj.backend.auth.entity.User;
import com.kmbbj.backend.balance.service.BalanceService;
import com.kmbbj.backend.global.config.exception.ApiException;
import com.kmbbj.backend.global.config.exception.ExceptionEnum;
import com.kmbbj.backend.global.config.security.FindUserBySecurity;
import com.kmbbj.backend.matching.dto.*;
import com.kmbbj.backend.matching.entity.Room;
import com.kmbbj.backend.matching.entity.UserRoom;
import com.kmbbj.backend.matching.repository.RoomRepository;
import com.kmbbj.backend.matching.service.userroom.UserRoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
@RequiredArgsConstructor
public class RoomServiceImpl implements RoomService{

    private final RoomRepository roomRepository;
    private final UserRoomService userRoomService;
    private final FindUserBySecurity findUserBySecurity;
    private final BalanceService balanceService;


    /**
     *
     * @param createRoomDTO     방 생성시 필요한 정보(제목, 시작 시드머니, 마지막 라운드,
     *                                              생성일자, 삭제여부, 시작여부, 시작 딜레이)
     * @return room     생성된 방
     */
    @Override
    @Transactional
    public Room createRoom(CreateRoomDTO createRoomDTO,User user) {
        if (userRoomService.findCurrentRoom() == null) {
            throw new ApiException(ExceptionEnum.IN_OTHER_ROOM);
        }
        // 방 생성
        Room room = new Room();
        room.setTitle(createRoomDTO.getTitle());
        room.setStartSeedMoney(createRoomDTO.getStartSeedMoney());
        room.setEnd(createRoomDTO.getEnd());
        room.setCreateDate(createRoomDTO.getCreateDate());
        room.setIsDeleted(false);
        room.setIsStarted(false);
        room.setDelay(createRoomDTO.getDelay());
        room.setUserCount(1);
        Long currentUserAsset = balanceService.totalBalanceFindByUserId(user.getId()).orElseThrow(()->new ApiException(ExceptionEnum.BALANCE_NOT_FOUND)).getAsset();
        if (room.getUserCount() == 1) {
            room.setAverageAsset(currentUserAsset);
        } else {
            long roomAverageAsset = (room.getAverageAsset() * (room.getUserCount() - 1) + currentUserAsset) / room.getUserCount();
            room.setAverageAsset(roomAverageAsset);
        }


        // 방 정보를 데이터베이스에 저장
        room = roomRepository.save(room);

        // 현재 접속 유저
        UserRoom userRoom = UserRoom.builder().user(user)
                .room(room)
                .isPlayed(true) // 방에 들어와 있는 거 체크
                .isManager(true) // 사용자를 방장으로 설정
                .build();

        // UserRoom 정보를 데이터베이스에 저장
        userRoomService.save(userRoom);

        return room;
    }

    @Override
    @Transactional
    public void editRoom(Long roomId, EditRoomDTO editRoomDTO) {
        User user = findUserBySecurity.getCurrentUser();
        Room room = findById(roomId);
        UserRoom userRoom = userRoomService.findByUserAndRoom(user, room);
        if (userRoom.getIsManager()) {
            room.setTitle(editRoomDTO.getTitle());
            room.setEnd(editRoomDTO.getEnd());
            roomRepository.save(room);
        } else {
            throw new ApiException(ExceptionEnum.FORBIDDEN);
        }

    }



    /**
     *
     * @param roomId    삭제할 방 번호
     */
    @Override
    @Transactional
    public void deleteRoom(Long roomId) {
        // 관리자 일때 방 삭제
        User user = findUserBySecurity.getCurrentUser();
        if (!user.getAuthority().equals(Authority.USER)) {
            Room room = findById(roomId);
            room.setIsDeleted(true);
            roomRepository.save(room);
        } else {
            throw new ApiException(ExceptionEnum.FORBIDDEN);
        }
    }

    /**
     *
     * @param searchingRoomDTO     검색 기능 필요한 정보 (페이지, 찾는 방 제목)
     * @return rooms    찾는 키워드가 포함 되는 방 목록
     */
    @Override
    @Transactional(readOnly = true)
    public Page<RoomListDTO> searchRoomsByTitle(SearchingRoomDTO searchingRoomDTO) {
        Pageable pageable = PageRequest.of(searchingRoomDTO.getPage(), 10);
        // 키워드 포함 목록 찾기
        Page<Room> rooms = roomRepository.findByTitleContainingIgnoreCase(searchingRoomDTO.getTitle(), pageable);
        Page<RoomListDTO> roomList = rooms.map(room ->
                RoomListDTO.builder()
                        .roomId(room.getRoomId())
                        .title(room.getTitle())
                        .startSeedMoney(room.getStartSeedMoney())
                        .end(room.getEnd())
                        .createDate(room.getCreateDate())
                        .delay(room.getDelay())
                        .userCount(room.getUserCount())
                        .build()
        );
        return roomList;
    }

    /** TODO
     *
     * @param sortConditionDTO     정렬 기능 필요한 정보 (삭제 여부, 시작 여부, 페이지, 정렬 필드명, 정렬 기준)
     * @return rooms    정렬된 방 목록
     */

    @Override
    @Transactional(readOnly = true)
    public Page<RoomListDTO> findAll(SortConditionDTO sortConditionDTO) {
        // 정렬 조건에 맞지 않을때 예외 처리
        // 정렬 정보
        Sort sort = Sort.by(Sort.Direction.fromString(sortConditionDTO.getSortDirection()), sortConditionDTO.getSortField());
        // 페이지 마다 정렬기준이 풀리지 않도록
        Pageable pageable = PageRequest.of(sortConditionDTO.getPage(), 10, sort);
        Page<Room> rooms = roomRepository.findAllByIsDeletedAndIsStarted(sortConditionDTO.isDeleted(), sortConditionDTO.isStarted(), pageable);
        Page<RoomListDTO> sortedRooms = rooms.map(room ->
                RoomListDTO.builder()
                        .roomId(room.getRoomId())
                        .title(room.getTitle())
                        .startSeedMoney(room.getStartSeedMoney())
                        .end(room.getEnd())
                        .createDate(room.getCreateDate())
                        .delay(room.getDelay())
                        .userCount(room.getUserCount())
                        .build()
        );
        return sortedRooms;
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

    /** TODO
     *
     * @param roomId    현재 방 번호
     */
    @Override
    @Transactional
    public void startGame(Long roomId) {
        // 게임 시작 전 delay 시간을 이메일로 알려주는 beforeStart 메서드 추가
        Room room = findById(roomId);
        if (room.getUserRooms().size() >= 4) {
            room.setIsStarted(true);
            roomRepository.save(room);
        }
    }

    /** TODO
     *
     * @param roomId    선택한 방 번호
     */
    @Override
    @Transactional
    public void enterRoom(Long roomId) {
        Room room = findById(roomId);
        User currentUser = findUserBySecurity.getCurrentUser();
        if (room.getUserRooms().stream().anyMatch(user -> user.equals(currentUser))) return;

        if (userRoomService.findCurrentRoom() == null) {
            throw new ApiException(ExceptionEnum.IN_OTHER_ROOM);
        }

        if (room.getUserRooms().size() < 10) {
            // 자산에 따라 들어갈수 있는 방 다르게 해야됨 쟤가 안만들어줌 (박석원 ㅋㅋ)

            UserRoom userRoom = null;
            try {
                // 이미 방에 들어와 있을때 예외 처리 해주기
                // 다른 방에 들어가 있는 상태일 경우 예외 처리 해주기
                userRoom = userRoomService.findByUserAndRoom(currentUser, findById(roomId));
                userRoom.setIsPlayed(true);

            } catch (Exception e) {
                userRoom = UserRoom.builder().user(currentUser)
                        .room(room)
                        .isPlayed(true)
                        .isManager(false)
                        .build();
                room.setUserCount(room.getUserCount() + 1);
            }

            roomRepository.save(room);
            userRoomService.save(userRoom);
        }

    }

    /**
     *
     * @param roomId    퇴장할 방 번호
     */
    @Override
    @Transactional
    public void quitRoom(Long roomId) {
        UserRoom userRoom = userRoomService.deleteUserFromRoom(roomId);
        Room room = userRoom.getRoom();
        room.setUserCount(room.getUserCount() - 1);
        roomRepository.save(room);
    }

    @Override
    public List<Room> findRoomsWithinAssetRange(Long asset, Long range) {
        List<Room> roomsWithinAssetRange = roomRepository.findRoomsWithinAssetRange(asset, range);
        return roomsWithinAssetRange;
    }

    @Override
    public Room findRoomByLatestCreateDate() {
        return roomRepository.findAllByOrderByCreateDateDesc().getFirst();
    }

}
