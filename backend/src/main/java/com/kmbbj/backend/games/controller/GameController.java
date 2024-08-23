package com.kmbbj.backend.games.controller;

import com.kmbbj.backend.games.dto.CurrentRoundDTO;
import com.kmbbj.backend.games.dto.GameStatusDTO;
import com.kmbbj.backend.games.entity.Game;
import com.kmbbj.backend.games.entity.RoundResult;
import com.kmbbj.backend.games.service.game.GameService;
import com.kmbbj.backend.games.service.round.RoundResultService;
import com.kmbbj.backend.global.config.reponse.CustomResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/games")
@RequiredArgsConstructor
public class GameController {

    private final GameService gameService;
    private final RoundResultService roundResultService;

    /** 방 ID 가져와서 게임 시작
     *
     * @param roomId 게임을 시작할 방 ID
     * @param authentication 인증 정보
     * @return 게임 시작 성공 여부
     */
    @PostMapping("/start/{roomId}")
    public ResponseEntity<CustomResponse<Game>> startGame(@PathVariable Long roomId, Authentication authentication) {
        Game game = gameService.startGame(roomId);
        CustomResponse<Game> response = new CustomResponse<>(HttpStatus.OK, "게임 시작 성공", game);
        return ResponseEntity.ok().body(response);
    }

    /** 게임 ID 가져와서 게임의 현재 상태 조회
     *
     * @param gameId 게임의 상태 조회
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


    /** 게임 ID를 받아서 게임 종료
     *
     * @param gameId 종료할 게임
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
        return new CustomResponse<>(HttpStatus.OK, "게임 종료 성공", "게임이 성공적으로 종료되었습니다.");

    }

    /** 게임 현재 라운드 조회
     *
     * @param gameId 라운드 조회
     * @return 현재 라운드 정보 조회
     */
    @GetMapping("/{encryptedGameId}/current-round")
    @Operation(summary = "현재 라운드 조회", description = "게임 ID를 받아 현재 라운드 정보를 조회함")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "현재 라운드 조회 성공"),
            @ApiResponse(responseCode = "404", description = "게임을 찾을 수 없음")
    })
    public CustomResponse<CurrentRoundDTO> getCurrentRound(@PathVariable String encryptedGameId) {
        gameService.isUserAuthorizedForGame(encryptedGameId);
        CurrentRoundDTO currentRound = gameService.getCurrentRound(encryptedGameId);
        return new CustomResponse<>(HttpStatus.OK, "현재 라운드 조회 성공", currentRound);

    /** 게임의 라운드 결과 조회
     *
     * @param gameId 라운드 결과 조회
     * @return 조회된 라운드 결과 리스트
     */
    @GetMapping("/{encryptedGameId}/round-results")
    @Operation(summary = "라운드 결과 조회", description = "게임 ID를 받아 모든 라운드 결과를 조회함")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "라운드 결과 조회 성공"),
            @ApiResponse(responseCode = "404", description = "게임을 찾을 수 없음")
    })
    public CustomResponse<List<RoundResult>> getRoundResults(@PathVariable String encryptedGameId) {
        gameService.isUserAuthorizedForGame(encryptedGameId);
        List<RoundResult> results = roundResultService.getRoundResultsForGameId(encryptedGameId);
        return new CustomResponse<>(HttpStatus.OK, "라운드 결과 조회 성공", results);

}
