package com.kmbbj.backend.games.service.game;


import com.kmbbj.backend.auth.entity.User;
import com.kmbbj.backend.auth.service.UserService;
import com.kmbbj.backend.games.dto.*;
import com.kmbbj.backend.games.entity.Game;
import com.kmbbj.backend.games.entity.Round;
import com.kmbbj.backend.games.enums.GameStatus;
import com.kmbbj.backend.games.repository.GameRepository;
import com.kmbbj.backend.games.repository.RoundRepository;
import com.kmbbj.backend.games.service.gamebalance.GameBalanceService;
import com.kmbbj.backend.games.service.gameresult.GameResultService;
import com.kmbbj.backend.games.service.round.RoundService;
import com.kmbbj.backend.games.service.roundresult.RoundResultService;
import com.kmbbj.backend.games.util.GameEncryptionUtil;
import com.kmbbj.backend.games.util.GameProperties;
import com.kmbbj.backend.global.config.exception.ApiException;
import com.kmbbj.backend.global.config.exception.ExceptionEnum;
import com.kmbbj.backend.global.config.security.FindUserBySecurity;
import com.kmbbj.backend.matching.entity.Room;
import com.kmbbj.backend.matching.entity.UserRoom;
import com.kmbbj.backend.matching.repository.RoomRepository;
import com.kmbbj.backend.matching.repository.UserRoomRepository;
import com.kmbbj.backend.matching.service.room.RoomService;
import com.kmbbj.backend.matching.service.userroom.UserRoomService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class GameServiceImpl implements GameService {

    private final GameRepository gameRepository;
    private final RoomService roomService;
    private final RoomRepository roomRepository;
    private final UserRoomService userRoomService;
    private final UserService userService;
    private final RoundRepository roundRepository;
    private final RoundService roundService;
    private final GameEncryptionUtil gameEncryptionUtil;
    private final GameProperties gameProperties;
    private final GameResultService gameResultService;
    private final GameBalanceService gameBalanceService;
    private final RoundResultService roundResultService;
    private final UserRoomRepository userRoomRepository;
    private final Map<Long, String> roomGameIdMap = new ConcurrentHashMap<>();
    private final FindUserBySecurity findUserBySecurity;


    /**
     * 방 ID를 통해 게임 시작
     * <p>
     * 방 정보 조회 -> 방 시작 확인여부
     * 새 게임 객체 생성 후 데이터베이스 저장
     * 첫 번째 라운드 생성 후 데이터베이스 저장
     * 게임 ID를 암호화 반환
     *
     * @param roomId 게임 시작 할 ID
     * @return
     * @throws ExceptionEnum 공통 예외
     */
    @Override
    @Transactional
    public GameStartDTO startGame(Long roomId) {
        String encryptedGameId = roomGameIdMap.computeIfAbsent(roomId, id -> createAndStartGame(id));
        return createGameStartDTO(roomId, encryptedGameId);
    }

    protected String createAndStartGame(Long roomId) {
        Room room = roomService.findById(roomId);

        // 이미 진행 중인 게임이 있는지 확인
        Game game = gameRepository.findActiveGameByRoom(room);

        if (game == null) {
            // 새 게임 생성 & 저장
            game = new Game();
            game.setGameStatus(GameStatus.ACTIVE);
            game.setRoom(room);
            game = gameRepository.save(game);

            // 방에 속한 플레이 중인 사용자들에게 게임 잔액 생성
            gameBalanceService.createGameBalance(game);

            // 첫 라운드 생성 & 저장
            Round round = new Round();
            round.setGame(game);
            round.setRoundNumber(1);
            int durationMinutes = getDurationMinutes();
            round.setDurationMinutes(durationMinutes);
            roundRepository.save(round);
        }

        // 게임 UUID 값을 암호화
        return gameEncryptionUtil.encryptUUID(game.getGameId());
    }

    private GameStartDTO createGameStartDTO(Long roomId, String encryptedGameId) {
        Room room = roomService.findById(roomId);
        List<UserRoom> userRooms = userRoomRepository.findAllByRoomAndIsPlayed(room, true);

        List<String> userIds = userRooms.stream()
                .map(userRoom -> userRoom.getUser().getId().toString())
                .collect(Collectors.toList());

        GameStartDTO gameStartDTO = new GameStartDTO();
        gameStartDTO.setGameId(encryptedGameId);
        gameStartDTO.setUserId(userIds);

        return gameStartDTO;
    }




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

        boolean isGameEnded = roundService.manageRounds(encryptedGameId);
        if (isGameEnded) {
            game.setGameStatus(GameStatus.COMPLETED); // 게임 완료
            gameRepository.save(game); // 데이터 베이스 저장

            // 모든 라운드 결과 가져오기
            List<RoundResultDTO> allRoundResults = roundResultService.getCompletedRoundResultsForGame(encryptedGameId);
            // 게임 결과 생성 메서드 호출
            gameResultService.createGameResults(encryptedGameId);
        }

        // 게임 결과 생성 후 게임 계좌는 삭제 처리
        gameBalanceService.deleteGameBalances(game);

        Room room = game.getRoom(); // 게임과 연결 된 방을 가져옴
        // 방 상태 업데이트
        room.setIsStarted(false);
        roomRepository.save(room);
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

        Round currentRound = roundRepository.findFirstByGameOrderByRoundNumberDesc(game)
                .orElseThrow(() -> new ApiException(ExceptionEnum.ROUND_NOT_FOUND));

        int durationMinutes = getDurationMinutes();
        List<RoundResultDTO> roundResults = roundResultService.getCompletedRoundResultsForGame(encryptedGameId);

        GameStatusDTO statusDTO = new GameStatusDTO();
        statusDTO.setStatus(game.getGameStatus());
        statusDTO.setDurationMinutes(durationMinutes);
        statusDTO.setResults(roundResults);

        return statusDTO;
    }


    // 게임 라운드의 지속시간 (24시간)
    private int getDurationMinutes() {
        return gameProperties.getGameRoundDurationMinutes();
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

    @Override
    public boolean isGameInProgress(Long roomId) {
        Room room = roomService.findById(roomId);

        // 방이 시작 상태인지 확인
        if (!room.getIsStarted()) {
            return false;
        }

        // 활성 상태의 게임이 있는지 확인
        return room.getIsStarted() && gameRepository.findActiveGameByRoom(room) != null;
    }

    @Override
    public Long getUserParticipatingRoom() {

        UserRoom userRoom = userRoomService.findUserRoomByUserAndIsPlayed()
                .orElseThrow(() -> new ApiException(ExceptionEnum.NOT_IN_ROOM));

        Room room = userRoom.getRoom();

        // 게임을 진행하고 있는 방에 없음
        if ((room.getIsStarted() && room.getIsDeleted())) {
            return 0L;
        } else {
            // 매칭 방에 있음
            return room.getRoomId();
        }
    }
}

