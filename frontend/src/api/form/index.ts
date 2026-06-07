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
  componentType: string // 控件类型
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

// 控件类型定义
export const COMPONENT_TYPES: Record<string, { label: string; value: string; hasDomain: boolean }> = {
  // 有值域时的控件
  select: { label: '下拉选择', value: 'select', hasDomain: true },
  radio: { label: '单选框', value: 'radio', hasDomain: true },
  checkbox: { label: '复选框', value: 'checkbox', hasDomain: true },
  // 无值域时的控件
  input: { label: '单行文本', value: 'input', hasDomain: false },
  textarea: { label: '多行文本', value: 'textarea', hasDomain: false },
  inputNumber: { label: '数字输入', value: 'inputNumber', hasDomain: false },
  slider: { label: '滑块', value: 'slider', hasDomain: false },
  rate: { label: '评分', value: 'rate', hasDomain: false },
  datePicker: { label: '日期选择', value: 'datePicker', hasDomain: false },
  dateTimePicker: { label: '日期时间', value: 'dateTimePicker', hasDomain: false },
  timePicker: { label: '时间选择', value: 'timePicker', hasDomain: false },
  switch: { label: '开关', value: 'switch', hasDomain: false },
}

// 根据字段类型获取默认控件
export function getDefaultComponentType(fieldType: string, hasDomain: boolean): string {
  if (hasDomain) return 'select'
  switch (fieldType) {
    case 'string': return 'input'
    case 'number': return 'inputNumber'
    case 'date': return 'datePicker'
    case 'datetime': return 'dateTimePicker'
    case 'boolean': return 'switch'
    default: return 'input'
  }
}

// 根据是否有值域获取可选控件列表
export function getAvailableComponentTypes(hasDomain: boolean): string[] {
  return Object.values(COMPONENT_TYPES)
    .filter(t => t.hasDomain === hasDomain)
    .map(t => t.value)
}

// 子表字段配置
export interface SubTableFieldDto {
  id: number
  fieldCode: string
  fieldName: string
  fieldType: string
  domainId?: number
  componentType?: string
  isRequired?: boolean
  isReadonly?: boolean
  placeholder?: string
  defaultValue?: string
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
 * 取消发布表单
 */
export function unpublishForm(id: number) {
  return api.post<void>(`/form/${id}/unpublish`)
}

/**
 * 设置默认表单
 */
export function setDefaultForm(id: number) {
  return api.post<void>(`/form/${id}/set-default`)
}
