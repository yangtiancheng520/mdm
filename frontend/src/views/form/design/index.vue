<template>
  <div class="form-designer">
    <!-- 顶部工具栏 -->
    <div class="designer-header">
      <div class="header-left">
        <el-button @click="handleBack">
          <el-icon><ArrowLeft /></el-icon>
          返回
        </el-button>
        <span class="form-title">{{ formData.formName || '表单设计器' }}{{ isViewMode ? '（查看）' : '' }}</span>
      </div>
      <div class="header-right">
        <el-button v-if="!isViewMode" @click="handlePreview">
          <el-icon><View /></el-icon>
          预览
        </el-button>
        <el-button v-if="!isViewMode" type="primary" @click="handleSave" :loading="saving">
          <el-icon><Check /></el-icon>
          保存
        </el-button>
      </div>
    </div>

    <!-- 主体区域 -->
    <div class="designer-body">
      <!-- 左侧组件库 -->
      <div class="component-panel" :class="{ collapsed: leftPanelCollapsed }">
        <div class="panel-header">
          <el-input v-model="searchKeyword" placeholder="搜索字段" clearable size="small" v-if="!leftPanelCollapsed">
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
          <el-button link @click="leftPanelCollapsed = !leftPanelCollapsed" class="collapse-btn">
            <el-icon><DArrowLeft v-if="!leftPanelCollapsed" /><DArrowRight v-else /></el-icon>
          </el-button>
        </div>
        <div class="panel-body" v-show="!leftPanelCollapsed">
          <el-collapse v-model="activeCollapse">
            <el-collapse-item v-for="entity in filteredEntities" :key="entity.id" :name="entity.id">
              <template #title>
                <span class="entity-title">
                  <el-icon v-if="entity.entityType === 'main'"><Document /></el-icon>
                  <el-icon v-else><Files /></el-icon>
                  {{ entity.entityName }}
                  <el-tag size="small" v-if="entity.entityType === 'sub'" type="warning">子表</el-tag>
                  <el-tag size="small" v-else type="success">主表</el-tag>
                </span>
                <!-- 主表：添加全部按钮 -->
                <el-button
                  v-if="entity.entityType === 'main' && !isViewMode"
                  link
                  type="primary"
                  size="small"
                  @click.stop="handleAddAllMainFields(entity)"
                  class="add-subtable-btn"
                >
                  添加全部
                </el-button>
                <!-- 子表：下拉菜单 -->
                <el-dropdown
                  v-else-if="entity.entityType !== 'main' && !isViewMode"
                  @command="(cmd: string) => handleSubTableCommand(cmd, entity)"
                  trigger="click"
                  @click.stop
                >
                  <el-button link type="primary" size="small" class="add-subtable-btn">
                    添加<el-icon class="el-icon--right"><ArrowDown /></el-icon>
                  </el-button>
                  <template #dropdown>
                    <el-dropdown-menu>
                      <el-dropdown-item command="addAll">
                        <el-icon><List /></el-icon>
                        添加全部字段
                      </el-dropdown-item>
                      <el-dropdown-item command="addEmpty">
                        <el-icon><Plus /></el-icon>
                        添加空表
                      </el-dropdown-item>
                    </el-dropdown-menu>
                  </template>
                </el-dropdown>
              </template>
              <template v-if="entity.entityType === 'main'">
                <div
                  v-for="field in getFilteredFields(entity.id)"
                  :key="field.id"
                  class="field-item"
                  :class="{ 'field-added': isFieldAdded(field.id), 'field-disabled': isViewMode }"
                  :draggable="!isFieldAdded(field.id) && !isViewMode"
                  @dragstart="!isViewMode && handleDragStart($event, field)"
                >
                  <el-icon><Document /></el-icon>
                  <span class="field-name">{{ field.fieldName }}</span>
                  <el-tag size="small" type="info">{{ field.fieldType }}</el-tag>
                  <el-tag v-if="isFieldAdded(field.id)" size="small" type="success" class="added-tag">已添加</el-tag>
                </div>
              </template>
              <template v-else>
                <div class="subtable-tip">
                  <el-icon><InfoFilled /></el-icon>
                  <span>{{ isViewMode ? '子表字段列表' : '拖拽字段到右侧子表表格' }}</span>
                </div>
                <div
                  v-for="field in getFilteredFields(entity.id)"
                  :key="field.id"
                  class="field-item"
                  :class="{ 'field-added': isFieldAddedToSubTable(field.id), 'field-disabled': isViewMode }"
                  :draggable="!isFieldAddedToSubTable(field.id) && !isViewMode"
                  @dragstart="!isViewMode && handleSubFieldDragStart($event, field)"
                >
                  <el-icon><Document /></el-icon>
                  <span class="field-name">{{ field.fieldName }}</span>
                  <el-tag size="small" type="info">{{ field.fieldType }}</el-tag>
                  <el-tag v-if="isFieldAddedToSubTable(field.id)" size="small" type="success" class="added-tag">已添加</el-tag>
                </div>
              </template>
            </el-collapse-item>
          </el-collapse>
          <el-empty v-if="filteredEntities.length === 0" description="暂无字段" />
        </div>
      </div>

      <!-- 中间画布 -->
      <div class="canvas-panel">
        <div class="canvas-toolbar">
          <div class="toolbar-left">
            <template v-if="!isViewMode">
              <el-button type="primary" size="small" @click="handleAddAllFields">
                一键生成
              </el-button>
              <el-button type="danger" size="small" @click="handleClearAll">
                清空
              </el-button>
              <el-divider direction="vertical" />
            </template>
            <span class="toolbar-label">表单布局：</span>
            <el-select v-model="subTableLayout" size="small" style="width: 140px" :disabled="isViewMode">
              <el-option value="tabs" label="主表+子表Tab" />
              <el-option value="full-tabs" label="全Tab布局" />
              <el-option value="collapse" label="折叠面板" />
              <el-option value="flat" label="平铺展示" />
            </el-select>
          </div>
          <el-button-group size="small" v-if="!isViewMode">
            <el-tooltip content="撤销" placement="bottom">
              <el-button @click="handleUndo" :disabled="!canUndo">
                <el-icon><RefreshLeft /></el-icon>
              </el-button>
            </el-tooltip>
            <el-tooltip content="还原" placement="bottom">
              <el-button @click="handleRedo" :disabled="!canRedo">
                <el-icon><RefreshRight /></el-icon>
              </el-button>
            </el-tooltip>
            <el-tooltip content="缩小" placement="bottom">
              <el-button @click="handleZoom('out')" :disabled="zoomLevel <= 50">
                <el-icon><ZoomOut /></el-icon>
              </el-button>
            </el-tooltip>
            <el-button disabled>{{ zoomLevel }}%</el-button>
            <el-tooltip content="放大" placement="bottom">
              <el-button @click="handleZoom('in')" :disabled="zoomLevel >= 150">
                <el-icon><ZoomIn /></el-icon>
              </el-button>
            </el-tooltip>
          </el-button-group>
        </div>
        <div class="canvas-wrapper">
          <div class="canvas-scale" :style="{ transform: `scale(${zoomLevel / 100})`, transformOrigin: 'top left' }">
            <div
              class="canvas"
              :class="{ 'canvas-empty': components.length === 0 && subTables.length === 0, 'view-mode': isViewMode }"
            >
            <!-- 全Tab布局 -->
            <template v-if="subTableLayout === 'full-tabs'">
              <div class="full-tabs-layout">
                <div class="full-tabs-header">
                  <div
                    class="full-tab"
                    :class="{ active: activeFullTab === -1 }"
                    @click="activeFullTab = -1"
                  >
                    主表
                  </div>
                  <div
                    v-for="(sub, index) in subTables"
                    :key="sub.entityId"
                    class="full-tab"
                    :class="{ active: activeFullTab === index }"
                    @click="activeFullTab = index"
                  >
                    {{ sub.entityName }}
                    <el-button v-if="!isViewMode" link size="small" @click.stop="handleRemoveSubTable(index)">
                      <el-icon><Close /></el-icon>
                    </el-button>
                  </div>
                </div>
                <div class="full-tabs-content">
                  <!-- 主表Tab内容 -->
                  <template v-if="activeFullTab === -1">
                    <div v-if="components.length > 0" class="canvas-section">
                      <div v-for="(row, rowIndex) in rows" :key="rowIndex" class="canvas-row">
                        <div
                          v-for="(comp, colIndex) in row"
                          :key="comp.id"
                          class="canvas-cell"
                          :style="{ flex: comp.colSpan || 1 }"
                          :class="{ selected: selectedComponent?.id === comp.id, dragging: !isViewMode && draggingComponent?.id === comp.id }"
                          @click="handleSelectComponent(comp)"
                          :draggable="!isViewMode"
                          @dragstart="!isViewMode && handleCellDragStart($event, comp)"
                          @dragover.prevent="!isViewMode && handleCellDragOver($event, comp)"
                          @drop="!isViewMode && handleCellDrop($event, comp)"
                        >
                          <div class="component-wrapper">
                            <div class="component-label">
                              <el-icon v-if="!isViewMode" class="drag-handle"><Rank /></el-icon>
                              {{ comp.fieldName }}
                            </div>
                            <div class="component-control">
                              <!-- 根据 componentType 渲染控件 -->
                              <el-input v-if="getComponentType(comp) === 'input'" disabled :placeholder="comp.placeholder || '请输入'" size="small" />
                              <el-input v-else-if="getComponentType(comp) === 'textarea'" disabled :placeholder="comp.placeholder || '请输入'" size="small" type="textarea" :rows="2" />
                              <el-input v-else-if="getComponentType(comp) === 'password'" disabled :placeholder="comp.placeholder || '请输入'" size="small" show-password />
                              <el-input-number v-else-if="getComponentType(comp) === 'inputNumber'" disabled size="small" style="width: 100%" controls-position="right" />
                              <el-slider v-else-if="getComponentType(comp) === 'slider'" disabled size="small" style="width: 100%" />
                              <el-rate v-else-if="getComponentType(comp) === 'rate'" disabled size="small" />
                              <el-date-picker v-else-if="getComponentType(comp) === 'datePicker'" disabled size="small" style="width: 100%" placeholder="请选择日期" />
                              <el-date-picker v-else-if="getComponentType(comp) === 'dateTimePicker'" disabled size="small" style="width: 100%" type="datetime" placeholder="请选择日期时间" />
                              <el-time-picker v-else-if="getComponentType(comp) === 'timePicker'" disabled size="small" style="width: 100%" placeholder="请选择时间" />
                              <el-switch v-else-if="getComponentType(comp) === 'switch'" disabled size="small" />
                              <el-select v-else-if="getComponentType(comp) === 'select'" disabled size="small" style="width: 100%" placeholder="请选择" />
                              <el-radio-group v-else-if="getComponentType(comp) === 'radio'" disabled size="small">
                                <el-radio v-for="opt in getDomainOptions(comp.domainId)" :key="opt.value" :value="opt.value">{{ opt.label }}</el-radio>
                              </el-radio-group>
                              <el-checkbox-group v-else-if="getComponentType(comp) === 'checkbox'" disabled size="small">
                                <el-checkbox v-for="opt in getDomainOptions(comp.domainId)" :key="opt.value" :value="opt.value">{{ opt.label }}</el-checkbox>
                              </el-checkbox-group>
                              <el-input v-else disabled :placeholder="comp.placeholder || '请输入'" size="small" />
                            </div>
                            <div v-if="!isViewMode" class="component-actions">
                              <el-button type="primary" link size="small" @click.stop="handleRemoveComponent(comp)">
                                <el-icon><Delete /></el-icon>
                              </el-button>
                            </div>
                          </div>
                        </div>
                      </div>
                    </div>
                    <div v-else class="canvas-placeholder">
                      <p>从左侧拖拽主表字段到此处</p>
                    </div>
                  </template>
                  <!-- 子表Tab内容 -->
                  <template v-else-if="subTables[activeFullTab]">
                    <div class="subtable-grid">
                      <div class="grid-header" v-if="subTables[activeFullTab].fields.length > 0">
                        <div
                          v-for="field in subTables[activeFullTab].fields"
                          :key="field._uid || field.id"
                          class="grid-cell"
                          :class="{ selected: selectedSubField?._uid === field._uid && selectedSubTableIndex === activeFullTab }"
                          :draggable="!isViewMode"
                          @click.stop="handleSelectSubField(activeFullTab, field)"
                          @dragstart="!isViewMode && handleGridFieldDragStart($event, activeFullTab, field)"
                          @dragover.prevent
                          @drop="!isViewMode && handleGridFieldDrop($event, activeFullTab, field)"
                        >
                          <el-icon v-if="!isViewMode" class="drag-handle"><Rank /></el-icon>
                          {{ field.fieldName }}
                          <el-button v-if="!isViewMode" link type="danger" size="small" @click.stop="handleRemoveSubField(activeFullTab, field)">
                            <el-icon><Close /></el-icon>
                          </el-button>
                        </div>
                      </div>
                      <div class="grid-body" v-if="subTables[activeFullTab].fields.length > 0">
                        <div class="grid-row">
                          <div class="grid-cell" v-for="field in subTables[activeFullTab].fields" :key="field._uid || field.id">
                            <el-input v-if="getComponentType(field) === 'input'" disabled size="small" />
                            <el-input-number v-else-if="getComponentType(field) === 'inputNumber'" disabled size="small" style="width: 100%" controls-position="right" />
                            <el-date-picker v-else-if="getComponentType(field) === 'datePicker'" disabled size="small" style="width: 100%" type="date" />
                            <el-time-picker v-else-if="getComponentType(field) === 'timePicker'" disabled size="small" style="width: 100%" />
                            <el-switch v-else-if="getComponentType(field) === 'switch'" disabled size="small" />
                            <el-select v-else-if="getComponentType(field) === 'select'" disabled size="small" style="width: 100%" />
                            <el-radio-group v-else-if="getComponentType(field) === 'radio'" disabled size="small">
                              <el-radio v-for="opt in getDomainOptions(field.domainId)" :key="opt.value" :value="opt.value">{{ opt.label }}</el-radio>
                            </el-radio-group>
                            <el-checkbox-group v-else-if="getComponentType(field) === 'checkbox'" disabled size="small">
                              <el-checkbox v-for="opt in getDomainOptions(field.domainId)" :key="opt.value" :value="opt.value">{{ opt.label }}</el-checkbox>
                            </el-checkbox-group>
                            <el-input v-else disabled size="small" />
                          </div>
                          <div v-if="!isViewMode" class="grid-cell action-cell">
                            <el-button link type="danger" size="small">删除</el-button>
                          </div>
                        </div>
                      </div>
                      <div class="drop-hint" v-if="subTables[activeFullTab].fields.length === 0 && !isViewMode">
                        拖拽字段到此处
                      </div>
                    </div>
                  </template>
                </div>
              </div>
            </template>

            <!-- 其他布局（非全Tab） -->
            <template v-else>
              <!-- 主表字段区域 -->
              <template v-if="components.length > 0">
                <div class="canvas-section">
                  <div class="section-title">主表信息</div>
                  <div v-for="(row, rowIndex) in rows" :key="rowIndex" class="canvas-row">
                    <div
                      v-for="(comp, colIndex) in row"
                      :key="comp.id"
                      class="canvas-cell"
                      :style="{ flex: comp.colSpan || 1 }"
                      :class="{ selected: selectedComponent?.id === comp.id, dragging: !isViewMode && draggingComponent?.id === comp.id }"
                      @click="handleSelectComponent(comp)"
                      :draggable="!isViewMode"
                      @dragstart="!isViewMode && handleCellDragStart($event, comp)"
                      @dragover.prevent="!isViewMode && handleCellDragOver($event, comp)"
                      @drop="!isViewMode && handleCellDrop($event, comp)"
                    >
                      <div class="component-wrapper">
                        <div class="component-label">
                          <el-icon v-if="!isViewMode" class="drag-handle"><Rank /></el-icon>
                          {{ comp.fieldName }}
                        </div>
                        <div class="component-control">
                          <el-input v-if="getComponentType(comp) === 'input'" disabled :placeholder="comp.placeholder || '请输入'" size="small" />
                          <el-input v-else-if="getComponentType(comp) === 'textarea'" disabled :placeholder="comp.placeholder || '请输入'" size="small" type="textarea" :rows="2" />
                          <el-input v-else-if="getComponentType(comp) === 'password'" disabled :placeholder="comp.placeholder || '请输入'" size="small" show-password />
                          <el-input-number v-else-if="getComponentType(comp) === 'inputNumber'" disabled size="small" style="width: 100%" controls-position="right" />
                          <el-slider v-else-if="getComponentType(comp) === 'slider'" disabled size="small" style="width: 100%" />
                          <el-rate v-else-if="getComponentType(comp) === 'rate'" disabled size="small" />
                          <el-date-picker v-else-if="getComponentType(comp) === 'datePicker'" disabled size="small" style="width: 100%" placeholder="请选择日期" />
                          <el-date-picker v-else-if="getComponentType(comp) === 'dateTimePicker'" disabled size="small" style="width: 100%" type="datetime" placeholder="请选择日期时间" />
                          <el-time-picker v-else-if="getComponentType(comp) === 'timePicker'" disabled size="small" style="width: 100%" placeholder="请选择时间" />
                          <el-switch v-else-if="getComponentType(comp) === 'switch'" disabled size="small" />
                          <el-select v-else-if="getComponentType(comp) === 'select'" disabled size="small" style="width: 100%" placeholder="请选择" />
                          <el-radio-group v-else-if="getComponentType(comp) === 'radio'" disabled size="small">
                            <el-radio v-for="opt in getDomainOptions(comp.domainId)" :key="opt.value" :value="opt.value">{{ opt.label }}</el-radio>
                          </el-radio-group>
                          <el-checkbox-group v-else-if="getComponentType(comp) === 'checkbox'" disabled size="small">
                            <el-checkbox v-for="opt in getDomainOptions(comp.domainId)" :key="opt.value" :value="opt.value">{{ opt.label }}</el-checkbox>
                          </el-checkbox-group>
                          <el-input v-else disabled :placeholder="comp.placeholder || '请输入'" size="small" />
                        </div>
                        <div v-if="!isViewMode" class="component-actions">
                          <el-button type="primary" link size="small" @click.stop="handleRemoveComponent(comp)">
                            <el-icon><Delete /></el-icon>
                          </el-button>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
              </template>

              <!-- 子表区域 -->
              <template v-if="subTables.length > 0">
                <div class="canvas-section subtable-section">
                  <div class="section-title">子表信息</div>

                  <!-- Tab布局 -->
                  <template v-if="subTableLayout === 'tabs'">
                    <div class="subtable-tabs">
                      <div
                        v-for="(sub, index) in subTables"
                        :key="sub.entityId"
                        class="subtable-tab"
                        :class="{ active: activeSubTableTab === index }"
                        @click="activeSubTableTab = index"
                      >
                        {{ sub.entityName }}
                        <el-button v-if="!isViewMode" link size="small" @click.stop="handleRemoveSubTable(index)">
                          <el-icon><Close /></el-icon>
                        </el-button>
                      </div>
                    </div>
                    <div class="subtable-content" v-if="subTables[activeSubTableTab]">
                      <div class="subtable-grid">
                        <div class="grid-header" v-if="subTables[activeSubTableTab].fields.length > 0">
                          <div
                            v-for="field in subTables[activeSubTableTab].fields"
                            :key="field._uid || field.id"
                            class="grid-cell"
                            :class="{ selected: selectedSubField?._uid === field._uid && selectedSubTableIndex === activeSubTableTab }"
                            :draggable="!isViewMode"
                            @click.stop="handleSelectSubField(activeSubTableTab, field)"
                            @dragstart="!isViewMode && handleGridFieldDragStart($event, activeSubTableTab, field)"
                            @dragover.prevent
                            @drop="!isViewMode && handleGridFieldDrop($event, activeSubTableTab, field)"
                          >
                            <el-icon v-if="!isViewMode" class="drag-handle"><Rank /></el-icon>
                            {{ field.fieldName }}
                            <el-button v-if="!isViewMode" link type="danger" size="small" @click.stop="handleRemoveSubField(activeSubTableTab, field)">
                              <el-icon><Close /></el-icon>
                            </el-button>
                          </div>
                        </div>
                        <div class="grid-body" v-if="subTables[activeSubTableTab].fields.length > 0">
                          <div class="grid-row">
                            <div class="grid-cell" v-for="field in subTables[activeSubTableTab].fields" :key="field._uid || field.id">
                              <el-input v-if="getComponentType(field) === 'input'" disabled size="small" />
                              <el-input-number v-else-if="getComponentType(field) === 'inputNumber'" disabled size="small" style="width: 100%" controls-position="right" />
                              <el-date-picker v-else-if="getComponentType(field) === 'datePicker'" disabled size="small" style="width: 100%" type="date" />
                              <el-time-picker v-else-if="getComponentType(field) === 'timePicker'" disabled size="small" style="width: 100%" />
                              <el-switch v-else-if="getComponentType(field) === 'switch'" disabled size="small" />
                              <el-select v-else-if="getComponentType(field) === 'select'" disabled size="small" style="width: 100%" />
                              <el-radio-group v-else-if="getComponentType(field) === 'radio'" disabled size="small">
                                <el-radio v-for="opt in getDomainOptions(field.domainId)" :key="opt.value" :value="opt.value">{{ opt.label }}</el-radio>
                              </el-radio-group>
                              <el-checkbox-group v-else-if="getComponentType(field) === 'checkbox'" disabled size="small">
                                <el-checkbox v-for="opt in getDomainOptions(field.domainId)" :key="opt.value" :value="opt.value">{{ opt.label }}</el-checkbox>
                              </el-checkbox-group>
                              <el-input v-else disabled size="small" />
                            </div>
                            <div v-if="!isViewMode" class="grid-cell action-cell">
                              <el-button link type="danger" size="small">删除</el-button>
                            </div>
                          </div>
                        </div>
                        <div class="drop-hint" v-if="subTables[activeSubTableTab].fields.length === 0 && !isViewMode">
                          拖拽字段到此处
                        </div>
                      </div>
                    </div>
                  </template>

                  <!-- 折叠面板布局 -->
                  <template v-else-if="subTableLayout === 'collapse'">
                    <el-collapse>
                      <el-collapse-item v-for="(sub, index) in subTables" :key="sub.entityId" :name="index">
                        <template #title>
                          <span>{{ sub.entityName }}</span>
                          <el-button v-if="!isViewMode" link type="danger" size="small" @click.stop="handleRemoveSubTable(index)">
                            删除
                          </el-button>
                        </template>
                        <div class="subtable-grid">
                          <div class="grid-header" v-if="sub.fields.length > 0">
                            <div
                              v-for="field in sub.fields"
                              :key="field._uid || field.id"
                              class="grid-cell"
                              :class="{ selected: selectedSubField?._uid === field._uid && selectedSubTableIndex === index }"
                              :draggable="!isViewMode"
                              @click.stop="handleSelectSubField(index, field)"
                              @dragstart="!isViewMode && handleGridFieldDragStart($event, index, field)"
                              @dragover.prevent
                              @drop="!isViewMode && handleGridFieldDrop($event, index, field)"
                            >
                              <el-icon v-if="!isViewMode" class="drag-handle"><Rank /></el-icon>
                              {{ field.fieldName }}
                              <el-button v-if="!isViewMode" link type="danger" size="small" @click.stop="handleRemoveSubField(index, field)">
                                <el-icon><Close /></el-icon>
                              </el-button>
                            </div>
                          </div>
                          <div class="grid-body" v-if="sub.fields.length > 0">
                            <div class="grid-row">
                              <div class="grid-cell" v-for="field in sub.fields" :key="field._uid || field.id">
                                <el-input v-if="getComponentType(field) === 'input'" disabled size="small" />
                                <el-input-number v-else-if="getComponentType(field) === 'inputNumber'" disabled size="small" style="width: 100%" controls-position="right" />
                                <el-date-picker v-else-if="getComponentType(field) === 'datePicker'" disabled size="small" style="width: 100%" type="date" />
                                <el-time-picker v-else-if="getComponentType(field) === 'timePicker'" disabled size="small" style="width: 100%" />
                                <el-switch v-else-if="getComponentType(field) === 'switch'" disabled size="small" />
                                <el-select v-else-if="getComponentType(field) === 'select'" disabled size="small" style="width: 100%" />
                                <el-radio-group v-else-if="getComponentType(field) === 'radio'" disabled size="small">
                                  <el-radio v-for="opt in getDomainOptions(field.domainId)" :key="opt.value" :value="opt.value">{{ opt.label }}</el-radio>
                                </el-radio-group>
                                <el-checkbox-group v-else-if="getComponentType(field) === 'checkbox'" disabled size="small">
                                  <el-checkbox v-for="opt in getDomainOptions(field.domainId)" :key="opt.value" :value="opt.value">{{ opt.label }}</el-checkbox>
                                </el-checkbox-group>
                                <el-input v-else disabled size="small" />
                              </div>
                              <div v-if="!isViewMode" class="grid-cell action-cell">
                                <el-button link type="danger" size="small">删除</el-button>
                              </div>
                            </div>
                          </div>
                          <div class="drop-hint" v-if="sub.fields.length === 0 && !isViewMode">
                            拖拽字段到此处
                          </div>
                        </div>
                      </el-collapse-item>
                    </el-collapse>
                  </template>

                  <!-- 平铺布局 -->
                  <template v-else-if="subTableLayout === 'flat'">
                    <div v-for="(sub, index) in subTables" :key="sub.entityId" class="subtable-flat">
                      <div class="subtable-flat-header">
                        <span>{{ sub.entityName }}</span>
                        <el-button v-if="!isViewMode" link type="danger" size="small" @click="handleRemoveSubTable(index)">
                          删除
                        </el-button>
                      </div>
                      <div class="subtable-grid">
                        <div class="grid-header" v-if="sub.fields.length > 0">
                          <div
                            v-for="field in sub.fields"
                            :key="field._uid || field.id"
                            class="grid-cell"
                            :class="{ selected: selectedSubField?._uid === field._uid && selectedSubTableIndex === index }"
                            :draggable="!isViewMode"
                            @click.stop="handleSelectSubField(index, field)"
                            @dragstart="!isViewMode && handleGridFieldDragStart($event, index, field)"
                            @dragover.prevent
                            @drop="!isViewMode && handleGridFieldDrop($event, index, field)"
                          >
                            <el-icon v-if="!isViewMode" class="drag-handle"><Rank /></el-icon>
                            {{ field.fieldName }}
                            <el-button v-if="!isViewMode" link type="danger" size="small" @click.stop="handleRemoveSubField(index, field)">
                              <el-icon><Close /></el-icon>
                            </el-button>
                          </div>
                        </div>
                        <div class="grid-body" v-if="sub.fields.length > 0">
                          <div class="grid-row">
                            <div class="grid-cell" v-for="field in sub.fields" :key="field._uid || field.id">
                              <el-input v-if="getComponentType(field) === 'input'" disabled size="small" />
                              <el-input-number v-else-if="getComponentType(field) === 'inputNumber'" disabled size="small" style="width: 100%" controls-position="right" />
                              <el-date-picker v-else-if="getComponentType(field) === 'datePicker'" disabled size="small" style="width: 100%" type="date" />
                              <el-time-picker v-else-if="getComponentType(field) === 'timePicker'" disabled size="small" style="width: 100%" />
                              <el-switch v-else-if="getComponentType(field) === 'switch'" disabled size="small" />
                              <el-select v-else-if="getComponentType(field) === 'select'" disabled size="small" style="width: 100%" />
                              <el-radio-group v-else-if="getComponentType(field) === 'radio'" disabled size="small">
                                <el-radio v-for="opt in getDomainOptions(field.domainId)" :key="opt.value" :value="opt.value">{{ opt.label }}</el-radio>
                              </el-radio-group>
                              <el-checkbox-group v-else-if="getComponentType(field) === 'checkbox'" disabled size="small">
                                <el-checkbox v-for="opt in getDomainOptions(field.domainId)" :key="opt.value" :value="opt.value">{{ opt.label }}</el-checkbox>
                              </el-checkbox-group>
                              <el-input v-else disabled size="small" />
                            </div>
                            <div v-if="!isViewMode" class="grid-cell action-cell">
                              <el-button link type="danger" size="small">删除</el-button>
                            </div>
                          </div>
                        </div>
                        <div class="drop-hint" v-if="sub.fields.length === 0 && !isViewMode">
                          拖拽字段到此处
                        </div>
                      </div>
                    </div>
                  </template>
                </div>
              </template>
            </template>

            <div v-if="components.length === 0 && subTables.length === 0 && !isViewMode" class="canvas-placeholder">
              <el-icon size="48"><Upload /></el-icon>
              <p>从左侧拖拽字段到此处</p>
            </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 右侧属性面板 -->
      <div class="property-panel" :class="{ collapsed: rightPanelCollapsed }">
        <div class="panel-header">
          <el-button link @click="rightPanelCollapsed = !rightPanelCollapsed" class="collapse-btn">
            <el-icon><DArrowRight v-if="!rightPanelCollapsed" /><DArrowLeft v-else /></el-icon>
          </el-button>
          <span v-if="!rightPanelCollapsed">属性配置</span>
        </div>
        <div class="panel-body" v-show="!rightPanelCollapsed">
          <!-- 主表字段属性 -->
          <template v-if="selectedComponent">
            <el-form :model="selectedComponent" label-width="80px" size="small">
              <el-form-item label="字段名称">
                <el-input v-model="selectedComponent.fieldName" :disabled="isViewMode" />
              </el-form-item>
              <el-form-item label="字段编码">
                <el-input v-model="selectedComponent.fieldCode" disabled />
              </el-form-item>
              <el-form-item label="字段类型">
                <el-input v-model="selectedComponent.fieldType" disabled />
              </el-form-item>
              <el-divider>控件设置</el-divider>
              <el-form-item label="控件类型">
                <el-select v-model="selectedComponent.componentType" style="width: 100%" :disabled="isViewMode" @change="onComponentTypeChange(selectedComponent)">
                  <el-option v-for="type in getComponentTypeOptions(selectedComponent.domainId)" :key="type.value" :label="type.label" :value="type.value" />
                </el-select>
              </el-form-item>
              <!-- 值域显示 -->
              <template v-if="selectedComponent.domainId && domainMap.get(selectedComponent.domainId)">
                <el-form-item label="值域">
                  <el-tag type="info">{{ domainMap.get(selectedComponent.domainId)?.domainName }}</el-tag>
                </el-form-item>
                <el-form-item label="值域选项">
                  <div class="domain-options">
                    <el-tag v-for="opt in domainMap.get(selectedComponent.domainId)?.options" :key="opt.value" size="small" class="domain-option-tag">
                      {{ opt.label }}
                    </el-tag>
                  </div>
                </el-form-item>
              </template>
              <el-divider>显示设置</el-divider>
              <el-form-item label="是否必填">
                <el-switch v-model="selectedComponent.isRequired" :disabled="isViewMode" />
              </el-form-item>
              <el-form-item label="是否只读">
                <el-switch v-model="selectedComponent.isReadonly" :disabled="isViewMode" />
              </el-form-item>
              <el-form-item label="是否隐藏">
                <el-switch v-model="selectedComponent.isHidden" :disabled="isViewMode" />
              </el-form-item>
              <el-divider>布局设置</el-divider>
              <el-form-item label="占用列数">
                <el-select v-model="selectedComponent.colSpan" style="width: 100%" :disabled="isViewMode">
                  <el-option :value="1" label="1列（25%）" />
                  <el-option :value="2" label="2列（50%）" />
                  <el-option :value="3" label="3列（75%）" />
                  <el-option :value="4" label="4列（100%）" />
                </el-select>
              </el-form-item>
              <el-divider>其他设置</el-divider>
              <el-form-item label="占位提示">
                <el-input v-model="selectedComponent.placeholder" :disabled="isViewMode" />
              </el-form-item>
              <el-form-item label="默认值">
                <el-input v-model="selectedComponent.defaultValue" :disabled="isViewMode" />
              </el-form-item>
            </el-form>
          </template>
          <!-- 子表字段属性 -->
          <template v-else-if="selectedSubField">
            <el-form :model="selectedSubField" label-width="80px" size="small">
              <el-form-item label="字段名称">
                <el-input v-model="selectedSubField.fieldName" :disabled="isViewMode" />
              </el-form-item>
              <el-form-item label="字段编码">
                <el-input v-model="selectedSubField.fieldCode" disabled />
              </el-form-item>
              <el-form-item label="字段类型">
                <el-input v-model="selectedSubField.fieldType" disabled />
              </el-form-item>
              <el-divider>控件设置</el-divider>
              <el-form-item label="控件类型">
                <el-select v-model="selectedSubField.componentType" style="width: 100%" :disabled="isViewMode">
                  <el-option v-for="type in getComponentTypeOptions(selectedSubField.domainId)" :key="type.value" :label="type.label" :value="type.value" />
                </el-select>
              </el-form-item>
              <!-- 值域显示 -->
              <template v-if="selectedSubField.domainId && domainMap.get(selectedSubField.domainId)">
                <el-form-item label="值域">
                  <el-tag type="info">{{ domainMap.get(selectedSubField.domainId)?.domainName }}</el-tag>
                </el-form-item>
                <el-form-item label="值域选项">
                  <div class="domain-options">
                    <el-tag v-for="opt in domainMap.get(selectedSubField.domainId)?.options" :key="opt.value" size="small" class="domain-option-tag">
                      {{ opt.label }}
                    </el-tag>
                  </div>
                </el-form-item>
              </template>
              <el-divider>显示设置</el-divider>
              <el-form-item label="是否必填">
                <el-switch v-model="selectedSubField.isRequired" :disabled="isViewMode" />
              </el-form-item>
              <el-form-item label="是否只读">
                <el-switch v-model="selectedSubField.isReadonly" :disabled="isViewMode" />
              </el-form-item>
              <el-divider>其他设置</el-divider>
              <el-form-item label="占位提示">
                <el-input v-model="selectedSubField.placeholder" :disabled="isViewMode" />
              </el-form-item>
              <el-form-item label="默认值">
                <el-input v-model="selectedSubField.defaultValue" :disabled="isViewMode" />
              </el-form-item>
            </el-form>
          </template>
          <el-empty v-else description="请选择组件" />
        </div>
      </div>
    </div>

    <!-- 预览弹窗 -->
    <div v-if="previewVisible" class="preview-fullscreen" @click.self="previewVisible = false">
      <div class="preview-container" :style="{ width: previewWidth + '%' }">
        <div class="preview-header">
          <span class="preview-title">表单预览</span>
          <div class="preview-actions">
            <el-button-group size="small">
              <el-button @click="previewWidth = 50" :type="previewWidth === 50 ? 'primary' : ''">50%</el-button>
              <el-button @click="previewWidth = 75" :type="previewWidth === 75 ? 'primary' : ''">75%</el-button>
              <el-button @click="previewWidth = 90" :type="previewWidth === 90 ? 'primary' : ''">100%</el-button>
            </el-button-group>
            <el-button link @click="previewVisible = false">
              <el-icon size="20"><Close /></el-icon>
            </el-button>
          </div>
        </div>
        <div class="preview-body">
          <!-- 全Tab布局预览 -->
          <template v-if="subTableLayout === 'full-tabs'">
            <el-tabs v-model="previewActiveTab">
              <el-tab-pane label="主表信息" name="main">
                <el-form label-width="120px" :model="previewFormData">
                  <el-row :gutter="20">
                    <el-col :span="6" v-for="comp in components" :key="comp.id">
                      <el-form-item :label="comp.fieldName" :required="comp.isRequired">
                        <el-input v-if="getComponentType(comp) === 'input'" v-model="previewFormData[comp.fieldCode]" :placeholder="comp.placeholder || '请输入'" />
                        <el-input v-else-if="getComponentType(comp) === 'textarea'" v-model="previewFormData[comp.fieldCode]" type="textarea" :rows="2" :placeholder="comp.placeholder || '请输入'" />
                        <el-input v-else-if="getComponentType(comp) === 'password'" v-model="previewFormData[comp.fieldCode]" show-password :placeholder="comp.placeholder || '请输入'" />
                        <el-input-number v-else-if="getComponentType(comp) === 'inputNumber'" v-model="previewFormData[comp.fieldCode]" style="width: 100%" controls-position="right" />
                        <el-slider v-else-if="getComponentType(comp) === 'slider'" v-model="previewFormData[comp.fieldCode]" style="width: 100%" />
                        <el-rate v-else-if="getComponentType(comp) === 'rate'" v-model="previewFormData[comp.fieldCode]" />
                        <el-date-picker v-else-if="getComponentType(comp) === 'datePicker'" v-model="previewFormData[comp.fieldCode]" style="width: 100%" placeholder="请选择日期" />
                        <el-date-picker v-else-if="getComponentType(comp) === 'dateTimePicker'" v-model="previewFormData[comp.fieldCode]" style="width: 100%" type="datetime" placeholder="请选择日期时间" />
                        <el-time-picker v-else-if="getComponentType(comp) === 'timePicker'" v-model="previewFormData[comp.fieldCode]" style="width: 100%" placeholder="请选择时间" />
                        <el-switch v-else-if="getComponentType(comp) === 'switch'" v-model="previewFormData[comp.fieldCode]" />
                        <el-select v-else-if="getComponentType(comp) === 'select'" v-model="previewFormData[comp.fieldCode]" style="width: 100%" placeholder="请选择">
                          <el-option v-for="opt in getDomainOptions(comp.domainId)" :key="opt.value" :label="opt.label" :value="opt.value" />
                        </el-select>
                        <el-radio-group v-else-if="getComponentType(comp) === 'radio'" v-model="previewFormData[comp.fieldCode]">
                          <el-radio v-for="opt in getDomainOptions(comp.domainId)" :key="opt.value" :value="opt.value">{{ opt.label }}</el-radio>
                        </el-radio-group>
                        <el-checkbox-group v-else-if="getComponentType(comp) === 'checkbox'" v-model="previewFormData[comp.fieldCode]">
                          <el-checkbox v-for="opt in getDomainOptions(comp.domainId)" :key="opt.value" :value="opt.value">{{ opt.label }}</el-checkbox>
                        </el-checkbox-group>
                        <el-input v-else v-model="previewFormData[comp.fieldCode]" :placeholder="comp.placeholder || '请输入'" />
                      </el-form-item>
                    </el-col>
                  </el-row>
                </el-form>
              </el-tab-pane>
              <el-tab-pane v-for="sub in subTables" :key="sub.entityId" :label="sub.entityName" :name="'sub-' + sub.entityId">
                <el-table :data="getSubTablePreviewData(sub.entityCode)" border>
                  <el-table-column v-for="field in sub.fields" :key="field.id" :prop="field.fieldCode" :label="field.fieldName" min-width="150">
                    <template #default="{ row, $index }">
                      <el-input v-if="getComponentType(field) === 'input'" v-model="row[field.fieldCode]" size="small" placeholder="请输入" />
                      <el-input v-else-if="getComponentType(field) === 'textarea'" v-model="row[field.fieldCode]" size="small" type="textarea" :rows="1" placeholder="请输入" />
                      <el-input-number v-else-if="getComponentType(field) === 'inputNumber'" v-model="row[field.fieldCode]" size="small" style="width: 100%" controls-position="right" />
                      <el-date-picker v-else-if="getComponentType(field) === 'datePicker'" v-model="row[field.fieldCode]" size="small" style="width: 100%" type="date" placeholder="请选择" />
                      <el-time-picker v-else-if="getComponentType(field) === 'timePicker'" v-model="row[field.fieldCode]" size="small" style="width: 100%" placeholder="请选择" />
                      <el-switch v-else-if="getComponentType(field) === 'switch'" v-model="row[field.fieldCode]" size="small" />
                      <el-select v-else-if="getComponentType(field) === 'select'" v-model="row[field.fieldCode]" size="small" style="width: 100%" placeholder="请选择">
                        <el-option v-for="opt in getDomainOptions(field.domainId)" :key="opt.value" :label="opt.label" :value="opt.value" />
                      </el-select>
                      <el-input v-else v-model="row[field.fieldCode]" size="small" placeholder="请输入" />
                    </template>
                  </el-table-column>
                  <el-table-column label="操作" width="100" fixed="right">
                    <template #default="{ $index }">
                      <el-button type="danger" link size="small" @click="removePreviewSubTableRow(sub.entityCode, $index)">删除</el-button>
                    </template>
                  </el-table-column>
                </el-table>
                <div style="margin-top: 10px">
                  <el-button type="primary" size="small" @click="addPreviewSubTableRow(sub.entityCode)">添加行</el-button>
                </div>
              </el-tab-pane>
            </el-tabs>
          </template>

          <!-- 其他布局预览 -->
          <template v-else>
            <!-- 主表信息 -->
            <div v-if="components.length > 0" class="preview-section">
              <div class="preview-section-title">主表信息</div>
              <el-form label-width="120px" :model="previewFormData">
                <el-row :gutter="20">
                  <el-col :span="6" v-for="comp in components" :key="comp.id">
                    <el-form-item :label="comp.fieldName" :required="comp.isRequired">
                      <el-input v-if="getComponentType(comp) === 'input'" v-model="previewFormData[comp.fieldCode]" :placeholder="comp.placeholder || '请输入'" />
                      <el-input v-else-if="getComponentType(comp) === 'textarea'" v-model="previewFormData[comp.fieldCode]" type="textarea" :rows="2" :placeholder="comp.placeholder || '请输入'" />
                      <el-input v-else-if="getComponentType(comp) === 'password'" v-model="previewFormData[comp.fieldCode]" show-password :placeholder="comp.placeholder || '请输入'" />
                      <el-input-number v-else-if="getComponentType(comp) === 'inputNumber'" v-model="previewFormData[comp.fieldCode]" style="width: 100%" controls-position="right" />
                      <el-slider v-else-if="getComponentType(comp) === 'slider'" v-model="previewFormData[comp.fieldCode]" style="width: 100%" />
                      <el-rate v-else-if="getComponentType(comp) === 'rate'" v-model="previewFormData[comp.fieldCode]" />
                      <el-date-picker v-else-if="getComponentType(comp) === 'datePicker'" v-model="previewFormData[comp.fieldCode]" style="width: 100%" placeholder="请选择日期" />
                      <el-date-picker v-else-if="getComponentType(comp) === 'dateTimePicker'" v-model="previewFormData[comp.fieldCode]" style="width: 100%" type="datetime" placeholder="请选择日期时间" />
                      <el-time-picker v-else-if="getComponentType(comp) === 'timePicker'" v-model="previewFormData[comp.fieldCode]" style="width: 100%" placeholder="请选择时间" />
                      <el-switch v-else-if="getComponentType(comp) === 'switch'" v-model="previewFormData[comp.fieldCode]" />
                      <el-select v-else-if="getComponentType(comp) === 'select'" v-model="previewFormData[comp.fieldCode]" style="width: 100%" placeholder="请选择">
                        <el-option v-for="opt in getDomainOptions(comp.domainId)" :key="opt.value" :label="opt.label" :value="opt.value" />
                      </el-select>
                      <el-radio-group v-else-if="getComponentType(comp) === 'radio'" v-model="previewFormData[comp.fieldCode]">
                        <el-radio v-for="opt in getDomainOptions(comp.domainId)" :key="opt.value" :value="opt.value">{{ opt.label }}</el-radio>
                      </el-radio-group>
                      <el-checkbox-group v-else-if="getComponentType(comp) === 'checkbox'" v-model="previewFormData[comp.fieldCode]">
                        <el-checkbox v-for="opt in getDomainOptions(comp.domainId)" :key="opt.value" :value="opt.value">{{ opt.label }}</el-checkbox>
                      </el-checkbox-group>
                      <el-input v-else v-model="previewFormData[comp.fieldCode]" :placeholder="comp.placeholder || '请输入'" />
                    </el-form-item>
                  </el-col>
                </el-row>
              </el-form>
            </div>

            <!-- 子表信息 -->
            <div v-if="subTables.length > 0" class="preview-section">
              <div class="preview-section-title">子表信息</div>

              <!-- Tab布局 -->
              <template v-if="subTableLayout === 'tabs'">
                <el-tabs v-model="previewActiveTab">
                  <el-tab-pane v-for="sub in subTables" :key="sub.entityId" :label="sub.entityName" :name="'sub-' + sub.entityId">
                    <el-table :data="getSubTablePreviewData(sub.entityCode)" border>
                      <el-table-column v-for="field in sub.fields" :key="field.id" :prop="field.fieldCode" :label="field.fieldName" min-width="150">
                        <template #default="{ row }">
                          <el-input v-if="getComponentType(field) === 'input'" v-model="row[field.fieldCode]" size="small" placeholder="请输入" />
                          <el-input v-else-if="getComponentType(field) === 'textarea'" v-model="row[field.fieldCode]" size="small" type="textarea" :rows="1" placeholder="请输入" />
                          <el-input-number v-else-if="getComponentType(field) === 'inputNumber'" v-model="row[field.fieldCode]" size="small" style="width: 100%" controls-position="right" />
                          <el-date-picker v-else-if="getComponentType(field) === 'datePicker'" v-model="row[field.fieldCode]" size="small" style="width: 100%" type="date" placeholder="请选择" />
                          <el-time-picker v-else-if="getComponentType(field) === 'timePicker'" v-model="row[field.fieldCode]" size="small" style="width: 100%" placeholder="请选择" />
                          <el-switch v-else-if="getComponentType(field) === 'switch'" v-model="row[field.fieldCode]" size="small" />
                          <el-select v-else-if="getComponentType(field) === 'select'" v-model="row[field.fieldCode]" size="small" style="width: 100%" placeholder="请选择">
                            <el-option v-for="opt in getDomainOptions(field.domainId)" :key="opt.value" :label="opt.label" :value="opt.value" />
                          </el-select>
                          <el-input v-else v-model="row[field.fieldCode]" size="small" placeholder="请输入" />
                        </template>
                      </el-table-column>
                      <el-table-column label="操作" width="100" fixed="right">
                        <template #default="{ $index }">
                          <el-button type="danger" link size="small" @click="removePreviewSubTableRow(sub.entityCode, $index)">删除</el-button>
                        </template>
                      </el-table-column>
                    </el-table>
                    <div style="margin-top: 10px">
                      <el-button type="primary" size="small" @click="addPreviewSubTableRow(sub.entityCode)">添加行</el-button>
                    </div>
                  </el-tab-pane>
                </el-tabs>
              </template>

              <!-- 折叠面板布局 -->
              <template v-else-if="subTableLayout === 'collapse'">
                <el-collapse>
                  <el-collapse-item v-for="sub in subTables" :key="sub.entityId" :title="sub.entityName" :name="sub.entityId">
                    <el-table :data="getSubTablePreviewData(sub.entityCode)" border>
                      <el-table-column v-for="field in sub.fields" :key="field.id" :prop="field.fieldCode" :label="field.fieldName" min-width="150">
                        <template #default="{ row }">
                          <el-input v-if="getComponentType(field) === 'input'" v-model="row[field.fieldCode]" size="small" placeholder="请输入" />
                          <el-input v-else-if="getComponentType(field) === 'textarea'" v-model="row[field.fieldCode]" size="small" type="textarea" :rows="1" placeholder="请输入" />
                          <el-input-number v-else-if="getComponentType(field) === 'inputNumber'" v-model="row[field.fieldCode]" size="small" style="width: 100%" controls-position="right" />
                          <el-date-picker v-else-if="getComponentType(field) === 'datePicker'" v-model="row[field.fieldCode]" size="small" style="width: 100%" type="date" placeholder="请选择" />
                          <el-time-picker v-else-if="getComponentType(field) === 'timePicker'" v-model="row[field.fieldCode]" size="small" style="width: 100%" placeholder="请选择" />
                          <el-switch v-else-if="getComponentType(field) === 'switch'" v-model="row[field.fieldCode]" size="small" />
                          <el-select v-else-if="getComponentType(field) === 'select'" v-model="row[field.fieldCode]" size="small" style="width: 100%" placeholder="请选择">
                            <el-option v-for="opt in getDomainOptions(field.domainId)" :key="opt.value" :label="opt.label" :value="opt.value" />
                          </el-select>
                          <el-input v-else v-model="row[field.fieldCode]" size="small" placeholder="请输入" />
                        </template>
                      </el-table-column>
                      <el-table-column label="操作" width="100" fixed="right">
                        <template #default="{ $index }">
                          <el-button type="danger" link size="small" @click="removePreviewSubTableRow(sub.entityCode, $index)">删除</el-button>
                        </template>
                      </el-table-column>
                    </el-table>
                    <div style="margin-top: 10px">
                      <el-button type="primary" size="small" @click="addPreviewSubTableRow(sub.entityCode)">添加行</el-button>
                    </div>
                  </el-collapse-item>
                </el-collapse>
              </template>

              <!-- 平铺布局 -->
              <template v-else-if="subTableLayout === 'flat'">
                <div v-for="sub in subTables" :key="sub.entityId" class="preview-subtable">
                  <div class="preview-subtable-title">{{ sub.entityName }}</div>
                  <el-table :data="getSubTablePreviewData(sub.entityCode)" border>
                    <el-table-column v-for="field in sub.fields" :key="field.id" :prop="field.fieldCode" :label="field.fieldName" min-width="150">
                      <template #default="{ row }">
                        <el-input v-if="getComponentType(field) === 'input'" v-model="row[field.fieldCode]" size="small" placeholder="请输入" />
                        <el-input v-else-if="getComponentType(field) === 'textarea'" v-model="row[field.fieldCode]" size="small" type="textarea" :rows="1" placeholder="请输入" />
                        <el-input-number v-else-if="getComponentType(field) === 'inputNumber'" v-model="row[field.fieldCode]" size="small" style="width: 100%" controls-position="right" />
                        <el-date-picker v-else-if="getComponentType(field) === 'datePicker'" v-model="row[field.fieldCode]" size="small" style="width: 100%" type="date" placeholder="请选择" />
                        <el-time-picker v-else-if="getComponentType(field) === 'timePicker'" v-model="row[field.fieldCode]" size="small" style="width: 100%" placeholder="请选择" />
                        <el-switch v-else-if="getComponentType(field) === 'switch'" v-model="row[field.fieldCode]" size="small" />
                        <el-select v-else-if="getComponentType(field) === 'select'" v-model="row[field.fieldCode]" size="small" style="width: 100%" placeholder="请选择">
                          <el-option v-for="opt in getDomainOptions(field.domainId)" :key="opt.value" :label="opt.label" :value="opt.value" />
                        </el-select>
                        <el-input v-else v-model="row[field.fieldCode]" size="small" placeholder="请输入" />
                      </template>
                    </el-table-column>
                    <el-table-column label="操作" width="100" fixed="right">
                      <template #default="{ $index }">
                        <el-button type="danger" link size="small" @click="removePreviewSubTableRow(sub.entityCode, $index)">删除</el-button>
                      </template>
                    </el-table-column>
                  </el-table>
                  <div style="margin-top: 10px">
                    <el-button type="primary" size="small" @click="addPreviewSubTableRow(sub.entityCode)">添加行</el-button>
                  </div>
                </div>
              </template>
            </div>
          </template>

          <!-- 空状态 -->
          <el-empty v-if="components.length === 0 && subTables.length === 0" description="暂无表单内容" />
        </div>
        <!-- 底部按钮 -->
        <div class="preview-footer">
          <el-button @click="previewVisible = false">取消</el-button>
          <el-button @click="handleAutoFill">模拟填写</el-button>
          <el-button type="primary" @click="handlePreviewSave">模拟保存</el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  ArrowLeft, View, Check, Search, Document, Files,
  ZoomOut, ZoomIn, Delete, Upload, DArrowLeft, DArrowRight, Close, InfoFilled, Rank,
  ArrowDown, List, Plus, RefreshLeft, RefreshRight
} from '@element-plus/icons-vue'
import {
  getFormDesign,
  saveFormDesign,
  type FormDesignRequest,
  type FormComponentDto,
  type FormGroupDto,
  type LayoutConfigDto,
  type SubTableFieldDto,
  COMPONENT_TYPES,
  getDefaultComponentType
} from '@/api/form'
import { getViewDetail, type ViewEntity, type ViewField } from '@/api/standard/viewDefinition'
import { getValueDomainById, type ValueDomain, type DomainOption } from '@/api/standard/valueDomain'

const route = useRoute()
const router = useRouter()

const formId = computed(() => Number(route.params.id))
const isViewMode = computed(() => route.query.mode === 'view')

// 表单数据
const formData = reactive<FormDesignRequest>({
  id: undefined,
  formCode: '',
  formName: '',
  formType: 'create',
  viewId: undefined,
  designMode: 'auto',
  styleTemplate: 'dual',
  groups: [],
  components: []
})

// 组件列表
const components = ref<FormComponentDto[]>([])
const groups = ref<FormGroupDto[]>([])

// 子表列表
interface SubTableItem {
  entityId: number
  entityName: string
  entityCode: string
  fields: SubTableFieldDto[]
}
const subTables = ref<SubTableItem[]>([])

// 视图数据
const entities = ref<ViewEntity[]>([])
const fieldMap = ref<Map<number, ViewField[]>>(new Map())

// 值域数据缓存
const domainMap = ref<Map<number, ValueDomain>>(new Map())

// 搜索
const searchKeyword = ref('')
const activeCollapse = ref<number[]>([])

// 面板折叠状态
const leftPanelCollapsed = ref(false)
const rightPanelCollapsed = ref(false)

// 子表布局方式
const subTableLayout = ref('tabs') // full-tabs | tabs | collapse | flat
const activeSubTableTab = ref(0) // 当前激活的子表Tab索引
const activeFullTab = ref(-1) // 全Tab布局当前激活的Tab（-1表示主表）

// 缩放
const zoomLevel = ref(100)

// 选中组件
const selectedComponent = ref<FormComponentDto | null>(null)
const selectedSubField = ref<ViewField | null>(null)
const selectedSubTableIndex = ref<number>(-1)

// 拖拽移动中的组件
const draggingComponent = ref<FormComponentDto | null>(null)

// 保存状态
const saving = ref(false)

// 预览
const previewVisible = ref(false)
const previewWidth = ref(75) // 默认75%宽度
const previewActiveTab = ref('main') // 预览时的激活Tab

// 预览表单数据
const previewFormData = ref<Record<string, any>>({})
const previewSubTableData = ref<Record<string, any[]>>({})

// 撤销/还原历史
interface HistoryState {
  components: FormComponentDto[]
  subTables: SubTableItem[]
  subTableLayout: string
}
const historyStack = ref<HistoryState[]>([])
const historyIndex = ref(-1)
const MAX_HISTORY = 50

// 保存历史状态
function saveHistory() {
  // 删除当前位置之后的历史
  historyStack.value = historyStack.value.slice(0, historyIndex.value + 1)

  // 保存当前状态
  const state: HistoryState = {
    components: JSON.parse(JSON.stringify(components.value)),
    subTables: JSON.parse(JSON.stringify(subTables.value)),
    subTableLayout: subTableLayout.value
  }
  historyStack.value.push(state)

  // 限制历史记录数量
  if (historyStack.value.length > MAX_HISTORY) {
    historyStack.value.shift()
  } else {
    historyIndex.value++
  }
}

// 撤销
const canUndo = computed(() => historyIndex.value > 0)
function handleUndo() {
  if (!canUndo.value) return
  historyIndex.value--
  const state = historyStack.value[historyIndex.value]
  components.value = JSON.parse(JSON.stringify(state.components))
  subTables.value = JSON.parse(JSON.stringify(state.subTables))
  subTableLayout.value = state.subTableLayout
}

// 还原
const canRedo = computed(() => historyIndex.value < historyStack.value.length - 1)
function handleRedo() {
  if (!canRedo.value) return
  historyIndex.value++
  const state = historyStack.value[historyIndex.value]
  components.value = JSON.parse(JSON.stringify(state.components))
  subTables.value = JSON.parse(JSON.stringify(state.subTables))
  subTableLayout.value = state.subTableLayout
}

// 过滤后的实体
const filteredEntities = computed(() => {
  if (!searchKeyword.value) return entities.value
  const keyword = searchKeyword.value.toLowerCase()
  return entities.value.filter(entity => {
    const fields = fieldMap.value.get(entity.id) || []
    return entity.entityName.toLowerCase().includes(keyword) ||
           fields.some(f => f.fieldName.toLowerCase().includes(keyword))
  })
})

// 获取过滤后的字段列表
function getFilteredFields(entityId: number) {
  const fields = fieldMap.value.get(entityId) || []
  if (!searchKeyword.value) return fields
  const keyword = searchKeyword.value.toLowerCase()
  return fields.filter(f => f.fieldName.toLowerCase().includes(keyword))
}

// 计算行数据
const rows = computed(() => {
  const result: FormComponentDto[][] = []
  let currentRow: FormComponentDto[] = []
  let currentCols = 0

  const sorted = [...components.value].sort((a, b) => {
    if (a.rowIndex !== b.rowIndex) return a.rowIndex - b.rowIndex
    return a.colIndex - b.colIndex
  })

  for (const comp of sorted) {
    const colSpan = comp.colSpan || 1
    if (currentCols + colSpan > 4) {
      if (currentRow.length > 0) result.push(currentRow)
      currentRow = [comp]
      currentCols = colSpan
    } else {
      currentRow.push(comp)
      currentCols += colSpan
    }
  }
  if (currentRow.length > 0) result.push(currentRow)

  return result
})

// 获取实体字段
function getEntityFields(entityId: number) {
  return fieldMap.value.get(entityId) || []
}

// 一键全部添加
function handleAddAllFields() {
  let mainFieldCount = 0
  let subTableCount = 0
  let subFieldCount = 0

  // 添加所有主表字段
  const mainEntity = entities.value.find(e => e.entityType === 'main')
  if (mainEntity) {
    const fields = fieldMap.value.get(mainEntity.id!) || []
    const existingFieldIds = new Set(components.value.map(c => c.viewFieldId))
    const newFields = fields.filter(f => !existingFieldIds.has(f.id))

    if (newFields.length > 0) {
      // 计算起始位置
      const maxRow = components.value.length > 0
        ? components.value.reduce((max, c) => Math.max(max, c.rowIndex || 0), 0)
        : -1
      const lastRowComps = components.value.filter(c => c.rowIndex === maxRow)
      const lastRowCols = lastRowComps.reduce((sum, c) => sum + (c.colSpan || 1), 0)

      let rowIndex = lastRowCols >= 4 ? maxRow + 1 : maxRow
      let colIndex = lastRowCols >= 4 ? 0 : lastRowCols

      newFields.forEach((field, idx) => {
        const comp: FormComponentDto = {
          id: Date.now() + idx,
          formId: formId.value,
          viewFieldId: field.id,
          fieldCode: field.fieldCode,
          fieldName: field.fieldName,
          fieldType: field.fieldType,
          domainId: field.domainId,
          componentType: getDefaultComponentType(field.fieldType, !!field.domainId),
          groupId: undefined,
          rowIndex,
          colIndex,
          colSpan: 1,
          rowSpan: 1,
          isRequired: field.isRequired,
          isReadonly: formData.formType === 'view',
          isHidden: false,
          placeholder: field.placeholder,
          defaultValue: undefined,
          validationRules: undefined,
          labelWidth: undefined,
          componentWidth: undefined,
          sort: components.value.length + idx,
          status: 'active'
        }
        components.value.push(comp)
        mainFieldCount++

        colIndex++
        if (colIndex >= 4) {
          rowIndex++
          colIndex = 0
        }
      })
    }
  }

  // 添加所有子表及其字段
  const subEntities = entities.value.filter(e => e.entityType === 'sub')
  for (const entity of subEntities) {
    // 检查是否已添加
    const existingSub = subTables.value.find(st => st.entityId === entity.id)
    if (existingSub) {
      // 已存在，添加未添加的字段
      const fields = fieldMap.value.get(entity.id!) || []
      const existingFieldIds = new Set(existingSub.fields.map(f => f.id))
      const newFields = fields.filter(f => !existingFieldIds.has(f.id))

      for (const field of newFields) {
        existingSub.fields.push({
          ...field,
          componentType: getDefaultComponentType(field.fieldType, !!field.domainId),
          _uid: Date.now() + Math.random()
        })
        subFieldCount++
      }
    } else {
      // 不存在，添加新子表及所有字段
      const fields = fieldMap.value.get(entity.id!) || []
      subTables.value.push({
        entityId: entity.id!,
        entityName: entity.entityName,
        entityCode: entity.entityCode,
        fields: fields.map(f => ({
          ...f,
          componentType: getDefaultComponentType(f.fieldType, !!f.domainId),
          _uid: Date.now() + Math.random()
        }))
      })
      subTableCount++
      subFieldCount += fields.length
    }
  }

  // 显示结果
  const messages: string[] = []
  if (mainFieldCount > 0) messages.push(`主表字段 ${mainFieldCount} 个`)
  if (subTableCount > 0) messages.push(`子表 ${subTableCount} 个`)
  if (subFieldCount > 0) messages.push(`子表字段 ${subFieldCount} 个`)

  if (messages.length > 0) {
    saveHistory()
    ElMessage.success(`已添加：${messages.join('、')}`)
  } else {
    ElMessage.info('所有字段已添加')
  }
}

// 添加子表
function handleAddSubTable(entity: ViewEntity) {
  // 检查是否已添加
  if (subTables.value.some(st => st.entityId === entity.id)) {
    ElMessage.warning('该子表已添加')
    return
  }

  // 添加空子表，用户可以拖拽字段
  subTables.value.push({
    entityId: entity.id!,
    entityName: entity.entityName,
    entityCode: entity.entityCode,
    fields: []
  })
  saveHistory()
  ElMessage.success(`已添加子表：${entity.entityName}，请拖拽字段到表格中`)
}

// 子表下拉菜单命令处理
function handleSubTableCommand(command: string, entity: ViewEntity) {
  if (command === 'addAll') {
    handleAddSubTableWithAllFields(entity)
  } else {
    handleAddSubTable(entity)
  }
}

// 添加子表并填充所有字段
function handleAddSubTableWithAllFields(entity: ViewEntity) {
  // 检查是否已添加
  if (subTables.value.some(st => st.entityId === entity.id)) {
    ElMessage.warning('该子表已添加')
    return
  }

  const fields = fieldMap.value.get(entity.id!) || []
  if (fields.length === 0) {
    ElMessage.warning('该实体没有字段')
    return
  }

  // 添加子表并填充所有字段
  subTables.value.push({
    entityId: entity.id!,
    entityName: entity.entityName,
    entityCode: entity.entityCode,
    fields: fields.map(f => ({
      ...f,
      componentType: getDefaultComponentType(f.fieldType, !!f.domainId),
      _uid: Date.now() + Math.random()
    }))
  })
  saveHistory()
  ElMessage.success(`已添加子表：${entity.entityName}，包含 ${fields.length} 个字段`)
}

// 检查主表字段是否已添加
function isFieldAdded(fieldId: number): boolean {
  return components.value.some(c => c.viewFieldId === fieldId)
}

// 检查子表字段是否已添加到任意子表
function isFieldAddedToSubTable(fieldId: number): boolean {
  return subTables.value.some(st => st.fields.some(f => f.id === fieldId))
}

// 子表字段拖拽开始
function handleSubFieldDragStart(event: DragEvent, field: ViewField) {
  // 设置拖拽数据，标记为子表字段
  event.dataTransfer!.setData('subfield', JSON.stringify(field))
}

// 批量添加主表所有字段
function handleAddAllMainFields(entity: ViewEntity) {
  const fields = fieldMap.value.get(entity.id!) || []
  if (fields.length === 0) {
    ElMessage.warning('该实体没有字段')
    return
  }

  // 过滤掉已添加的字段
  const existingFieldIds = new Set(components.value.map(c => c.viewFieldId))
  const newFields = fields.filter(f => !existingFieldIds.has(f.id))

  if (newFields.length === 0) {
    ElMessage.warning('所有字段已添加')
    return
  }

  // 计算起始位置
  const maxRow = components.value.length > 0
    ? components.value.reduce((max, c) => Math.max(max, c.rowIndex || 0), 0)
    : -1
  const lastRowComps = components.value.filter(c => c.rowIndex === maxRow)
  const lastRowCols = lastRowComps.reduce((sum, c) => sum + (c.colSpan || 1), 0)

  let rowIndex = lastRowCols >= 4 ? maxRow + 1 : maxRow
  let colIndex = lastRowCols >= 4 ? 0 : lastRowCols

  // 批量添加字段
  newFields.forEach((field, idx) => {
    const comp: FormComponentDto = {
      id: Date.now() + idx,
      formId: formId.value,
      viewFieldId: field.id,
      fieldCode: field.fieldCode,
      fieldName: field.fieldName,
      fieldType: field.fieldType,
      domainId: field.domainId,
      groupId: undefined,
      rowIndex,
      colIndex,
      colSpan: 1,
      rowSpan: 1,
      isRequired: field.isRequired,
      isReadonly: formData.formType === 'view',
      isHidden: false,
      placeholder: field.placeholder,
      defaultValue: undefined,
      validationRules: undefined,
      labelWidth: undefined,
      componentWidth: undefined,
      sort: components.value.length + idx,
      status: 'active'
    }

    components.value.push(comp)

    // 计算下一个字段位置
    colIndex++
    if (colIndex >= 4) {
      rowIndex++
      colIndex = 0
    }
  })

  ElMessage.success(`已添加 ${newFields.length} 个字段`)
}

// 删除子表
function handleRemoveSubTable(index: number) {
  const sub = subTables.value[index]
  subTables.value.splice(index, 1)
  // 调整当前激活的Tab索引
  if (activeSubTableTab.value >= subTables.value.length) {
    activeSubTableTab.value = Math.max(0, subTables.value.length - 1)
  }
  if (activeFullTab.value >= subTables.value.length) {
    activeFullTab.value = subTables.value.length > 0 ? subTables.value.length - 1 : -1
  }
  saveHistory()
  ElMessage.success(`已移除子表：${sub.entityName}`)
}

// 检查是否可以移动字段（防止重复添加）
function checkMove(evt: any) {
  const { draggedContext, relatedContext } = evt
  const draggedElement = draggedContext.element
  const targetList = relatedContext.list
  const sourceList = draggedContext.list

  // 如果是同一个列表内的排序，允许操作
  if (sourceList === targetList) {
    return true
  }

  // 跨列表拖拽时，检查目标列表中是否已存在相同字段
  const exists = targetList.some((f: ViewField) => f.id === draggedElement.id)

  if (exists) {
    ElMessage.warning('该字段已存在')
    return false
  }
  return true
}

// 子表字段放置处理
function handleSubFieldDrop(event: DragEvent, subTableIndex: number) {
  const fieldStr = event.dataTransfer!.getData('subfield')
  if (!fieldStr) return

  const field: ViewField = JSON.parse(fieldStr)
  const sub = subTables.value[subTableIndex]

  // 检查是否已存在
  if (sub.fields.some(f => f.id === field.id)) {
    ElMessage.warning('该字段已存在')
    return
  }

  // 添加字段
  sub.fields.push({
    ...field,
    componentType: getDefaultComponentType(field.fieldType, !!field.domainId),
    _uid: Date.now() + Math.random()
  })
  saveHistory()
  ElMessage.success(`已添加字段：${field.fieldName}`)
}

// 表格内字段拖拽开始（用于排序）
function handleGridFieldDragStart(event: DragEvent, subTableIndex: number, field: ViewField) {
  event.dataTransfer!.setData('gridField', JSON.stringify({ subTableIndex, field }))
  event.dataTransfer!.effectAllowed = 'move'
}

// 表格内字段放置（用于排序）
function handleGridFieldDrop(event: DragEvent, subTableIndex: number, targetField: ViewField) {
  event.preventDefault()

  const gridFieldStr = event.dataTransfer!.getData('gridField')
  const subFieldStr = event.dataTransfer!.getData('subfield')

  const sub = subTables.value[subTableIndex]

  if (gridFieldStr) {
    // 表格内排序
    const { field: sourceField } = JSON.parse(gridFieldStr)
    const sourceIndex = sub.fields.findIndex(f => f._uid === sourceField._uid || f.id === sourceField.id)
    const targetIndex = sub.fields.findIndex(f => f._uid === targetField._uid || f.id === targetField.id)

    if (sourceIndex > -1 && targetIndex > -1 && sourceIndex !== targetIndex) {
      const [removed] = sub.fields.splice(sourceIndex, 1)
      sub.fields.splice(targetIndex, 0, removed)
      saveHistory()
    }
  } else if (subFieldStr) {
    // 从左侧添加新字段
    const field: ViewField = JSON.parse(subFieldStr)

    // 检查是否已存在
    if (sub.fields.some(f => f.id === field.id)) {
      ElMessage.warning('该字段已存在')
      return
    }

    // 插入到目标位置
    const targetIndex = sub.fields.findIndex(f => f._uid === targetField._uid || f.id === targetField.id)
    sub.fields.splice(targetIndex, 0, {
      ...field,
      componentType: getDefaultComponentType(field.fieldType, !!field.domainId),
      _uid: Date.now() + Math.random()
    })
    saveHistory()
    ElMessage.success(`已添加字段：${field.fieldName}`)
  }
}

// 删除子表中的某个字段
function handleRemoveSubField(subTableIndex: number, field: ViewField) {
  const sub = subTables.value[subTableIndex]
  const fieldIndex = sub.fields.findIndex(f => f.id === field.id && f._uid === field._uid)
  if (fieldIndex > -1) {
    sub.fields.splice(fieldIndex, 1)
    // 清除选中状态
    if (selectedSubField.value?._uid === field._uid) {
      selectedSubField.value = null
      selectedSubTableIndex.value = -1
    }
    saveHistory()
    ElMessage.success(`已移除字段：${field.fieldName}`)
  }
}

// 选中子表字段
function handleSelectSubField(subTableIndex: number, field: ViewField) {
  // 清除主表字段选中
  selectedComponent.value = null
  // 设置子表字段选中
  selectedSubField.value = field
  selectedSubTableIndex.value = subTableIndex
  // 加载值域数据
  if (field.domainId && !domainMap.value.has(field.domainId)) {
    loadDomainData(field.domainId)
  }
}

// 获取控件类型选项（所有控件都可选）
function getComponentTypeOptions(domainId?: number) {
  // 所有控件类型都可选，但按是否有值域分组
  const hasDomain = !!domainId
  return [
    { label: '── 有值域时 ──', value: '', disabled: true },
    ...Object.values(COMPONENT_TYPES).filter(t => t.hasDomain),
    { label: '── 无值域时 ──', value: '', disabled: true },
    ...Object.values(COMPONENT_TYPES).filter(t => !t.hasDomain)
  ].filter(opt => !opt.value || (opt.value && !opt.disabled) || opt.disabled)
}

// 获取组件的控件类型
function getComponentType(comp: FormComponentDto | SubTableFieldDto): string {
  if (comp.componentType) return comp.componentType
  // 兼容旧数据，根据 fieldType 返回默认值
  const fieldType = comp.fieldType || 'string'
  return getDefaultComponentType(fieldType, !!comp.domainId)
}

// 获取值域选项
function getDomainOptions(domainId?: number): DomainOption[] {
  if (!domainId) return []
  return domainMap.value.get(domainId)?.options || []
}

// 控件类型变更处理
function onComponentTypeChange(comp: FormComponentDto) {
  // 控件类型变更后，触发界面更新
}

// 加载值域数据
async function loadDomainData(domainId: number) {
  if (domainMap.value.has(domainId)) return
  try {
    const res = await getValueDomainById(domainId)
    domainMap.value.set(domainId, res.data)
  } catch (error) {
    console.warn('加载值域数据失败', error)
  }
}

// 加载数据
async function loadData() {
  try {
    // 加载表单设计
    const res = await getFormDesign(formId.value)
    Object.assign(formData, res.data)
    components.value = res.data.components || []
    groups.value = res.data.groups || []

    // 解析布局配置
    if (res.data.layoutConfig) {
      try {
        const layoutConfig: LayoutConfigDto = JSON.parse(res.data.layoutConfig)
        subTableLayout.value = layoutConfig.subTableLayout || 'tabs'
        subTables.value = layoutConfig.subTables || []
      } catch (e) {
        console.warn('解析布局配置失败', e)
      }
    }

    // 加载视图字段
    if (formData.viewId) {
      const viewRes = await getViewDetail(formData.viewId)
      entities.value = viewRes.data.entities || []
      viewRes.data.entities?.forEach(entity => {
        fieldMap.value.set(entity.id!, entity.fields || [])
      })
      activeCollapse.value = entities.value.map(e => e.id!)
    }

    // 初始化主表字段的控件类型
    components.value.forEach(comp => {
      if (!comp.componentType) {
        comp.componentType = getDefaultComponentType(comp.fieldType, !!comp.domainId)
      }
    })

    // 初始化子表字段的控件类型
    subTables.value.forEach(sub => {
      sub.fields.forEach(field => {
        if (!field.componentType) {
          field.componentType = getDefaultComponentType(field.fieldType || 'string', !!field.domainId)
        }
      })
    })

    // 初始化历史状态
    saveHistory()
  } catch (error) {
    ElMessage.error('加载数据失败')
  }
}

// 拖拽开始
function handleDragStart(event: DragEvent, field: ViewField) {
  event.dataTransfer!.setData('field', JSON.stringify(field))
}

// 放置
function handleDrop(event: DragEvent) {
  const fieldStr = event.dataTransfer!.getData('field')
  if (!fieldStr) return

  const field: ViewField = JSON.parse(fieldStr)

  // 检查是否已存在
  if (components.value.some(c => c.viewFieldId === field.id)) {
    ElMessage.warning('该字段已添加')
    return
  }

  // 计算位置 - 新字段默认占用1列（25%宽度）
  const newColSpan = 1

  // 如果没有组件，放在第一行第一个位置
  if (components.value.length === 0) {
    const comp: FormComponentDto = {
      id: Date.now(),
      formId: formId.value,
      viewFieldId: field.id,
      fieldCode: field.fieldCode,
      fieldName: field.fieldName,
      fieldType: field.fieldType,
      domainId: field.domainId,
      componentType: getDefaultComponentType(field.fieldType, !!field.domainId),
      groupId: undefined,
      rowIndex: 0,
      colIndex: 0,
      colSpan: newColSpan,
      rowSpan: 1,
      isRequired: field.isRequired,
      isReadonly: formData.formType === 'view',
      isHidden: false,
      placeholder: field.placeholder,
      defaultValue: undefined,
      validationRules: undefined,
      labelWidth: undefined,
      componentWidth: undefined,
      sort: 0,
      status: 'active'
    }
    components.value.push(comp)
    selectedComponent.value = comp
    saveHistory()
    return
  }

  // 计算位置
  const maxRow = components.value.reduce((max, c) => Math.max(max, c.rowIndex || 0), 0)
  const lastRowComps = components.value.filter(c => c.rowIndex === maxRow)
  const lastRowCols = lastRowComps.reduce((sum, c) => sum + (c.colSpan || 1), 0)

  let rowIndex = maxRow
  let colIndex = lastRowCols

  // 如果当前行放不下，换到下一行
  if (lastRowCols + newColSpan > 4) {
    rowIndex = maxRow + 1
    colIndex = 0
  }

  const comp: FormComponentDto = {
    id: Date.now(), // 临时ID
    formId: formId.value,
    viewFieldId: field.id,
    fieldCode: field.fieldCode,
    fieldName: field.fieldName,
    fieldType: field.fieldType,
    domainId: field.domainId,
    componentType: getDefaultComponentType(field.fieldType, !!field.domainId),
    groupId: undefined,
    rowIndex,
    colIndex,
    colSpan: 1,
    rowSpan: 1,
    isRequired: field.isRequired,
    isReadonly: formData.formType === 'view',
    isHidden: false,
    placeholder: field.placeholder,
    defaultValue: undefined,
    validationRules: undefined,
    labelWidth: undefined,
    componentWidth: undefined,
    sort: components.value.length,
    status: 'active'
  }

  components.value.push(comp)
  selectedComponent.value = comp
  saveHistory()
}

// 选择组件
function handleSelectComponent(comp: FormComponentDto) {
  // 清除子表字段选中
  selectedSubField.value = null
  selectedSubTableIndex.value = -1
  // 设置主表字段选中
  selectedComponent.value = comp
  // 加载值域数据
  if (comp.domainId && !domainMap.value.has(comp.domainId)) {
    loadDomainData(comp.domainId)
  }
}

// 删除组件
function handleRemoveComponent(comp: FormComponentDto) {
  const index = components.value.findIndex(c => c.id === comp.id)
  if (index > -1) {
    components.value.splice(index, 1)
    if (selectedComponent.value?.id === comp.id) {
      selectedComponent.value = null
    }
    saveHistory()
  }
}

// 开始拖拽画布中的组件（移动位置）
function handleCellDragStart(event: DragEvent, comp: FormComponentDto) {
  draggingComponent.value = comp
  event.dataTransfer!.setData('moveComponent', JSON.stringify({ id: comp.id }))
  event.dataTransfer!.effectAllowed = 'move'
}

// 拖拽经过其他组件
function handleCellDragOver(event: DragEvent, targetComp: FormComponentDto) {
  event.dataTransfer!.dropEffect = 'move'
}

// 放置到其他组件位置（插入模式）
function handleCellDrop(event: DragEvent, targetComp: FormComponentDto) {
  event.preventDefault()

  // 检查是移动已有组件还是添加新组件
  const moveData = event.dataTransfer!.getData('moveComponent')
  const newData = event.dataTransfer!.getData('field')

  if (moveData) {
    // 移动已有组件
    const { id: sourceId } = JSON.parse(moveData)
    if (sourceId === targetComp.id) {
      draggingComponent.value = null
      return
    }

    const sourceIndex = components.value.findIndex(c => c.id === sourceId)
    const targetIndex = components.value.findIndex(c => c.id === targetComp.id)

    if (sourceIndex > -1 && targetIndex > -1) {
      // 从原位置移除，插入到目标位置
      const [removed] = components.value.splice(sourceIndex, 1)
      // 如果源在目标前面，目标索引需要-1
      const insertIndex = sourceIndex < targetIndex ? targetIndex - 1 : targetIndex
      components.value.splice(insertIndex, 0, removed)
      // 重新计算所有组件的位置
      recalculatePositions()
      saveHistory()
    }
  } else if (newData) {
    // 添加新组件到指定位置
    const field: ViewField = JSON.parse(newData)

    // 检查是否已存在
    if (components.value.some(c => c.viewFieldId === field.id)) {
      ElMessage.warning('该字段已添加')
      draggingComponent.value = null
      return
    }

    const targetIndex = components.value.findIndex(c => c.id === targetComp.id)

    const comp: FormComponentDto = {
      id: Date.now(),
      formId: formId.value,
      viewFieldId: field.id,
      fieldCode: field.fieldCode,
      fieldName: field.fieldName,
      fieldType: field.fieldType,
      domainId: field.domainId,
      groupId: undefined,
      rowIndex: 0,
      colIndex: 0,
      colSpan: 1,
      rowSpan: 1,
      isRequired: field.isRequired,
      isReadonly: formData.formType === 'view',
      isHidden: false,
      placeholder: field.placeholder,
      defaultValue: undefined,
      validationRules: undefined,
      labelWidth: undefined,
      componentWidth: undefined,
      sort: targetIndex,
      status: 'active'
    }

    // 插入到目标位置
    components.value.splice(targetIndex, 0, comp)
    selectedComponent.value = comp
    // 重新计算所有组件的位置
    recalculatePositions()
    saveHistory()
  }

  draggingComponent.value = null
}

// 重新计算所有组件的位置
function recalculatePositions() {
  components.value.forEach((comp, index) => {
    comp.rowIndex = Math.floor(index / 4)
    comp.colIndex = index % 4
    comp.sort = index
  })
}

// 缩放
function handleZoom(type: 'in' | 'out') {
  if (type === 'in' && zoomLevel.value < 150) {
    zoomLevel.value += 10
  } else if (type === 'out' && zoomLevel.value > 50) {
    zoomLevel.value -= 10
  }
}

// 预览
async function handlePreview() {
  // 初始化预览数据
  previewFormData.value = {}
  previewSubTableData.value = {}
  previewActiveTab.value = 'main' // 默认显示主表Tab

  // 加载所有字段的值域数据
  const domainIds = new Set<number>()

  // 收集主表字段的值域ID
  components.value.forEach(comp => {
    if (comp.domainId) domainIds.add(comp.domainId)
  })

  // 收集子表字段的值域ID
  subTables.value.forEach(sub => {
    sub.fields.forEach(field => {
      if (field.domainId) domainIds.add(field.domainId)
    })
  })

  // 加载未缓存的值域数据
  for (const domainId of domainIds) {
    if (!domainMap.value.has(domainId)) {
      await loadDomainData(domainId)
    }
  }

  previewVisible.value = true
}

// 获取子表预览数据
function getSubTablePreviewData(entityCode: string): any[] {
  if (!previewSubTableData.value[entityCode]) {
    previewSubTableData.value[entityCode] = [{}]
  }
  return previewSubTableData.value[entityCode]
}

// 添加子表行
function addPreviewSubTableRow(entityCode: string) {
  if (!previewSubTableData.value[entityCode]) {
    previewSubTableData.value[entityCode] = []
  }
  previewSubTableData.value[entityCode].push({})
}

// 删除子表行
function removePreviewSubTableRow(entityCode: string, index: number) {
  if (previewSubTableData.value[entityCode]) {
    previewSubTableData.value[entityCode].splice(index, 1)
  }
}

// 模拟填写
function handleAutoFill() {
  // 填充主表数据
  components.value.forEach(comp => {
    const componentType = getComponentType(comp)
    const domainOptions = getDomainOptions(comp.domainId)

    switch (componentType) {
      case 'input':
      case 'textarea':
        previewFormData.value[comp.fieldCode] = `测试${comp.fieldName}`
        break
      case 'password':
        previewFormData.value[comp.fieldCode] = 'Test@123456'
        break
      case 'inputNumber':
        previewFormData.value[comp.fieldCode] = Math.floor(Math.random() * 100) + 1
        break
      case 'slider':
        previewFormData.value[comp.fieldCode] = Math.floor(Math.random() * 100)
        break
      case 'rate':
        previewFormData.value[comp.fieldCode] = Math.floor(Math.random() * 5) + 1
        break
      case 'datePicker':
        previewFormData.value[comp.fieldCode] = new Date().toISOString().split('T')[0]
        break
      case 'dateTimePicker':
        previewFormData.value[comp.fieldCode] = new Date().toISOString()
        break
      case 'timePicker':
        previewFormData.value[comp.fieldCode] = new Date().toTimeString().slice(0, 8)
        break
      case 'switch':
        previewFormData.value[comp.fieldCode] = true
        break
      case 'select':
        if (domainOptions.length > 0) {
          previewFormData.value[comp.fieldCode] = domainOptions[0].value
        }
        break
      case 'radio':
        if (domainOptions.length > 0) {
          previewFormData.value[comp.fieldCode] = domainOptions[0].value
        }
        break
      case 'checkbox':
        if (domainOptions.length > 0) {
          previewFormData.value[comp.fieldCode] = [domainOptions[0].value]
        }
        break
      default:
        previewFormData.value[comp.fieldCode] = `测试${comp.fieldName}`
    }
  })

  // 填充子表数据
  subTables.value.forEach(sub => {
    const rows = []
    // 生成2-3行测试数据
    const rowCount = Math.floor(Math.random() * 2) + 2
    for (let i = 0; i < rowCount; i++) {
      const row: Record<string, any> = {}
      sub.fields.forEach(field => {
        const componentType = getComponentType(field)
        const domainOptions = getDomainOptions(field.domainId)

        switch (componentType) {
          case 'input':
          case 'textarea':
            row[field.fieldCode] = `测试${field.fieldName}${i + 1}`
            break
          case 'inputNumber':
            row[field.fieldCode] = Math.floor(Math.random() * 100) + 1
            break
          case 'datePicker':
            row[field.fieldCode] = new Date().toISOString().split('T')[0]
            break
          case 'timePicker':
            row[field.fieldCode] = new Date().toTimeString().slice(0, 8)
            break
          case 'switch':
            row[field.fieldCode] = true
            break
          case 'select':
            if (domainOptions.length > 0) {
              row[field.fieldCode] = domainOptions[Math.floor(Math.random() * domainOptions.length)].value
            }
            break
          default:
            row[field.fieldCode] = `测试${field.fieldName}${i + 1}`
        }
      })
      rows.push(row)
    }
    previewSubTableData.value[sub.entityCode] = rows
  })

  ElMessage.success('已自动填充测试数据')
}

// 模拟保存
function handlePreviewSave() {
  const saveData = {
    mainTable: { ...previewFormData.value },
    subTables: { ...previewSubTableData.value }
  }

  ElMessageBox.alert(
    `<pre style="max-height: 400px; overflow: auto; white-space: pre-wrap; word-break: break-all;">${JSON.stringify(saveData, null, 2)}</pre>`,
    '保存数据（JSON）',
    {
      dangerouslyUseHTMLString: true,
      confirmButtonText: '确定',
      customClass: 'preview-json-dialog'
    }
  )
}

// 保存
async function handleSave() {
  saving.value = true
  try {
    // 构建布局配置
    const layoutConfig: LayoutConfigDto = {
      subTableLayout: subTableLayout.value,
      subTables: subTables.value
    }

    const data: FormDesignRequest = {
      ...formData,
      components: components.value,
      groups: groups.value,
      layoutConfig: JSON.stringify(layoutConfig)
    }
    await saveFormDesign(formId.value, data)
    ElMessage.success('保存成功')
  } catch (error) {
    ElMessage.error('保存失败')
  } finally {
    saving.value = false
  }
}

// 清空所有
function handleClearAll() {
  if (components.value.length === 0 && subTables.value.length === 0) {
    ElMessage.info('没有可清空的内容')
    return
  }

  ElMessageBox.confirm('确定要清空所有主表字段和子表吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    components.value = []
    subTables.value = []
    selectedComponent.value = null
    saveHistory()
    ElMessage.success('已清空')
  }).catch(() => {
    // 取消操作
  })
}

// 返回
function handleBack() {
  router.push('/form/manage')
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.form-designer {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: #f5f7fa;
}

.designer-header {
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

.designer-body {
  flex: 1;
  display: flex;
  overflow: hidden;
}

/* 左侧组件库 */
.component-panel {
  width: 280px;
  background: #fff;
  border-right: 1px solid #e4e7ed;
  display: flex;
  flex-direction: column;
  transition: width 0.3s;
}

.component-panel.collapsed {
  width: 40px;
}

.panel-header {
  padding: 12px;
  border-bottom: 1px solid #e4e7ed;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.collapse-btn {
  padding: 4px;
}

.panel-body {
  flex: 1;
  overflow-y: auto;
  padding: 12px;
}

.entity-title {
  display: flex;
  align-items: center;
  gap: 8px;
}

.field-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  border-radius: 4px;
  cursor: grab;
  margin: 4px 0;
}

.field-item:hover {
  background: #f5f7fa;
}

.field-item.field-added {
  background: #f0f9eb;
  cursor: not-allowed;
  opacity: 0.7;
}

.field-item.field-added:hover {
  background: #f0f9eb;
}

.field-item.field-disabled {
  cursor: default;
  opacity: 0.6;
}

.field-item.field-disabled:hover {
  background: transparent;
}

.added-tag {
  margin-left: auto;
}

.field-name {
  flex: 1;
}

.field-list {
  min-height: 20px;
}

/* 中间画布 */
.canvas-panel {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.canvas-toolbar {
  height: 40px;
  background: #fff;
  border-bottom: 1px solid #e4e7ed;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 16px;
}

.canvas-wrapper {
  flex: 1;
  overflow: auto;
  display: flex;
  align-items: flex-start;
  padding: 20px;
}

.canvas-scale {
  flex-shrink: 0;
  width: 760px;
  margin: 0 auto;
}

.canvas {
  width: 760px;
  min-height: 400px;
  background: #fff;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  padding: 20px;
  box-sizing: border-box;
}

.canvas-empty {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 400px;
}

.canvas-placeholder {
  text-align: center;
  color: #909399;
}

.canvas-row {
  display: flex;
  gap: 20px;
  margin-bottom: 16px;
  width: 100%;
}

.canvas-cell {
  flex: 1;
  border: 1px dashed #dcdfe6;
  border-radius: 4px;
  padding: 12px;
  cursor: pointer;
  transition: all 0.2s;
  min-width: 0;
  box-sizing: border-box;
}

.canvas-cell:hover {
  border-color: #409eff;
}

.canvas-cell.selected {
  border-color: #409eff;
  background: #ecf5ff;
}

.component-wrapper {
  position: relative;
}

.component-label {
  font-size: 12px;
  color: #606266;
  margin-bottom: 8px;
  display: flex;
  align-items: center;
  gap: 4px;
}

.drag-handle {
  cursor: move;
  color: #c0c4cc;
  font-size: 14px;
}

.drag-handle:hover {
  color: #409eff;
}

.component-control {
  pointer-events: none;
}

.component-actions {
  position: absolute;
  top: 0;
  right: 0;
  opacity: 0;
  transition: opacity 0.2s;
}

.canvas-cell:hover .component-actions {
  opacity: 1;
}

/* 拖拽中的样式 */
.canvas-cell.dragging {
  opacity: 0.5;
  border-style: solid;
  border-color: #409eff;
}

/* 查看模式样式 */
.canvas.view-mode .canvas-cell {
  cursor: default;
}

.canvas.view-mode .canvas-cell:hover {
  border-color: #dcdfe6;
}

.canvas.view-mode .grid-cell {
  cursor: default;
}

/* 右侧属性面板 */
.property-panel {
  width: 300px;
  background: #fff;
  border-left: 1px solid #e4e7ed;
  display: flex;
  flex-direction: column;
  transition: width 0.3s;
}

.property-panel.collapsed {
  width: 40px;
}

.property-panel .panel-header {
  padding: 12px 16px;
  font-weight: 500;
}

.property-panel .panel-body {
  flex: 1;
  overflow-y: auto;
  padding: 12px;
}

/* 值域选项样式 */
.domain-options {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
  max-height: 120px;
  overflow-y: auto;
}

.domain-option-tag {
  margin: 0;
}

/* 全屏预览 */
.preview-fullscreen {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  z-index: 2000;
  display: flex;
  align-items: center;
  justify-content: center;
}

.preview-container {
  background: #fff;
  border-radius: 8px;
  max-height: 90vh;
  display: flex;
  flex-direction: column;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
  transition: width 0.3s;
}

.preview-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 20px;
  border-bottom: 1px solid #e4e7ed;
}

.preview-title {
  font-size: 16px;
  font-weight: 500;
}

.preview-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.preview-body {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
}

.preview-footer {
  padding: 16px 20px;
  border-top: 1px solid #e4e7ed;
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.preview-section {
  margin-bottom: 24px;
}

.preview-section-title {
  font-size: 16px;
  font-weight: 500;
  color: #303133;
  margin-bottom: 16px;
  padding-bottom: 8px;
  border-bottom: 1px solid #e4e7ed;
}

.preview-subtable {
  margin-bottom: 20px;
}

.preview-subtable-title {
  font-size: 14px;
  font-weight: 500;
  color: #303133;
  margin-bottom: 12px;
}

/* 工具栏左侧 */
.toolbar-left {
  display: flex;
  align-items: center;
  gap: 8px;
}

.toolbar-label {
  font-size: 13px;
  color: #606266;
}

/* 画布分区 */
.canvas-section {
  margin-bottom: 20px;
}

.section-title {
  font-size: 14px;
  font-weight: 500;
  color: #303133;
  margin-bottom: 12px;
  padding-bottom: 8px;
  border-bottom: 1px solid #e4e7ed;
}

.subtable-section {
  margin-top: 20px;
}

/* 全Tab布局 */
.full-tabs-layout {
  display: flex;
  flex-direction: column;
}

.full-tabs-header {
  display: flex;
  border-bottom: 1px solid #e4e7ed;
  margin-bottom: 16px;
}

.full-tab {
  padding: 10px 20px;
  cursor: pointer;
  border-bottom: 2px solid transparent;
  color: #606266;
  display: flex;
  align-items: center;
  gap: 8px;
}

.full-tab:hover {
  color: #409eff;
}

.full-tab.active {
  color: #409eff;
  border-bottom-color: #409eff;
}

.full-tabs-content {
  flex: 1;
}

/* 子表Tab布局 */
.subtable-tabs {
  display: flex;
  gap: 4px;
  margin-bottom: 12px;
  border-bottom: 1px solid #e4e7ed;
}

.subtable-tab {
  padding: 8px 16px;
  cursor: pointer;
  border-bottom: 2px solid transparent;
  color: #606266;
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
}

.subtable-tab:hover {
  color: #409eff;
}

.subtable-tab.active {
  color: #409eff;
  border-bottom-color: #409eff;
}

.subtable-content {
  padding: 8px;
}

/* 子表表格 */
.subtable-grid {
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  min-height: 60px;
}

.grid-header {
  display: flex;
  background: #f5f7fa;
  border-bottom: 1px solid #e4e7ed;
  flex-wrap: wrap;
  min-height: 40px;
}

.drop-hint {
  padding: 20px;
  text-align: center;
  color: #909399;
  font-size: 13px;
  background: #fafafa;
}

.grid-body {
  padding: 8px 0;
}

.grid-cell {
  flex: 1;
  padding: 8px 12px;
  font-size: 13px;
  color: #303133;
  border-right: 1px solid #e4e7ed;
  display: flex;
  align-items: center;
  gap: 4px;
  min-width: 80px;
  cursor: pointer;
  transition: all 0.2s;
}

.grid-cell:hover {
  background: #ecf5ff;
}

.grid-cell.selected {
  background: #ecf5ff;
  border-color: #409eff;
}

.grid-cell:last-child {
  border-right: none;
}

.add-cell {
  flex: 1;
  justify-content: center;
  color: #909399;
  font-style: italic;
}

.action-cell {
  flex: 0 0 80px;
  text-align: center;
}

.grid-body {
  padding: 8px 0;
  pointer-events: none; /* 让点击事件穿透 */
}

.grid-row {
  display: flex;
  border-bottom: 1px solid #ebeef5;
  pointer-events: auto; /* 恢复行的点击 */
}

.grid-row:last-child {
  border-bottom: none;
}

.grid-row .grid-cell {
  padding: 8px 12px;
}

/* 平铺布局 */
.subtable-flat {
  margin-bottom: 16px;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
}

.subtable-flat-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 12px;
  background: #f5f7fa;
  border-bottom: 1px solid #e4e7ed;
  font-size: 14px;
  color: #303133;
}

/* 子表提示 */
.subtable-tip {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px;
  background: #fdf6ec;
  border-radius: 4px;
  color: #e6a23c;
  font-size: 12px;
}

.add-subtable-btn {
  margin-left: auto;
}
</style>