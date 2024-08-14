package com.kmbbj.backend.admin.service;


import com.kmbbj.backend.auth.entity.User;
import com.kmbbj.backend.auth.repository.UserRepository;
import com.kmbbj.backend.global.config.exception.ApiException;
import com.kmbbj.backend.global.config.exception.ExceptionEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;


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
}
