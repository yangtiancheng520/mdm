package com.vueadmin.service.standard;

import com.vueadmin.dto.*;
import com.vueadmin.entity.standard.*;
import com.vueadmin.repository.*;
import com.vueadmin.entity.standard.FieldStandard;
import jakarta.persistence.EntityManager;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 视图定义Service
 */
@Service
public class ViewDefinitionService {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private ViewDefinitionRepository viewDefinitionRepository;

    @Autowired
    private ViewEntityRepository viewEntityRepository;

    @Autowired
    private ViewGroupRepository viewGroupRepository;

    @Autowired
    private ViewFieldRepository viewFieldRepository;

    @Autowired
    private ViewValidationRepository viewValidationRepository;

    @Autowired
    private ViewCategoryRepository viewCategoryRepository;

    @Autowired
    private FieldStandardRepository fieldStandardRepository;

    /**
     * 获取视图列表（每个视图只显示一条：优先显示修订中的版本，否则显示主干版本）
     */
    public List<ViewDefinitionDto> getViewList(String keyword, Long categoryId, String status) {
        List<ViewDefinition> views = viewDefinitionRepository.findAll();

        // 按视图编码分组，每个视图只显示一条
        Map<String, List<ViewDefinition>> groupByCode = views.stream()
                .collect(Collectors.groupingBy(ViewDefinition::getViewCode));

        List<ViewDefinition> filteredViews = new ArrayList<>();
        for (List<ViewDefinition> versions : groupByCode.values()) {
            // 优先显示修订中的版本
            ViewDefinition revising = versions.stream()
                    .filter(v -> "revising".equals(v.getStatus()))
                    .findFirst()
                    .orElse(null);

            if (revising != null) {
                filteredViews.add(revising);
            } else {
                // 没有修订版本，优先显示主干版本
                ViewDefinition trunk = versions.stream()
                        .filter(v -> v.getIsTrunk() != null && v.getIsTrunk())
                        .findFirst()
                        .orElse(null);

                if (trunk != null) {
                    filteredViews.add(trunk);
                } else {
                    // 如果没有主干版本，显示最新版本（按更新时间降序）
                    versions.stream()
                            .max(Comparator.comparing(ViewDefinition::getUpdatedAt))
                            .ifPresent(filteredViews::add);
                }
            }
        }

        // 关键字过滤
        if (keyword != null && !keyword.isEmpty()) {
            filteredViews = filteredViews.stream()
                    .filter(v -> v.getViewCode().contains(keyword) || v.getViewName().contains(keyword))
                    .collect(Collectors.toList());
        }

        // 分类过滤
        if (categoryId != null) {
            filteredViews = filteredViews.stream()
                    .filter(v -> categoryId.equals(v.getCategoryId()))
                    .collect(Collectors.toList());
        }

        // 状态过滤
        if (status != null && !status.isEmpty()) {
            filteredViews = filteredViews.stream()
                    .filter(v -> status.equals(v.getStatus()))
                    .collect(Collectors.toList());
        }

        return filteredViews.stream()
                .sorted(Comparator.comparing(ViewDefinition::getUpdatedAt).reversed())
                .map(this::convertToSimpleDto)
                .collect(Collectors.toList());
    }

    /**
     * 获取视图详情（完整结构）
     */
    public ViewDefinitionDto getViewDetail(Long id) {
        ViewDefinition view = viewDefinitionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("视图不存在"));

        ViewDefinitionDto dto = convertToDto(view);

        // 加载实体列表
        List<ViewEntity> entities = viewEntityRepository.findByViewIdOrderBySort(view.getId());
        dto.setEntities(entities.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList()));

        // 加载校验规则
        List<ViewValidation> validations = viewValidationRepository.findByViewId(view.getId());
        dto.setValidations(validations.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList()));

        return dto;
    }

    /**
     * 根据视图编码获取已发布版本
     */
    public ViewDefinitionDto getPublishedView(String viewCode) {
        ViewDefinition view = viewDefinitionRepository.findByViewCodeAndStatus(viewCode, "published")
                .orElseThrow(() -> new RuntimeException("视图不存在或未发布"));

        return getViewDetail(view.getId());
    }

    /**
     * 创建视图
     */
    @Transactional
    public ViewDefinitionDto createView(ViewDefinitionDto dto) {
        // 检查编码是否已存在
        if (viewDefinitionRepository.existsByViewCode(dto.getViewCode())) {
            throw new RuntimeException("视图编码[" + dto.getViewCode() + "]已存在，请使用其他编码");
        }

        ViewDefinition view = new ViewDefinition();
        BeanUtils.copyProperties(dto, view, "id", "entities", "validations");
        view.setVersion(1);
        view.setIsLatest(true);
        view.setStatus("draft");

        String currentUser = getCurrentUser();
        view.setCreatedBy(currentUser);
        view.setUpdatedBy(currentUser);

        view = viewDefinitionRepository.save(view);

        // 保存实体和字段
        if (dto.getEntities() != null) {
            saveEntities(view.getId(), dto.getEntities(), dto.getViewCode());
        }

        // 保存校验规则
        if (dto.getValidations() != null) {
            saveValidations(view.getId(), dto.getValidations());
        }

        return getViewDetail(view.getId());
    }

    /**
     * 更新视图
     * - 草稿/修订中状态：可以更新所有信息
     * - 已发布状态：只能更新基本信息（视图名称、分类、描述说明）
     */
    @Transactional
    public ViewDefinitionDto updateView(Long id, ViewDefinitionDto dto) {
        ViewDefinition view = viewDefinitionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("视图不存在"));

        String currentUser = getCurrentUser();

        if ("published".equals(view.getStatus())) {
            // 已发布状态，只能更新基本信息（视图名称、分类、描述说明）
            view.setViewName(dto.getViewName());
            view.setCategoryId(dto.getCategoryId());
            view.setDescription(dto.getDescription());
            view.setUpdatedBy(currentUser);
            view = viewDefinitionRepository.save(view);
            return getViewDetail(view.getId());
        }

        // 草稿状态，直接更新
        view.setViewName(dto.getViewName());
        view.setCategoryId(dto.getCategoryId());
        view.setLayoutColumns(dto.getLayoutColumns());
        view.setLabelWidth(dto.getLabelWidth());
        view.setEnableCopy(dto.getEnableCopy());
        view.setEnableImport(dto.getEnableImport());
        view.setEnableExport(dto.getEnableExport());
        view.setDescription(dto.getDescription());
        view.setUpdatedBy(currentUser);

        view = viewDefinitionRepository.save(view);

        // 删除旧的实体和字段
        deleteEntities(view.getId());

        // 保存新的实体和字段
        if (dto.getEntities() != null) {
            saveEntities(view.getId(), dto.getEntities(), view.getViewCode());
        }

        // 删除旧的校验规则
        viewValidationRepository.deleteByViewId(view.getId());

        // 保存新的校验规则
        if (dto.getValidations() != null) {
            saveValidations(view.getId(), dto.getValidations());
        }

        return getViewDetail(view.getId());
    }

    /**
     * 提交审批
     */
    @Transactional
    public ViewDefinitionDto submitForApproval(Long id) {
        ViewDefinition view = viewDefinitionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("视图不存在"));

        // 只有草稿或修订中状态才能提交审批
        if (!"draft".equals(view.getStatus()) && !"revising".equals(view.getStatus())) {
            throw new RuntimeException("只有草稿或修订中状态的视图才能提交审批");
        }

        // 获取视图详情，校验主表有效性
        ViewDefinitionDto viewDetail = getViewDetail(id);
        validateViewForSubmit(viewDetail);

        String currentUser = getCurrentUser();
        view.setStatus("pending_approval");
        view.setUpdatedBy(currentUser);
        viewDefinitionRepository.save(view);

        return getViewDetail(view.getId());
    }

    /**
     * 撤销审批
     */
    @Transactional
    public ViewDefinitionDto cancelApproval(Long id) {
        ViewDefinition view = viewDefinitionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("视图不存在"));

        // 只有审批中状态才能撤销审批
        if (!"pending_approval".equals(view.getStatus())) {
            throw new RuntimeException("只有审批中状态的视图才能撤销审批");
        }

        String currentUser = getCurrentUser();
        // 撤销审批，退回草稿状态
        view.setStatus("draft");
        view.setUpdatedBy(currentUser);
        viewDefinitionRepository.save(view);

        return getViewDetail(view.getId());
    }

    /**
     * 审批视图（审批通过后状态保持pending_approval，等待发布）
     */
    @Transactional
    public ViewDefinitionDto approveView(Long id, boolean approved, String comment) {
        ViewDefinition view = viewDefinitionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("视图不存在"));

        // 只有审批中状态才能审批
        if (!"pending_approval".equals(view.getStatus())) {
            throw new RuntimeException("只有审批中状态的视图才能进行审批");
        }

        String currentUser = getCurrentUser();
        if (approved) {
            // 审批通过，状态保持pending_approval，等待发布
            view.setStatus("pending_approval");
        } else {
            // 审批驳回，退回草稿状态
            view.setStatus("draft");
        }
        view.setUpdatedBy(currentUser);
        viewDefinitionRepository.save(view);

        return getViewDetail(view.getId());
    }

    /**
     * 发布视图
     */
    @Transactional
    public ViewDefinitionDto publishView(Long id) {
        ViewDefinition view = viewDefinitionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("视图不存在"));

        // 只有审批中(pending_approval)状态才能发布
        if (!"pending_approval".equals(view.getStatus())) {
            throw new RuntimeException("只有审批通过的视图才能发布，请先提交审批并等待审批通过");
        }

        // 获取视图详情（包含实体和字段）
        ViewDefinitionDto viewDetail = getViewDetail(id);

        // 校验视图是否完整
        validateViewForPublish(viewDetail);

        // 创建物理表
        try {
            createPhysicalTables(viewDetail);
        } catch (Exception e) {
            throw new RuntimeException("创建物理表失败：" + e.getMessage());
        }

        String currentUser = getCurrentUser();

        // 如果是修订版本发布，需要处理主干版本
        if ("revising".equals(view.getStatus()) && view.getBaseVersionId() != null) {
            // 将原主干版本设为历史版本
            ViewDefinition oldTrunk = viewDefinitionRepository.findById(view.getBaseVersionId()).orElse(null);
            if (oldTrunk != null) {
                oldTrunk.setStatus("history");  // 改为历史版本
                oldTrunk.setIsTrunk(false);
                oldTrunk.setIsLatest(false);
                oldTrunk.setUpdatedBy(currentUser);
                viewDefinitionRepository.save(oldTrunk);
            }

            // 发布时递增版本号
            view.setVersion(oldTrunk != null ? oldTrunk.getVersion() + 1 : view.getVersion() + 1);
        } else {
            // 将其他已发布或已停用的版本归档为历史版本
            List<ViewDefinition> publishedViews = viewDefinitionRepository.findAllByViewCodeAndStatus(view.getViewCode(), "published");
            for (ViewDefinition pv : publishedViews) {
                pv.setStatus("history"); // 改为历史版本
                pv.setIsTrunk(false);
                pv.setIsLatest(false);
                pv.setUpdatedBy(currentUser);
                viewDefinitionRepository.save(pv);
            }
            // 也处理已停用的版本
            List<ViewDefinition> disabledViews = viewDefinitionRepository.findAllByViewCodeAndStatus(view.getViewCode(), "disabled");
            for (ViewDefinition dv : disabledViews) {
                dv.setStatus("history"); // 改为历史版本
                dv.setIsTrunk(false);
                dv.setIsLatest(false);
                dv.setUpdatedBy(currentUser);
                viewDefinitionRepository.save(dv);
            }
        }

        // 发布当前版本
        view.setStatus("published");
        view.setIsTrunk(true);  // 设为主干
        view.setIsLatest(true);  // 设为最新
        view.setPublishTime(LocalDateTime.now());
        view.setUpdatedBy(currentUser);

        // 记录物理表名
        String mainTableName = "mdm_" + view.getViewCode().toLowerCase();
        view.setTableName(mainTableName);

        viewDefinitionRepository.save(view);

        return getViewDetail(view.getId());
    }

    /**
     * 校验视图是否可以提交审批
     */
    private void validateViewForSubmit(ViewDefinitionDto view) {
        if (view.getEntities() == null || view.getEntities().isEmpty()) {
            throw new RuntimeException("视图缺少实体定义");
        }

        // 检查主表
        ViewEntityDto mainEntity = view.getEntities().stream()
                .filter(e -> "main".equals(e.getEntityType()))
                .findFirst()
                .orElse(null);

        if (mainEntity == null) {
            throw new RuntimeException("视图缺少主表，请先添加主表");
        }

        if (mainEntity.getFields() == null || mainEntity.getFields().isEmpty()) {
            throw new RuntimeException("主表没有字段定义，请先添加字段");
        }
    }

    /**
     * 校验视图是否可以发布
     */
    private void validateViewForPublish(ViewDefinitionDto view) {
        if (view.getEntities() == null || view.getEntities().isEmpty()) {
            throw new RuntimeException("视图缺少实体定义");
        }

        // 检查主表
        ViewEntityDto mainEntity = view.getEntities().stream()
                .filter(e -> "main".equals(e.getEntityType()))
                .findFirst()
                .orElse(null);

        if (mainEntity == null) {
            throw new RuntimeException("视图缺少主表");
        }

        if (mainEntity.getFields() == null || mainEntity.getFields().isEmpty()) {
            throw new RuntimeException("主表没有字段定义");
        }
    }

    /**
     * 创建物理表（支持修订版本）
     */
    private void createPhysicalTables(ViewDefinitionDto view) {
        String viewCode = view.getViewCode().toLowerCase();
        List<String> createdTables = new ArrayList<>();

        for (ViewEntityDto entity : view.getEntities()) {
            String tableName;
            if ("main".equals(entity.getEntityType())) {
                tableName = "mdm_" + viewCode;
            } else {
                tableName = "mdm_" + viewCode + "_" + entity.getEntityCode().toLowerCase();
            }

            // 检查表是否已存在
            boolean tableExists = checkTableExists(tableName);

            if (tableExists) {
                // 表已存在，检查并添加新字段
                addNewColumns(tableName, entity);
                createdTables.add(tableName + "(已更新)");
            } else {
                // 表不存在，创建新表
                createNewTable(tableName, entity, view);
                createdTables.add(tableName);
            }
        }

        // 如果没有创建任何表，抛出异常
        if (createdTables.isEmpty()) {
            throw new RuntimeException("未创建任何物理表，请检查视图配置");
        }
    }

    /**
     * 检查表是否存在
     */
    private boolean checkTableExists(String tableName) {
        try {
            String sql = "SHOW TABLES LIKE '" + tableName + "'";
            List<?> result = entityManager.createNativeQuery(sql).getResultList();
            return !result.isEmpty();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 添加新字段（修订版本）
     */
    private void addNewColumns(String tableName, ViewEntityDto entity) {
        if (entity.getFields() == null || entity.getFields().isEmpty()) {
            return;
        }

        // 获取现有字段列表
        Set<String> existingColumns = getExistingColumns(tableName);

        for (ViewFieldDto field : entity.getFields()) {
            String columnCode = field.getFieldCode().toLowerCase();

            // 跳过系统保留字段
            if (isSystemColumn(columnCode)) {
                continue;
            }

            // 如果字段已存在，跳过
            if (existingColumns.contains(columnCode)) {
                continue;
            }

            // 新增字段，添加列
            String columnType = getColumnType(field.getFieldType(), field.getLength(), field.getPrecisionVal());
            String alterSql = String.format(
                "ALTER TABLE %s ADD COLUMN %s %s COMMENT '%s'",
                tableName,
                field.getFieldCode(),
                columnType,
                field.getFieldName()
            );

            try {
                entityManager.createNativeQuery(alterSql).executeUpdate();
            } catch (Exception e) {
                // 如果添加失败，记录日志但不抛出异常
                System.err.println("添加字段失败: " + field.getFieldCode() + ", 原因: " + e.getMessage());
            }
        }
    }

    /**
     * 获取表的现有字段列表
     */
    private Set<String> getExistingColumns(String tableName) {
        Set<String> columns = new HashSet<>();
        try {
            String sql = "SHOW COLUMNS FROM " + tableName;
            List<?> result = entityManager.createNativeQuery(sql).getResultList();
            for (Object row : result) {
                Object[] rowArray = (Object[]) row;
                String columnName = rowArray[0].toString().toLowerCase();
                columns.add(columnName);
            }
        } catch (Exception e) {
            System.err.println("获取字段列表失败: " + e.getMessage());
        }
        return columns;
    }

    /**
     * 创建新表
     */
    private void createNewTable(String tableName, ViewEntityDto entity, ViewDefinitionDto view) {
        // 构建DDL
        StringBuilder ddl = new StringBuilder();
        ddl.append("CREATE TABLE ").append(tableName).append(" (\n");

        // 系统字段
        ddl.append("    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',\n");
        if (!"main".equals(entity.getEntityType())) {
            ddl.append("    main_id BIGINT NOT NULL COMMENT '主表ID',\n");
        }
        ddl.append("    view_id BIGINT COMMENT '视图ID',\n");

        // 业务字段
        Set<String> addedColumns = new HashSet<>();
        if (entity.getFields() != null) {
            for (ViewFieldDto field : entity.getFields()) {
                String columnCode = field.getFieldCode().toLowerCase();
                // 跳过系统保留字段
                if (isSystemColumn(columnCode)) {
                    continue;
                }
                // 避免重复字段
                if (addedColumns.contains(columnCode)) {
                    continue;
                }
                addedColumns.add(columnCode);

                String columnType = getColumnType(field.getFieldType(), field.getLength(), field.getPrecisionVal());
                ddl.append("    ").append(field.getFieldCode()).append(" ").append(columnType);
                ddl.append(" COMMENT '").append(field.getFieldName()).append("',\n");
            }
        }

        // 审计字段
        ddl.append("    status VARCHAR(20) DEFAULT 'active' COMMENT '状态',\n");
        ddl.append("    created_by VARCHAR(50) COMMENT '创建人',\n");
        ddl.append("    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',\n");
        ddl.append("    updated_by VARCHAR(50) COMMENT '更新人',\n");
        ddl.append("    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'\n");

        ddl.append(") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='").append(view.getViewName());
        if (!"main".equals(entity.getEntityType())) {
            ddl.append("-").append(entity.getEntityName());
        }
        ddl.append("';");

        // 执行DDL
        try {
            entityManager.createNativeQuery(ddl.toString()).executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException("创建表 [" + tableName + "] 失败，原因：" + e.getMessage());
        }
    }

    /**
     * 判断是否为系统保留字段
     */
    private boolean isSystemColumn(String columnCode) {
        Set<String> systemColumns = new HashSet<>(Arrays.asList(
                "id", "main_id", "view_id", "status", "created_by", "created_at", "updated_by", "updated_at"
        ));
        return systemColumns.contains(columnCode.toLowerCase());
    }

    /**
     * 获取字段对应的数据库类型
     */
    private String getColumnType(String fieldType, Integer length, Integer precision) {
        if (fieldType == null) {
            return "VARCHAR(255)";
        }

        switch (fieldType.toLowerCase()) {
            case "string":
                return "VARCHAR(" + (length != null ? length : 255) + ")";
            case "integer":
                return "INT";
            case "decimal":
                int len = length != null ? length : 13;
                int prec = precision != null ? precision : 2;
                return "DECIMAL(" + len + "," + prec + ")";
            case "date":
                return "DATE";
            case "datetime":
                return "DATETIME";
            case "boolean":
                return "TINYINT(1)";
            case "text":
                return "TEXT";
            default:
                return "VARCHAR(255)";
        }
    }

    /**
     * 停用视图
     */
    @Transactional
    public ViewDefinitionDto disableView(Long id) {
        ViewDefinition view = viewDefinitionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("视图不存在"));

        if (!"published".equals(view.getStatus())) {
            throw new RuntimeException("只有已发布的视图才能停用");
        }

        view.setStatus("disabled");
        view.setUpdatedBy(getCurrentUser());
        viewDefinitionRepository.save(view);

        return getViewDetail(view.getId());
    }

    /**
     * 启用视图
     */
    @Transactional
    public ViewDefinitionDto enableView(Long id) {
        ViewDefinition view = viewDefinitionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("视图不存在"));

        if (!"disabled".equals(view.getStatus())) {
            throw new RuntimeException("只有已停用的视图才能启用");
        }

        view.setStatus("published");
        view.setUpdatedBy(getCurrentUser());
        viewDefinitionRepository.save(view);

        return getViewDetail(view.getId());
    }

    /**
     * 修订视图（创建新版本）
     */
    @Transactional
    public ViewDefinitionDto reviseView(Long id) {
        ViewDefinition oldView = viewDefinitionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("视图不存在"));

        if (!"published".equals(oldView.getStatus())) {
            throw new RuntimeException("只有已发布的视图才能修订");
        }

        String currentUser = getCurrentUser();

        // 创建新版本（分支）
        ViewDefinition newView = new ViewDefinition();
        BeanUtils.copyProperties(oldView, newView, "id", "createdAt", "updatedAt", "publishTime", "tableName");
        newView.setVersion(oldView.getVersion());  // 版本号保持不变
        newView.setBaseVersionId(oldView.getId());
        newView.setIsTrunk(false);  // 修订版本是分支
        newView.setIsLatest(true);  // 最新修改
        newView.setStatus("revising"); // 修订中
        newView.setPublishTime(null);
        newView.setTableName(null);
        newView.setCreatedBy(currentUser);
        newView.setUpdatedBy(currentUser);

        newView = viewDefinitionRepository.save(newView);

        // 复制实体和字段
        List<ViewEntity> oldEntities = viewEntityRepository.findByViewIdOrderBySort(oldView.getId());
        for (ViewEntity oldEntity : oldEntities) {
            ViewEntity newEntity = new ViewEntity();
            BeanUtils.copyProperties(oldEntity, newEntity, "id", "createdAt", "updatedAt");
            newEntity.setViewId(newView.getId());
            newEntity.setIsInherited(true);  // 标记为继承实体
            newEntity.setCreatedBy(currentUser);
            newEntity.setUpdatedBy(currentUser);
            newEntity = viewEntityRepository.save(newEntity);

            // 复制字段
            List<ViewField> oldFields = viewFieldRepository.findByEntityIdOrderBySort(oldEntity.getId());
            for (ViewField oldField : oldFields) {
                ViewField newField = new ViewField();
                BeanUtils.copyProperties(oldField, newField, "id", "createdAt", "updatedAt");
                newField.setEntityId(newEntity.getId());
                newField.setIsInherited(true);  // 标记为继承字段
                newField.setCreatedBy(currentUser);
                newField.setUpdatedBy(currentUser);
                viewFieldRepository.save(newField);
            }
        }

        return getViewDetail(newView.getId());
    }

    /**
     * 删除视图
     */
    @Transactional
    public void deleteView(Long id) {
        ViewDefinition view = viewDefinitionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("视图不存在"));

        // 已发布和已停用的主干版本不能删除
        if ("published".equals(view.getStatus()) && (view.getIsTrunk() == null || view.getIsTrunk())) {
            throw new RuntimeException("已发布的主干版本不能删除");
        }
        if ("disabled".equals(view.getStatus())) {
            throw new RuntimeException("已停用的视图不能删除");
        }

        // 删除相关数据
        deleteEntities(id);
        viewValidationRepository.deleteByViewId(id);
        viewDefinitionRepository.deleteById(id);
    }

    /**
     * 获取版本历史
     */
    public List<ViewDefinitionDto> getVersionHistory(String viewCode) {
        List<ViewDefinition> versions = viewDefinitionRepository.findByViewCodeOrderByVersionDesc(viewCode);
        return versions.stream()
                .map(this::convertToDtoWithEntities)
                .collect(Collectors.toList());
    }

    // ========== 私有方法 ==========

    private void saveEntities(Long viewId, List<ViewEntityDto> entityDtos, String viewCode) {
        String currentUser = getCurrentUser();

        for (ViewEntityDto entityDto : entityDtos) {
            ViewEntity entity = new ViewEntity();
            BeanUtils.copyProperties(entityDto, entity, "id", "groups", "fields");
            entity.setViewId(viewId);

            // 计算并设置物理表名
            String tableName;
            if ("main".equals(entityDto.getEntityType())) {
                tableName = "mdm_" + viewCode.toLowerCase();
            } else {
                tableName = "mdm_" + viewCode.toLowerCase() + "_" + entityDto.getEntityCode().toLowerCase();
            }
            entity.setTableName(tableName);

            // 判断是否为继承实体
            if (entityDto.getIsInherited() != null) {
                entity.setIsInherited(entityDto.getIsInherited());
            } else {
                // 如果ID不为空且小于阈值，说明是继承实体
                entity.setIsInherited(entityDto.getId() != null && entityDto.getId() < 1000000000000L);
            }

            entity.setCreatedBy(currentUser);
            entity.setUpdatedBy(currentUser);
            entity = viewEntityRepository.save(entity);

            // 保存分组
            if (entityDto.getGroups() != null) {
                saveGroups(entity.getId(), entityDto.getGroups());
            }

            // 保存字段
            if (entityDto.getFields() != null) {
                saveFields(entity.getId(), entityDto.getFields());
            }
        }
    }

    private void saveGroups(Long entityId, List<ViewGroupDto> groupDtos) {
        String currentUser = getCurrentUser();

        for (ViewGroupDto groupDto : groupDtos) {
            ViewGroup group = new ViewGroup();
            BeanUtils.copyProperties(groupDto, group, "id");
            group.setEntityId(entityId);
            group.setCreatedBy(currentUser);
            group.setUpdatedBy(currentUser);
            viewGroupRepository.save(group);
        }
    }

    private void saveFields(Long entityId, List<ViewFieldDto> fieldDtos) {
        String currentUser = getCurrentUser();

        for (ViewFieldDto fieldDto : fieldDtos) {
            ViewField field = new ViewField();
            // 排除显示用的字段和ID字段
            BeanUtils.copyProperties(fieldDto, field, "id", "fieldStandardCode", "fieldStandardName",
                    "groupName", "refName", "enumName", "triggerEntityName", "triggerFieldName",
                    "targetEntityName", "targetFieldName");
            field.setEntityId(entityId);

            // 判断是否为继承字段
            // 如果DTO中的isInherited为null，则根据ID判断
            if (fieldDto.getIsInherited() != null) {
                field.setIsInherited(fieldDto.getIsInherited());
            } else {
                // 如果ID不为空且小于阈值，说明是继承字段
                field.setIsInherited(fieldDto.getId() != null && fieldDto.getId() < 1000000000000L);
            }

            field.setCreatedBy(currentUser);
            field.setUpdatedBy(currentUser);
            viewFieldRepository.save(field);
        }
    }

    private void saveValidations(Long viewId, List<ViewValidationDto> validationDtos) {
        String currentUser = getCurrentUser();

        for (ViewValidationDto validationDto : validationDtos) {
            ViewValidation validation = new ViewValidation();
            BeanUtils.copyProperties(validationDto, validation, "id", "triggerEntityName",
                    "triggerFieldName", "targetEntityName", "targetFieldName");
            validation.setViewId(viewId);
            validation.setCreatedBy(currentUser);
            validation.setUpdatedBy(currentUser);
            viewValidationRepository.save(validation);
        }
    }

    private void deleteEntities(Long viewId) {
        List<ViewEntity> entities = viewEntityRepository.findByViewIdOrderBySort(viewId);
        for (ViewEntity entity : entities) {
            viewFieldRepository.deleteByEntityId(entity.getId());
            viewGroupRepository.deleteByEntityId(entity.getId());
        }
        // 使用 deleteAll 替代 deleteByViewId，确保立即删除
        viewEntityRepository.deleteAll(entities);
        // 强制flush，确保删除立即生效
        viewEntityRepository.flush();
    }

    private ViewDefinitionDto convertToSimpleDto(ViewDefinition view) {
        ViewDefinitionDto dto = new ViewDefinitionDto();
        BeanUtils.copyProperties(view, dto);

        // 设置分类名称
        if (view.getCategoryId() != null) {
            viewCategoryRepository.findById(view.getCategoryId())
                    .ifPresent(cat -> dto.setCategoryName(cat.getCategoryName()));
        }

        return dto;
    }

    private ViewDefinitionDto convertToDto(ViewDefinition view) {
        ViewDefinitionDto dto = new ViewDefinitionDto();
        BeanUtils.copyProperties(view, dto);

        // 设置分类名称
        if (view.getCategoryId() != null) {
            viewCategoryRepository.findById(view.getCategoryId())
                    .ifPresent(cat -> dto.setCategoryName(cat.getCategoryName()));
        }

        return dto;
    }

    private ViewDefinitionDto convertToDtoWithEntities(ViewDefinition view) {
        ViewDefinitionDto dto = new ViewDefinitionDto();
        BeanUtils.copyProperties(view, dto);

        // 设置分类名称
        if (view.getCategoryId() != null) {
            viewCategoryRepository.findById(view.getCategoryId())
                    .ifPresent(cat -> dto.setCategoryName(cat.getCategoryName()));
        }

        // 加载实体列表（包含字段）
        List<ViewEntity> entities = viewEntityRepository.findByViewIdOrderBySort(view.getId());
        dto.setEntities(entities.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList()));

        return dto;
    }

    private ViewEntityDto convertToDto(ViewEntity entity) {
        ViewEntityDto dto = new ViewEntityDto();
        BeanUtils.copyProperties(entity, dto);

        // 加载分组
        List<ViewGroup> groups = viewGroupRepository.findByEntityIdOrderBySort(entity.getId());
        dto.setGroups(groups.stream().map(this::convertToDto).collect(Collectors.toList()));

        // 加载字段
        List<ViewField> fields = viewFieldRepository.findByEntityIdOrderBySort(entity.getId());
        dto.setFields(fields.stream().map(this::convertToDto).collect(Collectors.toList()));

        return dto;
    }

    private ViewGroupDto convertToDto(ViewGroup group) {
        ViewGroupDto dto = new ViewGroupDto();
        BeanUtils.copyProperties(group, dto);
        return dto;
    }

    private ViewFieldDto convertToDto(ViewField field) {
        ViewFieldDto dto = new ViewFieldDto();
        BeanUtils.copyProperties(field, dto);

        // 设置字段标准信息
        if (field.getFieldStandardId() != null) {
            fieldStandardRepository.findById(field.getFieldStandardId())
                    .ifPresent(fs -> {
                        dto.setFieldStandardCode(fs.getFieldCode());
                        dto.setFieldStandardName(fs.getFieldName());
                    });
        }

        // 设置分组名称
        if (field.getGroupId() != null) {
            viewGroupRepository.findById(field.getGroupId())
                    .ifPresent(g -> dto.setGroupName(g.getGroupName()));
        }

        return dto;
    }

    private ViewValidationDto convertToDto(ViewValidation validation) {
        ViewValidationDto dto = new ViewValidationDto();
        BeanUtils.copyProperties(validation, dto);

        // 设置触发字段名称
        if (validation.getTriggerFieldId() != null) {
            viewFieldRepository.findById(validation.getTriggerFieldId())
                    .ifPresent(f -> dto.setTriggerFieldName(f.getFieldName()));
        }

        // 设置目标字段名称
        if (validation.getTargetFieldId() != null) {
            viewFieldRepository.findById(validation.getTargetFieldId())
                    .ifPresent(f -> dto.setTargetFieldName(f.getFieldName()));
        }

        return dto;
    }

    private String getCurrentUser() {
        return "admin"; // TODO: 从安全上下文获取
    }
}