package com.hospital.controller;

import com.hospital.annotation.RequireRole;
import com.hospital.common.Result;
import com.hospital.util.JwtUtil;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 文件上传控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/upload")
public class FileUploadController {

    @Autowired
    private JwtUtil jwtUtil;

    @Value("${file.upload.path:./uploads}")
    private String uploadPath;

    @Value("${file.upload.url-prefix:/uploads}")
    private String urlPrefix;
    
    /**
     * 检查用户是否为管理员
     */
    private boolean isAdmin(String token) {
        if (token == null || jwtUtil == null) {
            return false;
        }
        try {
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            if (!jwtUtil.validateToken(token)) {
                return false;
            }
            Claims claims = jwtUtil.getClaimsFromToken(token);
            Integer userType = claims.get("userType", Integer.class);
            return userType != null && userType == 3; // 3 = 管理员
        } catch (Exception e) {
            log.error("验证管理员权限失败", e);
            return false;
        }
    }

    /**
     * 上传医生头像
     */
    @PostMapping("/avatar")
    public ResponseEntity<Result<Map<String, String>>> uploadAvatar(
            @RequestParam("file") MultipartFile file,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        
        // 手动检查管理员权限
        if (!isAdmin(authHeader)) {
            return ResponseEntity.ok(Result.error(403, "权限不足，仅管理员可上传头像"));
        }
        try {
            // 验证文件
            if (file == null || file.isEmpty()) {
                return ResponseEntity.ok(Result.error(400, "文件不能为空"));
            }

            // 验证文件类型
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return ResponseEntity.ok(Result.error(400, "只能上传图片文件"));
            }

            // 验证文件大小（限制5MB）
            if (file.getSize() > 5 * 1024 * 1024) {
                return ResponseEntity.ok(Result.error(400, "文件大小不能超过5MB"));
            }

            // 创建上传目录
            Path uploadDir = Paths.get(uploadPath, "avatars");
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }

            // 生成唯一文件名
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String filename = UUID.randomUUID().toString() + extension;

            // 保存文件
            Path filePath = uploadDir.resolve(filename);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // 返回文件URL
            String fileUrl = urlPrefix + "/avatars/" + filename;
            Map<String, String> result = new HashMap<>();
            result.put("url", fileUrl);
            result.put("filename", filename);

            log.info("头像上传成功: {}", fileUrl);
            return ResponseEntity.ok(Result.success("上传成功", result));
        } catch (IOException e) {
            log.error("头像上传失败", e);
            return ResponseEntity.ok(Result.error(500, "上传失败：" + e.getMessage()));
        }
    }

    /**
     * 删除头像文件
     */
    @DeleteMapping("/avatar/{filename}")
    public ResponseEntity<Result<Object>> deleteAvatar(
            @PathVariable String filename,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        
        // 手动检查管理员权限
        if (!isAdmin(authHeader)) {
            return ResponseEntity.ok(Result.error(403, "权限不足，仅管理员可删除头像"));
        }
        try {
            Path filePath = Paths.get(uploadPath, "avatars", filename);
            if (Files.exists(filePath)) {
                Files.delete(filePath);
                log.info("头像删除成功: {}", filename);
                return ResponseEntity.ok(Result.success("删除成功", null));
            } else {
                return ResponseEntity.ok(Result.error(404, "文件不存在"));
            }
        } catch (IOException e) {
            log.error("头像删除失败", e);
            return ResponseEntity.ok(Result.error(500, "删除失败：" + e.getMessage()));
        }
    }
}

