package com.vueadmin.service.data;

import com.vueadmin.dto.ViewDefinitionDto;
import com.vueadmin.dto.ViewEntityDto;
import com.vueadmin.dto.ViewFieldDto;
import com.vueadmin.entity.form.Form;
import com.vueadmin.exception.BusinessException;
import com.vueadmin.repository.FormRepository;
import com.vueadmin.service.standard.ViewDefinitionService;
import com.vueadmin.service.standard.encoding.EncodingGeneratorService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

/**
 * 物理表数据Service
 * 负责从物理表（mdm_开头的表）读取和保存数据
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PhysicalTableDataService {

    private final FormRepository formRepository;
    private final ViewDefinitionService viewDefinitionService;
    private final EntityManager entityManager;
    private final DataOperationLogService operationLogService;
    private final EncodingGeneratorService encodingGeneratorService;
    private final OperationLogService logService; // 新增：操作日志服务

    /**
     * 查询数据列表（从物理表）
     */
    public List<Map<String, Object>> getDataList(Long formId, String status) {
        log.info("开始查询数据列表, formId: {}, status: {}", formId, status);

        // 1. 获取表单对应的视图定义
        Form form = formRepository.findById(formId)
                .orElseThrow(() -> new BusinessException("表单不存在"));

        if (form.getViewId() == null) {
            throw new BusinessException("表单未关联视图");
        }

        log.info("表单关联的视图ID: {}", form.getViewId());

        ViewDefinitionDto viewDefinition = viewDefinitionService.getViewDetail(form.getViewId());

        // 2. 获取主表实体
        ViewEntityDto mainEntity = viewDefinition.getEntities().stream()
                .filter(e -> "main".equals(e.getEntityType()))
                .findFirst()
                .orElseThrow(() -> new BusinessException("视图缺少主表定义"));

        String mainTableName = mainEntity.getTableName();
        log.info("主表物理表名: {}", mainTableName);

        if (mainTableName == null || mainTableName.isEmpty()) {
            throw new BusinessException("主表物理表名未定义");
        }

        // 3. 构建查询SQL
        StringBuilder sql = new StringBuilder("SELECT * FROM ");
        sql.append(mainTableName);

        // 如果指定了状态，按指定状态查询；否则查询所有非删除状态
        if (status != null && !status.isEmpty()) {
            sql.append(" WHERE status = ? ORDER BY created_at DESC");
        } else {
            // 默认查询所有状态（包括已作废），按创建时间倒序
            sql.append(" WHERE status IN ('DRAFT', 'PENDING', 'PENDING_QC', 'ACTIVE_QUALIFIED', 'ACTIVE_UNQUALIFIED', 'OBSOLETE') ORDER BY created_at DESC");
        }

        log.info("执行SQL: {}", sql.toString());

        // 4. 执行查询
        try {
            Query query = entityManager.createNativeQuery(sql.toString());
            if (status != null && !status.isEmpty()) {
                query.setParameter(1, status);
            }
            @SuppressWarnings("unchecked")
            List<Object[]> results = query.getResultList();

            log.info("查询到 {} 条记录", results.size());

            // 5. 转换为Map列表
            return convertToMapList(results, getTableColumns(mainTableName));
        } catch (Exception e) {
            log.error("查询数据失败: {}", e.getMessage(), e);
            throw new BusinessException("查询数据失败：" + e.getMessage());
        }
    }

    /**
     * 获取数据详情（从物理表）
     */
    public Map<String, Object> getDataById(Long formId, Long recordId) {
        // 1. 获取表单对应的视图定义
        Form form = formRepository.findById(formId)
                .orElseThrow(() -> new BusinessException("表单不存在"));

        ViewDefinitionDto viewDefinition = viewDefinitionService.getViewDetail(form.getViewId());

        // 2. 获取主表实体
        ViewEntityDto mainEntity = viewDefinition.getEntities().stream()
                .filter(e -> "main".equals(e.getEntityType()))
                .findFirst()
                .orElseThrow(() -> new BusinessException("视图缺少主表定义"));

        String mainTableName = mainEntity.getTableName();

        // 3. 查询主表数据
        String sql = "SELECT * FROM " + mainTableName + " WHERE id = ?";
        Query query = entityManager.createNativeQuery(sql);
        query.setParameter(1, recordId);

        @SuppressWarnings("unchecked")
        List<Object[]> results = query.getResultList();

        if (results.isEmpty()) {
            throw new BusinessException("数据不存在");
        }

        Map<String, Object> mainData = convertToMap(results.get(0), getTableColumns(mainTableName));

        // 4. 查询子表数据
        Map<String, Object> result = new HashMap<>();
        result.put("main", mainData);

        List<ViewEntityDto> subEntities = viewDefinition.getEntities().stream()
                .filter(e -> "sub".equals(e.getEntityType()))
                .toList();

        for (ViewEntityDto subEntity : subEntities) {
            String subTableName = subEntity.getTableName();
            if (subTableName != null && !subTableName.isEmpty()) {
                String subSql = "SELECT * FROM " + subTableName + " WHERE main_id = ? ORDER BY id";
                Query subQuery = entityManager.createNativeQuery(subSql);
                subQuery.setParameter(1, recordId);

                @SuppressWarnings("unchecked")
                List<Object[]> subResults = subQuery.getResultList();
                List<Map<String, Object>> subDataList = convertToMapList(subResults, getTableColumns(subTableName));
                result.put(subEntity.getEntityCode(), subDataList);
            }
        }

        return result;
    }

    /**
     * 保存数据（到物理表）
     */
    @Transactional
    public Long saveData(Long categoryId, Long formId, Map<String, Object> data, String createdBy) {
        // 1. 获取视图定义
        Form form = formRepository.findById(formId)
                .orElseThrow(() -> new BusinessException("表单不存在"));

        ViewDefinitionDto viewDefinition = viewDefinitionService.getViewDetail(form.getViewId());

        // 2. 获取主表实体
        ViewEntityDto mainEntity = viewDefinition.getEntities().stream()
                .filter(e -> "main".equals(e.getEntityType()))
                .findFirst()
                .orElseThrow(() -> new BusinessException("视图缺少主表定义"));

        String mainTableName = mainEntity.getTableName();

        // 3. 验证主表数据合法性
        validateData(mainEntity, data, "主表");

        // 4. 验证子表数据合法性
        @SuppressWarnings("unchecked")
        Map<String, Object> subData = (Map<String, Object>) data.get("sub");
        if (subData != null) {
            List<ViewEntityDto> subEntities = viewDefinition.getEntities().stream()
                    .filter(e -> "sub".equals(e.getEntityType()))
                    .toList();

            for (ViewEntityDto subEntity : subEntities) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> subTableDataList = (List<Map<String, Object>>) subData.get(subEntity.getEntityCode());
                if (subTableDataList != null) {
                    for (int i = 0; i < subTableDataList.size(); i++) {
                        validateData(subEntity, subTableDataList.get(i), subEntity.getEntityName() + "第" + (i + 1) + "行");
                    }
                }
            }
        }

        // 5. 插入主表数据
        Long mainRecordId = insertMainTable(mainTableName, mainEntity, data, createdBy, form.getViewId());

        // 6. 插入子表数据
        if (subData != null) {
            List<ViewEntityDto> subEntities = viewDefinition.getEntities().stream()
                    .filter(e -> "sub".equals(e.getEntityType()))
                    .toList();

            for (ViewEntityDto subEntity : subEntities) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> subTableDataList = (List<Map<String, Object>>) subData.get(subEntity.getEntityCode());
                if (subTableDataList != null && !subTableDataList.isEmpty()) {
                    String subTableName = subEntity.getTableName();
                    for (Map<String, Object> subTableData : subTableDataList) {
                        insertSubTable(subTableName, subEntity, subTableData, mainRecordId, createdBy);
                    }
                }
            }
        }

        // 7. 记录创建操作日志（状态变更：创建草稿）
        logService.logCreate(categoryId, formId, mainRecordId, createdBy);

        return mainRecordId;
    }

    /**
     * 更新数据（到物理表）
     */
    @Transactional
    public void updateData(Long categoryId, Long formId, Long recordId, Map<String, Object> data, String updatedBy) {
        // 1. 获取视图定义
        Form form = formRepository.findById(formId)
                .orElseThrow(() -> new BusinessException("表单不存在"));

        ViewDefinitionDto viewDefinition = viewDefinitionService.getViewDetail(form.getViewId());

        // 2. 获取更新前的数据快照（用于对比）
        Map<String, Object> oldData = getDataById(formId, recordId);

        // 3. 更新主表数据
        ViewEntityDto mainEntity = viewDefinition.getEntities().stream()
                .filter(e -> "main".equals(e.getEntityType()))
                .findFirst()
                .orElseThrow(() -> new BusinessException("视图缺少主表定义"));

        String mainTableName = mainEntity.getTableName();
        updateMainTable(mainTableName, mainEntity, data, recordId, updatedBy);

        // 4. 更新子表数据
        @SuppressWarnings("unchecked")
        Map<String, Object> subData = (Map<String, Object>) data.get("sub");
        List<ViewEntityDto> subEntities = viewDefinition.getEntities().stream()
                .filter(e -> "sub".equals(e.getEntityType()))
                .toList();

        for (ViewEntityDto subEntity : subEntities) {
            String subTableName = subEntity.getTableName();
            if (subTableName != null && !subTableName.isEmpty() && subData != null) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> newSubTableData = (List<Map<String, Object>>) subData.get(subEntity.getEntityCode());
                updateSubTable(subTableName, subEntity, newSubTableData, recordId, updatedBy);
            }
        }

        // 5. 判断是否为纯状态变更（只有status字段）
        boolean isPureStatusChange = data.size() == 1 && data.containsKey("status");

        // 6. 记录日志
        if (isPureStatusChange) {
            // 纯状态变更，只记录状态变更日志
            logStatusChangeIfNeeded(categoryId, formId, recordId, oldData, data, null, updatedBy);
        } else {
            // 计算增量数据（用于日志详情）
            Map<String, Object> changes = calculateChanges(oldData, data, mainEntity, subEntities);
            // 记录状态变更或更新日志
            logStatusChangeIfNeeded(categoryId, formId, recordId, oldData, data, changes, updatedBy);
        }
    }

    /**
     * 删除数据（物理删除）
     */
    @Transactional
    public void deleteData(Long categoryId, Long formId, Long recordId, String updatedBy) {
        Form form = formRepository.findById(formId)
                .orElseThrow(() -> new BusinessException("表单不存在"));

        ViewDefinitionDto viewDefinition = viewDefinitionService.getViewDetail(form.getViewId());

        ViewEntityDto mainEntity = viewDefinition.getEntities().stream()
                .filter(e -> "main".equals(e.getEntityType()))
                .findFirst()
                .orElseThrow(() -> new BusinessException("视图缺少主表定义"));

        String mainTableName = mainEntity.getTableName();

        // 1. 先获取完整的数据快照（包括主表和子表）
        Map<String, Object> deletedData = getDataById(formId, recordId);

        // 2. 删除子表数据
        List<ViewEntityDto> subEntities = viewDefinition.getEntities().stream()
                .filter(e -> "sub".equals(e.getEntityType()))
                .toList();

        for (ViewEntityDto subEntity : subEntities) {
            String subTableName = subEntity.getTableName();
            if (subTableName != null && !subTableName.isEmpty()) {
                String deleteSubSql = "DELETE FROM " + subTableName + " WHERE main_id = ?";
                Query deleteSubQuery = entityManager.createNativeQuery(deleteSubSql);
                deleteSubQuery.setParameter(1, recordId);
                deleteSubQuery.executeUpdate();
            }
        }

        // 3. 删除主表数据
        String deleteMainSql = "DELETE FROM " + mainTableName + " WHERE id = ?";
        Query deleteMainQuery = entityManager.createNativeQuery(deleteMainSql);
        deleteMainQuery.setParameter(1, recordId);
        int deletedRows = deleteMainQuery.executeUpdate();

        if (deletedRows == 0) {
            throw new BusinessException("数据不存在或已被删除");
        }

        // 4. 记录删除操作日志（存储完整快照，包括主表和子表）
        operationLogService.logDeleteOperation(categoryId, formId, recordId, deletedData, updatedBy);
    }

    /**
     * 插入主表数据
     */
    private Long insertMainTable(String tableName, ViewEntityDto entity, Map<String, Object> data,
                                 String createdBy, Long viewId) {
        // 1. 生成自动编号字段的值
        generateAutoNumbers(entity, data);

        StringBuilder columns = new StringBuilder();
        StringBuilder values = new StringBuilder();
        List<Object> params = new ArrayList<>();

        columns.append("view_id, ");
        values.append("?, ");
        params.add(viewId);

        // 系统自动管理的字段，不需要从用户数据中插入
        Set<String> systemFields = Set.of("id", "status", "created_by", "created_at", "updated_by", "updated_at", "view_id",
                "ernam", "erdat", "aenam", "laeda");  // 添加SAP风格字段

        for (var field : entity.getFields()) {
            String fieldCode = field.getFieldCode().toLowerCase();
            // 跳过系统字段
            if (systemFields.contains(fieldCode)) {
                continue;
            }
            if (data.containsKey(field.getFieldCode())) {
                columns.append(field.getFieldCode()).append(", ");
                values.append("?, ");
                params.add(data.get(field.getFieldCode()));
            }
        }

        // 自动填充系统字段
        columns.append("status, created_by, created_at");
        values.append("'DRAFT', ?, NOW()");  // 新增数据默认状态为草稿
        params.add(createdBy);

        // 自动填充SAP风格字段
        columns.append(", ernam, erdat");
        values.append(", ?, CURDATE()");
        params.add(createdBy);  // ERNAM = 创建人

        String sql = "INSERT INTO " + tableName + " (" + columns + ") VALUES (" + values + ")";
        Query query = entityManager.createNativeQuery(sql);
        for (int i = 0; i < params.size(); i++) {
            query.setParameter(i + 1, params.get(i));
        }

        query.executeUpdate();

        // 获取最后插入的ID
        Query idQuery = entityManager.createNativeQuery("SELECT LAST_INSERT_ID()");
        return ((Number) idQuery.getSingleResult()).longValue();
    }

    /**
     * 插入子表数据
     */
    private void insertSubTable(String tableName, ViewEntityDto entity, Map<String, Object> data,
                               Long mainRecordId, String createdBy) {
        StringBuilder columns = new StringBuilder();
        StringBuilder values = new StringBuilder();
        List<Object> params = new ArrayList<>();

        columns.append("main_id, ");
        values.append("?, ");
        params.add(mainRecordId);

        // 系统自动管理的字段，不需要从用户数据中插入
        Set<String> systemFields = Set.of("id", "status", "created_by", "created_at", "updated_by", "updated_at", "main_id");

        for (var field : entity.getFields()) {
            String fieldCode = field.getFieldCode().toLowerCase();
            // 跳过系统字段
            if (systemFields.contains(fieldCode)) {
                continue;
            }
            if (data.containsKey(field.getFieldCode())) {
                columns.append(field.getFieldCode()).append(", ");
                values.append("?, ");
                params.add(data.get(field.getFieldCode()));
            }
        }

        // 自动填充系统字段
        columns.append("status, created_by, created_at");
        values.append("'DRAFT', ?, NOW()");  // 新增数据默认状态为草稿
        params.add(createdBy);

        String sql = "INSERT INTO " + tableName + " (" + columns + ") VALUES (" + values + ")";
        Query query = entityManager.createNativeQuery(sql);
        for (int i = 0; i < params.size(); i++) {
            query.setParameter(i + 1, params.get(i));
        }
        query.executeUpdate();
    }

    /**
     * 更新主表数据
     */
    private void updateMainTable(String tableName, ViewEntityDto entity, Map<String, Object> data,
                                 Long recordId, String updatedBy) {
        StringBuilder sql = new StringBuilder("UPDATE " + tableName + " SET ");
        List<Object> params = new ArrayList<>();

        // 系统自动管理的字段，不允许前端修改（status除外，允许状态变更）
        Set<String> systemFields = Set.of("id", "created_by", "created_at", "updated_by", "updated_at", "view_id",
                "ernam", "erdat", "aenam", "laeda");  // 添加SAP风格字段

        // 先处理status字段（状态变更）
        if (data.containsKey("status")) {
            sql.append("status = ?, ");
            params.add(data.get("status"));
        }

        for (var field : entity.getFields()) {
            String fieldCode = field.getFieldCode();
            // 跳过系统字段
            if (systemFields.contains(fieldCode.toLowerCase())) {
                continue;
            }
            if (data.containsKey(fieldCode)) {
                sql.append(fieldCode).append(" = ?, ");
                params.add(data.get(fieldCode));
            }
        }

        if (params.isEmpty()) {
            return; // 没有需要更新的字段
        }

        // 自动填充修改人和修改时间
        sql.append("updated_by = ?, updated_at = NOW()");
        params.add(updatedBy);

        // 自动填充SAP风格字段：修改人和修改日期
        sql.append(", aenam = ?, laeda = CURDATE()");
        params.add(updatedBy);  // AENAM = 修改人

        sql.append(" WHERE id = ?");
        params.add(recordId);

        Query query = entityManager.createNativeQuery(sql.toString());
        for (int i = 0; i < params.size(); i++) {
            query.setParameter(i + 1, params.get(i));
        }
        query.executeUpdate();
    }

    /**
     * 更新子表数据（先删除旧的，再插入新的）
     */
    private void updateSubTable(String tableName, ViewEntityDto entity, List<Map<String, Object>> newDataList,
                                Long mainRecordId, String updatedBy) {
        // 1. 删除旧的子表数据
        String deleteSql = "DELETE FROM " + tableName + " WHERE main_id = ?";
        Query deleteQuery = entityManager.createNativeQuery(deleteSql);
        deleteQuery.setParameter(1, mainRecordId);
        deleteQuery.executeUpdate();

        // 2. 插入新的子表数据
        if (newDataList != null && !newDataList.isEmpty()) {
            for (Map<String, Object> newData : newDataList) {
                insertSubTable(tableName, entity, newData, mainRecordId, updatedBy);
            }
        }
    }

    /**
     * 计算增量数据（只存储变化的字段）
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> calculateChanges(Map<String, Object> oldData, Map<String, Object> newData,
                                                  ViewEntityDto mainEntity, List<ViewEntityDto> subEntities) {
        Map<String, Object> changes = new HashMap<>();

        // 1. 计算主表变更
        Map<String, Object> oldMain = (Map<String, Object>) oldData.get("main");
        Map<String, Object> newMain = new HashMap<>(newData);
        newMain.remove("sub"); // 移除子表数据

        Map<String, Object> mainChanges = new HashMap<>();
        for (var field : mainEntity.getFields()) {
            String fieldCode = field.getFieldCode();
            Object oldValue = oldMain.get(fieldCode);
            Object newValue = newMain.get(fieldCode);

            // 只记录变化的字段
            if (!Objects.equals(oldValue, newValue)) {
                Map<String, Object> change = new HashMap<>();
                change.put("oldValue", oldValue);
                change.put("newValue", newValue);
                mainChanges.put(fieldCode, change);
            }
        }
        if (!mainChanges.isEmpty()) {
            changes.put("mainTable", mainChanges);
        }

        // 2. 计算子表变更
        Map<String, Object> newSubData = (Map<String, Object>) newData.get("sub");
        Map<String, Object> subChanges = new HashMap<>();

        for (ViewEntityDto subEntity : subEntities) {
            String entityCode = subEntity.getEntityCode();

            List<Map<String, Object>> oldSubList = (List<Map<String, Object>>) oldData.get(entityCode);
            List<Map<String, Object>> newSubList = newSubData != null ?
                    (List<Map<String, Object>>) newSubData.get(entityCode) : null;

            // 比较子表数据变化
            Map<String, Object> subChange = new HashMap<>();
            int oldCount = oldSubList != null ? oldSubList.size() : 0;
            int newCount = newSubList != null ? newSubList.size() : 0;

            if (oldCount != newCount) {
                subChange.put("countChanged", true);
                subChange.put("oldCount", oldCount);
                subChange.put("newCount", newCount);
            }

            // 如果子表数据有变化，存储完整的新旧数据（子表数据量通常较小）
            if (!subChange.isEmpty() || !Objects.equals(oldSubList, newSubList)) {
                subChange.put("oldData", oldSubList);
                subChange.put("newData", newSubList);
                subChanges.put(entityCode, subChange);
            }
        }

        if (!subChanges.isEmpty()) {
            changes.put("subTables", subChanges);
        }

        return changes;
    }

    /**
     * 验证数据合法性
     * @param entity 视图实体定义
     * @param data 待验证的数据
     * @param context 上下文信息（用于错误提示）
     */
    private void validateData(ViewEntityDto entity, Map<String, Object> data, String context) {
        if (entity.getFields() == null) return;

        // 系统保留字段，不需要验证
        Set<String> systemFields = Set.of("ID", "VIEW_ID", "STATUS",
                "CREATED_BY", "CREATED_AT", "UPDATED_BY", "UPDATED_AT",
                "ERNAM", "ERDAT", "AENAM", "LAEDA");

        for (var field : entity.getFields()) {
            String fieldCode = field.getFieldCode();
            String fieldName = field.getFieldName();
            Object value = data.get(fieldCode);

            // 跳过系统保留字段
            if (systemFields.contains(fieldCode.toUpperCase())) {
                continue;
            }

            // 1. 必填验证（跳过自动编号字段）
            if (Boolean.TRUE.equals(field.getIsRequired()) && !Boolean.TRUE.equals(field.getAutoNumber())) {
                if (value == null || (value instanceof String && ((String) value).trim().isEmpty())) {
                    throw new BusinessException(context + "的「" + fieldName + "」为必填项");
                }
            }

            // 2. 长度验证（仅对字符串类型）
            if (value != null && value instanceof String && field.getLength() != null) {
                String strValue = (String) value;
                if (strValue.length() > field.getLength()) {
                    throw new BusinessException(context + "的「" + fieldName + "」长度超过限制，最大允许" + field.getLength() + "个字符，当前" + strValue.length() + "个字符");
                }
            }

            // 3. 小数位验证（仅对数字类型）
            if (value != null && value instanceof Number && field.getPrecisionVal() != null) {
                // 数字类型的小数位验证可以在这里添加
            }
        }
    }

    /**
     * 获取表的所有列名
     */
    private List<String> getTableColumns(String tableName) {
        String sql = "SHOW COLUMNS FROM " + tableName;
        Query query = entityManager.createNativeQuery(sql);
        @SuppressWarnings("unchecked")
        List<Object[]> results = query.getResultList();

        return results.stream()
                .map(row -> row[0].toString())
                .toList();
    }

    /**
     * 转换查询结果为Map列表
     */
    private List<Map<String, Object>> convertToMapList(List<Object[]> results, List<String> columns) {
        return results.stream()
                .map(row -> convertToMap(row, columns))
                .toList();
    }

    /**
     * 转换单行结果为Map
     */
    private Map<String, Object> convertToMap(Object[] row, List<String> columns) {
        Map<String, Object> map = new HashMap<>();
        for (int i = 0; i < columns.size() && i < row.length; i++) {
            map.put(columns.get(i), row[i]);
        }
        return map;
    }

    /**
     * 为自动编号字段生成编码
     * @param entity 视图实体定义
     * @param data 数据Map（会被修改，添加生成的编码）
     */
    private void generateAutoNumbers(ViewEntityDto entity, Map<String, Object> data) {
        if (entity.getFields() == null) return;

        for (ViewFieldDto field : entity.getFields()) {
            // 只处理自动编号字段
            if (Boolean.TRUE.equals(field.getAutoNumber()) && field.getEncodingRuleId() != null) {
                try {
                    // 获取编码规则ID
                    Long ruleId = field.getEncodingRuleId();

                    // 从数据库获取编码规则编码
                    String sql = "SELECT rule_code FROM std_encoding_rule WHERE id = ?";
                    Query query = entityManager.createNativeQuery(sql);
                    query.setParameter(1, ruleId);
                    Object result = query.getSingleResult();
                    String ruleCode = result != null ? result.toString() : null;

                    if (ruleCode == null) {
                        log.warn("字段 {} 的编码规则不存在: ruleId={}", field.getFieldName(), ruleId);
                        continue;
                    }

                    // 生成编码
                    String generatedCode = encodingGeneratorService.generate(ruleCode, data);

                    // 将生成的编码设置到数据中
                    data.put(field.getFieldCode(), generatedCode);

                    log.info("自动生成编号成功: 字段={}, 规则={}, 生成值={}",
                            field.getFieldName(), ruleCode, generatedCode);

                } catch (Exception e) {
                    log.error("生成自动编号失败: 字段={}, 错误={}",
                            field.getFieldName(), e.getMessage(), e);
                    throw new BusinessException("生成「" + field.getFieldName() + "」自动编号失败: " + e.getMessage());
                }
            }
        }
    }

    /**
     * 记录状态变更日志（辅助方法）
     * 在更新数据时，如果包含status字段，自动记录状态变更日志
     * 如果只是普通更新（无状态变更），记录普通更新日志
     */
    private void logStatusChangeIfNeeded(Long categoryId, Long formId, Long recordId,
                                         Map<String, Object> oldData, Map<String, Object> newData,
                                         Map<String, Object> changes, String user) {
        String newStatus = (String) newData.get("status");

        // 从 oldData 中获取旧状态
        String oldStatus = null;
        if (oldData != null && oldData.get("main") instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> mainData = (Map<String, Object>) oldData.get("main");
            oldStatus = mainData != null ? (String) mainData.get("status") : null;
        }

        // 判断是否有状态变更
        boolean statusChanged = newStatus != null && !newStatus.equals(oldStatus);

        if (statusChanged) {
            // 有状态变更，根据状态变更类型记录不同的日志
            switch (newStatus) {
                case "PENDING":
                    logService.logSubmitApproval(categoryId, formId, recordId, user);
                    break;
                case "PENDING_QC":
                    logService.logApprove(categoryId, formId, recordId, user);
                    break;
                case "DRAFT":
                    if ("PENDING".equals(oldStatus)) {
                        String reason = (String) newData.get("rejectReason");
                        logService.logReject(categoryId, formId, recordId, reason, user);
                    } else if ("OBSOLETE".equals(oldStatus)) {
                        logService.logRestore(categoryId, formId, recordId, user);
                    } else {
                        // 其他情况记录普通更新日志
                        operationLogService.logUpdateOperation(categoryId, formId, recordId, changes, user);
                    }
                    break;
                case "ACTIVE_QUALIFIED":
                    // 质检合格日志：记录评分
                    if ("PENDING_QC".equals(oldStatus) || "ACTIVE_UNQUALIFIED".equals(oldStatus)) {
                        Object scoreObj = newData.get("qualityScore");
                        BigDecimal qualityScore = null;
                        if (scoreObj != null) {
                            try {
                                qualityScore = new BigDecimal(scoreObj.toString());
                            } catch (Exception ignored) {}
                        }
                        logService.logQualityPass(categoryId, formId, recordId, qualityScore, null, user);
                    } else {
                        operationLogService.logUpdateOperation(categoryId, formId, recordId, changes, user);
                    }
                    break;
                case "ACTIVE_UNQUALIFIED":
                    // 质检不合格日志：记录评分和问题
                    if ("PENDING_QC".equals(oldStatus) || "ACTIVE_UNQUALIFIED".equals(oldStatus)) {
                        Object scoreObj = newData.get("qualityScore");
                        Object issuesObj = newData.get("qualityIssues");
                        BigDecimal qualityScore = null;
                        if (scoreObj != null) {
                            try {
                                qualityScore = new BigDecimal(scoreObj.toString());
                            } catch (Exception ignored) {}
                        }
                        logService.logQualityFail(categoryId, formId, recordId, qualityScore, issuesObj, user);
                    } else {
                        operationLogService.logUpdateOperation(categoryId, formId, recordId, changes, user);
                    }
                    break;
                case "OBSOLETE":
                    String reason = (String) newData.get("obsoleteReason");
                    logService.logObsolete(categoryId, formId, recordId, reason, user);
                    break;
                default:
                    // 未知状态，记录普通更新日志
                    operationLogService.logUpdateOperation(categoryId, formId, recordId, changes, user);
                    break;
            }
        } else {
            // 无状态变更，只记录普通更新日志（如果有变更）
            if (changes != null && !changes.isEmpty()) {
                operationLogService.logUpdateOperation(categoryId, formId, recordId, changes, user);
            }
        }
    }
}
