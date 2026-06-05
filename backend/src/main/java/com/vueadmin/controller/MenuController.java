package com.vueadmin.controller;

import com.vueadmin.dto.ApiResponse;
import com.vueadmin.dto.MenuDto;
import com.vueadmin.service.MenuService;
import com.vueadmin.config.security.JwtAuthenticationFilter.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 菜单控制器
 */
@Tag(name = "菜单管理", description = "菜单相关接口")
@RestController
@RequestMapping("/api/menu")
@RequiredArgsConstructor
public class MenuController {

    private final MenuService menuService;

    /**
     * 获取当前用户的菜单树
     */
    @Operation(summary = "获取用户菜单")
    @GetMapping("/user")
    public ApiResponse<List<MenuDto>> getUserMenu(Authentication authentication) {
        Long userId = getUserId(authentication);
        return ApiResponse.success(menuService.getUserMenuTree(userId));
    }

    /**
     * 获取所有菜单树（用于权限管理）
     */
    @Operation(summary = "获取所有菜单")
    @GetMapping("/all")
    public ApiResponse<List<MenuDto>> getAllMenu() {
        return ApiResponse.success(menuService.getAllMenuTree());
    }

    /**
     * 从认证信息中获取用户ID
     */
    private Long getUserId(Authentication authentication) {
        if (authentication == null) {
            return null;
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof UserPrincipal) {
            return ((UserPrincipal) principal).userId();
        }
        if (principal instanceof Long) {
            return (Long) principal;
        }
        return null;
    }
}
