package com.kmbbj.backend.matching.controller;


import com.kmbbj.backend.auth.entity.User;
import com.kmbbj.backend.global.config.reponse.CustomResponse;
import com.kmbbj.backend.global.config.security.FindUserBySecurity;
import com.kmbbj.backend.matching.dto.CreateRoomDTO;
import com.kmbbj.backend.matching.dto.RoomListDTO;
import com.kmbbj.backend.matching.dto.SearchingRoomDTO;
import com.kmbbj.backend.matching.dto.SortConditionDTO;
import com.kmbbj.backend.matching.entity.Room;
import com.kmbbj.backend.matching.service.room.RoomService;
import com.kmbbj.backend.matching.service.userroom.UserRoomService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/room")
@RequiredArgsConstructor
public class RoomController {

    private final RoomService roomService;
    private final FindUserBySecurity findUserBySecurity;

    /**
     * 방 생성
     * @param createRoomDTO     방 생성시 필요한 정보(제목, 시작 시드머니, 마지막 라운드,
     *                                              생성일자, 삭제여부, 시작여부, 시작 딜레이)
     * @return apiResponse      응답 (HttpStatus.OK, "방 생성 성공" ,room)
     */
    @PostMapping("/create")
    @Operation(summary = "방 생성", description = "매칭할 수 있는 방을 생성")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "방 생성 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "404", description = "사용자 찾을 수 없음")
    })
    public CustomResponse<Room> createRoom(@RequestBody CreateRoomDTO createRoomDTO) {
        User user = findUserBySecurity.getCurrentUser();
        Room room = roomService.createRoom(createRoomDTO,user);

        return new CustomResponse<>(HttpStatus.OK,String.format("%d번방 생성 성공",room.getRoomId()), room);
    }


    /** TODO
     * 방 삭제
     * @param roomId    삭제할 방 번호
     * @return apiResponse  응답 (HttpStatus.OK, "{roomId}방 삭제 완료")
     */
    // 관리자가 쓰는거
    @DeleteMapping("/delete/{roomId}")
    @Operation(summary = "방 삭제", description = "사용자가 없거나 게임이 끝난 방을 삭제")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "방 삭제 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "404", description = "방을 찾을 수 없음")
    })
    public CustomResponse<Room> deleteRoom(@PathVariable Long roomId) {
        roomService.deleteRoom(roomId);
        return new CustomResponse<>(HttpStatus.OK,String.format("%d번 방 삭제 완료",roomId),null);
    }

    /**
     * 검색
     * @param searchingRoomDTO      검색 기능 필요한 정보 (페이지, 찾는 방 제목)
     * @return apiResponse      응답 (HttpStatus.OK, "{title}로 검색 성공", rooms)
     */
    @PostMapping("/searching")
    @Operation(summary = "방 검색", description = "방 제목으로 검색")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "검색 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "404", description = "검색 결과 없음")
    })
    public CustomResponse<Page<RoomListDTO>> searchRooms(@RequestBody SearchingRoomDTO searchingRoomDTO) {
        Page<RoomListDTO> rooms = roomService.searchRoomsByTitle(searchingRoomDTO);
        return new CustomResponse<>(HttpStatus.OK,searchingRoomDTO.getTitle() + "로 검색 성공", rooms);
    }

    /**
     * 방 리스트
     * @param sortConditionDTO     정렬 기능 필요한 정보 (삭제 여부, 시작 여부, 페이지, 정렬 필드명, 정렬 기준)
     * @return apiResponse      응답 (HttpStatus.OK,"{sortField,sortDirection}기준으로 정렬 성공",rooms)
     */
    @PostMapping("/list")
    @Operation(summary = "방 목록", description = "방 목록 불러오기")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "방 목록 불러오기 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청")
    })
    public CustomResponse<Page<RoomListDTO>> matchingRoomList(@RequestBody SortConditionDTO sortConditionDTO) {
        // 여기에 들어가는 SortedRoomDTO 는 모두 초깃값
        Page<RoomListDTO> rooms = roomService.findAll(sortConditionDTO);
        return new CustomResponse<>(HttpStatus.OK,String.format("%s, %s 기준으로 정렬 성공", sortConditionDTO.getSortField(), sortConditionDTO.getSortDirection()),rooms);
    }

    /**
     * 선택 정렬
     * @param sortConditionDTO     정렬 기능 필요한 정보 (삭제 여부, 시작 여부, 페이지, 정렬 필드명, 정렬 기준)
     * @return apiResponse      응답 (HttpStatus.OK,"{sortField,sortDirection}기준으로 정렬 성공",rooms)
     */
    @PostMapping("/sorted")
    @Operation(summary = "방 목록 정렬", description = "사용자 지정 요소로 정렬된 방 목록")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "{field}, {direction} 기준으로 정렬 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
    })
    public CustomResponse<Page<RoomListDTO>> sortedRooms(@RequestBody SortConditionDTO sortConditionDTO) {
        // 여기 들어가는 SortedRoomDTO 는 isDeleted,isStarted 제외 모두 사용자 지정
        Page<RoomListDTO> rooms = roomService.findAll(sortConditionDTO);
        return new CustomResponse<>(HttpStatus.OK,String.format("%s, %s 기준으로 정렬 성공", sortConditionDTO.getSortField(), sortConditionDTO.getSortDirection()),rooms);
    }

    /**
     * 방 나가기
     * @param roomId    현재 방 위치
     * @return apiResponse      응답(HttpStatus.OK,"퇴장 성공")
     */
    @PostMapping("/quit/{roomId}")
    @Operation(summary = "방 나가기", description = "현재 있는 방에서 퇴장")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "{roomId}번 방 퇴장 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "404", description = "방을 찾을 수 없음")
    })
    public CustomResponse<Void> quitRoom(@PathVariable Long roomId) {
        roomService.quitRoom(roomId);
        return new CustomResponse<>(HttpStatus.OK,String.format("%d번 방 퇴장 성공",roomId),null);
    }

    /**
     * 선택한 방 입장
     * @param roomId    선택한 방 번호
     * @return ApiResponse (HttpStatus.OK, String.format("%d번방 입장 성공",roomId), null)
     */
    @PostMapping("/enter/{roomId}")
    @Operation(summary = "방 입장", description = "사용자가 선택한 방에 입장")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "{roomId}번 방 입장 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "404", description = "방을 찾을 수 없음")
    })
    public CustomResponse<?> enterRoom(@PathVariable Long roomId) {
        roomService.enterRoom(roomId);
        return new CustomResponse<>(HttpStatus.OK, String.format("%d번 방 입장 성공",roomId), null);
    }

    /** TODO
     * 게임 시작 전 방 상태를 게임 중으로 바꿈
     * @param roomId    현재 방 번호
     * @return ApiResponse (HttpStatus.OK,String.format("%d번방 게임 시작",roomId),null)
     */
    @PostMapping("/start/{roomId}")
    @Operation(summary = "게임 시작", description = "딜레이 시간이 지난 방 게임 시작")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "{roomId}번 방 게임 시작 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "404", description = "방을 찾을 수 없음")
    })
    public CustomResponse<Void> startGame(@PathVariable Long roomId) {
        roomService.startGame(roomId);
        return new CustomResponse<>(HttpStatus.OK,String.format("%d번방 게임 시작 성공",roomId),null);
    }

}
