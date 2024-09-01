package com.kmbbj.backend.global.sse;

import com.kmbbj.backend.global.config.exception.ApiException;
import com.kmbbj.backend.global.config.exception.ExceptionEnum;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SseServiceImpl implements SseService{
    private final Map<Long, SseEmitter> emitters = new ConcurrentHashMap<>();

    @Override
    public SseEmitter createEmitter(Long userId) {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        emitters.put(userId, emitter);

        emitter.onCompletion(() -> emitters.remove(userId));
        emitter.onTimeout(() -> emitters.remove(userId));
        emitter.onError((e) -> emitters.remove(userId));

        return emitter;
    }

    @Override
    public void sendRoomNotification(Long userId, Long roomId) {
        SseEmitter emitter = emitters.get(userId);
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event().name("roomNotification").data(roomId));
            } catch (Exception e) {
                throw new ApiException(ExceptionEnum.MISSING_SSE_EMITTER);
            }
        }
    }

    @Override
    public void sendGameStartNotification(Long userId, Long roomId) {
        SseEmitter emitter = emitters.get(userId);
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event().name("gameNotification").data(roomId));
            } catch (Exception e) {
                throw new ApiException(ExceptionEnum.MISSING_SSE_EMITTER);
            }
        }
    }

    @Override
    public void sendAdminNotification(Long userId,AdminDTO adminDTO) {
        SseEmitter emitter = emitters.get(userId);
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event().name("adminNotification").data(adminDTO));
            } catch (Exception e) {
                throw new ApiException(ExceptionEnum.MISSING_SSE_EMITTER);
            }
        }
    }
}
