package com.kmbbj.backend.charts.service;

import com.kmbbj.backend.charts.dto.CoinResponse;
import com.kmbbj.backend.charts.entity.coin.Coin;
import com.kmbbj.backend.charts.entity.OrderType;
import com.kmbbj.backend.charts.entity.coin.CoinDetail;
import com.kmbbj.backend.charts.repository.coin.CoinDetailRepository;
import com.kmbbj.backend.charts.repository.coin.CoinRepository;
import com.kmbbj.backend.global.config.exception.ApiException;
import com.kmbbj.backend.global.config.exception.ExceptionEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CoinServiceImpl implements CoinService {
    private final CoinRepository coinRepository;
    private final CoinDetailRepository coinDetailRepository;

    /**
     * symbol(코인코드)에 맞는 코인 정보를 가져옴
     * @param symbol 코인의 심볼 (예: BTCUSDT, ETHUSDT)
     * @return Coin 매개변수 symbol과 같은 symbol의 코인 데이터
     * @throws ApiException 매개변수 symbol에 해당하는 코인이 존재하지 않는 경우 예외 발생
     */
    public Coin getCoin(String symbol) {
        return coinRepository.findBySymbol(symbol).orElseThrow(() -> new ApiException(ExceptionEnum.NOT_FOUND_SYMBOL));
    }

    /**
     * symbol(코인코드)에 맞는 CoinResponse 정보를 가져옴
     * @param symbol 코인의 심볼 (예: BTCUSDT, ETHUSDT)
     * @return CoinResponse(coin, coinDetail) 매개변수 symbol과 같은 symbol의 코인 및 코인 가격 데이터
     */
    public CoinResponse getCoinResponse(String symbol) {
        Coin coin = getCoin(symbol);
        // 해당 코인의 가장 최신 CoinDetail(가격 정보) 가져오기
        CoinDetail coinDetail = coinDetailRepository.findTopByCoinOrderByTimezoneDesc(coin).orElseThrow(() ->
                new ApiException(ExceptionEnum.NOT_FOUND_SYMBOL));
        // Coin과 CoinDetail을 포함한 CoinResponse 객체 반환
        return new CoinResponse(coin, coinDetail);
    }

    /**
     * 모든 코인 정보를 페이지네이션하여 가져옴
     * @param pageable 페이지네이션 정보 (페이지 번호, 페이지 크기, 정렬 정보)
     * @return 페이지네이션된 코인 목록
     */
    public Page<Coin> getAllCoins(Pageable pageable) {
        // 코인 이름을 기준으로 내림차순 정렬하여 페이지네이션 설정
        Pageable sortedByCoinName = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(),
                Sort.by(Sort.Direction.DESC, "coinName"));
        // 설정된 페이지네이션 정보를 기반으로 코인 목록을 가져옴
        return coinRepository.findAll(sortedByCoinName);
    }

    /**
     * 새로운 코인을 추가함
     * @param symbol 코인의 심볼 (예: BTCUSDT, ETHUSDT)
     * @param coinName 코인의 이름
     * @throws ApiException 매개변수로 전달된 symbol 또는 coinName이 이미 존재하는 경우 예외 발생
     */
    public void addCoin(String symbol, String coinName) {
        // 동일한 symbol 또는 coinName을 가진 코인이 존재하는지 확인
        if(coinRepository.findBySymbol(symbol).isEmpty() && coinRepository.findByCoinName(coinName).isEmpty()) {
            Coin coin = new Coin();
            coin.setSymbol(symbol);
            coin.setCoinName(coinName);
            coinRepository.save(coin);
        } // 이미 존재하는 경우 예외 발생
        else throw new ApiException(ExceptionEnum.EXIST_COIN);
    }

    /**
     * 코인을 삭제함
     * @param symbol 삭제할 코인의 심볼 (예: BTCUSDT, ETHUSDT)
     */
    public void deleteCoin(String symbol) {
        Coin coin = getCoin(symbol);
        coinRepository.delete(coin);
    }

    /**
     * 코인의 상태 및 주문 유형을 업데이트함
     * @param symbol 업데이트할 코인의 심볼 (예: BTCUSDT, ETHUSDT)
     * @param status 코인의 새로운 상태
     * @param orderType 코인의 새로운 주문 유형
     */
    public void updateCoin(String symbol, String status, OrderType orderType) {
        Coin coin = getCoin(symbol);
        coin.setStatus(status);
        coin.setOrderTypes(orderType);

        coinRepository.save(coin);
    }
}