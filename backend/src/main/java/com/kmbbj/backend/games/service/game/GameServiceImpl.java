package com.kmbbj.backend.games.service.game;


import com.kmbbj.backend.games.dto.CurrentRoundDTO;
import com.kmbbj.backend.games.dto.GameStatusDTO;
import com.kmbbj.backend.games.dto.RoundResultDTO;
import com.kmbbj.backend.games.entity.Game;
import com.kmbbj.backend.games.entity.Round;
import com.kmbbj.backend.games.entity.RoundResult;
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



    /**
     * 방 ID로 게임 시작
     *
     * 방 정보 조회 -> 게임 시작 확인여부 -> 새 게임 객체 만들고 저장
     * 첫 라운드 생성 하고 저장 -> 비동기적으로 게임 시작
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

        // 새 게임 생성 & 저장
        Game game = new Game();
        game = gameRepository.save(game);

        // 첫 라운드 생성 & 저장
        Round round = new Round();
        round.setGame(game);
        round.setRoundNumber(1); //  라운드 시작
        round.setDurationMinutes(Integer.parseInt(System.getenv("GAME_ROUND_DURATION_MINUTES")));
        roundRepository.save(round);


        // 비동기적으로 게임을 시작함
        startGameAsync(game, room.getEnd());


        return game;
    }


    /** 비동기적으로 게임 시작
     *  1. 마지막 라운드 도달할 떄까지 새 라운드 계속 시작함
     *  2. 마지막 라운드에 도달하면 게임 종료
     *
     * @param game           게임 객체
     * @param endRoundNumber 게임이 종료될 라운드 번호
     */
    @Async
    public void startGameAsync(Game game, int endRoundNumber) {
        // 마지막 라운드 까지 반복함
        while (!roundService.isLastRound(game, endRoundNumber)) {
            // 새 라운드 바로 시작함
            roundService.startNewRound(game);
        }

        // 마지막 라운드 도달하면 게임 종료 됨
        endGame(game.getGameId());
    }


    /** 게임 종료
     *
     * 1. 게임과 최신 라운드 정보 조회 -> 게임 종료 조건 확인함
     * 2. 게임 상태를 COMPLETED 가져오고 -> 방 상태 업데이트 함
     *
     * @param roomId
     */
    @Override
    @Transactional
    public void endGame(Long roomId) {
        // 방 & 게임 정보 조회
        Room room = roomService.findById(roomId);

        Game game = gameRepository.findById(room.getRoomId())
                .orElseThrow(() -> new ApiException(ExceptionEnum.GAME_NOT_FOUND));

        Round latestRound = roundRepository.findFirstByGameOrderByRoundNumberDesc(game)
                .orElseThrow(() -> new ApiException(ExceptionEnum.ROUND_NOT_FOUND));

        // 게임 종료 조건 확인
        if (latestRound.getRoundNumber() >= room.getEnd()) {
            game.setGameStatus(GameStatus.COMPLETED); // 게임 완료
            gameRepository.save(game);

            // 모든 라운드 결과 가져오기


            // 여기에 최종 결과 구현 할것 (미구현)


            // 방 상태 업데이트
            room.setIsStarted(false);
            room.setIsDeleted(true);
            roomRepository.save(room);
        }
    }


    /** 게임 상태 조회
     *
     *  게임 정보 조회하고 -> 게임 상태 정보를 DTO 담아 반환
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

    /** 현재 진행 중인 라운드 정보 조회
     *
     * 1. 게임 정보 조회함
     * 2. 최신 라운드 정보 조회함
     * 3. 현재 라운드 정보를 DTO 담아 반환
     *
     * @param gameId 조회할 게임 ID
     * @return 현재 라운드 DTO
     */
    @Override
    public CurrentRoundDTO getCurrentRound(Long gameId) {
        // 게임 정보 조회
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new ApiException(ExceptionEnum.GAME_NOT_FOUND));

        // 현재 라운드 정보 조회
        Round currentRound = roundRepository.findFirstByGameOrderByRoundNumberDesc(game)
                .orElseThrow(() -> new ApiException(ExceptionEnum.ROUND_NOT_FOUND));

        // 현재 라운드 DTO 생성 & 반환
        CurrentRoundDTO dto = new CurrentRoundDTO();
        dto.setGameId(gameId);
        dto.setCurrentRoundNumber(currentRound.getRoundNumber());
        dto.setGameStatus(game.getGameStatus().toString());

        return dto;
    }


    /** 사용자 해당 게임에 접근할 수 있는 권한
     *
     * @param encryptedGameId 게임 암호화된 ID
     * @return 사용자 해당 접근
     * @throws ApiException 공통 예외처리
     */
    @Override
    public boolean isUserAuthorizedForGame(String encryptedGameId) {
        UserRoom currentUserRoom = userRoomService.findCurrentRoom();

        // 사용자가 방에 참여하지 않으면 예외 발생
        if (currentUserRoom == null || !currentUserRoom.getIsPlayed()) {
            throw new ApiException(ExceptionEnum.FORBIDDEN);
        }

        UUID gameId = gameEncryptionUtil.decryptToUUID(encryptedGameId);

        // 게임을 찾을 수 없으면 예외 발생
        Game requestedGame = gameRepository.findById(gameId)
                .orElseThrow(() -> new ApiException(ExceptionEnum.GAME_NOT_FOUND));

        // 사용자가 현재 방 ID와 요청된 게임의 ID 일치 하는지 확인
        return currentUserRoom.getRoom().getRoomId().equals(requestedGame.getRoom().getRoomId());
    }



    }

}




