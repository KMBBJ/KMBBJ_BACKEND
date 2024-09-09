package com.kmbbj.backend.feature.auth.service.profile;

import com.kmbbj.backend.feature.auth.controller.response.UserProfileReponse;

public interface ProfileService {
    UserProfileReponse UserProfilefindByUserId(Long userId);
}
