package com.hospital.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hospital.common.Result;
import com.hospital.dto.LoginDTO;
import com.hospital.dto.LoginResponseDTO;
import com.hospital.entity.SysUser;
import com.hospital.repository.SysUserRepository;
import com.hospital.service.AuthService;
import com.hospital.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final SysUserRepository sysUserRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public ResponseEntity<Result<LoginResponseDTO>> login(@RequestBody LoginDTO loginDTO) {
        LoginResponseDTO response = authService.login(loginDTO);
        return ResponseEntity.ok(Result.success("登录成功", response));
    }

    @PostMapping("/logout")
    public ResponseEntity<Result<Object>> logout(
            @RequestHeader(value = "Authorization", required = false) String token) {
        if (token != null && token.startsWith("Bearer ")) {
            authService.logout(token.substring(7));
        }
        return ResponseEntity.ok(Result.success("退出成功", null));
    }

    @GetMapping("/current")
    public ResponseEntity<Result<LoginResponseDTO.UserInfo>> getCurrentUser(
            @RequestHeader(value = "Authorization", required = false) String token) {
        if (token == null || !token.startsWith("Bearer ")) {
            return ResponseEntity.ok(Result.error(401, "未登录"));
        }
        LoginResponseDTO.UserInfo userInfo = authService.getCurrentUser(token.substring(7));
        return ResponseEntity.ok(Result.success("查询成功", userInfo));
    }

    /**
     * 修改密码接口，所有角色均可访问
     */
    @PostMapping("/change-password")
    public ResponseEntity<Result<Object>> changePassword(
            @RequestHeader("Authorization") String token,
            @RequestBody ChangePasswordDTO dto) {
        String jwtToken = token.substring(7);
        String username = jwtUtil.getUsernameFromToken(jwtToken);

        // 查询用户
        SysUser user = sysUserRepository.selectOne(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, username));

        // 验证原密码
        if (!passwordEncoder.matches(dto.getOldPassword(), user.getPassword())) {
            return ResponseEntity.ok(Result.error(400, "原密码错误"));
        }

        // 更新密码
        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        sysUserRepository.updateById(user);

        return ResponseEntity.ok(Result.success("密码修改成功", null));
    }

    @lombok.Data
    static class ChangePasswordDTO {
        private String oldPassword;
        private String newPassword;
    }
}
