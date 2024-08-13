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

    // 유저 리스트 페이징
    @Transactional
    public Page<User> findAllUser(Pageable pageable) {
        Pageable sortedByDescId = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(),
                Sort.by(Sort.Direction.DESC, "id"));
        Page<User> users = userRepository.findAll(sortedByDescId);
        if (users.isEmpty()) {
            throw new ApiException(ExceptionEnum.USER_NOT_FOUND);
        }
        return users;
    }

    // 이메일 검색
    @Transactional
    public List<User> searchUserByEmail(String email) {
        List<User> users = userRepository.findByEmailContainingIgnoreCase(email);
        if (users.isEmpty()) {
            throw new ApiException(ExceptionEnum.USER_NOT_FOUND);
        }
        return users;
    }
}
