package com.kmbbj.backend.admin.service;


import com.kmbbj.backend.admin.entity.AdminAlarm;
import com.kmbbj.backend.admin.repository.AdminAlarmRepository;
import com.kmbbj.backend.auth.entity.User;
import com.kmbbj.backend.auth.repository.UserRepository;
import com.kmbbj.backend.global.config.exception.ApiException;
import com.kmbbj.backend.global.config.exception.ExceptionEnum;
import com.kmbbj.backend.global.config.jwt.infrastructure.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;


@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final AdminAlarmRepository adminAlarmRepository;

    /**
     *
     * 유저 리스트 페이징
     * @param pageable 페이지 넘버, 페이지 사이즈, 정렬 정보를 포함하는 Pageable 객체
     * @return  유저 리스트가 들어 있는 Page<User> 객체
     */
    @Transactional
    public Page<User> findAllUser(Pageable pageable) {
        Pageable sortedByDescId = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(),
                Sort.by(Sort.Direction.DESC, "id")); //id를 따라 내림차순 정렬
        Page<User> users = userRepository.findAll(sortedByDescId); //모든 유저 내림차순으로 가져오기
        if (users.isEmpty()) {
            throw new ApiException(ExceptionEnum.USER_NOT_FOUND); // 유저가 없을 경우 예외 발생
        }
        return users;
    }

    /**
     *이메일 검색
     *
     * @param email 검색당한 이메일
     * @return 검색한 이메일 리턴
     */
    @Transactional
    public List<User> searchUserByEmail(String email) {
        List<User> users = userRepository.findByEmailContainingIgnoreCase(email); // 값에 따른 이메일 가져옴
        if (users.isEmpty()) {
            throw new ApiException(ExceptionEnum.USER_NOT_FOUND); // 값에 따른 이메일의 유저가 없을 경우 예외 발생
        }
        return users;
    }





    /**
     * 관리자 알림 페이지
     *
     * @param pageable 페이지 넘버, 페이지 사이즈, 정렬 정보를 포함하는 Pageable 객체
     * @return 페이징 처리된 알람들 리턴
     */
    @Transactional
    public Page<AdminAlarm> findAllAdminAlarm(Pageable pageable) {
        Pageable sortedByDescId = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(),
                Sort.by(Sort.Direction.DESC, "id")); // id를 따라 내림차순 정렬
        Page<AdminAlarm> adminAlarms = adminAlarmRepository.findAll(sortedByDescId); // 모든 알람 정보 내림차순으로 가져오기

        if (adminAlarms.isEmpty()) {
            throw new ApiException(ExceptionEnum.ALARM_NOT_FOUND); // 알람이 없는 경우 예외 발생
        }
        return adminAlarms;
    }



    /**
     * 알림 저장 서비스
     *
     * @param id 알람 발행 유저 아이디
     * @param adminAlarm 알람 title / content
     * @return 저장된 알람 정보 리턴
     */
    @Transactional
    public AdminAlarm saveAlarm(Long id, AdminAlarm adminAlarm) {
        if (id == null) {
            throw new ApiException(ExceptionEnum.USER_NOT_FOUND); // 유저가 null인 경우 예외 발생
        }
        if (adminAlarm == null) {
            throw new ApiException(ExceptionEnum.ALARM_NOT_FOUND); // 알람이 null인 경우 예외 발생
        }

        try {
            // 유저가 존재하는지 확인
            User user = userRepository.findById(id)
                    .orElseThrow(() -> new ApiException(ExceptionEnum.USER_NOT_FOUND)); // 유저를 찾지 못한 경우 예외 발생

            adminAlarm.setUser(user); // adminAlarm에 사용자 정보 설정
            return adminAlarmRepository.save(adminAlarm); // 알람 저장
        } catch (Exception e) {
            throw new ApiException(ExceptionEnum.INTERNAL_SERVER_ERROR); // 저장 중 예외 발생 시
        }
    }



    /**
     * 인증된 사용자 정보를 가져오는 서비스 메서드
     *
     * @return 인증된 사용자 정보를 반환
     */
    @Transactional(readOnly = true)
    public User getAuthenticatedUser() {
        // 인증 정보를 가져오기
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // 인증 정보가 없거나 사용자가 인증되지 않은 경우 예외 발생
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ApiException(ExceptionEnum.UNAUTHORIZED);
        }

        // 인증 정보에서 사용자 세부 정보가 유효하지 않은 경우 예외 발생
        if (!(authentication.getPrincipal() instanceof CustomUserDetails customUserDetails)) {
            throw new ApiException(ExceptionEnum.INVALID_USER_DETAILS);
        }

        Long id = customUserDetails.getUserId();  // 사용자 ID를 가져옴

        return userRepository.findById(id)// 사용자 ID를 통해 데이터베이스에서 사용자를 조회
                .orElseThrow(() -> new ApiException(ExceptionEnum.USER_NOT_FOUND)); // 사용자가 존재하지 않으면 예외를 발생시킴
    }

}