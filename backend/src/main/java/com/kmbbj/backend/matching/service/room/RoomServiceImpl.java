package com.kmbbj.backend.matching.service.room;

import com.kmbbj.backend.auth.entity.Authority;
import com.kmbbj.backend.auth.entity.User;
import com.kmbbj.backend.auth.service.UserService;
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

import java.util.Comparator;
import java.util.List;
import java.util.Optional;


@Service
@RequiredArgsConstructor
public class RoomServiceImpl implements RoomService{

    private final RoomRepository roomRepository;
    private final UserRoomService userRoomService;
    private final FindUserBySecurity findUserBySecurity;
    private final BalanceService balanceService;
    private final UserService userService;



    /**
     *
     * @param createRoomDTO     방 생성시 필요한 정보(제목, 시작 시드머니, 마지막 라운드,
     *                                              생성일자, 삭제여부, 시작여부, 시작 딜레이)
     * @return room     생성된 방
     */
    @Override
    @Transactional
    public Room createRoom(CreateRoomDTO createRoomDTO,User user) {
        // 이미 다른 방에 들어가 있는 경우
        if (userRoomService.findCurrentRoom() != null) {
            throw new ApiException(ExceptionEnum.IN_OTHER_ROOM);
        }

        // 설정 초기 시드머니가 자신의 자산 1/3 보다 높을경우
        if (createRoomDTO.getStartSeedMoney().getAmount() > (balanceService.totalBalanceFindByUserId(user.getId()).get().getAsset() / 3)) {
            throw new ApiException(ExceptionEnum.INSUFFICIENT_ASSET);
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
        Long currentUserAsset = balanceService.totalBalanceFindByUserId(user.getId()).orElseThrow(() -> new ApiException(ExceptionEnum.BALANCE_NOT_FOUND)).getAsset();
        // 방 평균 자산 설정
        room.setAverageAsset(currentUserAsset);



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

        // 현재 들어갈 방의 유저 정보
        List<UserRoom> userRoomList = userRoomService.findUserRooms(room);

        // 현재 들어가 있는 방에서 자산이 가장 적은 유저
        UserRoom min = userRoomList.stream()
                .min(Comparator.comparing(userRoom -> balanceService.totalBalanceFindByUserId(userRoom.getUser().getId()).get().getAsset()))
                .orElseThrow(() -> new ApiException(ExceptionEnum.ANYONE_IN_ROOM));

        // 현재 유저 정보
        UserRoom userRoom = userRoomService.findByUserAndRoomAndIsPlayed(user, room).orElse(null);

        // 설정한 시작 시드머니가 자산이 가장 적은 유저의 자산 1/3 보다 클 경우
        if (editRoomDTO.getStartSeedMoney().getAmount() > balanceService.totalBalanceFindByUserId(min.getUser().getId()).get().getAsset() / 3) {
            throw new ApiException(ExceptionEnum.INSUFFICIENT_ASSET_USER);
        }

        // 방장 여부 확인
        if (userRoom != null) {
            if (userRoom.getIsManager()) {
                room.setTitle(editRoomDTO.getTitle());
                room.setEnd(editRoomDTO.getEnd());
                room.setStartSeedMoney(editRoomDTO.getStartSeedMoney());
                roomRepository.save(room);
            } else {
                throw new ApiException(ExceptionEnum.FORBIDDEN);
            }
        } else {
            throw new ApiException(ExceptionEnum.NOT_ENTRY_ROOM);
        }


    }



    /**
     * 방 삭제
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
        return rooms.map(room ->
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
        return rooms.map(room ->
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

    /**
     *
     * @param user  현재 유저
     * @param roomId    들어갈 방 roomId
     */
    @Override
    @Transactional
    public void enterRoom(User user,Long roomId) {
        Room room = roomRepository.findById(roomId).orElseThrow(()->new ApiException(ExceptionEnum.ROOM_NOT_FOUND));

        // 현재 유저 자산
        Long currentUserAsset = balanceService.totalBalanceFindByUserId(user.getId()).get().getAsset();

        // 방에 들어온 상태 확인
        if (room.getUserRooms().stream().anyMatch(userRoom -> userRoom.getUser().equals(user) && userRoom.getIsPlayed())) {
            return; // 이미 입장함
        }

        // 다른 방에 있을 때 예외 처리
        if (userRoomService.findCurrentRoom() != null) {
            throw new ApiException(ExceptionEnum.IN_OTHER_ROOM);
        }

        Long asset = balanceService.totalBalanceFindByUserId(user.getId()).orElseThrow(() -> new ApiException(ExceptionEnum.BALANCE_NOT_FOUND)).getAsset();
        if (room.getUserRooms().size() >= 10) {
            throw new ApiException(ExceptionEnum.ROOM_FULL);
        }
        if (asset / 3 < room.getStartSeedMoney().getAmount()) {
            throw new ApiException(ExceptionEnum.INSUFFICIENT_ASSET);
        }

        UserRoom userRoom = userRoomService.findByUserAndRoomAndIsPlayed(user, room)
                .orElse(null);

        if (userRoom == null) {
            userRoom = UserRoom.builder()
                    .user(user)
                    .room(room)
                    .isPlayed(true)  // 처음 생성 시만 설정
                    .isManager(false)
                    .build();

            // 새로 생성된 객체만 저장
            userRoomService.save(userRoom);

            // 인원 수 업데이트
            room.setUserCount(room.getUserCount() + 1);

            // 방 평균 자산 설정
            Long roomAverageAsset = (room.getAverageAsset() * (room.getUserCount() - 1) + currentUserAsset) / room.getUserCount();
            room.setAverageAsset(roomAverageAsset);
            roomRepository.save(room);
        } else {
            if (!userRoom.getIsPlayed()) {
                userRoom.setIsPlayed(true);  // 상태 업데이트
                userRoomService.save(userRoom);
                // 방 평균 자산 설정
                Long roomAverageAsset = (room.getAverageAsset() * (room.getUserCount() - 1) + currentUserAsset) / room.getUserCount();
                room.setAverageAsset(roomAverageAsset);
                roomRepository.save(room);
            }
        }
    }

    /**
     *
     * @param room  들어갈 방
     * @return  들어갈 방의 정보
     */
    @Override
    public EnterRoomDTO getEnterRoomDto(Room room) {
        List<UserRoom> userRooms = room.getUserRooms();
        List<UserRoom> userRoomList = userRooms.stream().filter(UserRoom::getIsPlayed).toList();
        List<RoomUserListDTO> roomUserList = userRoomList.stream()
                .map(currentUserRoom -> RoomUserListDTO.builder()
                        .userName(currentUserRoom.getUser().getNickname())
                        .userAsset(balanceService.totalBalanceFindByUserId(currentUserRoom.getUser().getId()).get().getAsset())
                        .isManager(currentUserRoom.getIsManager())
                        .build())
                .toList();

        return EnterRoomDTO.builder()
                .roomTitle(room.getTitle())
                .averageAsset(room.getAverageAsset())
                .userCount(room.getUserCount())
                .roomUser(roomUserList)
                .build();
    }

    /**
     *
     * @param roomId    퇴장할 방 번호
     */
    @Override
    @Transactional
    public void quitRoom(Long roomId) {
        User currentUser = findUserBySecurity.getCurrentUser();
        UserRoom userRoom = userRoomService.findByUserAndRoomAndIsPlayed(currentUser, findById(roomId)).orElseThrow(() -> new ApiException(ExceptionEnum.USER_NOT_FOUND));
        List<UserRoom> userRoomList = userRoomService.findUserRooms(findById(roomId));
        // 방장이 나갈 경우 자산 가장 많은 사람으로 방장 바뀜
        if (userRoom.getIsManager()) {
            UserRoom max = userRoomList.stream()
                    .filter(userRoom1 -> !userRoom1.equals(userRoom))
                    .max(Comparator.comparing(currentUserRoom -> balanceService.totalBalanceFindByUserId(userRoom.getUser().getId()).get().getAsset()))
                    .orElseThrow(() -> new ApiException(ExceptionEnum.ANYONE_IN_ROOM));
            max.setIsManager(true);
            userRoom.setIsManager(false);
        }
        userRoomService.deleteUserFromRoom(roomId);
        Room room = findById(roomId);

        // 방을 나갔을때 아무도 없을경우 방 삭제 여부 true
        if (room.getUserCount() - 1 == 0) {
            room.setUserCount(0);
            room.setIsDeleted(true);
        }
        room.setUserCount(room.getUserCount() - 1);
        roomRepository.save(room);
    }

    @Override
    public List<Room> findRoomsWithinAssetRange(Long maxAsset) {
        return roomRepository.findRoomsWithinAssetRange(maxAsset);
    }

    @Override
    public Room findRoomByLatestCreateDate() {
        return roomRepository.findAllByOrderByCreateDateDesc().getFirst();
    }

}
