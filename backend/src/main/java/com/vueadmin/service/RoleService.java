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
public class RoleService {

    private final RoleRepository roleRepository;
    private final RolePermissionRepository rolePermissionRepository;
    private final PermissionRepository permissionRepository;

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public List<Role> getAllRoles() {
        return roleRepository.findAll();
    }

    public PageResult<RoleDto> getRoleList(String name) {
        List<Role> roles = roleRepository.searchRoles(name);

        List<RoleDto> list = roles.stream().map(role -> {
            RoleDto dto = new RoleDto();
            dto.setId(role.getId());
            dto.setName(role.getName());
            dto.setCode(role.getCode());
            dto.setDescription(role.getDescription());
            dto.setStatus(role.getStatus().name());
            dto.setPermissions(rolePermissionRepository.findPermissionIdsByRoleId(role.getId()));
            dto.setCreatedAt(role.getCreatedAt() != null ? role.getCreatedAt().format(formatter) : null);
            return dto;
        }).collect(Collectors.toList());

        return new PageResult<>(list, (long) list.size());
    }

    @Transactional
    public Role createRole(RoleDto roleDto) {
        Role role = new Role();
        role.setName(roleDto.getName());
        role.setCode(roleDto.getCode());
        role.setDescription(roleDto.getDescription());
        role.setStatus(roleDto.getStatus() != null ? Role.Status.valueOf(roleDto.getStatus()) : Role.Status.active);

        Role saved = roleRepository.save(role);

        if (roleDto.getPermissions() != null && !roleDto.getPermissions().isEmpty()) {
            for (Long permId : roleDto.getPermissions()) {
                RolePermission rp = new RolePermission();
                rp.setRoleId(saved.getId());
                rp.setPermissionId(permId);
                rolePermissionRepository.save(rp);
            }
        }

        return saved;
    }

    @Transactional
    public void updateRole(Long id, RoleDto roleDto) {
        Role role = roleRepository.findById(id).orElse(null);
        if (role == null) return;

        role.setName(roleDto.getName());
        role.setCode(roleDto.getCode());
        role.setDescription(roleDto.getDescription());
        role.setStatus(Role.Status.valueOf(roleDto.getStatus()));

        roleRepository.save(role);

        if (roleDto.getPermissions() != null) {
            rolePermissionRepository.deleteByRoleId(id);
            for (Long permId : roleDto.getPermissions()) {
                RolePermission rp = new RolePermission();
                rp.setRoleId(id);
                rp.setPermissionId(permId);
                rolePermissionRepository.save(rp);
            }
        }
    }

    @Transactional
    public void deleteRole(Long id) {
        roleRepository.deleteById(id);
    }
}
