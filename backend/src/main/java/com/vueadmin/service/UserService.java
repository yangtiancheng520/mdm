package com.vueadmin.service;

import com.vueadmin.dto.*;
import com.vueadmin.entity.*;
import com.vueadmin.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final RoleRepository roleRepository;
    private final RolePermissionRepository rolePermissionRepository;
    private final PermissionRepository permissionRepository;

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public User login(String account, String password) {
        return userRepository.findByAccountAndPassword(account, password).orElse(null);
    }

    public User getByAccount(String account) {
        return userRepository.findByAccount(account).orElse(null);
    }

    public User getById(Long id) {
        return userRepository.findById(id).orElse(null);
    }

    public List<String> getRoleNamesByUserId(Long userId) {
        List<Long> roleIds = userRoleRepository.findRoleIdsByUserId(userId);
        return roleRepository.findAllById(roleIds).stream()
                .map(Role::getName)
                .collect(Collectors.toList());
    }

    public List<String> getPermissionCodesByUserId(Long userId) {
        List<Long> roleIds = userRoleRepository.findRoleIdsByUserId(userId);
        List<Long> permissionIds = rolePermissionRepository.findPermissionIdsByRoleIds(roleIds);
        return permissionRepository.findAllById(permissionIds).stream()
                .map(Permission::getCode)
                .collect(Collectors.toList());
    }

    public PageResult<UserDto> getUserList(String account, String name, String status) {
        User.Status statusEnum = status != null && !status.isEmpty() ? User.Status.valueOf(status) : null;
        List<User> users = userRepository.searchUsers(account, name, statusEnum);

        List<UserDto> list = users.stream().map(user -> {
            UserDto dto = new UserDto();
            dto.setId(user.getId());
            dto.setAccount(user.getAccount());
            dto.setName(user.getName());
            dto.setEmail(user.getEmail());
            dto.setPhone(user.getPhone());
            dto.setStatus(user.getStatus().name());
            dto.setRoles(userRoleRepository.findRoleIdsByUserId(user.getId()));
            dto.setCreatedAt(user.getCreatedAt() != null ? user.getCreatedAt().format(formatter) : null);
            return dto;
        }).collect(Collectors.toList());

        return new PageResult<>(list, (long) list.size());
    }

    @Transactional
    public User createUser(UserDto userDto) {
        User user = new User();
        user.setAccount(userDto.getAccount());
        user.setPassword(userDto.getPassword());
        user.setName(userDto.getName());
        user.setEmail(userDto.getEmail());
        user.setPhone(userDto.getPhone());
        user.setStatus(userDto.getStatus() != null ? User.Status.valueOf(userDto.getStatus()) : User.Status.active);

        User saved = userRepository.save(user);

        if (userDto.getRoles() != null && !userDto.getRoles().isEmpty()) {
            for (Long roleId : userDto.getRoles()) {
                UserRole ur = new UserRole();
                ur.setUserId(saved.getId());
                ur.setRoleId(roleId);
                userRoleRepository.save(ur);
            }
        }

        return saved;
    }

    @Transactional
    public void updateUser(Long id, UserDto userDto) {
        User user = userRepository.findById(id).orElse(null);
        if (user == null) return;

        if (userDto.getPassword() != null && !userDto.getPassword().isEmpty()) {
            user.setPassword(userDto.getPassword());
        }
        if (userDto.getName() != null) {
            user.setName(userDto.getName());
        }
        if (userDto.getEmail() != null) {
            user.setEmail(userDto.getEmail());
        }
        if (userDto.getPhone() != null) {
            user.setPhone(userDto.getPhone());
        }
        if (userDto.getStatus() != null) {
            user.setStatus(User.Status.valueOf(userDto.getStatus()));
        }

        userRepository.save(user);

        if (userDto.getRoles() != null) {
            userRoleRepository.deleteByUserId(id);
            for (Long roleId : userDto.getRoles()) {
                UserRole ur = new UserRole();
                ur.setUserId(id);
                ur.setRoleId(roleId);
                userRoleRepository.save(ur);
            }
        }
    }

    @Transactional
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    @Transactional
    public void batchDeleteUsers(List<Long> ids) {
        userRepository.deleteAllById(ids);
    }
}
