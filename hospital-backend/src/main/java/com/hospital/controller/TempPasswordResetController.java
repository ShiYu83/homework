package com.hospital.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hospital.entity.SysUser;
import com.hospital.repository.SysUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/temp")
@RequiredArgsConstructor
public class TempPasswordResetController {

    private final SysUserRepository sysUserRepository;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/reset-password")
    public String resetPassword(@RequestParam String username, @RequestParam String newPassword) {
        // 查询用户
        SysUser user = sysUserRepository.selectOne(
                new LambdaQueryWrapper<SysUser>()
                        .eq(SysUser::getUsername, username)
        );

        if (user == null) {
            return "User not found";
        }

        // 加密新密码
        String encryptedPassword = passwordEncoder.encode(newPassword);
        user.setPassword(encryptedPassword);

        // 更新用户
        sysUserRepository.updateById(user);

        return "Password reset successfully for user: " + username + ", encrypted password: " + encryptedPassword;
    }
}