package org.iotf.authservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.iotf.authservice.service.AliOssService;
import org.iotf.authservice.service.ITFileService;
import org.iotf.entity.auth.JwtPayload;
import org.iotf.requestFormation.auth.*;
import org.iotf.wrapper.responseHandle.ApiResponse;
import org.iotf.wrapper.responseHandle.ApiResponseWrap;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Set;

/**
 * <p>
 * 文件 前端控制器
 * </p>
 *
 * @author Centripet
 * @since 2026-05-08
 */
@Slf4j
@Tag(name = "", description = "")
@RestController
@RequestMapping("/api/file")
@RequiredArgsConstructor
public class TFileController {
    private final AliOssService ossService;
    private final ITFileService fileService;
    private static final Set<String> ALLOWED_SUFFIXES = Set.of(
            ".jpg", ".jpeg", ".png", ".gif", ".bmp",
            ".pdf", ".doc", ".docx", ".xls", ".xlsx",
            ".txt", ".zip", ".rar", ".mp4" ,
            "gltf", "obj"
    );

    @PostMapping("/uploadPolicy")
    @ApiResponseWrap
    @Operation(summary = "文件上传凭证生成", description = "MAX_SIZE_FLAG: 0/1 限制文件大小 50M/500M")
    public ApiResponse<?> uploadPolicy(
            @Valid @RequestBody fileUploadRequest request,
            HttpServletResponse response,
            @AuthenticationPrincipal JwtPayload payload
    ) {
        Long user_id = payload.getUser_id();

        if (request.suffix() == null ||
                request.suffix().isBlank() ||
                !ALLOWED_SUFFIXES.contains(request.suffix().toLowerCase())) {
            return ApiResponse.fail(400, "非法的文件后缀名");
        }

        String key = ossService.generateObjectKey("general", user_id, request.suffix());
        if (request.MAX_SIZE_FLAG() == null || request.MAX_SIZE_FLAG() == 0) {
            return ApiResponse.success(ossService.generateUploadPolicy(key, AliOssService.MAX_FILE_SIZE_50MB));
        } else if (request.MAX_SIZE_FLAG() == 1) {
            return ApiResponse.success(ossService.generateUploadPolicy(key, AliOssService.MAX_FILE_SIZE_500MB));
        }

        return ApiResponse.fail(500, "生成凭证失败");
    }

    @PostMapping("/uploadStatus")
    @ApiResponseWrap
    @Operation(summary = "文件上传状态", description = "文件在OSS中上传情况")
    public ApiResponse<?> uploadStatus(
            @Valid @RequestBody uploadStatusRequest request,
            HttpServletResponse response
    ) {
        if (!request.files().isEmpty()) {
            Map<uploadSubmitRequest, Boolean> keyMap = ossService.fileExistsList(request.files());

            return ApiResponse.success(keyMap);
        } else
            return ApiResponse.fail(400, "无效参数");
    }

    @PostMapping("/fileSubmit")
    @ApiResponseWrap
    @Operation(summary = "文件提交", description = "文件上传成功则文件信息入库返回文件信息")
    public ApiResponse<?> fileSubmit(
            @Valid @RequestBody fileSubmitRequest request,
            HttpServletResponse response,
            @AuthenticationPrincipal JwtPayload payload
    ) {
        Long user_id = payload.getUser_id();

        if (!request.files().isEmpty()) {
            Map<uploadSubmitRequest, Boolean> keyMap = ossService.fileExistsList(request.files());

            if (ossService.fileExists(keyMap)) {
                return ApiResponse.success(fileService.uploadSubmit(user_id, request, false));
            }

            return ApiResponse.fail(400, "文件上传未全部成功",keyMap);
        } else
            return ApiResponse.fail(400, "无效参数");

    }

    @PostMapping("/fileUrlsGen")
    @ApiResponseWrap
    @Operation(summary = "文件url获取", description = "")
    public ApiResponse<?> fileUrlsGen(
            @Valid @RequestBody fileUrlsGenRequest request,
            HttpServletResponse response,
            @AuthenticationPrincipal JwtPayload payload
    ) {
        Long user_id = payload.getUser_id();

        if (request.files_id().isEmpty()) {
            return ApiResponse.fail(401, "空文件id列表");
        }

        if (request.files_id().size()>100) {
            return ApiResponse.fail(401, "过大的id列表");
        }

        return ApiResponse.success(fileService.fileUrlsGen(request));
    }

}
