package com.kmbbj.backend.global.config.websocket;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class MatchWebSocketHandler extends TextWebSocketHandler {
    private Map<Long, WebSocketSession> userSessions = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        Long userId = extractUserIdFromSession(session);
        if (userId != null) {
            userSessions.put(userId, session);
        }
    }

    private Long extractUserIdFromSession(WebSocketSession session) {
        // 세션에서 유저 ID 추출 로직 구현
        return Long.parseLong(session.getAttributes().get("userId").toString());
    }

    public void notifyAboutMatch(Long userId, Long roomId) {
        WebSocketSession session = userSessions.get(userId);
        if (session != null && session.isOpen()) {
            try {
                session.sendMessage(new TextMessage("방 생성 성공: 방 ID " + roomId));
                System.out.println("방 생성 성공: 방 ID " + roomId);
                // 예외 처리 ㄱㄱ
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
