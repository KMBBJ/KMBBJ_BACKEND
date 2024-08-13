package com.kmbbj.backend.games.service.game;


import com.kmbbj.backend.games.dto.CurrentRoundDTO;
import com.kmbbj.backend.games.dto.GameStatusDTO;
import com.kmbbj.backend.games.dto.RoundResultDTO;
import com.kmbbj.backend.games.entity.Game;
import com.kmbbj.backend.games.entity.Round;
import com.kmbbj.backend.games.enums.GameStatus;
import com.kmbbj.backend.games.repository.GameRepository;
import com.kmbbj.backend.games.repository.RoundRepository;
import com.kmbbj.backend.games.repository.RoundResultRepository;
import com.kmbbj.backend.games.service.round.RoundResultService;
import com.kmbbj.backend.games.service.round.RoundService;
import com.kmbbj.backend.global.config.exception.ApiException;
import com.kmbbj.backend.global.config.exception.ExceptionEnum;
import com.kmbbj.backend.matching.entity.Room;
import com.kmbbj.backend.matching.repository.RoomRepository;
import com.kmbbj.backend.matching.service.room.RoomService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
@RequiredArgsConstructor
public class GameServiceImpl implements GameService {

    private final GameRepository gameRepository;
    private final RoomService roomService;
    private final RoomRepository roomRepository;
    private final RoundRepository roundRepository;
    private final RoundResultService roundResultService;
    private final RoundService roundService;

    @Value("${game.round.duration.minutes:1440}")
    private int roundDurationMinutes;


    /**
     * 방 ID로 게임 시작
     *
     * @param roomId 게임 시작 할 ID
     * @return 시작된 게임 객체
     * @throws ExceptionEnum 공통 예외
     */
    @Override
    @Transactional
    public Game startGame(Long roomId) {
        Room room = roomService.findById(roomId); // 방 ID 조회함

        // 시작된 상태 확인
        if (room.getIsStarted()) {
            throw new ApiException(ExceptionEnum.GAME_ALREADY_STARTED);
        }

        Game game = new Game();
        game = gameRepository.save(game);

        Round round = new Round();
        round.setGame(game);
        round.setRoundNumber(1); //  라운드 시작
        round.setDurationMinutes(roundDurationMinutes);
        roundRepository.save(round);


        // 비동기적으로 게임을 시작함
        startGameAsync(game, room.getEnd());


        return game;
    }


    /**
     * 비동기적으로 게임 시작
     *
     * @param game           게임 객체
     * @param endRoundNumber 게임이 종료될 라운드 번호
     */
    @Async
    public void startGameAsync(Game game, int endRoundNumber) {
        // 마지막 라운드 도달하면 새 라운드를 시작하는 루프
        while (!roundService.isLastRound(game, endRoundNumber)) {
            // 새 라운드 바로 시작함
            roundService.startNewRound(game);
        }

        // 마지막 라운드 도달하면 게임 종료 됨
        endGame(game.getGameId());
    }


    /**
     * 게임 종료
     *
     * @param roomId
     */
    @Override
    @Transactional
    public void endGame(Long roomId) {
        Room room = roomService.findById(roomId);

        Game game = gameRepository.findById(room.getRoomId())
                .orElseThrow(() -> new ApiException(ExceptionEnum.GAME_NOT_FOUND));

        Round latestRound = roundRepository.findFirstByGameOrderByRoundNumberDesc(game)
                .orElseThrow(() -> new ApiException(ExceptionEnum.ROUND_NOT_FOUND));

        // 게임 종료 조건 확인
        if (latestRound.getRoundNumber() >= room.getEnd()) {
            game.setGameStatus(GameStatus.COMPLETED); // 게임 완료
            gameRepository.save(game);




            // 방 상태 업데이트
            room.setIsStarted(false);
            room.setIsDeleted(true);
            roomRepository.save(room);
        }
    }


    /**
     * 게임 상태 조회
     *
     * @param gameId
     * @return 게임 상태 DTO
     */
    @Override
    public GameStatusDTO getGameStatus(Long gameId) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new ApiException(ExceptionEnum.GAME_NOT_FOUND));

        GameStatusDTO statusDTO = new GameStatusDTO();
        statusDTO.setGameId(gameId);
        statusDTO.setStatus(game.getGameStatus());

        return statusDTO;
    }

    @Override
    public CurrentRoundDTO getCurrentRound(Long gameId) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new ApiException(ExceptionEnum.GAME_NOT_FOUND));

        Round currentRound = roundRepository.findFirstByGameOrderByRoundNumberDesc(game)
                .orElseThrow(() -> new ApiException(ExceptionEnum.ROUND_NOT_FOUND));

        CurrentRoundDTO dto = new CurrentRoundDTO();
        dto.setGameId(gameId);
        dto.setCurrentRoundNumber(currentRound.getRoundNumber());
        dto.setGameStatus(game.getGameStatus().toString());

        return dto;
    }
}



