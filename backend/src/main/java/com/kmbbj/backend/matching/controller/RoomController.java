package com.kmbbj.backend.matching.controller;


import com.kmbbj.backend.auth.entity.Authority;
import com.kmbbj.backend.auth.entity.User;
import com.kmbbj.backend.global.config.reponse.CustomResponse;
import com.kmbbj.backend.global.config.security.FindUserBySecurity;
import com.kmbbj.backend.matching.dto.*;
import com.kmbbj.backend.matching.entity.Room;
import com.kmbbj.backend.matching.service.room.RoomService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/room")
@RequiredArgsConstructor
@Tag(name = "Room", description = "매칭 룸 API")
public class RoomController {

    private final RoomService roomService;
    private final FindUserBySecurity findUserBySecurity;

    /**
     * 방 생성
     * @param createRoomDTO     방 생성시 필요한 정보(제목, 시작 시드머니, 마지막 라운드,
     *                                              생성일자, 삭제여부, 시작여부, 시작 딜레이)
     * @return CustomResponse      응답 (HttpStatus.OK, "방 생성 성공" ,room)
     */
    @PostMapping("/create")
    @Operation(summary = "방 생성", description = "매칭할 수 있는 방을 생성")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "방 생성 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청입니다."),
            @ApiResponse(responseCode = "404", description = "유저를 찾지 못했습니다."),
            @ApiResponse(responseCode = "404", description = "자산을 찾지 못했습니다."),
            @ApiResponse(responseCode = "409", description = "이미 다른 방에 입장해 있습니다.")
    })
    public CustomResponse<Room> createRoom(@RequestBody CreateRoomDTO createRoomDTO) {
        User user = findUserBySecurity.getCurrentUser();
        Room room = roomService.createRoom(createRoomDTO,user);

        return new CustomResponse<>(HttpStatus.OK,String.format("%d번방 생성 성공",room.getRoomId()), room);
    }

    @PostMapping("/edit/{roomId}")
    @Operation(summary = "방 수정", description = "이미 만들어진 방을 방장이 수정")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "방 수정 성공"),
            @ApiResponse(responseCode = "404", description = "해당 방에 아무도 없습니다."),
            @ApiResponse(responseCode = "403", description = "권한이 없습니다."),
            @ApiResponse(responseCode = "409", description = "방 조건에 맞지 않는 유저가 있습니다."),
            @ApiResponse(responseCode = "404", description = "해당 방 입장기록이 없습니다.")

    })
    public CustomResponse<Void> editRoom(@PathVariable(name = "roomId") Long roomId,@RequestBody EditRoomDTO editRoomDTO) {
        roomService.editRoom(roomId,editRoomDTO);
        return new CustomResponse<>(HttpStatus.OK, String.format("%d번방 수정 성공", roomId), null);
    }


    /**
     * 방 삭제
     * @param roomId    삭제할 방 번호
     * @return CustomResponse  응답 (HttpStatus.OK, "{roomId}방 삭제 완료")
     */
    // 관리자가 쓰는거
    @DeleteMapping("/delete/{roomId}")
    @Operation(summary = "방 삭제", description = "사용자가 없거나 게임이 끝난 방을 삭제")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "방 삭제 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청입니다."),
            @ApiResponse(responseCode = "403", description = "접근 권한이 없습니다."),
            @ApiResponse(responseCode = "404", description = "방을 찾을 수 없습니다.")
    })
    public CustomResponse<Room> deleteRoom(@PathVariable Long roomId) {
        roomService.deleteRoom(roomId);
        return new CustomResponse<>(HttpStatus.OK,String.format("%d번 방 삭제 완료",roomId),null);
    }

    /** TODO
     * 검색
     * @param searchingRoomDTO      검색 기능 필요한 정보 (페이지, 찾는 방 제목)
     * @return CustomResponse      응답 (HttpStatus.OK, "{title}로 검색 성공", rooms)
     */
    // 검색한 것 중에 정렬 되도록 추가하기
    @PostMapping("/searching")
    @Operation(summary = "방 검색", description = "방 제목으로 검색")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "검색 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청입니다."),
            @ApiResponse(responseCode = "404", description = "검색 결과가 없습니다.")
    })
    public CustomResponse<Page<RoomListDTO>> searchRooms(@RequestBody SearchingRoomDTO searchingRoomDTO) {
        Page<RoomListDTO> rooms = roomService.searchRoomsByTitle(searchingRoomDTO);
        return new CustomResponse<>(HttpStatus.OK,searchingRoomDTO.getTitle() + "로 검색 성공", rooms);
    }

    /**
     * 방 리스트
     * @param sortConditionDTO     정렬 기능 필요한 정보 (삭제 여부, 시작 여부, 페이지, 정렬 필드명, 정렬 기준)
     * @return CustomResponse      응답 (HttpStatus.OK,"{sortField,sortDirection}기준으로 정렬 성공",rooms)
     */
    @PostMapping("/list")
    @Operation(summary = "방 목록", description = "방 목록 불러오기")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "방 목록 불러오기 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청입니다.")
    })
    public CustomResponse<Page<RoomListDTO>> matchingRoomList(@RequestBody SortConditionDTO sortConditionDTO) {
        // 여기에 들어가는 SortedRoomDTO 는 모두 초깃값
        Page<RoomListDTO> rooms = roomService.findAll(sortConditionDTO);
        return new CustomResponse<>(HttpStatus.OK,String.format("%s, %s 기준으로 정렬 성공", sortConditionDTO.getSortField(), sortConditionDTO.getSortDirection()),rooms);
    }

    /**
     * 선택 정렬
     * @param sortConditionDTO     정렬 기능 필요한 정보 (삭제 여부, 시작 여부, 페이지, 정렬 필드명, 정렬 기준)
     * @return CustomResponse      응답 (HttpStatus.OK,"{sortField,sortDirection}기준으로 정렬 성공",rooms)
     */
    @PostMapping("/sorted")
    @Operation(summary = "방 목록 정렬", description = "사용자 지정 요소로 정렬된 방 목록")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "{field}, {direction} 기준으로 정렬 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청입니다."),
    })
    public CustomResponse<Page<RoomListDTO>> sortedRooms(@RequestBody SortConditionDTO sortConditionDTO) {
        // 여기 들어가는 SortedRoomDTO 는 isDeleted,isStarted 제외 모두 사용자 지정
        Page<RoomListDTO> rooms = roomService.findAll(sortConditionDTO);
        return new CustomResponse<>(HttpStatus.OK,String.format("%s, %s 기준으로 정렬 성공", sortConditionDTO.getSortField(), sortConditionDTO.getSortDirection()),rooms);
    }

    /**
     * 방 나가기
     * @param roomId    현재 방 위치
     * @return CustomResponse      응답(HttpStatus.OK,"퇴장 성공")
     */
    @PostMapping("/quit/{roomId}")
    @Operation(summary = "방 나가기", description = "현재 있는 방에서 퇴장")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "{roomId}번 방 퇴장 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청입니다."),
            @ApiResponse(responseCode = "404", description = "방을 찾을 수 없음")
    })
    public CustomResponse<Void> quitRoom(@PathVariable Long roomId) {
        roomService.quitRoom(roomId);
        return new CustomResponse<>(HttpStatus.OK,String.format("%d번 방 퇴장 성공",roomId),null);
    }

    /**
     * 선택한 방 입장
     * @param roomId    선택한 방 번호
     * @return CustomResponse (HttpStatus.OK, String.format("%d번방 입장 성공",roomId), userRooms)
     */
    @PostMapping("/enter/{roomId}")
    @Operation(summary = "방 입장", description = "사용자가 선택한 방에 입장")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "{roomId}번 방 입장 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청입니다."),
            @ApiResponse(responseCode = "403", description = "방 조건에 알맞는 자산이 부족합니다."),
            @ApiResponse(responseCode = "404", description = "방을 찾을 수 없습니다."),
            @ApiResponse(responseCode = "409", description = "이미 다른 방에 들어가있습니다."),
            @ApiResponse(responseCode = "409", description = "방이 가득 찼습니다.")
    })
    public CustomResponse<EnterRoomDTO> enterRoom(@PathVariable Long roomId) {
        User currentUser = findUserBySecurity.getCurrentUser();
        roomService.enterRoom(currentUser,roomId);
        Room room = roomService.findById(roomId);
        EnterRoomDTO enterRoomDto = roomService.getEnterRoomDto(room);
        return new CustomResponse<>(HttpStatus.OK, String.format("%d번 방 입장 성공",roomId), enterRoomDto);
    }

    /** TODO
     * 게임 시작 전 방 상태를 게임 중으로 바꿈
     * @param roomId    현재 방 번호
     * @return CustomResponse (HttpStatus.OK,String.format("%d번방 게임 시작",roomId),null)
     */
    @PostMapping("/start/{roomId}")
    @Operation(summary = "게임 시작", description = "딜레이 시간이 지난 방 게임 시작")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "{roomId}번 방 게임 시작 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청입니다."),
            @ApiResponse(responseCode = "404", description = "방을 찾을 수 없음")
    })
    public CustomResponse<Integer> startGame(@PathVariable Long roomId) {
        int delay = roomService.beforeStart(roomId);
        return new CustomResponse<>(HttpStatus.OK,String.format("%d번방 게임 시작 성공",roomId),delay);
    }

}
