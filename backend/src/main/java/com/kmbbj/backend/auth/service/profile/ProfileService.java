package com.kmbbj.backend.auth.service.profile;

import com.kmbbj.backend.auth.controller.response.UserProfileReponse;

public interface ProfileService {
    UserProfileReponse UserProfilefindByUserId(Long userId);
}
