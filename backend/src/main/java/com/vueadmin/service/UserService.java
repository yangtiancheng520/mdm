package com.vueadmin.service;

import com.vueadmin.dto.*;
import com.vueadmin.entity.*;
import com.vueadmin.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final RoleRepository roleRepository;
    private final RolePermissionRepository rolePermissionRepository;
    private final PermissionRepository permissionRepository;
    private final OrganizationRepository organizationRepository;

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

    public PageResult<UserDto> getUserList(String account, String name, String status, Long orgId) {
        User.Status statusEnum = status != null && !status.isEmpty() ? User.Status.valueOf(status) : null;

        // orgId 为 0 或 null 时，查询所有用户
        Long effectiveOrgId = (orgId != null && orgId > 0) ? orgId : null;

        log.info("查询用户列表: account={}, name={}, status={}, orgId={}, effectiveOrgId={}",
            account, name, status, orgId, effectiveOrgId);

        List<User> users = userRepository.searchUsers(account, name, statusEnum, effectiveOrgId);

        List<UserDto> list = users.stream().map(user -> {
            UserDto dto = new UserDto();
            dto.setId(user.getId());
            dto.setAccount(user.getAccount());
            dto.setName(user.getName());
            dto.setEmail(user.getEmail());
            dto.setPhone(user.getPhone());
            dto.setOrgId(user.getOrgId());

            // 获取组织名称
            if (user.getOrgId() != null) {
                organizationRepository.findById(user.getOrgId())
                    .ifPresent(org -> dto.setOrgName(org.getOrgName()));
            }

            dto.setStatus(user.getStatus().name());
            dto.setRoles(userRoleRepository.findRoleIdsByUserId(user.getId()));
            dto.setCreatedAt(user.getCreatedAt() != null ? user.getCreatedAt().format(formatter) : null);
            return dto;
        }).collect(Collectors.toList());

        return new PageResult<>(list, (long) list.size());
    }

    @Transactional
    public User createUser(UserDto userDto) {
        log.info("创建用户: account={}, name={}, orgId={}", userDto.getAccount(), userDto.getName(), userDto.getOrgId());

        User user = new User();
        user.setAccount(userDto.getAccount());
        user.setPassword(userDto.getPassword());
        user.setName(userDto.getName());
        user.setEmail(userDto.getEmail());
        user.setPhone(userDto.getPhone());
        user.setOrgId(userDto.getOrgId());
        user.setStatus(userDto.getStatus() != null ? User.Status.valueOf(userDto.getStatus()) : User.Status.active);

        User saved = userRepository.save(user);
        log.info("用户创建成功: id={}", saved.getId());

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

        log.info("更新用户 ID: {}, 接收到的数据: account={}, name={}, orgId={}, email={}, phone={}, status={}",
            id, userDto.getAccount(), userDto.getName(), userDto.getOrgId(),
            userDto.getEmail(), userDto.getPhone(), userDto.getStatus());

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
        // orgId 可以为 null，表示清空组织
        user.setOrgId(userDto.getOrgId());
        if (userDto.getStatus() != null) {
            user.setStatus(User.Status.valueOf(userDto.getStatus()));
        }

        log.info("保存用户前: orgId={}", user.getOrgId());
        userRepository.save(user);
        log.info("用户保存成功");

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
