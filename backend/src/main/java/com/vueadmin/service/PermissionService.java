package com.vueadmin.service;

import com.vueadmin.dto.*;
import com.vueadmin.entity.*;
import com.vueadmin.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PermissionService {

    private final PermissionRepository permissionRepository;

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public PageResult<PermissionDto> getPermissionList(String name) {
        List<Permission> permissions = permissionRepository.searchPermissions(name);

        List<PermissionDto> list = permissions.stream().map(p -> {
            PermissionDto dto = new PermissionDto();
            dto.setId(p.getId());
            dto.setName(p.getName());
            dto.setCode(p.getCode());
            dto.setType(p.getType().name());
            dto.setParentId(p.getParentId());
            dto.setPath(p.getPath());
            dto.setIcon(p.getIcon());
            dto.setSort(p.getSort());
            dto.setStatus(p.getStatus().name());
            dto.setCreatedAt(p.getCreatedAt() != null ? p.getCreatedAt().format(formatter) : null);
            return dto;
        }).collect(Collectors.toList());

        return new PageResult<>(list, (long) list.size());
    }

    public List<PermissionDto> getPermissionTree() {
        List<Permission> permissions = permissionRepository.findAll();

        List<PermissionDto> list = permissions.stream().map(p -> {
            PermissionDto dto = new PermissionDto();
            dto.setId(p.getId());
            dto.setName(p.getName());
            dto.setCode(p.getCode());
            dto.setType(p.getType().name());
            dto.setParentId(p.getParentId());
            dto.setPath(p.getPath());
            dto.setIcon(p.getIcon());
            dto.setSort(p.getSort());
            dto.setStatus(p.getStatus().name());
            dto.setCreatedAt(p.getCreatedAt() != null ? p.getCreatedAt().format(formatter) : null);
            return dto;
        }).collect(Collectors.toList());

        return buildTree(list, null);
    }

    private List<PermissionDto> buildTree(List<PermissionDto> items, Long parentId) {
        List<PermissionDto> result = new ArrayList<>();
        for (PermissionDto item : items) {
            if ((parentId == null && item.getParentId() == null) ||
                (parentId != null && parentId.equals(item.getParentId()))) {
                item.setChildren(buildTree(items, item.getId()));
                result.add(item);
            }
        }
        return result;
    }

    @Transactional
    public Permission createPermission(PermissionDto dto) {
        Permission p = new Permission();
        p.setName(dto.getName());
        p.setCode(dto.getCode());
        p.setType(dto.getType() != null ? Permission.Type.valueOf(dto.getType()) : Permission.Type.menu);
        p.setParentId(dto.getParentId());
        p.setPath(dto.getPath());
        p.setIcon(dto.getIcon());
        p.setSort(dto.getSort() != null ? dto.getSort() : 0);
        p.setStatus(dto.getStatus() != null ? Permission.Status.valueOf(dto.getStatus()) : Permission.Status.active);

        return permissionRepository.save(p);
    }

    @Transactional
    public void updatePermission(Long id, PermissionDto dto) {
        Permission p = permissionRepository.findById(id).orElse(null);
        if (p == null) return;

        p.setName(dto.getName());
        p.setCode(dto.getCode());
        p.setType(Permission.Type.valueOf(dto.getType()));
        p.setParentId(dto.getParentId());
        p.setPath(dto.getPath());
        p.setIcon(dto.getIcon());
        p.setSort(dto.getSort());
        p.setStatus(Permission.Status.valueOf(dto.getStatus()));

        permissionRepository.save(p);
    }

    @Transactional
    public void deletePermission(Long id) {
        permissionRepository.deleteById(id);
    }
}
