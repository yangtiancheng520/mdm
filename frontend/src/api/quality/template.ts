import api from '../index'

// 质量规则模板类型定义
export interface QualityRuleTemplate {
  id?: number
  templateCode: string
  templateName: string
  templateType?: string
  checkType?: string
  checkConfig?: string
  severity?: string
  description?: string
  status?: string
  isSystem?: boolean  // 是否系统模板
  category?: string   // 模板分类
  tags?: string       // 标签
  createdBy?: string
  createdAt?: string
  updatedBy?: string
  updatedAt?: string
}

// 获取模板列表
export function getTemplateList(params?: { templateType?: string; status?: string }) {
  return api.get<{ code: number; data: QualityRuleTemplate[] }>('/quality/template/list', { params })
}

// 获取模板详情
export function getTemplateById(id: number) {
  return api.get<{ code: number; data: QualityRuleTemplate }>(`/quality/template/${id}`)
}

// 创建模板
export function createTemplate(data: QualityRuleTemplate) {
  return api.post<{ code: number; data: QualityRuleTemplate }>('/quality/template/create', data)
}

// 更新模板
export function updateTemplate(id: number, data: QualityRuleTemplate) {
  return api.put<{ code: number; data: QualityRuleTemplate }>(`/quality/template/update/${id}`, data)
}

// 删除模板
export function deleteTemplate(id: number) {
  return api.delete<{ code: number }>(`/quality/template/delete/${id}`)
}

// 发布模板（草稿 -> 启用）
export function publishTemplate(id: number) {
  return api.post<{ code: number; data: QualityRuleTemplate }>(`/quality/template/publish/${id}`)
}

// 重置模板（启用 -> 草稿）
export function resetTemplate(id: number) {
  return api.post<{ code: number; data: QualityRuleTemplate }>(`/quality/template/reset/${id}`)
}

// 复制模板
export function copyTemplate(id: number, newName?: string) {
  return api.post<{ code: number; data: QualityRuleTemplate }>(`/quality/template/copy/${id}`, null, {
    params: { newName }
  })
}
