package com.kmbbj.backend.games.controller;



import com.kmbbj.backend.games.dto.RoundRankingSimpleDTO;
import com.kmbbj.backend.games.dto.RoundResultDTO;
import com.kmbbj.backend.games.service.round.RoundService;
import com.kmbbj.backend.games.service.roundresult.RoundResultService;
import com.kmbbj.backend.global.config.reponse.CustomResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/rounds")
@RequiredArgsConstructor
@Tag(name = "Round", description = "라운드 API")
public class RoundController {

    private final RoundService roundService;
    private final RoundResultService roundResultService;


    /** 게임 라운드별 순위를 조회
     *
     * @param encryptedGameId 암호화된 게임 ID
     * @return 각 라운드별 순위 리스트
     */
    @GetMapping("/{encryptedGameId}/rankings")
    @Operation(summary = "게임 라운드별 순위 조회", description = "암호화된 게임 ID를 통해 해당 게임의 모든 라운드별 순위를 조회")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "라운드 순위 조회 성공"),
            @ApiResponse(responseCode = "404", description = "게임을 찾을 수 없음")
    })
    public ResponseEntity<CustomResponse<List<List<RoundRankingSimpleDTO>>>> getRoundRankings(@PathVariable String encryptedGameId) {
        List<List<RoundRankingSimpleDTO>> rankings = roundService.getRoundRankingsForGame(encryptedGameId);
        return ResponseEntity.ok(new CustomResponse<>(HttpStatus.OK, "라운드 순위 조회 성공", rankings));
    }

    /**
     * 게임의 모든 라운드 결과 조회
     *
     * @param encryptedGameId 암호화된 게임 ID
     * @return 조회된 라운드 결과 DTO 리스트
     */
    @GetMapping("/{encryptedGameId}/round-results")
    @Operation(summary = "라운드 결과 조회", description = "게임 ID를 받아 모든 라운드 결과를 조회함")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "라운드 결과 조회 성공"),
            @ApiResponse(responseCode = "404", description = "게임을 찾을 수 없음")
    })
    public ResponseEntity<CustomResponse<List<RoundResultDTO>>> getRoundResults(@PathVariable String encryptedGameId) {
        List<RoundResultDTO> results = roundResultService.getCompletedRoundResultsForGame(encryptedGameId);
        return ResponseEntity.ok(new CustomResponse<>(HttpStatus.OK, "라운드 결과 조회 성공", results));
    }
}
