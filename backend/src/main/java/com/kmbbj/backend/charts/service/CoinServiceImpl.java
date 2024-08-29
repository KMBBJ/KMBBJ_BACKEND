package com.kmbbj.backend.charts.service;

import com.kmbbj.backend.charts.dto.CoinResponse;
import com.kmbbj.backend.charts.entity.CoinStatus;
import com.kmbbj.backend.charts.entity.coin.Coin;
import com.kmbbj.backend.charts.entity.coin.Coin24hDetail;
import com.kmbbj.backend.charts.event.CoinDetailUpdateEvent;
import com.kmbbj.backend.charts.repository.coin.Coin24hDetailRepository;
import com.kmbbj.backend.charts.repository.coin.CoinRepository;
import com.kmbbj.backend.global.config.exception.ApiException;
import com.kmbbj.backend.global.config.exception.ExceptionEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CoinServiceImpl implements CoinService {
    private final CoinRepository coinRepository;
    private final Coin24hDetailRepository coin24hDetailRepository;
    private final BinanceApiService binanceApiService;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    //@Scheduled(fixedRate = 60000)
    public void updateCoinDataEvent() {
        // 코인 데이터를 업데이트
        binanceApiService.updateCoinData();
        List<Coin> coins = coinRepository.findAllByStatus(CoinStatus.TRADING);
        List<CoinResponse> updatedCoins = getCoinResponseList(coins);

        // 이벤트 발행
        eventPublisher.publishEvent(new CoinDetailUpdateEvent(this, updatedCoins));
    }

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
        Coin24hDetail coin24hDetail = coin24hDetailRepository.findTopByCoinOrderByTimezoneDesc(coin).orElseThrow(() ->
                new ApiException(ExceptionEnum.NOT_FOUND_SYMBOL));
        // Coin과 CoinDetail을 포함한 CoinResponse 객체 반환
        return new CoinResponse(coin, coin24hDetail);
    }

    /**
     * List<Coin> coins(코인목록)에 맞는 CoinResponse 리스트 정보를 가져옴(가장 최신 코인 정보를 가져옴)
     * @param coins CoinResponse 리스트 정보를 받아올 코인 목록
     * @return List<CoinResponse>(coin, coinDetail) 매개변수 coins로 받아온 CoinResponse 데이터
     */
    public List<CoinResponse> getCoinResponseList(List<Coin> coins) {
        // CoinResponse 리스트로 변환
        return coins.stream().map(coin -> {
            // 각 코인에 대한 최신 Coin24hDetail 가져오기
            Coin24hDetail coin24hDetail = coin24hDetailRepository.findTopByCoinOrderByTimezoneDesc(coin)
                    .orElse(null); // 코인 디테일이 없는 경우 null 반환
            return new CoinResponse(coin, coin24hDetail);
        }).toList();
    }

    /**
     * 모든 코인 정보를 페이지네이션하여 가져옴
     * @param pageNo 페이지 번호
     * @param size 페이지별 아이템 수
     * @param orderBy 정렬 기준
     * @param sort 정렬 방향
     * @param searchQuery 검색할 단어
     * @return 페이지네이션된 코인, 코인 상세 정보를 CoinResponse에 담아서 보냄
     */
    public Page<CoinResponse> getAllCoins(int pageNo, int size, String orderBy, String sort, String searchQuery) {
        // 모든 코인 데이터를 가져옴
        List<Coin> coins = coinRepository.findAllByStatus(CoinStatus.TRADING);

        // 코인 이름에 검색어가 포함된 경우 필터링
        if (searchQuery != null && !searchQuery.isEmpty()) {
            coins = coins.stream()
                    .filter(coin -> coin.getCoinName().toLowerCase().contains(searchQuery.toLowerCase()))
                    .toList();
        }

        // CoinResponse 리스트로 변환
        List<CoinResponse> coinResponses = getCoinResponseList(coins);

        // 정렬 수행
        Comparator<CoinResponse> comparator = switch (orderBy) {
            case "price" ->
                    Comparator.comparing(coinResponse -> coinResponse.getCoin24hDetail().getPrice(), Comparator.nullsLast(Double::compareTo));
            case "volume" ->
                    Comparator.comparing(coinResponse -> coinResponse.getCoin24hDetail().getVolume(), Comparator.nullsLast(Double::compareTo));
            case "highPrice" ->
                    Comparator.comparing(coinResponse -> coinResponse.getCoin24hDetail().getHighPrice(), Comparator.nullsLast(Double::compareTo));
            case "lowPrice" ->
                    Comparator.comparing(coinResponse -> coinResponse.getCoin24hDetail().getLowPrice(), Comparator.nullsLast(Double::compareTo));
            case "priceChange" ->
                    Comparator.comparing(coinResponse -> coinResponse.getCoin24hDetail().getPriceChange(), Comparator.nullsLast(Double::compareTo));
            default -> Comparator.comparing(coinResponse -> coinResponse.getCoin().getCoinName());
        };

        if (sort.equals("desc")) {
            comparator = comparator.reversed();
        }

        // 정렬된 리스트로 변환
        List<CoinResponse> sortedCoinResponses = coinResponses.stream()
                .sorted(comparator)
                .collect(Collectors.toList());

        // 페이지네이션 적용
        int start = Math.min((int) PageRequest.of(pageNo, size).getOffset(), sortedCoinResponses.size());
        int end = Math.min((start + size), sortedCoinResponses.size());
        List<CoinResponse> pagedCoinResponses = sortedCoinResponses.subList(start, end);

        // 페이지로 변환
        return new PageImpl<>(pagedCoinResponses, PageRequest.of(pageNo, size), sortedCoinResponses.size());
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
            coin.setStatus(CoinStatus.TRADING);
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
        coin.setStatus(CoinStatus.DELETED);
        coinRepository.save(coin);
    }

    /**
     * 코인의 상태 및 주문 유형을 업데이트함
     * @param symbol 업데이트할 코인의 심볼 (예: BTCUSDT, ETHUSDT)
     * @param status 코인의 새로운 상태
     */
    public void updateCoin(String symbol, CoinStatus status) {
        Coin coin = getCoin(symbol);
        coin.setStatus(status);

        coinRepository.save(coin);
    }
}