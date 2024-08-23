package com.kmbbj.backend.games.service.round;

import com.kmbbj.backend.games.entity.RoundResult;
import com.kmbbj.backend.games.repository.RoundResultRepository;
import com.kmbbj.backend.games.util.GameEncryptionUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RoundResultService {

    private final RoundResultRepository roundResultRepository;
    private final GameEncryptionUtil gameEncryptionUtil;

    public List<RoundResult> getRoundResultsForGameId(String encryptedGameId) {
        UUID gameId = gameEncryptionUtil.decryptToUUID(encryptedGameId);
        return roundResultRepository.findByRoundGameGameIdOrderByRoundRoundIdAsc(gameId);
    }

}



