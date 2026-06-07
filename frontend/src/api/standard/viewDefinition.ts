import api from '../index'

// 视图定义相关类型
export interface ViewDefinition {
  id?: number
  viewCode: string
  viewName: string
  categoryId?: number
  categoryName?: string
  version?: number
  baseVersionId?: number
  isLatest?: boolean
  layoutColumns?: number
  labelWidth?: number
  enableCopy?: boolean
  enableImport?: boolean
  enableExport?: boolean
  status?: string
  publishTime?: string
  tableName?: string // 主表物理表名
  description?: string
  createdBy?: string
  createdAt?: string
  updatedBy?: string
  updatedAt?: string
  entities?: ViewEntity[]
  validations?: ViewValidation[]
}

export interface ViewEntity {
  id?: number
  viewId?: number
  entityCode: string
  entityName: string
  entityType: 'main' | 'sub'
  sort?: number
  minRows?: number
  maxRows?: number
  enableAdd?: boolean
  enableDelete?: boolean
  enableCopy?: boolean
  enableSort?: boolean
  isInherited?: boolean  // 是否继承实体（修订时）
  status?: string
  description?: string
  groups?: ViewGroup[]
  fields?: ViewField[]
}

export interface ViewGroup {
  id?: number
  entityId?: number
  groupCode: string
  groupName: string
  sort?: number
  columnCount?: number
  collapsible?: boolean
  defaultCollapsed?: boolean
  status?: string
}

export interface ViewField {
  id?: number
  entityId?: number
  fieldCode: string
  fieldName: string
  fieldStandardId?: number
  fieldStandardCode?: string
  fieldStandardName?: string
  fieldType?: string
  length?: number
  precisionVal?: number         // 小数位
  domainId?: number | null      // 值域ID
  domainCode?: string | null    // 值域编码
  domainName?: string | null    // 值域名称
  groupId?: number
  groupName?: string
  sort?: number
  columnSpan?: number
  isRequired?: boolean
  isReadonly?: boolean
  isHidden?: boolean
  isUnique?: boolean
  isQuery?: boolean
  isQueryResult?: boolean
  isSortable?: boolean
  isInherited?: boolean         // 是否继承字段（修订时）
  autoNumber?: boolean           // 是否启用自动编号
  encodingRuleId?: number | null // 编码规则ID
  encodingRuleName?: string | null // 编码规则名称
  defaultValue?: string
  defaultValueType?: string
  refSource?: string
  refId?: number
  refFilter?: string
  refCascadeField?: string
  enumCode?: string
  minLength?: number
  maxLength?: number
  minValue?: number
  maxValue?: number
  regexPattern?: string
  errorMessage?: string
  linkConfig?: string
  placeholder?: string
  tooltip?: string
  status?: string
}

export interface ViewValidation {
  id?: number
  viewId?: number
  ruleCode: string
  ruleName: string
  ruleType: 'cross' | 'custom'
  triggerEntityId?: number
  triggerEntityName?: string
  triggerFieldId?: number
  triggerFieldName?: string
  triggerCondition?: string
  targetEntityId?: number
  targetEntityName?: string
  targetFieldId?: number
  targetFieldName?: string
  action?: string
  actionValue?: string
  errorMessage?: string
  errorLevel?: string
  status?: string
}

// API 接口

// 获取视图列表
export function getViewList(params?: {
  keyword?: string
  categoryId?: number
  status?: string
}) {
  return api.get<{ code: number; data: ViewDefinition[] }>('/standard/view/list', { params })
}

// 获取视图详情
export function getViewDetail(id: number) {
  return api.get<{ code: number; data: ViewDefinition }>(`/standard/view/${id}`)
}

// 获取已发布视图
export function getPublishedView(viewCode: string) {
  return api.get<{ code: number; data: ViewDefinition }>(`/standard/view/published/${viewCode}`)
}

// 创建视图
export function createView(data: ViewDefinition) {
  return api.post<{ code: number; data: ViewDefinition }>('/standard/view', data)
}

// 更新视图
export function updateView(id: number, data: ViewDefinition) {
  return api.put<{ code: number; data: ViewDefinition }>(`/standard/view/${id}`, data)
}

// 发布视图
export function publishView(id: number) {
  return api.post<{ code: number; data: ViewDefinition }>(`/standard/view/${id}/publish`)
}

// 停用视图
export function disableView(id: number) {
  return api.post<{ code: number; data: ViewDefinition }>(`/standard/view/${id}/disable`)
}

// 启用视图
export function enableView(id: number) {
  return api.post<{ code: number; data: ViewDefinition }>(`/standard/view/${id}/enable`)
}

// 修订视图
export function reviseView(id: number) {
  return api.post<{ code: number; data: ViewDefinition }>(`/standard/view/${id}/revise`)
}

// 删除视图
export function deleteView(id: number) {
  return api.delete<{ code: number }>(`/standard/view/${id}`)
}

// 获取版本历史
export function getVersionHistory(viewCode: string) {
  return api.get<{ code: number; data: ViewDefinition[] }>(`/standard/view/versions/${viewCode}`)
}

// 获取视图设计数据（包含实体和字段）
export function getViewDesign(id: number) {
  return api.get<{ code: number; data: ViewDefinition }>(`/standard/view/${id}/design`)
}

// 导出类型别名，方便使用
export type ViewDefinitionDto = ViewDefinition
export type ViewEntityDto = ViewEntity
export type ViewFieldDto = ViewField
