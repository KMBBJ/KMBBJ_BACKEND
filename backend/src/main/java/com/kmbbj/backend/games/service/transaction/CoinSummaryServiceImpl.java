package com.kmbbj.backend.games.service.transaction;


import com.kmbbj.backend.games.dto.RoundResultDTO;
import com.kmbbj.backend.games.entity.Round;
import com.kmbbj.backend.games.entity.RoundResult;
import com.kmbbj.backend.games.repository.RoundRepository;
import com.kmbbj.backend.games.repository.RoundResultRepository;
import com.kmbbj.backend.global.config.exception.ApiException;
import com.kmbbj.backend.global.config.exception.ExceptionEnum;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CoinSummaryServiceImpl implements CoinSummaryService {

    private final RoundResultRepository roundResultRepository;
    private final RoundRepository roundRepository;

    /** 라운드 결과 저장
     * 외부에서 계산된 라운드 결과
     * (가장 많이 매수된 코인, 가장 큰 수익/손실 코인) 받아서 저장함
     *
     * @param result 저장할 라운드 결과 DTO
     */
    @Override
    @Transactional
    public void recordRoundResult(RoundResultDTO result) {
        Round round = roundRepository.findById(result.getRoundId())
                .orElseThrow(() -> new ApiException(ExceptionEnum.ROUND_NOT_FOUND));

        RoundResult entity = convertToEntity(result); // DTO 변환
        entity.setRound(round); // 변환된 엔티티 라운드 정보 설정
        roundResultRepository.save(entity); // 엔티티를 데이터 베이스 저장
    }

    /**
     * 라운드의 결과 조회
     *
     * @param roundId 라운드 ID
     * @return 라운드 결과 DTO
     */
    @Override
    @Transactional (readOnly = true)
    public RoundResultDTO getRoundResult(Long roundId) {
        RoundResult entity = roundResultRepository.findByRound_RoundId(roundId)
                .orElseThrow(() -> new ApiException(ExceptionEnum.ROUND_RESULT_NOT_FOUND));
        return convertToDTO(entity);
    }

    /** RoundResultDTO -> RoundResult 엔티티 변환
     *
     * @param dto 변환할 DTO
     * @return 변환된 RoundResult 엔티티
     */
    private RoundResult convertToEntity(RoundResultDTO dto) {
        RoundResult entity = new RoundResult();
        copyProperties(dto, entity);
        return entity;
    }

    /** RoundResult -> RoundResultDTO 변환
     *
     * @param entity 변환할 엔티티
     * @return 변환된 RoundResultDTO
     */
    private RoundResultDTO convertToDTO(RoundResult entity) {
        RoundResultDTO dto = new RoundResultDTO();
        copyProperties(entity, dto);
        dto.setRoundId(entity.getRound().getRoundId());
        return dto;
    }

    /** 두 객체 간에 공통 속성을 복사
     *
     * @param source 원본 객체
     * @param target 대상 객체
     */

    private void copyProperties(Object source, Object target) {
        copyProperty(source, target, "topBuyCoin");
        copyProperty(source, target, "topBuyPercent");
        copyProperty(source, target, "topProfitCoin");
        copyProperty(source, target, "topProfitPercent");
        copyProperty(source, target, "topLossCoin");
        copyProperty(source, target, "topLossPercent");
    }

    /** 한 객체의 특정 속성을 다른 객체로 복사합니다.
     *
     * @param source 원본 객체
     * @param target 대상 객체
     * @param propertyName 복사할 속성 이름
     */
    private void copyProperty(Object source, Object target, String propertyName) {
        try {
            // 원본 객체 속성값 가져옴
            Object value = source.getClass().getMethod("get" + capitalize(propertyName)).invoke(source);
            if (value != null) {
                // null 아닐 경우 대상 객체 속성 값을 설정함
                target.getClass().getMethod("set" + capitalize(propertyName), value.getClass()).invoke(target, value);
            }
        } catch (Exception e) {
            throw new RuntimeException("Error copying property: " + propertyName, e);
        }
    }

    private String capitalize(String str) {
        return str.substring(0, 1).toUpperCase() + str.substring(1);

    }
}
