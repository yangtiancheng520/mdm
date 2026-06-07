import api from '@/api/index'

/**
 * 表单管理接口
 */

export interface FormDto {
  id: number
  formCode: string
  formName: string
  formType: string
  viewId: number
  viewName?: string
  designMode: string
  styleTemplate: string
  layoutConfig: string
  enableCopy: boolean
  enableImport: boolean
  enableExport: boolean
  status: string
  isDefault: boolean
  version: number
  createdBy: string
  createdAt: string
  updatedBy: string
  updatedAt: string
  description: string
}

export interface FormGroupDto {
  id: number
  formId: number
  groupCode: string
  groupName: string
  groupType: string // group/tab/table
  sort: number
  isCollapsed: boolean
}

export interface FormComponentDto {
  id: number
  formId: number
  viewFieldId: number
  fieldCode: string
  fieldName: string
  fieldType: string
  domainId: number
  groupId: number
  rowIndex: number
  colIndex: number
  colSpan: number
  rowSpan: number
  isRequired: boolean
  isReadonly: boolean
  isHidden: boolean
  defaultValue: string
  placeholder: string
  validationRules: string
  labelWidth: number
  componentWidth: string
  sort: number
  status: string
}

// 子表字段配置
export interface SubTableFieldDto {
  id: number
  fieldCode: string
  fieldName: string
  fieldType: string
  _uid?: number
}

// 子表配置
export interface SubTableDto {
  entityId: number
  entityName: string
  entityCode: string
  fields: SubTableFieldDto[]
}

// 布局配置
export interface LayoutConfigDto {
  subTableLayout: string // full-tabs | tabs | collapse | flat
  subTables: SubTableDto[]
}

export interface FormDesignRequest {
  id?: number
  formCode?: string
  formName: string
  formType: string
  viewId?: number
  designMode?: string
  styleTemplate?: string
  layoutConfig?: string // JSON string of LayoutConfigDto
  enableCopy?: boolean
  enableImport?: boolean
  enableExport?: boolean
  description?: string
  groups?: FormGroupDto[]
  components?: FormComponentDto[]
}

/**
 * 获取表单列表
 */
export function getFormList(params?: {
  formName?: string
  formType?: string
  viewId?: number
  status?: string
}) {
  return api.get<FormDto[]>('/form/list', { params })
}

/**
 * 获取表单详情
 */
export function getForm(id: number) {
  return api.get<FormDto>(`/form/${id}`)
}

/**
 * 获取表单设计数据
 */
export function getFormDesign(id: number) {
  return api.get<FormDesignRequest>(`/form/${id}/design`)
}

/**
 * 创建空白表单
 */
export function createForm(data: FormDesignRequest) {
  return api.post<FormDto>('/form/create', data)
}

/**
 * 自动生成表单
 */
export function autoGenerateForm(data: FormDesignRequest) {
  return api.post<FormDto>('/form/auto-generate', data)
}

/**
 * 保存表单设计
 */
export function saveFormDesign(id: number, data: FormDesignRequest) {
  return api.post<void>(`/form/${id}/save-design`, data)
}

/**
 * 更新表单
 */
export function updateForm(id: number, data: FormDesignRequest) {
  return api.put<FormDto>(`/form/${id}`, data)
}

/**
 * 删除表单
 */
export function deleteForm(id: number) {
  return api.delete<void>(`/form/${id}`)
}

/**
 * 发布表单
 */
export function publishForm(id: number) {
  return api.post<void>(`/form/${id}/publish`)
}

/**
 * 设置默认表单
 */
export function setDefaultForm(id: number) {
  return api.post<void>(`/form/${id}/set-default`)
}
