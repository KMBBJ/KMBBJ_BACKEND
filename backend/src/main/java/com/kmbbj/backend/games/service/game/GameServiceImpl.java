package com.kmbbj.backend.games.service.game;


import com.kmbbj.backend.games.dto.CurrentRoundDTO;
import com.kmbbj.backend.games.dto.GameStatusDTO;
import com.kmbbj.backend.games.entity.Game;
import com.kmbbj.backend.games.entity.Round;
import com.kmbbj.backend.games.enums.GameStatus;
import com.kmbbj.backend.games.repository.GameRepository;
import com.kmbbj.backend.games.repository.RoundRepository;
import com.kmbbj.backend.games.service.round.RoundResultService;
import com.kmbbj.backend.games.service.round.RoundService;
import com.kmbbj.backend.games.util.GameEncryptionUtil;
import com.kmbbj.backend.games.util.GameProperties;
import com.kmbbj.backend.global.config.exception.ApiException;
import com.kmbbj.backend.global.config.exception.ExceptionEnum;
import com.kmbbj.backend.matching.entity.Room;
import com.kmbbj.backend.matching.repository.RoomRepository;
import com.kmbbj.backend.matching.service.room.RoomService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;


@Service
@RequiredArgsConstructor
public class GameServiceImpl implements GameService {

    private final GameRepository gameRepository;
    private final RoomService roomService;
    private final RoomRepository roomRepository;
    private final RoundRepository roundRepository;
    private final RoundResultService roundResultService;
    private final RoundService roundService;
    private final GameEncryptionUtil gameEncryptionUtil;
    private final GameProperties gameProperties;



    /** 방 ID를 통해 게임 시작
     *
     * 방 정보 조회 -> 방 시작 확인여부
     * 새 게임 객체 생성 후 데이터베이스 저장
     * 첫 번째 라운드 생성 후 데이터베이스 저장
     * 게임 ID를 암호화 반환
     *
     * @param roomId 게임 시작 할 ID
     * @return 시작된 게임 상태 정보 DTO 객체
     * @throws ExceptionEnum 공통 예외
     */
    @Override
    @Transactional
    public GameStatusDTO startGame(Long roomId) {
        Room room = roomService.findById(roomId); // 방 ID 조회함

        // 시작된 상태 확인
        if (room.getIsStarted()) {
            throw new ApiException(ExceptionEnum.GAME_ALREADY_STARTED);
        }

        // 새 게임 생성 & 저장
        Game game = new Game();
        game.setGameStatus(GameStatus.ACTIVE); // 게임 상태를 설정
        game.setRoom(room); // 방 <-> 게임 연결
        game = gameRepository.save(game); // 데이터 베이스 저장


        // 첫 라운드 생성 & 저장
        Round round = new Round();
        round.setGame(game);
        round.setRoundNumber(1); //  첫 라운드 시작
        int durationMinutes = getDurationMinutes(); // 라운드의 지속 시간
        round.setDurationMinutes(durationMinutes);
        roundRepository.save(round);

        // 게임 UUID 값을 암호화
        String encryptedGameId = gameEncryptionUtil.encryptUUID(game.getGameId());

        // 게임 상태 정보 DTO
        GameStatusDTO status = new GameStatusDTO();
        status.setGameId(encryptedGameId); // UUID -> 암호화 ID 변경
        status.setStatus(GameStatus.ACTIVE);

        return status;
    }


//    /** 비동기적으로 게임 시작 -> 스케줄러 으로 변경함
//     *  1. 마지막 라운드 도달할 떄까지 새 라운드 계속 시작함
//     *  2. 마지막 라운드에 도달하면 게임 종료
//     *
//     * @param game           게임 객체
//     * @param endRoundNumber 게임이 종료될 라운드 번호
//     */
//    @Async
//    public void startGameAsync(Game game, int endRoundNumber) {
//        try {
//            // 마지막 라운드 도달할 때까지 계속 라운드를 진행
//            while (!roundService.isLastRound(game, endRoundNumber)) {
//                try {
//                    roundService.startNewRound(game); // 새 라운드 시작
//
//                    // 라운드가 끝날 때까지 대기 (24시간)
//                    Thread.sleep(24 * 60 * 60 * 1000); // 24시간 대기
//                } catch (Exception ex) {
//                    throw ex; // 예외가 발생하면 반복문을 멈추고 상위로 예외를 던짐
//                }
//            }
//
//            // 게임 완료 처리
//            endGame(game.getRoom().getRoomId());
//        } catch (InterruptedException e) {
//            Thread.currentThread().interrupt(); // 쓰레드 인터럽트 시도
//        } catch (Exception e) {
//            game.setGameStatus(GameStatus.COMPLETED); // 예외 발생 시 게임 상태를 완료로 설정
//            gameRepository.save(game);
//            throw e; // 예외를 다시 던져 트랜잭션을 롤백하도록 처리
//        }
//    }


    /** 게임 종료
     *
     * 게임 객체와 최신 라운드 정보 조회
     * 게임 종료 조건 확인
     * 게임 상태 COMPLETED 로 변경
     *
     *
     * @param encryptedGameId 암호화 게임 ID
     */
    @Override
    @Transactional
    public void endGame(String encryptedGameId) {
        // 암호화된 게임 ID 복호화
        UUID gameId = gameEncryptionUtil.decryptToUUID(encryptedGameId);

        // 게임 존재 확인 여부
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new ApiException(ExceptionEnum.GAME_NOT_FOUND));

        // 최신 라운드 존재 확인 여부
        Round latestRound = roundRepository.findFirstByGameOrderByRoundNumberDesc(game)
                .orElseThrow(() -> new ApiException(ExceptionEnum.ROUND_NOT_FOUND));

        Room room = game.getRoom(); // 게임과 연결 된 방을 가져옴

        // 게임 종료 조건 확인
        if (latestRound.getRoundNumber() >= room.getEnd()) {
            game.setGameStatus(GameStatus.COMPLETED); // 게임 완료
            gameRepository.save(game); // 데이터 베이스 저장

            // 모든 라운드 결과 가져오기


            // 여기에 최종 결과 구현 할것 (미구현)


            // 방 상태 업데이트
            room.setIsStarted(false);
            room.setIsDeleted(true);
            roomRepository.save(room);
        }
    }


    /** 게임의 상태 조회
     *
     *  게임 객체 조회
     *  게임 상태 정보를 담은 DTO 객체 생성하여 반환
     *
     * @param encryptedGameId 암호화된 게임 ID
     * @return 게임 상태 정보를 게임 상태 DTO
     */
    @Override
    public GameStatusDTO getGameStatus(String encryptedGameId) {
        UUID gameId = gameEncryptionUtil.decryptToUUID(encryptedGameId);
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new ApiException(ExceptionEnum.GAME_NOT_FOUND));

        GameStatusDTO statusDTO = new GameStatusDTO();
        statusDTO.setGameId(encryptedGameId);
        statusDTO.setStatus(game.getGameStatus());

        return statusDTO;
    }

    /** 현재 진행 중인 라운드 정보 조회
     *
     * 게임 객체 조회
     * 최신 라운드 정보 조회
     * 현재 라운드 정보를 담은 DTO 객체 반환
     *
     * @param encryptedGameId 조회할 암호화된 ID
     * @return 현재 진행 중인 라운드 정보 DTO 객체 반환
     */
    @Override
    public CurrentRoundDTO getCurrentRound(String encryptedGameId) {
        UUID gameId = gameEncryptionUtil.decryptToUUID(encryptedGameId);
        // 게임 정보 조회
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new ApiException(ExceptionEnum.GAME_NOT_FOUND));

        // 현재 라운드 정보 조회
        Round currentRound = roundRepository.findFirstByGameOrderByRoundNumberDesc(game)
                .orElseThrow(() -> new ApiException(ExceptionEnum.ROUND_NOT_FOUND));

        // 현재 라운드 DTO 생성 & 반환
        CurrentRoundDTO dto = new CurrentRoundDTO();
        dto.setGameId(encryptedGameId);
        dto.setCurrentRoundNumber(currentRound.getRoundNumber());
        dto.setGameStatus(game.getGameStatus().toString());

        return dto;
    }

    // 게임 라운드의 지속시간 (24시간)
    private int getDurationMinutes() {
        return gameProperties.getGameRoundDurationMinutes();
    }
}