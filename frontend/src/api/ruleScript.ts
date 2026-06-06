import api from './index'

export interface RuleScript {
  id: number
  scriptCode: string
  scriptName: string
  scriptType: string
  scriptContent: string
  inputParams: ScriptParam[]
  outputParams: ScriptParam[]
  status: string
  createdBy: string
  createdAt: string
  description: string
}

export interface ScriptParam {
  name: string
  type: string
  required: boolean
  description: string
}

export interface RuleScriptForm {
  id?: number
  scriptCode: string
  scriptName: string
  scriptType: string
  scriptContent: string
  inputParams?: ScriptParam[]
  outputParams?: ScriptParam[]
  status?: string
  createdBy?: string
  description?: string
}

// 获取规则脚本列表
export function getRuleScriptList(params?: { scriptCode?: string; scriptName?: string; scriptType?: string; status?: string }) {
  return api.get('/rule-script/list', { params })
}

// 获取启用的脚本
export function getActiveRuleScripts() {
  return api.get('/rule-script/active')
}

// 获取脚本详情
export function getRuleScriptById(id: number) {
  return api.get(`/rule-script/${id}`)
}

// 根据编码获取脚本
export function getRuleScriptByCode(scriptCode: string) {
  return api.get(`/rule-script/code/${scriptCode}`)
}

// 创建规则脚本
export function createRuleScript(data: RuleScriptForm) {
  return api.post('/rule-script/create', data)
}

// 更新规则脚本
export function updateRuleScript(id: number, data: RuleScriptForm) {
  return api.put(`/rule-script/update/${id}`, data)
}

// 删除规则脚本
export function deleteRuleScript(id: number) {
  return api.delete(`/rule-script/delete/${id}`)
}

// 测试脚本
export function testRuleScript(id: number, testInput?: string) {
  return api.post(`/rule-script/test/${id}`, { testInput })
}

// 执行脚本
export function executeRuleScript(scriptCode: string, params?: Record<string, any>) {
  return api.post(`/rule-script/execute/${scriptCode}`, params)
}
