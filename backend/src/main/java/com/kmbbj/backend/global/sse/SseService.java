package com.kmbbj.backend.global.sse;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface SseService {
    void sendRoomNotification(Long userId, Long roomId);
    SseEmitter createEmitter(Long userId);
    void sendGameStartNotification(Long userId, Long roomId);
}
