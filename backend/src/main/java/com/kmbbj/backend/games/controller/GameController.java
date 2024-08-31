package com.kmbbj.backend.games.controller;

import com.kmbbj.backend.games.dto.*;
import com.kmbbj.backend.games.service.game.GameService;
import com.kmbbj.backend.games.service.gamebalance.GameBalanceService;
import com.kmbbj.backend.games.service.gameresult.GameResultService;
import com.kmbbj.backend.global.config.reponse.CustomResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/games")
@RequiredArgsConstructor
@Tag(name = "Game", description = "게임 API")
public class GameController {

    private final GameService gameService;
    private final GameBalanceService gameBalanceService;
    private final GameResultService gameResultService;

    /** 방 ID 가져와서 게임 시작
     *
     * @param roomId 게임을 시작할 방 ID
     * @param authentication 인증 정보
     * @return 게임 시작 성공 여부
     */
    @PostMapping("/start/{roomId}")
    @Operation(summary = "게임 시작", description = "방 ID를 받아 게임 시작")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "게임 시작 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "404", description = "방을 찾을 수 없음")
    })
    public CustomResponse<GameStartDTO> startGame(@PathVariable Long roomId, Authentication authentication) {
        GameStartDTO gameStart = gameService.startGame(roomId);
        return new CustomResponse<>(HttpStatus.OK, "게임 시작 성공", gameStart);
    }

    /** 게임 ID 가져와서 게임의 현재 상태 조회
     *
     * @param encryptedGameId 게임의 상태 조회
     * @return 게임 상태를 담은 DTO 객체
     */
    @GetMapping("/status/{encryptedGameId}")
    @Operation(summary = "게임 상태 조회", description = "암호화된 게임 ID를 받아 현재 게임 상태 조회")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "게임 상태 조회 성공"),
            @ApiResponse(responseCode = "404", description = "게임을 찾을 수 없음")
    })
    public CustomResponse<GameStatusDTO> getGameStatus(@PathVariable String encryptedGameId) {
        gameService.isUserAuthorizedForGame(encryptedGameId);
        GameStatusDTO status = gameService.getGameStatus(encryptedGameId);
        return new CustomResponse<>(HttpStatus.OK, "게임 상태 조회 성공", status);
    }


    /** 게임 ID를 받아서 게임 종료
     *
     * @param encryptedGameId 종료할 게임
     * @param authentication 인증 정보
     * @return 게임 종료 성공 여부
     */
    @PostMapping("/end/{encryptedGameId}")
    @Operation(summary = "게임 종료", description = "암호화된 게임 ID를 받아 현재 게임 상태 조회")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "게임 종료 성공"),
            @ApiResponse(responseCode = "404", description = "게임을 찾을 수 없음")
    })
    public CustomResponse<String> endGame(@PathVariable String encryptedGameId, Authentication authentication) {
        gameService.isUserAuthorizedForGame(encryptedGameId);
        gameService.endGame(encryptedGameId);
        return new CustomResponse<>(HttpStatus.OK, "게임 종료 성공", null);
    }

    /** 사용자 게임 잔액 조회
     *
     * @param userId 사용자의 ID
     * @return 사용자의 게임 잔액 정보
     */
    @GetMapping("/balance/{userId}")
    @Operation(summary = "사용자 게임 잔액 조회", description = "사용자 ID를 받아 해당 사용자의 게임 잔액 정보를 조회")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "게임 잔액 조회 성공"),
            @ApiResponse(responseCode = "404", description = "게임 잔액을 찾을 수 없음")
    })
    public CustomResponse<GameBalanceDTO> getGameBalance(@PathVariable Long userId) {
        GameBalanceDTO gameBalanceDTO = gameBalanceService.getGameBalance(userId);
        return new CustomResponse<>(HttpStatus.OK, "게임 잔액 조회 성공", gameBalanceDTO);
    }

    /** 특정 게임의 결과 조회
     *
     * @param encryptedGameId 암호화된 게임 ID
     * @return 게임 결과
     */
    @GetMapping("/{encryptedGameId}/results")
    @Operation(summary = "게임 결과 조회", description = "암호화된 게임 ID를 통해 해당 게임의 결과를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "게임 결과 조회 성공"),
            @ApiResponse(responseCode = "404", description = "게임을 찾을 수 없음")
    })
    public CustomResponse<List<GameResultDTO>> getGameResults(@PathVariable String encryptedGameId) {
        List<GameResultDTO> gameResults = gameResultService.getGameResults(encryptedGameId);
        return new CustomResponse<>(HttpStatus.OK, "게임 결과 조회 성공", gameResults);
    }


}
