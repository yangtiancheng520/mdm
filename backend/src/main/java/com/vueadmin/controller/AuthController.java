package com.vueadmin.controller;

import com.vueadmin.dto.*;
import com.vueadmin.entity.User;
import com.vueadmin.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @PostMapping("/login")
    public ApiResponse<?> login(@RequestBody Map<String, String> params) {
        String account = params.get("account");
        String password = params.get("password");

        User user = userService.login(account, password);
        if (user == null) {
            return ApiResponse.error(401, "账号或密码错误");
        }

        List<String> roles = userService.getRoleNamesByUserId(user.getId());
        List<String> permissions = userService.getPermissionCodesByUserId(user.getId());

        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", user.getId());
        userInfo.put("account", user.getAccount());
        userInfo.put("name", user.getName());
        userInfo.put("avatar", "https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png");
        userInfo.put("roles", roles);
        userInfo.put("permissions", permissions);

        Map<String, Object> data = new HashMap<>();
        data.put("token", "token_" + user.getId() + "_" + System.currentTimeMillis());
        data.put("user", userInfo);

        return ApiResponse.success(data);
    }

    @GetMapping("/info")
    public ApiResponse<?> getInfo(@RequestHeader(value = "Authorization", required = false) String token) {
        if (token == null || !token.startsWith("Bearer token_")) {
            return ApiResponse.error(401, "未登录");
        }

        String[] parts = token.replace("Bearer token_", "").split("_");
        Long userId = Long.parseLong(parts[0]);

        User user = userService.getById(userId);
        if (user == null) {
            return ApiResponse.error(401, "用户不存在");
        }

        List<String> roles = userService.getRoleNamesByUserId(user.getId());
        List<String> permissions = userService.getPermissionCodesByUserId(user.getId());

        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", user.getId());
        userInfo.put("account", user.getAccount());
        userInfo.put("name", user.getName());
        userInfo.put("avatar", "https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png");
        userInfo.put("roles", roles);
        userInfo.put("permissions", permissions);

        return ApiResponse.success(userInfo);
    }
}
