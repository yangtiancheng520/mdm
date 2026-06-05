package com.vueadmin.controller;

import com.vueadmin.dto.*;
import com.vueadmin.entity.User;
import com.vueadmin.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/list")
    public ApiResponse<PageResult<UserDto>> list(
            @RequestParam(required = false) String account,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String status) {
        return ApiResponse.success(userService.getUserList(account, name, status));
    }

    @PostMapping("/create")
    public ApiResponse<UserDto> create(@RequestBody UserDto userDto) {
        User user = userService.createUser(userDto);
        UserDto result = new UserDto();
        result.setId(user.getId());
        result.setAccount(user.getAccount());
        result.setName(user.getName());
        return ApiResponse.success(result);
    }

    @PutMapping("/update/{id}")
    public ApiResponse<?> update(@PathVariable Long id, @RequestBody UserDto userDto) {
        userService.updateUser(id, userDto);
        return ApiResponse.success();
    }

    @DeleteMapping("/delete/{id}")
    public ApiResponse<?> delete(@PathVariable Long id) {
        userService.deleteUser(id);
        return ApiResponse.success();
    }

    @PostMapping("/batch-delete")
    public ApiResponse<?> batchDelete(@RequestBody Map<String, List<Long>> params) {
        List<Long> ids = params.get("ids");
        userService.batchDeleteUsers(ids);
        return ApiResponse.success();
    }
}
