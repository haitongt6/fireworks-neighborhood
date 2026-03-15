package com.fireworks.admin.controller;

import com.fireworks.common.api.Result;
import com.fireworks.model.constant.PermissionConstant;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * 文件上传接口。
 * <p>
 * category: main-主图, video-主图视频, desc-详情图
 * </p>
 */
@RestController
@RequestMapping("/admin/file")
public class FileUploadController {

    private static final List<String> IMAGE_EXT = Arrays.asList(".jpg", ".jpeg", ".png", ".gif", ".webp");
    private static final List<String> VIDEO_EXT = Arrays.asList(".mp4", ".webm", ".mov", ".avi");

    @Value("${file.upload-dir:D:/firrword-neighborhood-file}")
    private String uploadDir;

    @PostMapping("/upload")
    @PreAuthorize("hasAuthority(T(com.fireworks.model.constant.PermissionConstant).PRODUCT_ADD) or hasAuthority(T(com.fireworks.model.constant.PermissionConstant).PRODUCT_EDIT)")
    public Result<String> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam("category") String category) {
        if (file == null || file.isEmpty()) {
            return Result.failed("请选择文件");
        }
        String originalName = file.getOriginalFilename();
        if (originalName == null || originalName.isEmpty()) {
            return Result.failed("文件名无效");
        }
        String ext = originalName.contains(".") ? originalName.substring(originalName.lastIndexOf(".")).toLowerCase() : "";
        Path dir;
        switch (category) {
            case "main":
                if (!IMAGE_EXT.contains(ext)) {
                    return Result.failed("主图仅支持: " + String.join(", ", IMAGE_EXT));
                }
                dir = Paths.get(uploadDir);
                break;
            case "video":
                if (!VIDEO_EXT.contains(ext)) {
                    return Result.failed("视频仅支持: " + String.join(", ", VIDEO_EXT));
                }
                dir = Paths.get(uploadDir, "videos");
                break;
            case "desc":
                if (!IMAGE_EXT.contains(ext)) {
                    return Result.failed("详情图仅支持: " + String.join(", ", IMAGE_EXT));
                }
                dir = Paths.get(uploadDir, "descs");
                break;
            default:
                return Result.failed("分类无效");
        }
        try {
            Files.createDirectories(dir);
            String fileName = UUID.randomUUID().toString().replace("-", "") + ext;
            Path target = dir.resolve(fileName);
            file.transferTo(target.toFile());
            // 返回前端可访问的 URL：/api/file/xxx（经代理转发）
            // relativePath 须与 dir 子目录一致：main→根目录, video→videos/, desc→descs/
            String relativePath;
            if ("main".equals(category)) {
                relativePath = fileName;
            } else if ("video".equals(category)) {
                relativePath = "videos/" + fileName;
            } else if ("desc".equals(category)) {
                relativePath = "descs/" + fileName;
            } else {
                relativePath = category + "/" + fileName;
            }
            return Result.success("/api/file/" + relativePath);
        } catch (IOException e) {
            return Result.failed("上传失败: " + e.getMessage());
        }
    }
}
