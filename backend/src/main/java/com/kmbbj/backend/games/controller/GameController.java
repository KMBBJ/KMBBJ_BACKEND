package com.kmbbj.backend.games.controller;

import com.kmbbj.backend.games.dto.CurrentRoundDTO;
import com.kmbbj.backend.games.dto.GameStatusDTO;
import com.kmbbj.backend.games.entity.Game;
import com.kmbbj.backend.games.entity.RoundResult;
import com.kmbbj.backend.games.service.game.GameService;
import com.kmbbj.backend.games.service.round.RoundResultService;
import com.kmbbj.backend.global.config.exception.ApiException;
import com.kmbbj.backend.global.config.reponse.CustomResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    @GetMapping("/status/{gameId}")
    public ResponseEntity<CustomResponse<GameStatusDTO>> getGameStatus(@PathVariable Long gameId) {
        GameStatusDTO status = gameService.getGameStatus(gameId);
        CustomResponse<GameStatusDTO> response = new CustomResponse<>(HttpStatus.OK, "게임 상태 조회 성공", status);
        return ResponseEntity.ok().body(response);
    }


    /** 게임 ID를 받아서 게임 종료
     *
     * @param gameId 종료할 게임
     * @param authentication 인증 정보
     * @return 게임 종료 성공 여부
     */
    @PostMapping("/end/{gameId}")
    public ResponseEntity<CustomResponse<String>> endGame(@PathVariable Long gameId, Authentication authentication) {
        gameService.endGame(gameId);
        CustomResponse<String> response = new CustomResponse<>(HttpStatus.OK, "게임 종료 성공", "게임이 성공적으로 종료되었습니다.");
        return ResponseEntity.ok().body(response);
    }

    /** 게임 현재 라운드 조회
     *
     * @param gameId 라운드 조회
     * @return 현재 라운드 정보 조회
     */
    @GetMapping("/{gameId}/current-round")
    public ResponseEntity<CustomResponse<CurrentRoundDTO>> getCurrentRound(@PathVariable Long gameId) {
        CurrentRoundDTO currentRound = gameService.getCurrentRound(gameId);
        return ResponseEntity.ok(new CustomResponse<>(HttpStatus.OK, "현재 라운드 조회 성공", currentRound));
    }


    /** 게임의 라운드 결과 조회
     *
     * @param gameId 라운드 결과 조회
     * @return 조회된 라운드 결과 리스트
     */
    @GetMapping("/{gameId}/round-results")
    public ResponseEntity<CustomResponse<List<RoundResult>>> getRoundResults(@PathVariable Long gameId) {
        List<RoundResult> results = roundResultService.getRoundResultsForGame(gameId);
        return ResponseEntity.ok(new CustomResponse<>(HttpStatus.OK, "라운드 결과 조회 성공", results));
    }



}
