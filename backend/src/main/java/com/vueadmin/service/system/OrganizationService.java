package com.vueadmin.service.system;

import com.vueadmin.dto.OrganizationDto;
import com.vueadmin.entity.system.Organization;
import com.vueadmin.exception.BusinessException;
import com.vueadmin.exception.ErrorCode;
import com.vueadmin.repository.OrganizationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 组织机构服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrganizationService {

    private final OrganizationRepository organizationRepository;

    /**
     * 获取组织树
     */
    public List<OrganizationDto> getOrganizationTree() {
        List<Organization> allOrgs = organizationRepository.findAllByOrderBySortAsc();
        return buildTree(allOrgs, null);
    }

    /**
     * 获取启用的组织树
     */
    public List<OrganizationDto> getActiveOrganizationTree() {
        List<Organization> allOrgs = organizationRepository.findByStatusOrderBySortAsc("active");
        return buildTree(allOrgs, null);
    }

    /**
     * 构建树形结构
     */
    private List<OrganizationDto> buildTree(List<Organization> allOrgs, Long parentId) {
        Map<Long, List<Organization>> orgMap = allOrgs.stream()
                .collect(Collectors.groupingBy(o -> o.getParentId() == null ? 0L : o.getParentId()));

        List<Organization> children = orgMap.getOrDefault(parentId == null ? 0L : parentId, new ArrayList<>());

        return children.stream().map(org -> {
            OrganizationDto dto = convertToDto(org);
            List<OrganizationDto> childList = buildTree(allOrgs, org.getId());
            dto.setChildren(childList.isEmpty() ? null : childList);
            dto.setHasChildren(!childList.isEmpty());
            dto.setLabel(org.getOrgName());
            dto.setValue(org.getId());
            return dto;
        }).collect(Collectors.toList());
    }

    /**
     * 获取组织列表（平铺）
     */
    public List<OrganizationDto> getOrganizationList() {
        List<Organization> orgs = organizationRepository.findAllByOrderBySortAsc();
        return orgs.stream().map(this::convertToDto).collect(Collectors.toList());
    }

    /**
     * 根据ID获取组织
     */
    public OrganizationDto getById(Long id) {
        Organization org = organizationRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.DATA_NOT_FOUND, "组织不存在"));
        OrganizationDto dto = convertToDto(org);
        dto.setHasChildren(organizationRepository.countByParentId(id) > 0);
        return dto;
    }

    /**
     * 获取子组织
     */
    public List<OrganizationDto> getChildren(Long parentId) {
        List<Organization> children = organizationRepository.findByParentIdOrderBySortAsc(parentId);
        return children.stream().map(org -> {
            OrganizationDto dto = convertToDto(org);
            dto.setHasChildren(organizationRepository.countByParentId(org.getId()) > 0);
            return dto;
        }).collect(Collectors.toList());
    }

    /**
     * 创建组织
     */
    @Transactional
    public OrganizationDto create(OrganizationDto dto) {
        // 检查编码是否已存在
        if (organizationRepository.existsByOrgCode(dto.getOrgCode())) {
            throw new BusinessException(ErrorCode.DATA_ALREADY_EXISTS, "组织编码已存在");
        }

        Organization org = new Organization();
        org.setOrgCode(dto.getOrgCode());
        org.setOrgName(dto.getOrgName());
        org.setOrgType(dto.getOrgType());
        org.setParentId(dto.getParentId());
        org.setManager(dto.getManager());
        org.setPhone(dto.getPhone());
        org.setEmail(dto.getEmail());
        org.setSort(dto.getSort() != null ? dto.getSort() : 0);
        org.setStatus(dto.getStatus() != null ? dto.getStatus() : "active");
        org.setDescription(dto.getDescription());
        org.setCreatedBy(dto.getCreatedBy());

        // 计算层级和路径
        calculateLevelAndPath(org, dto.getParentId());

        Organization saved = organizationRepository.save(org);
        log.info("创建组织: {}", saved.getOrgName());
        return convertToDto(saved);
    }

    /**
     * 更新组织
     */
    @Transactional
    public OrganizationDto update(Long id, OrganizationDto dto) {
        Organization org = organizationRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.DATA_NOT_FOUND, "组织不存在"));

        // 检查编码是否被其他组织使用
        if (!org.getOrgCode().equals(dto.getOrgCode())
                && organizationRepository.existsByOrgCode(dto.getOrgCode())) {
            throw new BusinessException(ErrorCode.DATA_ALREADY_EXISTS, "组织编码已存在");
        }

        org.setOrgCode(dto.getOrgCode());
        org.setOrgName(dto.getOrgName());
        org.setOrgType(dto.getOrgType());
        org.setManager(dto.getManager());
        org.setPhone(dto.getPhone());
        org.setEmail(dto.getEmail());
        org.setSort(dto.getSort());
        org.setStatus(dto.getStatus());
        org.setDescription(dto.getDescription());
        org.setUpdatedBy(dto.getUpdatedBy());

        // 如果父级变化，重新计算层级和路径
        if ((org.getParentId() == null && dto.getParentId() != null)
                || (org.getParentId() != null && !org.getParentId().equals(dto.getParentId()))) {
            org.setParentId(dto.getParentId());
            calculateLevelAndPath(org, dto.getParentId());
        }

        Organization saved = organizationRepository.save(org);
        log.info("更新组织: {}", saved.getOrgName());
        return convertToDto(saved);
    }

    /**
     * 删除组织
     */
    @Transactional
    public void delete(Long id) {
        Organization org = organizationRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.DATA_NOT_FOUND, "组织不存在"));

        // 检查是否有子组织
        long childCount = organizationRepository.countByParentId(id);
        if (childCount > 0) {
            throw new BusinessException(ErrorCode.DATA_STATUS_ERROR, "存在子组织，无法删除");
        }

        organizationRepository.delete(org);
        log.info("删除组织: {}", org.getOrgName());
    }

    /**
     * 批量删除
     */
    @Transactional
    public void batchDelete(List<Long> ids) {
        for (Long id : ids) {
            delete(id);
        }
    }

    /**
     * 计算层级和路径
     */
    private void calculateLevelAndPath(Organization org, Long parentId) {
        if (parentId == null) {
            org.setLevel(1);
            org.setPath("/" + org.getId());
        } else {
            Organization parent = organizationRepository.findById(parentId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.DATA_NOT_FOUND, "父组织不存在"));
            org.setLevel(parent.getLevel() + 1);
            org.setPath(parent.getPath() + "/" + org.getId());
        }
    }

    /**
     * 转换为DTO
     */
    private OrganizationDto convertToDto(Organization org) {
        OrganizationDto dto = new OrganizationDto();
        dto.setId(org.getId());
        dto.setOrgCode(org.getOrgCode());
        dto.setOrgName(org.getOrgName());
        dto.setOrgType(org.getOrgType());
        dto.setParentId(org.getParentId());
        dto.setLevel(org.getLevel());
        dto.setPath(org.getPath());
        dto.setManager(org.getManager());
        dto.setPhone(org.getPhone());
        dto.setEmail(org.getEmail());
        dto.setSort(org.getSort());
        dto.setStatus(org.getStatus());
        dto.setCreatedBy(org.getCreatedBy());
        dto.setCreatedAt(org.getCreatedAt());
        dto.setUpdatedBy(org.getUpdatedBy());
        dto.setUpdatedAt(org.getUpdatedAt());
        dto.setDescription(org.getDescription());
        dto.setLabel(org.getOrgName());
        dto.setValue(org.getId());
        return dto;
    }
}
