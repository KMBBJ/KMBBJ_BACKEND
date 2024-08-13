package com.kmbbj.backend.charts.entity;

public enum OrderType {
    STOP_LOSS, //(손절매): 특정 가격 이하(매도) 또는 이상(매수)으로 가격이 하락하거나 상승할 경우 자동으로 시장가 주문이 실행되는 주문 유형입니다.
    TAKE_PROFIT // (익절): 특정 가격 이상(매도) 또는 이하(매수)로 가격이 상승하거나 하락할 경우 자동으로 시장가 주문이 실행되는 주문 유형입니다.
}
