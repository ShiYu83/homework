package com.hospital.service;

import com.hospital.dto.LoginDTO;
import com.hospital.dto.LoginResponseDTO;

public interface AuthService {
    LoginResponseDTO login(LoginDTO loginDTO);
    void logout(String token);
    LoginResponseDTO.UserInfo getCurrentUser(String token);
}

