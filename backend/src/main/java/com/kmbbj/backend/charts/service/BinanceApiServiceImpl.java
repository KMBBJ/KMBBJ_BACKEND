package com.kmbbj.backend.charts.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import com.kmbbj.backend.charts.entity.coin.Coin;
import com.kmbbj.backend.charts.entity.coin.Coin24hDetail;
import com.kmbbj.backend.charts.repository.coin.Coin24hDetailRepository;
import com.kmbbj.backend.charts.repository.coin.CoinRepository;
import com.kmbbj.backend.global.config.exception.ApiException;
import com.kmbbj.backend.global.config.exception.ExceptionEnum;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class BinanceApiServiceImpl implements BinanceApiService {
    private final Coin24hDetailRepository coin24hDetailRepository;
    private final CoinRepository coinRepository;

    @Value("${RESTAPI_BINANCE_ACCESSKEY}")
    private String accessKey;

    @Value("${WEB_CLIENT_BASE_URL}")
    private String webClientBaseURL;

    @Value("${API_ENDPOINT_KLINES}")
    private String klinesEndPoint;

    @Value("${API_ENDPOINT_24H_TICKER}")
    private String tickerEndPoint;

    private final WebClient webClient;

    /* 기본 Url 설정 */
    public BinanceApiServiceImpl(Coin24hDetailRepository coin24hDetailRepository, CoinRepository coinRepository, WebClient.Builder webClientBuilder) {
        this.coinRepository = coinRepository;
        this.coin24hDetailRepository = coin24hDetailRepository;
        // WebClient를 Binance API의 기본 URL로 빌드하고, 타임아웃 설정 추가
        this.webClient = webClientBuilder
                .baseUrl(webClientBaseURL)
                .clientConnector(new ReactorClientHttpConnector(HttpClient.create()
                        .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 60000) // 연결 타임아웃 설정
                        .doOnConnected(conn ->
                                conn.addHandlerLast(new ReadTimeoutHandler(60))  // 읽기 타임아웃 설정
                                        .addHandlerLast(new WriteTimeoutHandler(60)))
                )) // 쓰기 타임아웃 설정
                .build();
    }

    /**
     * 매개변수 조건에 해당하는 kline 데이터를 가져옴
     * @param symbol 코인의 심볼 (예: BTCUSDT, ETHUSDT)
     * @param interval 시간 간격
     * @param startTime 시작 시간 설정
     * @param endTime 종료 시간 설정
     * @param limit 받아올 klines 데이터 갯수 제한(기본 500, 최대 1000)
     * @return 매개변수의 조건에 해당하는 kline 데이터
     */
    @Override
    public Mono<String> getKlines(String symbol, String interval, Long startTime, Long endTime, Integer limit) {
        String endpoint = klinesEndPoint;
        StringBuilder queryString = new StringBuilder();
        // 심볼과 간격을 쿼리 문자열에 추가
        queryString.append("symbol=").append(symbol)
                .append("&interval=").append(interval);
        // 시작 시간 설정이 있을 경우 쿼리 문자열에 추가
        if (startTime != null) {
            queryString.append("&startTime=").append(startTime);
        }
        // 종료 시간 설정이 있을 경우 쿼리 문자열에 추가
        if (endTime != null) {
            queryString.append("&endTime=").append(endTime);
        }
        // 데이터 갯수 제한 설정이 있을 경우 쿼리 문자열에 추가
        if (limit != null) {
            queryString.append("&limit=").append(limit);
        }

        // WebClient를 통해 JSON 데이터를 받아와서 String 형태로 변환하여 반환
        return getJsonToWebClientForSingleSymbol(endpoint, queryString).map(JsonArray::toString);
    }

    /**
     *  DB에 저장된 모든 코인의 데이터를 업데이트
     */
    @Override
    public void updateCoinData() {
        // 모든 코인 심볼을 가져와 USDT를 붙인 심볼 리스트를 생성
        List<String> symbols = coinRepository.findAll()
                .stream()
                .map(coin -> coin.getSymbol() + "USDT")
                .toList();

        // 심볼 리스트를 한번에 처리할 수 있도록 API 요청을 보냄
        get24hrTickerData(symbols)
                .flatMap(tickerDataList -> {
                    List<Coin24hDetail> coinDetails = parse24hrTickerData(tickerDataList);
                    coin24hDetailRepository.saveAll(coinDetails);
                    return Mono.empty();
                })
                .subscribe(); // 비동기적으로 실행
    }

    /**
     * 24시간 티커 데이터를 가져옴
     * @param symbols 코인의 심볼 리스트 (예: BTCUSDT, ETHUSDT)
     * @return 24시간 티커 데이터를 포함하는 Mono<List<Map<String, Object>>>
     */
    public Mono<List<Map<String, Object>>> get24hrTickerData(List<String> symbols) {
        String endpoint = tickerEndPoint;
        StringBuilder queryString = buildSymbolsQuery(symbols); // 심볼 리스트를 쿼리 문자열로 변환

        return getJsonToWebClientForMultipleSymbols(endpoint, queryString);
    }

    /**
     * 24ticker 데이터를 기반으로 코인 24시간 기준 정보 데이터를 파싱
     * @param tickerDataList 코인 24시간 기준 정보 데이터 리스트
     * @return 파싱된 코인 정보 리스트
     */
    public List<Coin24hDetail> parse24hrTickerData(List<Map<String, Object>> tickerDataList) {
        List<Coin24hDetail> coin24hDetails = new ArrayList<>();

        for (Map<String, Object> tickerData : tickerDataList) {
            String symbol = (String) tickerData.get("symbol");
            String symbolWithoutUSDT = symbol.replace("USDT", "");

            // 코인 정보 조회
            Coin coin = coinRepository.findBySymbol(symbolWithoutUSDT)
                    .orElseThrow(() -> new ApiException(ExceptionEnum.NOT_FOUND_SYMBOL));

            // Builder 패턴을 사용해 CoinDetail 객체 생성
            Coin24hDetail coin24hDetail = Coin24hDetail.builder()
                    .coin(coin)
                    .price(Double.parseDouble((String) tickerData.get("lastPrice")))
                    .bidPrice(Double.parseDouble((String) tickerData.get("bidPrice")))
                    .bidQty(Double.parseDouble((String) tickerData.get("bidQty")))
                    .askPrice(Double.parseDouble((String) tickerData.get("askPrice")))
                    .askQty(Double.parseDouble((String) tickerData.get("askQty")))
                    .priceChange(Double.parseDouble((String) tickerData.get("priceChange")))
                    .priceChangePercent(Double.parseDouble((String) tickerData.get("priceChangePercent")))
                    .weightedAvgPrice(Double.parseDouble((String) tickerData.get("weightedAvgPrice")))
                    .prevClosePrice(Double.parseDouble((String) tickerData.get("prevClosePrice")))
                    .openPrice(Double.parseDouble((String) tickerData.get("openPrice")))
                    .highPrice(Double.parseDouble((String) tickerData.get("highPrice")))
                    .lowPrice(Double.parseDouble((String) tickerData.get("lowPrice")))
                    .volume(Double.parseDouble((String) tickerData.get("volume")))
                    .quoteVolume(Double.parseDouble((String) tickerData.get("quoteVolume")))
                    .tradeCount(((Number) tickerData.get("count")).longValue())
                    .openTime(((Number) tickerData.get("openTime")).longValue())
                    .closeTime(((Number) tickerData.get("closeTime")).longValue())
                    .timezone(LocalDateTime.now())
                    .build();

            coin24hDetails.add(coin24hDetail);
        }

        return coin24hDetails;
    }

    /**
     * 주어진 심볼 배열을 쿼리 문자열로 빌드
     * @param symbols 코인의 심볼 배열 (예: BTCUSDT, ETHUSDT)
     * @return 빌드된 쿼리 문자열
     */
    private StringBuilder buildSymbolsQuery(List<String> symbols) {
        StringBuilder queryString = new StringBuilder();
        // 심볼이 하나일 경우 단일 심볼 쿼리 생성
        if (symbols.size() == 1) {
            queryString.append("symbol=").append(symbols.getFirst());
        } else {
            // symbol이 여러개일 경우 symbol=["BTCUSDT","BNBUSDT"] 형태로 쿼리 문자열 생성
            queryString.append("symbols=");
            StringBuilder symbolsArray = new StringBuilder();
            symbolsArray.append("[");
            for (int i = 0; i < symbols.size(); i++) {
                symbolsArray.append("\"").append(symbols.get(i)).append("\"");
                if (i < symbols.size() - 1) {
                    symbolsArray.append(",");
                }
            }
            symbolsArray.append("]");
            queryString.append(symbolsArray);
        }
        return queryString;
    }

    /**
     * 주어진 엔드포인트와 쿼리 문자열에 대한 JSON 데이터를 가져옴(하나의 심볼)
     * @param endpoint 엔드포인트 URL
     * @param queryString 쿼리 문자열
     * @return JSON 데이터를 포함하는 Mono<JsonArray>
     */
    private Mono<JsonArray> getJsonToWebClientForSingleSymbol(String endpoint, StringBuilder queryString) {
        // WebClient를 통해 GET 요청을 보내고, 응답을 JsonArray로 변환하여 반환
        return this.webClient.get()
                .uri(uriBuilder -> uriBuilder.path(endpoint).query(queryString.toString()).build()) // URI 빌드
                .header("Content-Type", "application/json")
                .header("X-MBX-APIKEY", accessKey)
                .retrieve()// 요청 전송
                .bodyToMono(String.class) // 응답 본문을 비동기적으로 수신
                .map(response -> JsonParser.parseString(response).getAsJsonArray());
    }

    /**
     * 여러 심볼에 대해 Binance API로부터 데이터를 가져오는 메소드.
     * @param endpoint     Binance API의 엔드포인트 경로 (예: "/api/v3/ticker/24hr")
     * @param queryString  심볼들을 포함한 쿼리 문자열을 생성하는 StringBuilder 객체
     * @return            심볼에 해당하는 데이터 리스트를 포함하는 Mono 객체
     */
    public Mono<List<Map<String, Object>>> getJsonToWebClientForMultipleSymbols(String endpoint, StringBuilder queryString) {
        // WebClient를 통해 GET 요청을 보내고, 응답 본문을 Mono<List<Map<String, Object>>> 타입으로 변환하여 반환
        return this.webClient.get()
                .uri(uriBuilder -> uriBuilder.path(endpoint).query(queryString.toString()).build()) // URI 빌드
                .retrieve() // 요청 전송
                .bodyToMono(new ParameterizedTypeReference<>() {}); // 응답 본문을 비동기적으로 수신
    }
}