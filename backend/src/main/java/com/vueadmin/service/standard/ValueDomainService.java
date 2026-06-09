package com.vueadmin.service.standard;

import com.vueadmin.dto.DomainOptionDto;
import com.vueadmin.dto.PageResult;
import com.vueadmin.dto.ValueDomainDto;
import com.vueadmin.entity.standard.ValueDomain;
import com.vueadmin.entity.standard.ValueDomainItem;
import com.vueadmin.repository.ValueDomainRepository;
import com.vueadmin.repository.ValueDomainItemRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 值域服务
 * 提供值域的业务逻辑处理
 */
@Service
public class ValueDomainService {

    private final ValueDomainRepository valueDomainRepository;
    private final ValueDomainItemRepository valueDomainItemRepository;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public ValueDomainService(ValueDomainRepository valueDomainRepository,
                              ValueDomainItemRepository valueDomainItemRepository) {
        this.valueDomainRepository = valueDomainRepository;
        this.valueDomainItemRepository = valueDomainItemRepository;
    }

    /**
     * 分页查询值域列表
     */
    public PageResult<ValueDomainDto> search(String domainCode, String domainName, String dataType,
                                              String status, Integer page, Integer pageSize) {
        domainCode = (domainCode != null && domainCode.trim().isEmpty()) ? null : domainCode;
        domainName = (domainName != null && domainName.trim().isEmpty()) ? null : domainName;
        dataType = (dataType != null && dataType.trim().isEmpty()) ? null : dataType;
        status = (status != null && status.trim().isEmpty()) ? null : status;

        Pageable pageable = PageRequest.of(
                page != null ? page - 1 : 0,
                pageSize != null ? pageSize : 10,
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        Page<ValueDomain> pageResult = valueDomainRepository.searchValueDomains(
                domainCode, domainName, dataType, status, pageable);

        List<ValueDomainDto> list = pageResult.getContent().stream()
                .map(this::toDto)
                .collect(Collectors.toList());

        int currentPage = page != null ? page : 1;
        int currentPageSize = pageSize != null ? pageSize : 10;

        return PageResult.of(list, pageResult.getTotalElements(), currentPage, currentPageSize);
    }

    /**
     * 获取所有启用的值域列表
     */
    public List<ValueDomainDto> getAllActive() {
        return valueDomainRepository.findAllActive().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /**
     * 获取所有值域列表（不分页）
     */
    public List<ValueDomainDto> getAll() {
        return valueDomainRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /**
     * 根据ID获取值域详情
     */
    public ValueDomainDto getById(Long id) {
        return valueDomainRepository.findById(id)
                .map(this::toDto)
                .orElse(null);
    }

    /**
     * 根据值域编码获取值域详情
     */
    public ValueDomainDto getByDomainCode(String domainCode) {
        return valueDomainRepository.findByDomainCode(domainCode)
                .map(this::toDto)
                .orElse(null);
    }

    /**
     * 根据值域ID获取选项列表
     */
    public List<DomainOptionDto> getOptionsByDomainId(Long domainId) {
        List<ValueDomainItem> items = valueDomainItemRepository
                .findByDomainIdAndStatusOrderBySortAsc(domainId, "启用");
        return items.stream()
                .map(item -> new DomainOptionDto(
                    item.getItemCode(),      // code: 编码
                    item.getItemCode(),      // value: 编码（存储到数据库的值）
                    item.getItemValue(),     // label: 名称（显示给用户）
                    item.getSort()
                ))
                .collect(Collectors.toList());
    }

    /**
     * 根据值域编码获取选项列表
     */
    public List<DomainOptionDto> getOptionsByDomainCode(String domainCode) {
        return valueDomainRepository.findByDomainCode(domainCode)
                .filter(domain -> "active".equals(domain.getStatus()))
                .map(domain -> getOptionsByDomainId(domain.getId()))
                .orElse(List.of());
    }

    /**
     * 创建值域
     */
    @Transactional
    public ValueDomainDto create(ValueDomainDto dto) {
        System.out.println("========== 创建值域 ==========");
        System.out.println("前端传来的status: " + dto.getStatus());

        if (valueDomainRepository.existsByDomainCode(dto.getDomainCode())) {
            throw new RuntimeException("值域编码已存在: " + dto.getDomainCode());
        }

        ValueDomain entity = new ValueDomain();
        entity.setDomainCode(dto.getDomainCode());
        entity.setDomainName(dto.getDomainName());
        entity.setDataType(dto.getDataType());
        entity.setDataLength(dto.getDataLength());
        entity.setDescription(dto.getDescription());
        entity.setCreatedBy(dto.getCreatedBy());
        // 强制设置为草稿，忽略前端传来的status
        entity.setStatus("草稿");

        System.out.println("设置后的status: " + entity.getStatus());

        ValueDomain saved = valueDomainRepository.save(entity);

        System.out.println("保存后的status: " + saved.getStatus());

        // 保存值域项
        if (dto.getOptions() != null && !dto.getOptions().isEmpty()) {
            saveDomainItems(saved.getId(), dto.getOptions());
        }

        return toDto(saved);
    }

    /**
     * 更新值域
     */
    @Transactional
    public ValueDomainDto update(Long id, ValueDomainDto dto) {
        ValueDomain entity = valueDomainRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("值域不存在: " + id));

        if (!entity.getDomainCode().equals(dto.getDomainCode())) {
            if (valueDomainRepository.existsByDomainCode(dto.getDomainCode())) {
                throw new RuntimeException("值域编码已存在: " + dto.getDomainCode());
            }
            entity.setDomainCode(dto.getDomainCode());
        }

        entity.setDomainName(dto.getDomainName());
        entity.setDataType(dto.getDataType());
        entity.setDataLength(dto.getDataLength());
        entity.setDescription(dto.getDescription());
        entity.setUpdatedBy(dto.getUpdatedBy());

        ValueDomain saved = valueDomainRepository.save(entity);

        // 如果传入了选项列表，更新值域项
        if (dto.getOptions() != null) {
            // 删除原有项
            valueDomainItemRepository.deleteByDomainId(saved.getId());
            // 保存新项
            saveDomainItems(saved.getId(), dto.getOptions());
        }

        return toDto(saved);
    }

    /**
     * 更新值域项
     */
    @Transactional
    public void updateOptions(Long domainId, List<DomainOptionDto> options) {
        valueDomainRepository.findById(domainId)
                .orElseThrow(() -> new RuntimeException("值域不存在: " + domainId));

        // 删除原有项
        valueDomainItemRepository.deleteByDomainId(domainId);

        // 保存新项
        saveDomainItems(domainId, options);
    }

    /**
     * 保存值域项
     */
    private void saveDomainItems(Long domainId, List<DomainOptionDto> options) {
        if (options == null || options.isEmpty()) {
            return;
        }

        for (DomainOptionDto opt : options) {
            if (opt.getValue() != null && !opt.getValue().trim().isEmpty()) {
                ValueDomainItem item = new ValueDomainItem();
                item.setDomainId(domainId);
                item.setItemCode(opt.getCode());
                item.setItemValue(opt.getValue());
                item.setSort(opt.getSort() != null ? opt.getSort() : 0);
                item.setStatus("启用");
                valueDomainItemRepository.save(item);
            }
        }
    }

    /**
     * 删除值域
     */
    @Transactional
    public void delete(Long id) {
        ValueDomain entity = valueDomainRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("值域不存在: " + id));

        // 检查是否已发布，发布后不可删除
        if ("启用".equals(entity.getStatus())) {
            throw new RuntimeException("已发布的值域不可删除");
        }

        // 先删除值域项
        valueDomainItemRepository.deleteByDomainId(id);
        // 再删除值域
        valueDomainRepository.deleteById(id);
    }

    /**
     * 发布值域（草稿 -> 启用）
     */
    @Transactional
    public ValueDomainDto publish(Long id) {
        ValueDomain entity = valueDomainRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("值域不存在: " + id));

        // 检查是否为草稿状态
        if (!"草稿".equals(entity.getStatus())) {
            throw new RuntimeException("只有草稿状态的值域才能发布");
        }

        entity.setStatus("启用");
        ValueDomain saved = valueDomainRepository.save(entity);
        return toDto(saved);
    }

    /**
     * 启用值域
     */
    @Transactional
    public ValueDomainDto activate(Long id) {
        ValueDomain entity = valueDomainRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("值域不存在: " + id));

        entity.setStatus("启用");
        ValueDomain saved = valueDomainRepository.save(entity);
        return toDto(saved);
    }

    /**
     * 停用值域
     */
    @Transactional
    public ValueDomainDto deactivate(Long id) {
        ValueDomain entity = valueDomainRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("值域不存在: " + id));

        entity.setStatus("停用");
        ValueDomain saved = valueDomainRepository.save(entity);
        return toDto(saved);
    }

    /**
     * 批量删除值域
     */
    @Transactional
    public void batchDelete(List<Long> ids) {
        for (Long id : ids) {
            valueDomainRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("值域不存在: " + id));
            valueDomainItemRepository.deleteByDomainId(id);
            valueDomainRepository.deleteById(id);
        }
    }

    /**
     * 获取值域统计
     */
    public long countByStatus(String status) {
        return valueDomainRepository.countByStatus(status);
    }

    /**
     * 转换为DTO
     */
    private ValueDomainDto toDto(ValueDomain entity) {
        ValueDomainDto dto = new ValueDomainDto();
        dto.setId(entity.getId());
        dto.setDomainCode(entity.getDomainCode());
        dto.setDomainName(entity.getDomainName());
        dto.setDataType(entity.getDataType());
        dto.setDataLength(entity.getDataLength());
        dto.setStatus(entity.getStatus());
        dto.setDescription(entity.getDescription());
        dto.setCreatedBy(entity.getCreatedBy());
        dto.setUpdatedBy(entity.getUpdatedBy());

        if (entity.getCreatedAt() != null) {
            dto.setCreatedAt(entity.getCreatedAt().format(formatter));
        }
        if (entity.getUpdatedAt() != null) {
            dto.setUpdatedAt(entity.getUpdatedAt().format(formatter));
        }

        // 获取值域项
        List<DomainOptionDto> options = getOptionsByDomainId(entity.getId());
        dto.setOptions(options);

        return dto;
    }
}
