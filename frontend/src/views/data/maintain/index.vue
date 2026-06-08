<template>
  <div class="data-maintain-page">
    <!-- 全屏表单模式 -->
    <template v-if="showFullScreenForm">
      <div class="full-screen-form">
        <!-- 顶部工具栏 -->
        <div class="form-header">
          <div class="header-left">
            <el-button @click="handleCloseForm">
              <el-icon><ArrowLeft /></el-icon>
              返回
            </el-button>
            <span class="form-title">{{ formDialogTitle }}</span>
          </div>
          <div class="header-right">
            <el-button type="primary" @click="handleSaveForm" :loading="saving">
              <el-icon><Check /></el-icon>
              保存
            </el-button>
            <el-button v-if="!editingInstance" type="warning" @click="handleFillTestData">
              <el-icon><Position /></el-icon>
              一键填入测试数据
            </el-button>
          </div>
        </div>

        <!-- 表单主体 -->
        <div class="form-body">
          <!-- 全Tab布局 -->
          <template v-if="layoutConfig.subTableLayout === 'full-tabs'">
            <div class="form-tabs-layout">
              <div class="form-tabs-header">
                <div
                  class="form-tab"
                  :class="{ active: activeFormTab === -1 }"
                  @click="activeFormTab = -1"
                >
                  主表信息
                </div>
                <div
                  v-for="(entity, index) in subEntities"
                  :key="entity.id"
                  class="form-tab"
                  :class="{ active: activeFormTab === index }"
                  @click="activeFormTab = index"
                >
                  {{ entity.entityName }}
                </div>
              </div>

              <div class="form-tabs-content">
                <!-- 主表内容 -->
                <template v-if="activeFormTab === -1">
                  <el-form label-width="120px" :model="formData">
                    <el-row :gutter="20">
                      <el-col :span="6" v-for="field in filteredFormFields" :key="field.fieldCode">
                        <el-form-item :label="field.fieldName" :required="field.isRequired && !field.autoNumber">
                          <component
                            :is="getFieldComponent(field)"
                            v-model="formData[field.fieldCode]"
                            :placeholder="`请输入${field.fieldName}`"
                            :maxlength="field.length"
                            style="width: 100%"
                          />
                        </el-form-item>
                      </el-col>
                    </el-row>
                  </el-form>
                </template>

                <!-- 子表内容 -->
                <template v-else>
                  <div class="subtable-section">
                    <div class="subtable-toolbar">
                      <el-button type="primary" size="small" @click="handleAddSubTableRow(activeFormTab)">
                        <el-icon><Plus /></el-icon>
                        添加行
                      </el-button>
                      <el-button size="small" @click="handleDeleteSubTableRow(activeFormTab)">
                        <el-icon><Delete /></el-icon>
                        删除选中
                      </el-button>
                    </div>
                    <el-table
                      :data="getSubTableData(activeFormTab)"
                      border
                      stripe
                      max-height="400"
                      @selection-change="(rows: any[]) => handleSubTableSelectionChange(activeFormTab, rows)"
                    >
                      <el-table-column type="selection" width="50" />
                      <el-table-column
                        v-for="field in getSubTableFields(activeFormTab)"
                        :key="field.fieldCode"
                        :prop="field.fieldCode"
                        :label="field.fieldName"
                        min-width="150"
                      >
                        <template #default="{ row }">
                          <!-- 文本输入 -->
                          <el-input
                            v-if="getInputType(field) === 'input'"
                            v-model="row[field.fieldCode]"
                            size="small"
                            :placeholder="`请输入`"
                            :maxlength="field.length"
                          />
                          <!-- 多行文本 -->
                          <el-input
                            v-else-if="getInputType(field) === 'textarea'"
                            v-model="row[field.fieldCode]"
                            size="small"
                            type="textarea"
                            :rows="1"
                          />
                          <!-- 数字输入 -->
                          <el-input-number
                            v-else-if="getInputType(field) === 'number'"
                            v-model="row[field.fieldCode]"
                            size="small"
                            controls-position="right"
                            :precision="field.precisionVal"
                          />
                          <!-- 日期选择 -->
                          <el-date-picker
                            v-else-if="getInputType(field) === 'date'"
                            v-model="row[field.fieldCode]"
                            size="small"
                            type="date"
                            placeholder="请选择"
                            value-format="YYYY-MM-DD"
                          />
                          <!-- 日期时间选择 -->
                          <el-date-picker
                            v-else-if="getInputType(field) === 'datetime'"
                            v-model="row[field.fieldCode]"
                            size="small"
                            type="datetime"
                            placeholder="请选择"
                            value-format="YYYY-MM-DD HH:mm:ss"
                          />
                          <!-- 下拉选择 -->
                          <el-select
                            v-else-if="getInputType(field) === 'select'"
                            v-model="row[field.fieldCode]"
                            size="small"
                            placeholder="请选择"
                            clearable
                          >
                            <el-option
                              v-for="opt in getDomainOptions(field.domainId)"
                              :key="opt.value"
                              :label="opt.label"
                              :value="opt.value"
                            />
                          </el-select>
                          <!-- 开关 -->
                          <el-switch
                            v-else-if="getInputType(field) === 'switch'"
                            v-model="row[field.fieldCode]"
                            size="small"
                          />
                          <!-- 默认文本输入 -->
                          <el-input
                            v-else
                            v-model="row[field.fieldCode]"
                            size="small"
                          />
                        </template>
                      </el-table-column>
                    </el-table>
                  </div>
                </template>
              </div>
            </div>
          </template>

          <!-- 主表+子表Tab布局 -->
          <template v-else-if="layoutConfig.subTableLayout === 'tabs'">
            <div class="form-main-layout">
              <!-- 主表信息 -->
              <div class="form-section">
                <div class="form-section-title">主表信息</div>
                <el-form ref="formRef" label-width="120px" :model="formData" :rules="formRules">
                  <el-row :gutter="20">
                    <el-col :span="6" v-for="field in formFields" :key="field.fieldCode">
                      <el-form-item :label="field.fieldName" :prop="field.fieldCode">
                        <!-- 文本输入 -->
                        <el-input
                          v-if="getInputType(field) === 'input'"
                          v-model="formData[field.fieldCode]"
                          :placeholder="getFieldPlaceholder(field)"
                          :maxlength="field.length"
                          :disabled="isFieldDisabled(field)"
                        />
                        <!-- 多行文本 -->
                        <el-input
                          v-else-if="getInputType(field) === 'textarea'"
                          v-model="formData[field.fieldCode]"
                          type="textarea"
                          :rows="2"
                          :placeholder="getFieldPlaceholder(field)"
                          :disabled="isFieldDisabled(field)"
                        />
                        <!-- 数字输入 -->
                        <el-input-number
                          v-else-if="getInputType(field) === 'number'"
                          v-model="formData[field.fieldCode]"
                          style="width: 100%"
                          controls-position="right"
                          :precision="field.precisionVal"
                          :disabled="isFieldDisabled(field)"
                        />
                        <!-- 日期选择 -->
                        <el-date-picker
                          v-else-if="getInputType(field) === 'date'"
                          v-model="formData[field.fieldCode]"
                          style="width: 100%"
                          type="date"
                          placeholder="请选择日期"
                          value-format="YYYY-MM-DD"
                          :disabled="isFieldDisabled(field)"
                        />
                        <!-- 日期时间选择 -->
                        <el-date-picker
                          v-else-if="getInputType(field) === 'datetime'"
                          v-model="formData[field.fieldCode]"
                          style="width: 100%"
                          type="datetime"
                          placeholder="请选择日期时间"
                          value-format="YYYY-MM-DD HH:mm:ss"
                          :disabled="isFieldDisabled(field)"
                        />
                        <!-- 下拉选择 -->
                        <el-select
                          v-else-if="getInputType(field) === 'select'"
                          v-model="formData[field.fieldCode]"
                          style="width: 100%"
                          :placeholder="`请选择${field.fieldName}`"
                          clearable
                          :disabled="isFieldDisabled(field)"
                        >
                          <el-option
                            v-for="opt in getDomainOptions(field.domainId)"
                            :key="opt.value"
                            :label="opt.label"
                            :value="opt.value"
                          />
                        </el-select>
                        <!-- 开关 -->
                        <el-switch
                          v-else-if="getInputType(field) === 'switch'"
                          v-model="formData[field.fieldCode]"
                          :disabled="isFieldDisabled(field)"
                        />
                        <!-- 默认文本输入 -->
                        <el-input
                          v-else
                          v-model="formData[field.fieldCode]"
                          :placeholder="getFieldPlaceholder(field)"
                          :disabled="isFieldDisabled(field)"
                        />
                      </el-form-item>
                    </el-col>
                  </el-row>
                </el-form>
              </div>

              <!-- 子表Tab -->
              <div class="form-section" v-if="subEntities.length > 0">
                <div class="form-section-title">子表信息</div>
                <div class="form-tabs-layout">
                  <div class="form-tabs-header">
                    <div
                      v-for="(entity, index) in subEntities"
                      :key="entity.id"
                      class="form-tab"
                      :class="{ active: activeFormTab === index }"
                      @click="activeFormTab = index"
                    >
                      {{ entity.entityName }}
                    </div>
                  </div>

                  <div class="form-tabs-content">
                    <div class="subtable-section">
                      <div class="subtable-toolbar">
                        <el-button type="primary" size="small" @click="handleAddSubTableRow(activeFormTab)">
                          <el-icon><Plus /></el-icon>
                          添加行
                        </el-button>
                        <el-button size="small" @click="handleDeleteSubTableRow(activeFormTab)">
                          <el-icon><Delete /></el-icon>
                          删除选中
                        </el-button>
                      </div>
                      <el-table
                        :data="getSubTableData(activeFormTab)"
                        border
                        stripe
                        max-height="400"
                        @selection-change="(rows: any[]) => handleSubTableSelectionChange(activeFormTab, rows)"
                      >
                        <el-table-column type="selection" width="50" />
                        <el-table-column
                          v-for="field in getSubTableFields(activeFormTab)"
                          :key="field.fieldCode"
                          :prop="field.fieldCode"
                          min-width="150"
                        >
                          <template #header>
                            <span v-if="field.isRequired" class="required-star">*</span>
                            {{ field.fieldName }}
                          </template>
                          <template #default="{ row }">
                            <!-- 文本输入 -->
                            <el-input
                              v-if="getInputType(field) === 'input'"
                              v-model="row[field.fieldCode]"
                              size="small"
                              :placeholder="`请输入`"
                              :maxlength="field.length"
                            />
                            <!-- 多行文本 -->
                            <el-input
                              v-else-if="getInputType(field) === 'textarea'"
                              v-model="row[field.fieldCode]"
                              size="small"
                              type="textarea"
                              :rows="1"
                            />
                            <!-- 数字输入 -->
                            <el-input-number
                              v-else-if="getInputType(field) === 'number'"
                              v-model="row[field.fieldCode]"
                              size="small"
                              controls-position="right"
                              :precision="field.precisionVal"
                            />
                            <!-- 日期选择 -->
                            <el-date-picker
                              v-else-if="getInputType(field) === 'date'"
                              v-model="row[field.fieldCode]"
                              size="small"
                              type="date"
                              placeholder="请选择"
                              value-format="YYYY-MM-DD"
                            />
                            <!-- 日期时间选择 -->
                            <el-date-picker
                              v-else-if="getInputType(field) === 'datetime'"
                              v-model="row[field.fieldCode]"
                              size="small"
                              type="datetime"
                              placeholder="请选择"
                              value-format="YYYY-MM-DD HH:mm:ss"
                            />
                            <!-- 下拉选择 -->
                            <el-select
                              v-else-if="getInputType(field) === 'select'"
                              v-model="row[field.fieldCode]"
                              size="small"
                              placeholder="请选择"
                              clearable
                            >
                              <el-option
                                v-for="opt in getDomainOptions(field.domainId)"
                                :key="opt.value"
                                :label="opt.label"
                                :value="opt.value"
                              />
                            </el-select>
                            <!-- 开关 -->
                            <el-switch
                              v-else-if="getInputType(field) === 'switch'"
                              v-model="row[field.fieldCode]"
                              size="small"
                            />
                            <!-- 默认文本输入 -->
                            <el-input
                              v-else
                              v-model="row[field.fieldCode]"
                              size="small"
                            />
                          </template>
                        </el-table-column>
                      </el-table>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </template>

          <!-- 折叠面板布局 -->
          <template v-else-if="layoutConfig.subTableLayout === 'collapse'">
            <div class="form-main-layout">
              <!-- 主表信息 -->
              <div class="form-section">
                <div class="form-section-title">主表信息</div>
                <el-form ref="formRef" label-width="120px" :model="formData" :rules="formRules">
                  <el-row :gutter="20">
                    <el-col :span="6" v-for="field in formFields" :key="field.fieldCode">
                      <el-form-item :label="field.fieldName" :prop="field.fieldCode">
                        <el-input
                          v-if="getInputType(field) === 'input'"
                          v-model="formData[field.fieldCode]"
                          :placeholder="`请输入${field.fieldName}`"
                          :maxlength="field.length"
                          :disabled="field.isReadonly"
                        />
                        <el-input
                          v-else-if="getInputType(field) === 'textarea'"
                          v-model="formData[field.fieldCode]"
                          type="textarea"
                          :rows="2"
                          :placeholder="`请输入${field.fieldName}`"
                          :disabled="field.isReadonly"
                        />
                        <el-input-number
                          v-else-if="getInputType(field) === 'number'"
                          v-model="formData[field.fieldCode]"
                          style="width: 100%"
                          controls-position="right"
                          :precision="field.precisionVal"
                          :disabled="field.isReadonly"
                        />
                        <el-date-picker
                          v-else-if="getInputType(field) === 'date'"
                          v-model="formData[field.fieldCode]"
                          style="width: 100%"
                          type="date"
                          placeholder="请选择日期"
                          value-format="YYYY-MM-DD"
                          :disabled="field.isReadonly"
                        />
                        <el-date-picker
                          v-else-if="getInputType(field) === 'datetime'"
                          v-model="formData[field.fieldCode]"
                          style="width: 100%"
                          type="datetime"
                          placeholder="请选择日期时间"
                          value-format="YYYY-MM-DD HH:mm:ss"
                          :disabled="field.isReadonly"
                        />
                        <el-select
                          v-else-if="getInputType(field) === 'select'"
                          v-model="formData[field.fieldCode]"
                          style="width: 100%"
                          :placeholder="`请选择${field.fieldName}`"
                          clearable
                          :disabled="field.isReadonly"
                        >
                          <el-option
                            v-for="opt in getDomainOptions(field.domainId)"
                            :key="opt.value"
                            :label="opt.label"
                            :value="opt.value"
                          />
                        </el-select>
                        <el-switch
                          v-else-if="getInputType(field) === 'switch'"
                          v-model="formData[field.fieldCode]"
                          :disabled="field.isReadonly"
                        />
                        <el-input
                          v-else
                          v-model="formData[field.fieldCode]"
                          :placeholder="`请输入${field.fieldName}`"
                          :disabled="field.isReadonly"
                        />
                      </el-form-item>
                    </el-col>
                  </el-row>
                </el-form>
              </div>

              <!-- 子表折叠面板 -->
              <div class="form-section" v-if="subEntities.length > 0">
                <el-collapse>
                  <el-collapse-item
                    v-for="(entity, index) in subEntities"
                    :key="entity.id"
                    :title="entity.entityName"
                    :name="index"
                  >
                    <div class="subtable-section">
                      <div class="subtable-toolbar">
                        <el-button type="primary" size="small" @click="handleAddSubTableRow(index)">
                          <el-icon><Plus /></el-icon>
                          添加行
                        </el-button>
                        <el-button size="small" @click="handleDeleteSubTableRow(index)">
                          <el-icon><Delete /></el-icon>
                          删除选中
                        </el-button>
                      </div>
                      <el-table
                        :data="getSubTableData(index)"
                        border
                        stripe
                        max-height="400"
                        @selection-change="(rows: any[]) => handleSubTableSelectionChange(index, rows)"
                      >
                        <el-table-column type="selection" width="50" />
                        <el-table-column
                          v-for="field in getSubTableFields(index)"
                          :key="field.fieldCode"
                          :prop="field.fieldCode"
                          :label="field.fieldName"
                          min-width="150"
                        >
                          <template #default="{ row }">
                            <!-- 文本输入 -->
                            <el-input
                              v-if="getInputType(field) === 'input'"
                              v-model="row[field.fieldCode]"
                              size="small"
                              :placeholder="`请输入`"
                              :maxlength="field.length"
                              :disabled="field.isReadonly"
                            />
                            <!-- 多行文本 -->
                            <el-input
                              v-else-if="getInputType(field) === 'textarea'"
                              v-model="row[field.fieldCode]"
                              size="small"
                              type="textarea"
                              :rows="1"
                              :disabled="field.isReadonly"
                            />
                            <!-- 数字输入 -->
                            <el-input-number
                              v-else-if="getInputType(field) === 'number'"
                              v-model="row[field.fieldCode]"
                              size="small"
                              controls-position="right"
                              :precision="field.precisionVal"
                              :disabled="field.isReadonly"
                            />
                            <!-- 日期选择 -->
                            <el-date-picker
                              v-else-if="getInputType(field) === 'date'"
                              v-model="row[field.fieldCode]"
                              size="small"
                              type="date"
                              placeholder="请选择"
                              value-format="YYYY-MM-DD"
                              :disabled="field.isReadonly"
                            />
                            <!-- 日期时间选择 -->
                            <el-date-picker
                              v-else-if="getInputType(field) === 'datetime'"
                              v-model="row[field.fieldCode]"
                              size="small"
                              type="datetime"
                              placeholder="请选择"
                              value-format="YYYY-MM-DD HH:mm:ss"
                              :disabled="field.isReadonly"
                            />
                            <!-- 下拉选择 -->
                            <el-select
                              v-else-if="getInputType(field) === 'select'"
                              v-model="row[field.fieldCode]"
                              size="small"
                              placeholder="请选择"
                              clearable
                              :disabled="field.isReadonly"
                            >
                              <el-option
                                v-for="opt in getDomainOptions(field.domainId)"
                                :key="opt.value"
                                :label="opt.label"
                                :value="opt.value"
                              />
                            </el-select>
                            <!-- 开关 -->
                            <el-switch
                              v-else-if="getInputType(field) === 'switch'"
                              v-model="row[field.fieldCode]"
                              size="small"
                              :disabled="field.isReadonly"
                            />
                            <!-- 默认文本输入 -->
                            <el-input
                              v-else
                              v-model="row[field.fieldCode]"
                              size="small"
                              :disabled="field.isReadonly"
                            />
                          </template>
                        </el-table-column>
                      </el-table>
                    </div>
                  </el-collapse-item>
                </el-collapse>
              </div>
            </div>
          </template>

          <!-- 平铺布局 -->
          <template v-else-if="layoutConfig.subTableLayout === 'flat'">
            <div class="form-main-layout">
              <!-- 主表信息 -->
              <div class="form-section">
                <div class="form-section-title">主表信息</div>
                <el-form ref="formRef" label-width="120px" :model="formData" :rules="formRules">
                  <el-row :gutter="20">
                    <el-col :span="6" v-for="field in formFields" :key="field.fieldCode">
                      <el-form-item :label="field.fieldName" :prop="field.fieldCode">
                        <el-input
                          v-if="getInputType(field) === 'input'"
                          v-model="formData[field.fieldCode]"
                          :placeholder="`请输入${field.fieldName}`"
                          :maxlength="field.length"
                          :disabled="field.isReadonly"
                        />
                        <el-input
                          v-else-if="getInputType(field) === 'textarea'"
                          v-model="formData[field.fieldCode]"
                          type="textarea"
                          :rows="2"
                          :placeholder="`请输入${field.fieldName}`"
                          :disabled="field.isReadonly"
                        />
                        <el-input-number
                          v-else-if="getInputType(field) === 'number'"
                          v-model="formData[field.fieldCode]"
                          style="width: 100%"
                          controls-position="right"
                          :precision="field.precisionVal"
                          :disabled="field.isReadonly"
                        />
                        <el-date-picker
                          v-else-if="getInputType(field) === 'date'"
                          v-model="formData[field.fieldCode]"
                          style="width: 100%"
                          type="date"
                          placeholder="请选择日期"
                          value-format="YYYY-MM-DD"
                          :disabled="field.isReadonly"
                        />
                        <el-date-picker
                          v-else-if="getInputType(field) === 'datetime'"
                          v-model="formData[field.fieldCode]"
                          style="width: 100%"
                          type="datetime"
                          placeholder="请选择日期时间"
                          value-format="YYYY-MM-DD HH:mm:ss"
                          :disabled="field.isReadonly"
                        />
                        <el-select
                          v-else-if="getInputType(field) === 'select'"
                          v-model="formData[field.fieldCode]"
                          style="width: 100%"
                          :placeholder="`请选择${field.fieldName}`"
                          clearable
                          :disabled="field.isReadonly"
                        >
                          <el-option
                            v-for="opt in getDomainOptions(field.domainId)"
                            :key="opt.value"
                            :label="opt.label"
                            :value="opt.value"
                          />
                        </el-select>
                        <el-switch
                          v-else-if="getInputType(field) === 'switch'"
                          v-model="formData[field.fieldCode]"
                          :disabled="field.isReadonly"
                        />
                        <el-input
                          v-else
                          v-model="formData[field.fieldCode]"
                          :placeholder="`请输入${field.fieldName}`"
                          :disabled="field.isReadonly"
                        />
                      </el-form-item>
                    </el-col>
                  </el-row>
                </el-form>
              </div>

              <!-- 子表平铺 -->
              <div class="form-section" v-for="(entity, index) in subEntities" :key="entity.id">
                <div class="form-section-title">{{ entity.entityName }}</div>
                <div class="subtable-section">
                  <div class="subtable-toolbar">
                    <el-button type="primary" size="small" @click="handleAddSubTableRow(index)">
                      <el-icon><Plus /></el-icon>
                      添加行
                    </el-button>
                    <el-button size="small" @click="handleDeleteSubTableRow(index)">
                      <el-icon><Delete /></el-icon>
                      删除选中
                    </el-button>
                  </div>
                  <el-table
                    :data="getSubTableData(index)"
                    border
                    stripe
                    max-height="400"
                    @selection-change="(rows: any[]) => handleSubTableSelectionChange(index, rows)"
                  >
                    <el-table-column type="selection" width="50" />
                    <el-table-column
                      v-for="field in getSubTableFields(index)"
                      :key="field.fieldCode"
                      :prop="field.fieldCode"
                      :label="field.fieldName"
                      min-width="150"
                    >
                      <template #default="{ row }">
                        <el-input v-model="row[field.fieldCode]" size="small" />
                      </template>
                    </el-table-column>
                  </el-table>
                </div>
              </div>
            </div>
          </template>
        </div>
      </div>
    </template>

    <!-- 正常列表模式 -->
    <template v-else>
      <!-- 左侧分类树 -->
      <div class="left-panel" :class="{ collapsed: leftPanelCollapsed }">
        <div class="panel-header">
          <h3 v-if="!leftPanelCollapsed">数据分类</h3>
          <el-button
            link
            class="collapse-btn"
            @click="leftPanelCollapsed = !leftPanelCollapsed"
          >
            <el-icon :class="{ 'is-rotate': leftPanelCollapsed }">
              <ArrowLeft />
            </el-icon>
          </el-button>
        </div>
        <div class="panel-content" v-show="!leftPanelCollapsed">
          <el-tree
            ref="treeRef"
            :data="treeData"
            :props="{ children: 'children', label: 'name' }"
            node-key="id"
            default-expand-all
            :highlight-current="true"
            @node-click="handleNodeClick"
          >
            <template #default="{ data }">
              <span class="tree-node">
                <el-icon v-if="data.type === 'folder'"><Folder /></el-icon>
                <el-icon v-else><Document /></el-icon>
                <span>{{ data.name }}</span>
              </span>
            </template>
          </el-tree>

          <el-empty v-if="treeData.length === 0" description="暂无分类" :image-size="60" />
        </div>
      </div>

      <!-- 右侧内容区 -->
      <div class="right-panel">
        <!-- 未选中状态 -->
        <div v-if="!selectedNode" class="empty-state">
          <el-icon size="48"><Guide /></el-icon>
          <p>请从左侧选择表单</p>
        </div>

        <!-- 文件夹内容 -->
        <template v-else-if="selectedNode.type === 'folder'">
          <div class="mdm-top-bar">
            <div class="mdm-filter-row">
              <span class="page-title">{{ selectedNode.name }}</span>
            </div>
          </div>
          <div class="folder-content">
            <div v-if="selectedNode.children && selectedNode.children.length > 0" class="items-grid">
              <div
                v-for="item in selectedNode.children"
                :key="item.id"
                class="item-card"
                @click="handleItemClick(item)"
              >
                <el-icon class="item-icon" :class="item.type">
                  <Folder v-if="item.type === 'folder'" />
                  <Document v-else />
                </el-icon>
                <span class="item-name">{{ item.name }}</span>
                <span v-if="item.type === 'form'" class="item-tag">表单</span>
              </div>
            </div>
            <el-empty v-else description="该文件夹下暂无内容" />
          </div>
        </template>

        <!-- 表单数据 -->
        <template v-else-if="selectedNode.type === 'form'">
          <!-- 顶部操作栏 -->
          <div class="mdm-top-bar">
            <div class="mdm-filter-row" style="width: 100%; justify-content: space-between;">
              <!-- 新增按钮放在最前面 -->
              <button class="mdm-btn-red" @click="handleAdd">+ 新增</button>
              <!-- 右侧：高级查询 + 状态筛选 + 查询/重置 -->
              <div class="mdm-right-group">
                <!-- 高级查询按钮 + 下拉面板 -->
                <el-popover
                  placement="bottom"
                  :width="400"
                  trigger="click"
                  v-model:visible="showMoreFilter"
                >
                  <template #reference>
                    <el-button type="primary" link style="color: #fff;">
                      高级查询
                      <el-icon :class="{ 'is-rotate': showMoreFilter }"><ArrowDown /></el-icon>
                    </el-button>
                  </template>
                  <div class="mdm-popover-filter">
                    <div
                      v-for="field in queryFields"
                      :key="field.fieldCode"
                      class="mdm-popover-filter-item"
                    >
                      <label>{{ field.fieldName }}</label>
                      <el-input
                        v-model="searchForm[field.fieldCode]"
                        :placeholder="'请输入' + field.fieldName"
                        clearable
                      />
                    </div>
                    <div class="mdm-popover-filter-footer">
                      <el-button type="primary" @click="handleMoreFilterSearch">查询</el-button>
                      <el-button @click="handleMoreFilterReset">重置</el-button>
                    </div>
                  </div>
                </el-popover>
                <el-select
                  v-model="searchForm.status"
                  placeholder="状态"
                  clearable
                  style="width: 120px"
                >
                  <el-option label="草稿" value="DRAFT" />
                  <el-option label="审核中" value="PENDING" />
                  <el-option label="已生效" value="ACTIVE" />
                </el-select>
                <button class="mdm-btn-outline" @click="handleSearch">查询</button>
                <button class="mdm-btn-outline" @click="handleReset">重置</button>
              </div>
            </div>
          </div>

          <!-- 数据表格 -->
          <div class="table-container">
            <el-table
              ref="tableRef"
              :data="dataList"
              v-loading="loading"
              border
              stripe
              height="100%"
              @selection-change="handleSelectionChange"
            >
              <el-table-column type="selection" width="50" />
              <el-table-column
                v-for="field in resultFields"
                :key="field.fieldCode"
                :prop="field.fieldCode"
                :label="field.fieldName"
                min-width="120"
                show-overflow-tooltip
              >
                <template #default="{ row }">
                  {{ getFieldValue(row, field.fieldCode) }}
                </template>
              </el-table-column>
              <el-table-column prop="status" label="状态" width="100">
                <template #default="{ row }">
                  <el-tag :type="getStatusType(row.status)">{{ getStatusLabel(row.status) }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="created_at" label="创建时间" width="160">
                <template #default="{ row }">
                  {{ formatDateTime(row.created_at) }}
                </template>
              </el-table-column>
              <el-table-column prop="created_by" label="创建人" width="100">
                <template #default="{ row }">
                  {{ row.created_by || '-' }}
                </template>
              </el-table-column>
              <el-table-column label="操作" width="280" fixed="right">
                <template #default="{ row }">
                  <!-- 草稿状态 -->
                  <template v-if="row.status === 'DRAFT'">
                    <el-button size="small" type="success" @click="handleStatusChange(row, 'PENDING')">提交审批</el-button>
                    <el-button size="small" type="primary" @click="handleEdit(row)">编辑</el-button>
                    <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
                  </template>
                  <!-- 审核中状态 -->
                  <template v-else-if="row.status === 'PENDING'">
                    <el-button size="small" type="info" @click="handleView(row)">查看</el-button>
                  </template>
                  <!-- 已生效状态 -->
                  <template v-else-if="row.status === 'ACTIVE'">
                    <el-button size="small" type="primary" @click="handleEdit(row)">编辑</el-button>
                    <el-button size="small" type="info" @click="handleView(row)">查看</el-button>
                  </template>
                  <!-- 默认显示查看 -->
                  <template v-else>
                    <el-button size="small" type="primary" @click="handleView(row)">查看</el-button>
                  </template>
                </template>
              </el-table-column>
            </el-table>
          </div>

          <!-- 分页 -->
          <div class="pagination-container">
            <el-pagination
              v-model:current-page="currentPage"
              v-model:page-size="pageSize"
              :page-sizes="[10, 20, 50, 100]"
              :total="total"
              layout="total, sizes, prev, pager, next, jumper"
              @size-change="handleSizeChange"
              @current-change="handlePageChange"
            />
          </div>
        </template>
      </div>
    </template>

    <!-- 查看数据弹窗 -->
    <MdmDialog v-model="viewDialogVisible" title="查看数据" width="600px">
      <div v-if="viewData" class="view-content">
        <div v-for="field in resultFields" :key="field.fieldCode" class="view-item">
          <span class="label">{{ field.fieldName }}:</span>
          <span class="value">{{ viewData[field.fieldCode] || '-' }}</span>
        </div>
      </div>
      <template #footer>
        <button class="mdm-btn-cancel" @click="viewDialogVisible = false">关闭</button>
      </template>
    </MdmDialog>

    <!-- 分发弹窗 -->
    <el-dialog v-model="distributeVisible" title="分发到目标系统" width="500px">
      <el-form label-width="100px">
        <el-form-item label="数据ID">
          <el-input :value="distributeRow?.id" disabled />
        </el-form-item>
        <el-form-item label="数据编码">
          <el-input :value="distributeRow?.code || distributeRow?.vendor_code" disabled />
        </el-form-item>
        <el-form-item label="数据名称">
          <el-input :value="distributeRow?.name || distributeRow?.vendor_name" disabled />
        </el-form-item>
        <el-form-item label="目标系统" required>
          <el-select v-model="selectedSystemConfig" placeholder="请选择目标系统" style="width: 100%">
            <el-option
              v-for="item in systemConfigList"
              :key="item.id"
              :label="item.configName"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="distributeVisible = false">取消</el-button>
        <el-button type="primary" @click="executeDistribute" :loading="distributeLoading">
          执行分发
        </el-button>
      </template>
    </el-dialog>

    <!-- 分发结果弹窗 -->
    <el-dialog v-model="distributeResultVisible" title="分发结果" width="500px">
      <el-descriptions :column="1" border>
        <el-descriptions-item label="状态">
          <el-tag :type="distributeResult?.success ? 'success' : 'danger'">
            {{ distributeResult?.success ? '成功' : '失败' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="消息">{{ distributeResult?.message }}</el-descriptions-item>
        <el-descriptions-item v-if="distributeResult?.targetKey" label="目标Key">
          {{ distributeResult?.targetKey }}
        </el-descriptions-item>
        <el-descriptions-item v-if="distributeResult?.errorMsg" label="错误信息">
          <span style="color: #f56c6c">{{ distributeResult?.errorMsg }}</span>
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Folder, Document, Guide, ArrowLeft, Check, Plus, Delete, Position, ArrowDown, Search } from '@element-plus/icons-vue'
import MdmDialog from '@/components/MdmDialog.vue'
import { getCategoryTree, type DataCategoryDto } from '@/api/data/category'
import {
  getInstanceList,
  getInstanceById,
  saveInstance,
  updateInstance,
  deleteInstance,
  type DataInstanceDto
} from '@/api/data/instance'
import { getFormDesign, type FormDesignRequest } from '@/api/form'
import { getViewDesign, type ViewDefinition, type ViewField, type ViewEntity } from '@/api/standard/viewDefinition'
import { getValueDomainById, type DomainOption } from '@/api/standard/valueDomain'
import { distribute, getConfigList, getLogHistory } from '@/api/distribution'

// 系统保留字段列表（这些字段在表单中隐藏，系统自动处理）
const SYSTEM_RESERVED_FIELDS = [
  'ID',
  'VIEW_ID',
  'STATUS',
  'CREATED_BY',
  'CREATED_AT',
  'UPDATED_BY',
  'UPDATED_AT',
  // SAP风格系统字段
  'ERNAM',  // 创建人
  'ERDAT',  // 创建日期
  'AENAM',  // 修改人
  'LAEDA'   // 修改日期
]

// 树数据
const treeData = ref<DataCategoryDto[]>([])
const treeRef = ref()
const tableRef = ref()
const selectedNode = ref<DataCategoryDto | null>(null)
const leftPanelCollapsed = ref(false)  // 左侧面板折叠状态
const showMoreFilter = ref(false)  // 更多查询弹窗显示状态

// 视图相关
const viewDesign = ref<ViewDefinition | null>(null)
const queryFields = ref<ViewField[]>([])
const resultFields = ref<ViewField[]>([])
const formFields = ref<ViewField[]>([])
const subEntities = ref<ViewEntity[]>([])

// 过滤后的表单字段（排除系统保留字段）
const filteredFormFields = computed(() => {
  return formFields.value.filter(field =>
    !SYSTEM_RESERVED_FIELDS.includes(field.fieldCode.toUpperCase())
  )
})

// 全屏表单模式
const showFullScreenForm = ref(false)
const activeFormTab = ref(0)  // 默认显示第一个TAB（子表或主表）
const formDialogTitle = computed(() => editingInstance.value ? '编辑数据' : '新增数据')

// 表单相关
const formDesign = ref<FormDesignRequest | null>(null)
const formRef = ref()  // 表单引用
const formData = ref<Record<string, any>>({})
const subTableData = ref<Record<string, any[]>>({})
const subTableSelection = ref<Record<number, any[]>>({})
const editingInstance = ref<DataInstanceDto | null>(null)
const saving = ref(false)

// 表单验证规则
const formRules = computed(() => {
  const rules: Record<string, any> = {}
  filteredFormFields.value.forEach(field => {
    // 自动编号字段不需要必输验证
    if (field.isRequired && !field.autoNumber) {
      rules[field.fieldCode] = [
        { required: true, message: `请输入${field.fieldName}`, trigger: 'blur' }
      ]
    }
  })
  return rules
})

// 布局配置
const layoutConfig = computed(() => {
  if (formDesign.value?.layoutConfig) {
    try {
      return JSON.parse(formDesign.value.layoutConfig)
    } catch {
      return { subTableLayout: 'tabs', subTables: [] }
    }
  }
  return { subTableLayout: 'tabs', subTables: [] }
})

// 值域缓存
const domainMap = ref<Map<number, DomainOption[]>>(new Map())

// 搜索表单
const searchForm = ref<Record<string, any>>({})

// 数据列表（后端返回的是直接的字段数据，不是 DataInstanceDto）
const dataList = ref<any[]>([])
const selectedRows = ref<any[]>([])
const loading = ref(false)

// 分页
const currentPage = ref(1)
const pageSize = ref(10)

// 分发相关
const distributeVisible = ref(false)
const distributeLoading = ref(false)
const distributeResult = ref<any>(null)
const distributeResultVisible = ref(false)
const systemConfigList = ref<any[]>([])
const selectedSystemConfig = ref<number | null>(null)
const distributeRow = ref<any>(null)
const total = ref(0)

// 查看弹窗
const viewDialogVisible = ref(false)
const viewData = ref<Record<string, any> | null>(null)

// 加载分类树
async function loadTree() {
  try {
    const res = await getCategoryTree()
    treeData.value = res.data || []
  } catch (error) {
    ElMessage.error('加载分类失败')
  }
}

// 加载视图设计
async function loadViewDesign(viewId: number) {
  try {
    const res = await getViewDesign(viewId)
    viewDesign.value = res.data

    // 获取主表实体
    const mainEntity = res.data.entities?.find(e => e.entityType === 'main')
    if (mainEntity && mainEntity.fields) {
      queryFields.value = mainEntity.fields.filter(f =>
        f.isQuery && !SYSTEM_RESERVED_FIELDS.includes(f.fieldCode.toUpperCase())
      )
      // 查询结果字段也要排除系统保留字段（状态、创建人、创建时间等）
      resultFields.value = mainEntity.fields.filter(f =>
        f.isQueryResult && !SYSTEM_RESERVED_FIELDS.includes(f.fieldCode.toUpperCase())
      )
      // 过滤掉隐藏字段，并排除系统保留字段
      formFields.value = mainEntity.fields.filter(f =>
        !f.isHidden && !SYSTEM_RESERVED_FIELDS.includes(f.fieldCode.toUpperCase())
      )
    } else {
      queryFields.value = []
      resultFields.value = []
      formFields.value = []
    }

    // 获取子表实体
    subEntities.value = res.data.entities?.filter(e => e.entityType === 'sub') || []

    // 加载值域
    loadDomainData()
  } catch (error) {
    ElMessage.error('加载视图失败')
    queryFields.value = []
    resultFields.value = []
    formFields.value = []
    subEntities.value = []
  }
}

// 加载值域数据
async function loadDomainData() {
  const domainIds = new Set<number>()
  filteredFormFields.value.forEach(field => {
    if (field.domainId) domainIds.add(field.domainId)
  })
  subEntities.value.forEach(entity => {
    entity.fields?.forEach(field => {
      if (field.domainId) domainIds.add(field.domainId)
    })
  })

  for (const domainId of domainIds) {
    if (!domainMap.value.has(domainId)) {
      try {
        const res = await getValueDomainById(domainId)
        domainMap.value.set(domainId, res.data?.options || [])
      } catch {
        // 忽略错误
      }
    }
  }
}

// 加载表单设计
async function loadFormDesign(formId: number) {
  try {
    const res = await getFormDesign(formId)
    formDesign.value = res.data
  } catch (error) {
    ElMessage.error('加载表单失败')
  }
}

// 加载数据列表
async function loadDataList() {
  if (!selectedNode.value || selectedNode.value.type !== 'form') return

  loading.value = true
  try {
    const res = await getInstanceList({
      categoryId: selectedNode.value.id,
      formId: selectedNode.value.formId!,
      status: searchForm.value.status
    })
    dataList.value = res.data || []
    total.value = dataList.value.length
  } catch (error) {
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

// 点击树节点
async function handleNodeClick(data: DataCategoryDto) {
  selectedNode.value = data
  editingInstance.value = null
  formData.value = {}
  searchForm.value = {}
  selectedRows.value = []

  if (data.type === 'form' && data.formId) {
    await loadFormDesign(data.formId)
    if (formDesign.value?.viewId) {
      await loadViewDesign(formDesign.value.viewId)
    }
    await loadDataList()
  }
}

// 点击文件夹下的项目
function handleItemClick(item: DataCategoryDto) {
  if (item.type === 'form') {
    handleNodeClick(item)
  }
}

// 搜索
function handleSearch() {
  currentPage.value = 1
  loadDataList()
}

// 重置
function handleReset() {
  searchForm.value = {}
  currentPage.value = 1
  loadDataList()
}

// 更多查询弹窗 - 查询
function handleMoreFilterSearch() {
  showMoreFilter.value = false
  currentPage.value = 1
  loadDataList()
}

// 更多查询弹窗 - 重置
function handleMoreFilterReset() {
  // 只重置弹窗中的查询字段，保留状态筛选
  queryFields.value.forEach(field => {
    delete searchForm.value[field.fieldCode]
  })
}

// 新增
function handleAdd() {
  editingInstance.value = null
  formData.value = {}
  subTableData.value = {}
  // 有子表时默认显示第一个子表TAB，没有子表时显示主表
  activeFormTab.value = subEntities.value.length > 0 ? 0 : -1
  showFullScreenForm.value = true
}

// 编辑
async function handleEdit(row: any) {
  try {
    // 加载完整数据（包括子表）
    const res = await getInstanceById(selectedNode.value!.formId!, row.id)
    const fullData = res.data

    console.log('编辑加载的完整数据:', fullData)

    // 填充主表数据
    formData.value = { ...fullData.main }

    // 填充子表数据
    subTableData.value = {}
    // 遍历所有属性，将子表数据归类到 sub 对象下
    for (const key in fullData) {
      if (key !== 'main' && Array.isArray(fullData[key])) {
        subTableData.value[key] = fullData[key]
      }
    }

    console.log('主表数据:', formData.value)
    console.log('子表数据:', subTableData.value)

    editingInstance.value = { id: row.id } as any
    // 有子表时默认显示第一个子表TAB，没有子表时显示主表
    activeFormTab.value = subEntities.value.length > 0 ? 0 : -1
    showFullScreenForm.value = true
  } catch (error) {
    console.error('加载数据失败:', error)
    ElMessage.error('加载数据失败')
  }
}

// 关闭表单
function handleCloseForm() {
  showFullScreenForm.value = false
  editingInstance.value = null
  formData.value = {}
  subTableData.value = {}
}

// 保存表单
async function handleSaveForm() {
  if (!selectedNode.value) return

  // 主表验证
  try {
    await formRef.value?.validate()
  } catch {
    ElMessage.warning('请填写主表必填项')
    return
  }

  // 子表验证
  const subValidError = validateSubTableData()
  if (subValidError) {
    ElMessage.warning(subValidError)
    return
  }

  saving.value = true
  try {
    // 合并主表和子表数据
    const allData = {
      ...formData.value,
      sub: subTableData.value
    }

    if (editingInstance.value) {
      await updateInstance(selectedNode.value.id, selectedNode.value.formId!, editingInstance.value.id, allData)
      ElMessage.success('更新成功')
    } else {
      await saveInstance({
        categoryId: selectedNode.value.id,
        formId: selectedNode.value.formId!,
        data: allData
      })
      ElMessage.success('保存成功')
    }
    showFullScreenForm.value = false
    editingInstance.value = null
    formData.value = {}
    subTableData.value = {}
    loadDataList()
  } catch (error) {
    ElMessage.error('保存失败')
  } finally {
    saving.value = false
  }
}

// 验证子表数据
function validateSubTableData(): string | null {
  for (const entity of subEntities.value) {
    // 过滤出必填且非隐藏且非自动编号的字段
    const fields = entity.fields?.filter(f => f.isRequired && !f.isHidden && !f.autoNumber) || []
    if (fields.length === 0) continue

    const data = subTableData.value[entity.entityCode] || []
    for (let i = 0; i < data.length; i++) {
      const row = data[i]
      for (const field of fields) {
        const value = row[field.fieldCode]
        if (value === undefined || value === null || value === '') {
          return `${entity.entityName}第${i + 1}行的"${field.fieldName}"为必填项`
        }
      }
    }
  }
  return null
}

// 查看数据
async function handleView(row: any) {
  // 后端返回的数据直接包含字段值
  viewData.value = row
  viewDialogVisible.value = true
}

// 删除数据
async function handleDelete(row: any) {
  try {
    await ElMessageBox.confirm('确定要删除此数据吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await deleteInstance(selectedNode.value.id, selectedNode.value.formId!, row.id)
    ElMessage.success('删除成功')
    loadDataList()
  } catch {
    // 用户取消
  }
}

// 表格选择变化
function handleSelectionChange(rows: any[]) {
  selectedRows.value = rows
}

// 获取字段值（后端直接返回字段数据，不需要解析 dataJson）
function getFieldValue(row: any, fieldCode: string) {
  // 后端返回的数据直接包含字段值
  const value = row[fieldCode]
  if (value !== undefined && value !== null && value !== '') {
    return value
  }
  return '-'
}

// 获取输入类型
function getInputType(field: ViewField): string {
  const fieldType = field.fieldType?.toLowerCase() || 'text'
  switch (fieldType) {
    case 'text':
    case 'textarea':
    case 'longtext':
      return 'textarea'
    case 'number':
    case 'decimal':
    case 'int':
    case 'float':
    case 'double':
      return 'number'
    case 'date':
      return 'date'
    case 'datetime':
    case 'timestamp':
      return 'datetime'
    case 'boolean':
    case 'bool':
      return 'switch'
    case 'select':
    case 'enum':
      return 'select'
    default:
      if (field.domainId) return 'select'
      return 'input'
  }
}

// 获取字段占位符（自动编号字段显示"自动生成"）
function getFieldPlaceholder(field: ViewField): string {
  if (field.autoNumber) {
    return '自动生成'
  }
  return `请输入${field.fieldName}`
}

// 判断字段是否禁用（自动编号字段在新增时禁用）
function isFieldDisabled(field: ViewField): boolean {
  // 如果字段本身是只读的，则禁用
  if (field.isReadonly) {
    return true
  }
  // 如果是自动编号字段，在新增模式下禁用（不允许手动输入）
  if (field.autoNumber && !editingInstance.value) {
    return true
  }
  return false
}

// 获取值域选项
function getDomainOptions(domainId?: number): DomainOption[] {
  if (!domainId) return []
  return domainMap.value.get(domainId) || []
}

// 获取子表数据
function getSubTableData(index: number): any[] {
  const entity = subEntities.value[index]
  if (!entity) return []
  return subTableData.value[entity.entityCode] || []
}

// 获取子表字段
function getSubTableFields(index: number): ViewField[] {
  const entity = subEntities.value[index]
  if (!entity) return []
  return entity.fields?.filter(f => !f.isHidden) || []
}

// 添加子表行
function handleAddSubTableRow(index: number) {
  const entity = subEntities.value[index]
  if (!entity) return
  const key = entity.entityCode
  if (!subTableData.value[key]) {
    subTableData.value[key] = []
  }
  subTableData.value[key].push({})
}

// 删除子表选中行
async function handleDeleteSubTableRow(index: number) {
  const entity = subEntities.value[index]
  if (!entity) return
  const key = entity.entityCode
  const selected = subTableSelection.value[index] || []
  if (selected.length === 0) {
    ElMessage.warning('请先选择要删除的行')
    return
  }

  // 添加确认提示
  try {
    await ElMessageBox.confirm(`确定要删除选中的 ${selected.length} 行数据吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    // 确认后删除
    subTableData.value[key] = subTableData.value[key].filter(
      (row: any) => !selected.includes(row)
    )
    // 清空选中
    subTableSelection.value[index] = []
  } catch {
    // 用户取消
  }
}

// 子表选择变化
function handleSubTableSelectionChange(index: number, rows: any[]) {
  subTableSelection.value[index] = rows
}

// 分页
function handlePageChange(page: number) {
  currentPage.value = page
  loadDataList()
}

function handleSizeChange(size: number) {
  pageSize.value = size
  currentPage.value = 1
  loadDataList()
}

// 格式化日期时间
const formatDateTime = (dateStr: string | undefined) => {
  if (!dateStr) return '-'
  const date = new Date(dateStr)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  }).replace(/\//g, '-')
}

// 获取状态标签类型
const getStatusType = (status: string) => {
  const typeMap: Record<string, string> = {
    'DRAFT': 'info',
    'PENDING': 'warning',
    'ACTIVE': 'success'
  }
  return typeMap[status] || 'info'
}

// 获取状态标签文本
const getStatusLabel = (status: string) => {
  const labelMap: Record<string, string> = {
    'DRAFT': '草稿',
    'PENDING': '审核中',
    'ACTIVE': '已生效'
  }
  return labelMap[status] || status
}

// 一键填入测试数据
function handleFillTestData() {
  // 生成随机字符串（可指定最大长度）
  const randomString = (maxLength: number = 8) => {
    // 生成比最大长度稍短的随机字符串，留出一些余量
    const length = Math.min(maxLength, Math.max(4, Math.floor(maxLength * 0.7)))
    return Math.random().toString(36).substring(2, length + 2).toUpperCase()
  }

  // 生成随机数字（考虑字段长度限制）
  const randomNumber = (maxLength: number = 3) => {
    // 根据长度计算最大值，例如长度3最大为999
    const maxValue = Math.pow(10, maxLength) - 1
    return Math.floor(Math.random() * maxValue) + 1
  }

  // 从值域中随机选择一个选项（验证长度）
  const randomDomainValue = (domainId: number | undefined, maxLength: number | undefined) => {
    if (!domainId) return null
    const options = getDomainOptions(domainId)
    if (options.length === 0) return null

    // 过滤出符合长度要求的选项
    const validOptions = options.filter(opt =>
      !maxLength || opt.value.length <= maxLength
    )

    // 如果有符合条件的选项，随机选择；否则返回null
    if (validOptions.length > 0) {
      return validOptions[Math.floor(Math.random() * validOptions.length)].value
    }
    return null
  }

  // 填充主表数据
  filteredFormFields.value.forEach(field => {
    // 跳过自动编号字段
    if (field.autoNumber) return

    // 如果已有值，跳过
    if (formData.value[field.fieldCode]) return

    // 获取字段长度限制
    const maxLength = field.length || 50

    // 根据字段类型和值域生成数据
    if (field.domainId) {
      // 有关联值域，从值域中随机选择（验证长度）
      const domainValue = randomDomainValue(field.domainId, maxLength)
      if (domainValue !== null) {
        formData.value[field.fieldCode] = domainValue
      }
    } else if (field.fieldType === 'boolean') {
      // 布尔类型（没有值域）：使用true/false
      formData.value[field.fieldCode] = Math.random() > 0.5
    } else {
      // 没有关联值域，根据字段类型生成（遵守长度限制）
      switch (field.fieldType) {
        case 'string':
          // 字符串类型：生成TEST_开头的随机字符串，不超过字段长度
          const prefix = 'TEST_'
          const availableLength = maxLength - prefix.length
          if (availableLength > 0) {
            formData.value[field.fieldCode] = prefix + randomString(availableLength)
          } else {
            // 如果长度不够，直接生成随机字符串
            formData.value[field.fieldCode] = randomString(maxLength)
          }
          break
        case 'integer':
          // 整型：根据长度限制生成数字
          formData.value[field.fieldCode] = randomNumber(Math.min(maxLength, 10))
          break
        case 'decimal':
          // 浮点型：生成随机小数，整数部分不超过长度
          const intLength = Math.max(1, maxLength - 3) // 留3位给小数
          const intPart = Math.floor(Math.random() * Math.pow(10, intLength))
          formData.value[field.fieldCode] = parseFloat(`${intPart}.${Math.floor(Math.random() * 100)}`)
          break
        case 'date':
          const today = new Date()
          formData.value[field.fieldCode] = today.toISOString().split('T')[0]
          break
        case 'datetime':
          formData.value[field.fieldCode] = new Date().toISOString().slice(0, 19).replace('T', ' ')
          break
        case 'text':
          // 长文本：生成不超过长度的测试文本
          const testText = `测试数据-${field.fieldName}-说明内容`
          formData.value[field.fieldCode] = testText.substring(0, maxLength)
          break
        default:
          formData.value[field.fieldCode] = randomString(Math.min(maxLength, 8))
      }
    }
  })

  // 填充子表数据
  subEntities.value.forEach(entity => {
    const entityCode = entity.entityCode
    if (!subTableData.value[entityCode]) {
      subTableData.value[entityCode] = []
    }

    // 生成1-3行测试数据
    const rowCount = Math.floor(Math.random() * 3) + 1
    for (let i = 0; i < rowCount; i++) {
      const row: Record<string, any> = {}

      entity.fields?.forEach(field => {
        // 跳过自动编号字段
        if (field.autoNumber) return

        // 获取字段长度限制
        const maxLength = field.length || 50

        // 根据字段类型和值域生成数据
        if (field.domainId) {
          const domainValue = randomDomainValue(field.domainId, maxLength)
          if (domainValue !== null) {
            row[field.fieldCode] = domainValue
          }
        } else if (field.fieldType === 'boolean') {
          // 布尔类型（没有值域）：使用true/false
          row[field.fieldCode] = Math.random() > 0.5
        } else {
          switch (field.fieldType) {
            case 'string':
              const availableLen = maxLength - 1
              row[field.fieldCode] = availableLen > 0 ? 'T' + randomString(availableLen) : randomString(maxLength)
              break
            case 'integer':
              row[field.fieldCode] = randomNumber(Math.min(maxLength, 5))
              break
            case 'decimal':
              const intLen = Math.max(1, maxLength - 3)
              row[field.fieldCode] = parseFloat(`${Math.floor(Math.random() * Math.pow(10, intLen))}.${Math.floor(Math.random() * 100)}`)
              break
            case 'date':
              row[field.fieldCode] = new Date().toISOString().split('T')[0]
              break
            default:
              row[field.fieldCode] = randomString(Math.min(maxLength, 6))
          }
        }
      })

      subTableData.value[entityCode].push(row)
    }
  })

  ElMessage.success('已填入测试数据')
}

// ==================== 状态操作 ====================

// 统一的状态变更函数
async function handleStatusChange(row: any, newStatus: string) {
  const statusLabels: Record<string, string> = {
    'DRAFT': '草稿',
    'PENDING': '审核中',
    'ACTIVE': '已生效'
  }

  const actionLabels: Record<string, string> = {
    'PENDING': '提交审批'
  }

  const action = actionLabels[newStatus] || '变更状态'

  try {
    await ElMessageBox.confirm(`确定要${action}吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'info'
    })

    // 更新状态字段
    await updateInstance(selectedNode.value!.id, selectedNode.value!.formId!, row.id, {
      status: newStatus
    })

    ElMessage.success(`已${action}`)
    loadDataList()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.response?.data?.message || '操作失败')
    }
  }
}

// ==================== 分发相关 ====================

// 加载系统配置列表
async function loadSystemConfigs() {
  try {
    const res = await getConfigList() as any
    systemConfigList.value = (res.data || []).filter((c: any) => c.status === 'active')
  } catch (e) {
    console.error(e)
  }
}

// 打开发发弹窗
async function handleDistribute(row: any) {
  distributeRow.value = row
  if (systemConfigList.value.length === 0) {
    await loadSystemConfigs()
  }
  selectedSystemConfig.value = systemConfigList.value.find((c: any) => c.isDefault)?.id || null
  distributeVisible.value = true
}

// 执行分发
async function executeDistribute() {
  if (!selectedSystemConfig.value) {
    ElMessage.warning('请选择目标系统')
    return
  }
  if (!distributeRow.value) {
    ElMessage.warning('请选择要分发的数据')
    return
  }

  distributeLoading.value = true
  try {
    // 根据当前分类确定数据类型
    const dataType = selectedNode.value?.categoryCode || 'VENDOR'

    const res = await distribute(
      dataType,
      distributeRow.value.id,
      selectedSystemConfig.value,
      distributeRow.value
    ) as any

    distributeResult.value = res.data
    distributeResultVisible.value = true
    distributeVisible.value = false

    if (res.data?.success) {
      ElMessage.success('分发成功')
    } else {
      ElMessage.error(res.data?.errorMsg || '分发失败')
    }
  } catch (e) {
    console.error(e)
    ElMessage.error('分发失败')
  } finally {
    distributeLoading.value = false
  }
}

onMounted(() => {
  loadTree()
})
</script>

<style scoped lang="scss">
@import '@/assets/styles/mdm-common.scss';

// 必填星号样式
.required-star {
  color: #f56c6c;
  margin-right: 2px;
}

// 箭头旋转动画
.is-rotate {
  transform: rotate(180deg);
  transition: transform 0.3s;
}

// 高级查询弹出面板样式
.mdm-popover-filter {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.mdm-popover-filter-item {
  display: flex;
  align-items: center;
  gap: 10px;

  label {
    width: 80px;
    font-size: 13px;
    color: #333;
    flex-shrink: 0;
  }

  .el-input {
    flex: 1;
  }
}

.mdm-popover-filter-footer {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  margin-top: 8px;
  padding-top: 12px;
  border-top: 1px solid #ebeef5;
}

.data-maintain-page {
  position: relative;
  display: flex;
  gap: 16px;
  padding: 20px;
  background: #f5f5f5;
  height: 100%;
  overflow: hidden;
}

// 全屏表单样式 - 占满TAB页签内
.full-screen-form {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 100;
  background: #f5f7fa;
  display: flex;
  flex-direction: column;
  border-radius: 4px;
}

.form-header {
  height: 50px;
  background: #fff;
  border-bottom: 1px solid #e4e7ed;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.form-title {
  font-size: 16px;
  font-weight: 500;
}

.header-right {
  display: flex;
  gap: 12px;
}

.form-body {
  flex: 1;
  overflow: auto;
  padding: 20px;
}

.form-tabs-layout {
  background: #fff;
  border-radius: 4px;
  overflow: hidden;
}

.form-tabs-header {
  display: flex;
  background: #f5f7fa;
  border-bottom: 1px solid #e4e7ed;
}

.form-tab {
  padding: 12px 24px;
  cursor: pointer;
  border-right: 1px solid #e4e7ed;
  transition: all 0.2s;

  &:hover {
    background: #e9ecf0;
  }

  &.active {
    background: #fff;
    font-weight: 500;
    border-bottom: 2px solid #409eff;
  }
}

.form-section {
  background: #fff;
  border-radius: 4px;
  overflow: hidden;

  .el-form {
    padding: 16px 0 0 0;
  }
}

.form-tabs-content {
  padding: 20px;
}

.form-main-layout {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.form-section-title {
  padding: 12px 20px;
  background: #f5f7fa;
  border-bottom: 1px solid #e4e7ed;
  font-weight: 500;
  font-size: 14px;
}

.subtable-section {
  padding: 20px;

  .subtable-toolbar {
    margin-bottom: 12px;
    display: flex;
    gap: 8px;
  }
}

// 左侧面板
.left-panel {
  width: 260px;
  flex-shrink: 0;
  background: #fff;
  border-radius: 4px;
  overflow: hidden;
  transition: width 0.3s ease;

  &.collapsed {
    width: 40px;

    .panel-header {
      justify-content: center;
      padding: 16px 8px;
    }
  }

  .panel-header {
    padding: 16px 20px;
    border-bottom: 1px solid #e8e8e8;
    display: flex;
    align-items: center;
    justify-content: space-between;

    h3 {
      margin: 0;
      font-size: 16px;
      font-weight: 600;
      color: #333;
    }

    .collapse-btn {
      padding: 4px;
      font-size: 16px;

      .el-icon {
        transition: transform 0.3s ease;

        &.is-rotate {
          transform: rotate(180deg);
        }
      }
    }
  }

  .panel-content {
    padding: 12px;
    max-height: calc(100vh - 160px);
    overflow-y: auto;
  }
}

.tree-node {
  display: flex;
  align-items: center;
  gap: 6px;
}

// 右侧面板
.right-panel {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 16px;
  min-width: 0;
}

.mdm-top-bar {
  flex-shrink: 0;
  background: #fff;
  padding: 12px 16px;
  border-radius: 4px;
}

.table-container {
  flex: 1;
  background: #fff;
  border-radius: 4px;
  overflow: hidden;
}

.pagination-container {
  flex-shrink: 0;
  background: #fff;
  padding: 12px 16px;
  border-radius: 4px;
  display: flex;
  justify-content: flex-end;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: #909399;
  background: #fff;
  border-radius: 4px;
}

.folder-content {
  flex: 1;
  background: #fff;
  border-radius: 4px;
  padding: 20px;
  overflow: auto;
}

.items-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(160px, 1fr));
  gap: 16px;
}

.item-card {
  background: #f8fafc;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  padding: 20px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  transition: all 0.2s;

  &:hover {
    border-color: #409eff;
    box-shadow: 0 2px 12px rgba(64, 158, 255, 0.2);
  }
}

.item-icon {
  font-size: 32px;
  color: #909399;

  &.folder {
    color: #e6a23c;
  }

  &.form {
    color: #409eff;
  }
}

.item-name {
  font-size: 14px;
  text-align: center;
}

.item-tag {
  font-size: 12px;
  color: #909399;
  background: #f4f4f5;
  padding: 2px 6px;
  border-radius: 2px;
}

.view-content {
  .view-item {
    display: flex;
    padding: 8px 0;
    border-bottom: 1px solid #f0f0f0;

    &:last-child {
      border-bottom: none;
    }

    .label {
      width: 120px;
      color: #909399;
      flex-shrink: 0;
    }

    .value {
      flex: 1;
    }
  }
}

.page-title {
  font-size: 16px;
  font-weight: 500;
}
</style>
