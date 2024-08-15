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


import java.util.Date;
import java.util.List;
import java.util.Optional;

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
     * @return
     */
    @Transactional
    public Page<AdminAlarm> findAllAdminAlarm(Pageable pageable) {
        Pageable sortedByDescId = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(),
                Sort.by(Sort.Direction.DESC, "id"));
        Page<AdminAlarm> adminAlarms = adminAlarmRepository.findAll(sortedByDescId);
        if (adminAlarms.isEmpty()) {
            throw new ApiException(ExceptionEnum.ALARM_NOT_FOUND);
        }
        return adminAlarms;
    }



    /**
     * 알림 저장 서비스
     *
     * @param id
     * @param adminAlarm
     * @return
     */
    @Transactional
    public AdminAlarm saveAlarm(Long id, AdminAlarm adminAlarm) {
        if (id == null) {
            System.out.println("아이디 없음 --------------------------------------------");
            throw new ApiException(ExceptionEnum.USER_NOT_FOUND); // 유저가 null인 경우 예외 발생
        }
        if (adminAlarm == null) {
            System.out.println("알람 없음 ----------------------------------------------");
            throw new ApiException(ExceptionEnum.BAD_REQUEST); // 알람이 null인 경우 예외 발생
        }

        try {
            // adminAlarm 엔티티의 user_id를 설정
            User user = userRepository.findById(id).get();

            adminAlarm.setUser(user);
            return adminAlarmRepository.save(adminAlarm);
        } catch (Exception e) {
            throw new ApiException(ExceptionEnum.INTERNAL_SERVER_ERROR); // 저장 중 예외 발생 시 처리
        }
    }




    /**
     * 인증된 사용자 정보 가져오기
     *
     * @return
     */
    @Transactional(readOnly = true)
    public User getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // 인증 체크
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ApiException(ExceptionEnum.UNAUTHORIZED); // 인증되지 않은 경우 예외 발생
        }

        // 사용자 정보 타입 체크
        if (!(authentication.getPrincipal() instanceof CustomUserDetails customUserDetails)) {
            throw new ApiException(ExceptionEnum.INVALID_USER_DETAILS); // 사용자 정보가 올바르지 않을 때 예외 발생
        }

        Long id = customUserDetails.getUserId();

        return userRepository.findById(id)
                .orElseThrow(() -> new ApiException(ExceptionEnum.USER_NOT_FOUND)); // 사용자를 찾지 못할 경우 ApiException 사용
    }

}
