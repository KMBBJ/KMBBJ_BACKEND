package com.kmbbj.backend.matching.controller;


import com.kmbbj.backend.auth.entity.User;
import com.kmbbj.backend.global.config.reponse.ApiResponse;
import com.kmbbj.backend.matching.dto.CreateRoomDTO;
import com.kmbbj.backend.matching.dto.SearchingRoomDTO;
import com.kmbbj.backend.matching.dto.SortedRoomDTO;
import com.kmbbj.backend.matching.entity.Room;
import com.kmbbj.backend.matching.service.RoomService;
import com.kmbbj.backend.matching.service.UserRoomService;
import com.kmbbj.backend.matching.service.UserRoomServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/room")
@RequiredArgsConstructor
public class RoomController {

    private final RoomService roomService;
    private final UserRoomService userRoomService;

    /**
     * 방 생성
     * @param createRoomDTO     방 생성시 필요한 정보(제목, 시작 시드머니, 마지막 라운드,
     *                                              생성일자, 삭제여부, 시작여부, 시작 딜레이)
     * @param authentication    인증 정보
     * @return apiResponse      응답 (HttpStatus.OK, "방 생성 성공" ,room)
     */
    @PostMapping("/create")
    public ApiResponse<Room> createRoom(@RequestBody CreateRoomDTO createRoomDTO, Authentication authentication) {
        Room room = roomService.createRoom(createRoomDTO,authentication);

        return new ApiResponse<>(HttpStatus.OK,String.format("%d번방 생성 성공",room.getRoomId()), room);
    }


    /** 테스트 완료
     * 방 삭제
     * @param roomId    삭제할 방 번호
     * @return apiResponse  응답 (HttpStatus.OK, "{roomId}방 삭제 완료")
     */
    // 방 삭제는 유저가 모두 나갔거나 게임이 끝났을때 진행
    @DeleteMapping("/delete/{roomId}")
    public ApiResponse<Room> deleteRoom(@PathVariable Long roomId) {
        roomService.deleteRoom(roomId);
        return new ApiResponse<>(HttpStatus.OK,String.format("%d번 방 삭제 완료",roomId),null);
    }

    /**
     * 검색
     * @param searchingRoomDTO      검색 기능 필요한 정보 (페이지, 찾는 방 제목)
     * @return apiResponse      응답 (HttpStatus.OK, "{title}로 검색 성공", rooms)
     */
    @GetMapping("/searchRooms")
    public ApiResponse<Page<Room>> searchRooms(@RequestBody SearchingRoomDTO searchingRoomDTO) {
        Page<Room> rooms = roomService.searchRoomsByTitle(searchingRoomDTO);
        return new ApiResponse<>(HttpStatus.OK,searchingRoomDTO.getTitle() + "로 검색 성공", rooms);
    }

    /** 테스트 완료
     * 방 리스트
     * @param sortedRoomDTO     정렬 기능 필요한 정보 (삭제 여부, 시작 여부, 페이지, 정렬 필드명, 정렬 기준)
     * @return apiResponse      응답 (HttpStatus.OK,"{sortField,sortDirection}기준으로 정렬 성공",rooms)
     */
    @GetMapping("/list")
    public ApiResponse<Page<Room>> matchingRoomList(@RequestBody SortedRoomDTO sortedRoomDTO) {
        // 여기에 들어가는 SortedRoomDTO 는 모두 초깃값
        Page<Room> rooms = roomService.findAll(sortedRoomDTO);
        return new ApiResponse<>(HttpStatus.OK,String.format("%s, %s 기준으로 정렬 성공",sortedRoomDTO.getSortField(),sortedRoomDTO.getSortDirection()),rooms);
    }

    /** 테스트 완료
     * 선택 정렬
     * @param sortedRoomDTO     정렬 기능 필요한 정보 (삭제 여부, 시작 여부, 페이지, 정렬 필드명, 정렬 기준)
     * @return apiResponse      응답 (HttpStatus.OK,"{sortField,sortDirection}기준으로 정렬 성공",rooms)
     */
    @GetMapping("/sorted")
    public ApiResponse<Page<Room>> sortedRooms(@RequestBody SortedRoomDTO sortedRoomDTO) {
        // 여기 들어가는 SortedRoomDTO 는 isDeleted,isStarted 제외 모두 사용자 지정
        Page<Room> rooms = roomService.findAll(sortedRoomDTO);
        return new ApiResponse<>(HttpStatus.OK,String.format("%s, %s 기준으로 정렬 성공",sortedRoomDTO.getSortField(),sortedRoomDTO.getSortDirection()),rooms);
    }

    /** 테스트 완료
     * 방 나가기
     * @param roomId    현재 방 위치
     * @param authentication    인증 정보
     * @return apiResponse      응답(HttpStatus.OK,"퇴장 성공")
     */
    @PostMapping("/quit/{roomId}")
    public ApiResponse<Void> quitRoom(@PathVariable Long roomId, Authentication authentication) {
        userRoomService.deleteUserFromRoom(roomId,authentication);
        return new ApiResponse<>(HttpStatus.OK,"퇴장 성공",null);
    }

    /**
     * 선택한 방 입장
     * @param roomId    선택한 방 번호
     * @param authentication    현재 유저 정보
     * @return ApiResponse (HttpStatus.OK, String.format("%d번방 입장 성공",roomId), null)
     */
    @PostMapping("/enter/{roomId}")
    public ApiResponse<?> enterRoom(@PathVariable Long roomId,Authentication authentication) {
        roomService.enterRoom(roomId,authentication);
        return new ApiResponse<>(HttpStatus.OK, String.format("%d번방 입장 성공",roomId), null);
    }

    /**
     * 게임 시작 전 방 상태를 게임 중으로 바꿈
     * @param roomId    현재 방 번호
     * @return ApiResponse (HttpStatus.OK,String.format("%d번방 게임 시작",roomId),null)
     */
    @PostMapping("/start/{roomId}")
    public ApiResponse<Void> startGame(@PathVariable Long roomId) {
        roomService.startGame(roomId);
        return new ApiResponse<>(HttpStatus.OK,String.format("%d번방 게임 시작",roomId),null);
    }

}
