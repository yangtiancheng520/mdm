-- =============================================
-- 删除视图编码为 qqqq 的视图相关数据
-- =============================================

USE mdm;

-- 1. 先查看要删除的视图信息
SELECT id, view_code, view_name, category_id, status FROM std_view WHERE view_code = 'qqqq';

-- ==========================================
-- 执行删除（按依赖关系从子表到主表）
-- ==========================================

-- 2. 删除视图校验规则
DELETE FROM std_view_validation
WHERE view_id IN (SELECT id FROM std_view WHERE view_code = 'qqqq');

-- 3. 删除字段分组
DELETE FROM std_view_group
WHERE entity_id IN (
    SELECT id FROM std_view_entity
    WHERE view_id IN (SELECT id FROM std_view WHERE view_code = 'qqqq')
);

-- 4. 删除视图字段
DELETE FROM std_view_field
WHERE entity_id IN (
    SELECT id FROM std_view_entity
    WHERE view_id IN (SELECT id FROM std_view WHERE view_code = 'qqqq')
);

-- 5. 删除视图实体
DELETE FROM std_view_entity
WHERE view_id IN (SELECT id FROM std_view WHERE view_code = 'qqqq');

-- 6. 删除关联的表单组件
DELETE FROM frm_component
WHERE form_id IN (
    SELECT id FROM frm_form
    WHERE view_id IN (SELECT id FROM std_view WHERE view_code = 'qqqq')
);

-- 7. 删除关联的表单分组
DELETE FROM frm_group
WHERE form_id IN (
    SELECT id FROM frm_form
    WHERE view_id IN (SELECT id FROM std_view WHERE view_code = 'qqqq')
);

-- 8. 删除数据分类中关联的表单
DELETE FROM data_category
WHERE form_id IN (
    SELECT id FROM frm_form
    WHERE view_id IN (SELECT id FROM std_view WHERE view_code = 'qqqq')
);

-- 9. 删除关联的表单
DELETE FROM frm_form
WHERE view_id IN (SELECT id FROM std_view WHERE view_code = 'qqqq');

-- 10. 最后删除视图定义
DELETE FROM std_view WHERE view_code = 'qqqq';

-- 验证删除结果
SELECT '视图删除完成' AS result;
SELECT COUNT(*) AS remaining_views FROM std_view WHERE view_code = 'qqqq';
