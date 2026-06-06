package com.vueadmin.service.system;

import com.alibaba.fastjson2.JSON;
import com.vueadmin.dto.DataPermissionDto;
import com.vueadmin.dto.PageResult;
import com.vueadmin.entity.Role;
import com.vueadmin.entity.system.DataPermission;
import com.vueadmin.repository.DataPermissionRepository;
import com.vueadmin.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 数据权限服务
 */
@Service
@RequiredArgsConstructor
public class DataPermissionService {

    private final DataPermissionRepository dataPermissionRepository;
    private final RoleRepository roleRepository;

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // 权限类型映射
    private static final Map<String, String> PERMISSION_TYPE_MAP = new HashMap<>() {{
        put("all", "全部数据");
        put("org", "本组织数据");
        put("suborg", "本组织及下级数据");
        put("self", "仅本人数据");
        put("custom", "自定义规则");
    }};

    // 数据类型映射
    private static final Map<String, String> DATA_TYPE_MAP = new HashMap<>() {{
        put("master_data_instance", "主数据实例");
        put("user", "用户数据");
        put("organization", "组织数据");
    }};

    /**
     * 获取角色的数据权限配置
     */
    public List<DataPermissionDto> getByRoleId(Long roleId) {
        List<DataPermission> permissions = dataPermissionRepository.findByRoleId(roleId);
        return permissions.stream().map(this::toDto).collect(Collectors.toList());
    }

    /**
     * 获取所有数据权限列表
     */
    public PageResult<DataPermissionDto> getList(Long roleId, String dataType) {
        List<DataPermission> permissions;

        if (roleId != null) {
            permissions = dataPermissionRepository.findByRoleId(roleId);
        } else {
            permissions = dataPermissionRepository.findAll();
        }

        if (dataType != null && !dataType.isEmpty()) {
            permissions = permissions.stream()
                    .filter(p -> dataType.equals(p.getDataType()))
                    .collect(Collectors.toList());
        }

        List<DataPermissionDto> list = permissions.stream().map(this::toDto).collect(Collectors.toList());
        return new PageResult<>(list, (long) list.size());
    }

    /**
     * 获取数据权限详情
     */
    public DataPermissionDto getById(Long id) {
        return dataPermissionRepository.findById(id)
                .map(this::toDto)
                .orElse(null);
    }

    /**
     * 创建数据权限
     */
    @Transactional
    public DataPermission create(DataPermissionDto dto) {
        // 检查是否已存在
        DataPermission existing = dataPermissionRepository
                .findByRoleIdAndDataType(dto.getRoleId(), dto.getDataType())
                .orElse(null);

        if (existing != null) {
            // 更新现有记录
            existing.setPermissionType(dto.getPermissionType());
            existing.setOrgIds(dto.getOrgIds() != null ? JSON.toJSONString(dto.getOrgIds()) : null);
            existing.setCustomRule(dto.getCustomRule());
            return dataPermissionRepository.save(existing);
        }

        DataPermission permission = new DataPermission();
        permission.setRoleId(dto.getRoleId());
        permission.setDataType(dto.getDataType());
        permission.setPermissionType(dto.getPermissionType());
        permission.setOrgIds(dto.getOrgIds() != null ? JSON.toJSONString(dto.getOrgIds()) : null);
        permission.setCustomRule(dto.getCustomRule());
        permission.setCreatedBy(dto.getCreatedBy());

        return dataPermissionRepository.save(permission);
    }

    /**
     * 更新数据权限
     */
    @Transactional
    public void update(Long id, DataPermissionDto dto) {
        DataPermission permission = dataPermissionRepository.findById(id).orElse(null);
        if (permission == null) return;

        permission.setPermissionType(dto.getPermissionType());
        permission.setOrgIds(dto.getOrgIds() != null ? JSON.toJSONString(dto.getOrgIds()) : null);
        permission.setCustomRule(dto.getCustomRule());
        permission.setUpdatedBy(dto.getUpdatedBy());

        dataPermissionRepository.save(permission);
    }

    /**
     * 删除数据权限
     */
    @Transactional
    public void delete(Long id) {
        dataPermissionRepository.deleteById(id);
    }

    /**
     * 批量保存角色的数据权限配置
     */
    @Transactional
    public void saveRolePermissions(Long roleId, List<DataPermissionDto> permissions) {
        // 先删除原有配置
        dataPermissionRepository.deleteByRoleId(roleId);

        // 保存新配置
        for (DataPermissionDto dto : permissions) {
            dto.setRoleId(roleId);
            create(dto);
        }
    }

    /**
     * 获取用户的数据权限（根据用户角色合并）
     */
    public Map<String, DataPermission> getUserDataPermissions(List<Long> roleIds, String dataType) {
        List<DataPermission> permissions = dataPermissionRepository.findByRoleIdsAndDataType(roleIds, dataType);

        // 合并权限，取最大权限
        Map<String, DataPermission> result = new HashMap<>();
        for (DataPermission p : permissions) {
            String key = p.getDataType();
            if (!result.containsKey(key) || "all".equals(p.getPermissionType())) {
                result.put(key, p);
            }
        }
        return result;
    }

    /**
     * 转换为DTO
     */
    private DataPermissionDto toDto(DataPermission permission) {
        DataPermissionDto dto = new DataPermissionDto();
        dto.setId(permission.getId());
        dto.setRoleId(permission.getRoleId());
        dto.setDataType(permission.getDataType());
        dto.setPermissionType(permission.getPermissionType());
        dto.setCustomRule(permission.getCustomRule());
        dto.setCreatedBy(permission.getCreatedBy());
        dto.setUpdatedBy(permission.getUpdatedBy());

        // 解析orgIds
        if (permission.getOrgIds() != null && !permission.getOrgIds().isEmpty()) {
            dto.setOrgIds(JSON.parseArray(permission.getOrgIds(), Long.class));
        }

        // 设置显示名称
        dto.setPermissionTypeName(PERMISSION_TYPE_MAP.getOrDefault(permission.getPermissionType(), permission.getPermissionType()));
        dto.setDataTypeName(DATA_TYPE_MAP.getOrDefault(permission.getDataType(), permission.getDataType()));

        // 获取角色名称
        roleRepository.findById(permission.getRoleId())
                .map(Role::getName)
                .ifPresent(dto::setRoleName);

        // 格式化时间
        if (permission.getCreatedAt() != null) {
            dto.setCreatedAt(permission.getCreatedAt().format(formatter));
        }
        if (permission.getUpdatedAt() != null) {
            dto.setUpdatedAt(permission.getUpdatedAt().format(formatter));
        }

        return dto;
    }
}
