package com.vueadmin.service;

import com.vueadmin.dto.MenuDto;
import com.vueadmin.entity.Permission;
import com.vueadmin.repository.PermissionRepository;
import com.vueadmin.repository.RolePermissionRepository;
import com.vueadmin.repository.UserRoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 菜单服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MenuService {

    private final UserRoleRepository userRoleRepository;
    private final RolePermissionRepository rolePermissionRepository;
    private final PermissionRepository permissionRepository;

    /**
     * 获取用户的菜单树
     */
    public List<MenuDto> getUserMenuTree(Long userId) {
        log.debug("获取用户菜单树，userId: {}", userId);

        // 1. 获取用户的所有权限ID
        List<Long> roleIds = userRoleRepository.findRoleIdsByUserId(userId);
        log.debug("用户角色ID列表: {}", roleIds);

        if (roleIds.isEmpty()) {
            log.warn("用户没有任何角色");
            return new ArrayList<>();
        }

        List<Long> permissionIds = rolePermissionRepository.findPermissionIdsByRoleIds(roleIds);
        log.debug("用户权限ID列表大小: {}", permissionIds.size());

        if (permissionIds.isEmpty()) {
            log.warn("用户没有任何权限");
            return new ArrayList<>();
        }

        // 2. 获取所有菜单权限（type = menu）
        List<Permission> allMenus = permissionRepository.findByTypeOrderBySortAsc(Permission.Type.menu);
        log.debug("所有菜单数量: {}", allMenus.size());

        // 3. 过滤出用户有权限的菜单，并自动包含所有父菜单
        Set<Long> userPermissionIds = new HashSet<>(permissionIds);
        Set<Long> menuIdsWithParents = new HashSet<>();

        // 构建菜单ID到父ID的映射
        Map<Long, Long> menuParentMap = allMenus.stream()
                .filter(m -> m.getParentId() != null)
                .collect(Collectors.toMap(Permission::getId, Permission::getParentId));

        // 对于每个用户有权限的菜单，添加它及其所有父菜单
        for (Permission menu : allMenus) {
            if (userPermissionIds.contains(menu.getId())) {
                menuIdsWithParents.add(menu.getId());
                // 递归添加所有父菜单
                Long parentId = menu.getParentId();
                while (parentId != null) {
                    menuIdsWithParents.add(parentId);
                    parentId = menuParentMap.get(parentId);
                }
            }
        }

        List<Permission> userMenus = allMenus.stream()
                .filter(p -> menuIdsWithParents.contains(p.getId()))
                .collect(Collectors.toList());

        log.debug("用户有权限的菜单数量（包含父菜单）: {}", userMenus.size());

        // 4. 构建树形结构
        return buildMenuTree(userMenus, null);
    }

    /**
     * 获取所有菜单树（用于权限管理）
     */
    public List<MenuDto> getAllMenuTree() {
        List<Permission> allMenus = permissionRepository.findByTypeOrderBySortAsc(Permission.Type.menu);
        return buildMenuTree(allMenus, null);
    }

    /**
     * 构建菜单树
     */
    private List<MenuDto> buildMenuTree(List<Permission> allMenus, Long parentId) {
        // 按父ID分组
        Map<Long, List<Permission>> menuMap = allMenus.stream()
                .collect(Collectors.groupingBy(p -> p.getParentId() == null ? 0L : p.getParentId()));

        // 获取当前层级的菜单
        List<Permission> menus = menuMap.getOrDefault(parentId == null ? 0L : parentId, new ArrayList<>());

        return menus.stream().map(menu -> {
            MenuDto dto = new MenuDto();
            dto.setId(menu.getId());
            dto.setName(menu.getName());
            dto.setCode(menu.getCode());
            dto.setPath(menu.getPath());
            dto.setIcon(menu.getIcon());
            dto.setSort(menu.getSort());
            dto.setType(menu.getType().name());
            dto.setParentId(menu.getParentId());

            // 递归获取子菜单
            List<MenuDto> children = buildMenuTree(allMenus, menu.getId());
            dto.setChildren(children.isEmpty() ? null : children);

            return dto;
        }).collect(Collectors.toList());
    }
}
