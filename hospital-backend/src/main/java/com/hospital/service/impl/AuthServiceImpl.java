package com.hospital.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hospital.dto.LoginDTO;
import com.hospital.dto.LoginResponseDTO;
import com.hospital.entity.SysUser;
import com.hospital.repository.SysUserRepository;
import com.hospital.service.AuthService;
import com.hospital.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    
    private final SysUserRepository sysUserRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    
    @Override
    public LoginResponseDTO login(LoginDTO loginDTO) {
        // 查询用户
        SysUser user = sysUserRepository.selectOne(
            new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, loginDTO.getUsername())
                .eq(SysUser::getStatus, 1)
        );
        
        if (user == null) {
            throw new RuntimeException("用户名或密码错误");
        }
        
        // 验证密码
        if (!passwordEncoder.matches(loginDTO.getPassword(), user.getPassword())) {
            throw new RuntimeException("用户名或密码错误");
        }
        
        // 生成Token
        String token = jwtUtil.generateToken(user.getUsername(), user.getId(), user.getUserType());
        
        // 构建响应
        LoginResponseDTO response = new LoginResponseDTO();
        response.setToken(token);
        
        LoginResponseDTO.UserInfo userInfo = new LoginResponseDTO.UserInfo();
        userInfo.setId(user.getId());
        userInfo.setUsername(user.getUsername());
        userInfo.setUserType(user.getUserType());
        userInfo.setPhone(user.getPhone());
        userInfo.setPatientId(user.getPatientId());
        userInfo.setDoctorId(user.getDoctorId());
        response.setUserInfo(userInfo);
        
        return response;
    }
    
    @Override
    public void logout(String token) {
        // JWT是无状态的，这里可以记录到Redis黑名单
        // 简化实现，直接返回成功
    }
    
    @Override
    public LoginResponseDTO.UserInfo getCurrentUser(String token) {
        String username = jwtUtil.getUsernameFromToken(token);
        SysUser user = sysUserRepository.selectOne(
            new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, username)
        );
        
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        
        LoginResponseDTO.UserInfo userInfo = new LoginResponseDTO.UserInfo();
        userInfo.setId(user.getId());
        userInfo.setUsername(user.getUsername());
        userInfo.setUserType(user.getUserType());
        userInfo.setPhone(user.getPhone());
        userInfo.setPatientId(user.getPatientId());
        userInfo.setDoctorId(user.getDoctorId());
        
        return userInfo;
    }
}

