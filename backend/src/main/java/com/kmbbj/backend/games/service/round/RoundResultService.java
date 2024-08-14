package com.kmbbj.backend.games.service.round;

import com.kmbbj.backend.games.entity.RoundResult;
import com.kmbbj.backend.games.repository.RoundResultRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RoundResultService {

    private final RoundResultRepository roundResultRepository;

    public List<RoundResult> getRoundResultsForGame(Long gameId) {
        // 라운드 결과를 조회 - > 라운드 ID 순서대로 정렬해서 반환
        return roundResultRepository.findByRoundGameGameIdOrderByRoundRoundIdAsc(gameId);
    }

}



