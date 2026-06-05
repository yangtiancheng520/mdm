# MDM 主数据管理平台完整实现计划 - 第二部分：架构扩展与技术选型

## 三、后端包结构扩展方案

### 3.1 完整包结构

com.vueadmin
├── config/                      # 配置类
│   ├── security/               # 安全配置
│   │   ├── JwtTokenFilter.java
│   │   ├── SecurityConfig.java
│   │   └── PermissionInterceptor.java
│   ├── cache/                  # 缓存配置
│   │   ├── RedisConfig.java
│   │   └── CacheConfig.java
│   ├── mq/                     # 消息队列配置
│   │   └── RabbitMQConfig.java
│   └── schedule/               # 定时任务配置
│       └── QuartzConfig.java
│
├── entity/                      # 实体类
│   ├── standard/               # 数据标准相关实体
│   ├── form/                   # 表单相关实体
│   ├── workflow/               # 流程相关实体
│   ├── masterdata/             # 主数据相关实体
│   ├── quality/                # 质量相关实体
│   ├── distribution/           # 分发相关实体
│   ├── version/                # 版本相关实体
│   └── system/                 # 系统相关实体
│
├── dto/                         # 数据传输对象
├── repository/                  # 数据访问层
├── service/                     # 服务层
├── controller/                  # 控制器层
├── engine/                      # 引擎层（核心）
│   ├── form/                   # 表单引擎
│   ├── workflow/               # 工作流引擎
│   ├── rule/                   # 规则引擎
│   ├── lifecycle/              # 生命周期引擎
│   ├── quality/                # 质量引擎
│   └── distribution/           # 分发引擎
│
├── integration/                 # 集成层
│   ├── lbpm/                   # 蓝凌LBPM集成
│   ├── dataplatform/           # 数据中台集成
│   ├── tianyancha/             # 天眼查集成
│   └── common/                 # 通用集成组件
│
├── mq/                          # 消息队列
├── schedule/                    # 定时任务
├── cache/                       # 缓存层
├── util/                        # 工具类
├── exception/                   # 异常处理
└── aspect/                      # 切面

## 四、前端目录结构扩展方案

### 4.1 完整目录结构

frontend/src/
├── api/                         # API接口
│   ├── standard/               # 数据标准API
│   │   ├── fieldStandard.ts
│   │   ├── dataStandard.ts
│   │   ├── encodingRule.ts
│   │   └── valueDomain.ts
│   ├── form/                   # 表单API
│   ├── workflow/               # 流程API
│   ├── masterdata/             # 主数据API
│   ├── quality/                # 质量API
│   ├── distribution/           # 分发API
│   ├── version/                # 版本API
│   └── system/                 # 系统API
│
├── components/                  # 公共组件
│   ├── common/                 # 通用组件
│   │   ├── SearchForm.vue      # 搜索表单
│   │   ├── DataTable.vue       # 数据表格
│   │   ├── TreeSelect.vue      # 树形选择
│   │   ├── CodeEditor.vue      # 代码编辑器
│   │   └── JsonEditor.vue      # JSON编辑器
│   ├── form/                   # 表单组件
│   │   ├── FormBuilder.vue     # 表单构建器
│   │   ├── FormRender.vue      # 表单渲染器
│   │   ├── FormDesigner.vue    # 表单设计器
│   │   └── fields/             # 字段组件
│   ├── workflow/               # 流程组件
│   │   ├── WorkflowDesigner.vue # 流程设计器
│   │   ├── WorkflowPreview.vue  # 流程预览
│   │   ├── TaskList.vue         # 任务列表
│   │   └── ApprovalPanel.vue    # 审批面板
│   ├── quality/                # 质量组件
│   └── version/                # 版本组件
│
├── views/                       # 页面视图
│   ├── layout/                 # 布局组件
│   ├── login/                  # 登录页
│   ├── standard/               # 数据标准模块
│   │   ├── FieldStandard.vue   # 字段标准管理
│   │   ├── DataStandard.vue    # 数据标准视图
│   │   ├── EncodingRule.vue    # 编码规则
│   │   ├── ValueDomain.vue     # 值域管理
│   │   └── ChangeApproval.vue  # 变更审批
│   ├── form/                   # 表单模块
│   ├── workflow/               # 流程模块
│   ├── masterdata/             # 主数据模块
│   ├── quality/                # 质量模块
│   ├── distribution/           # 分发模块
│   ├── version/                # 版本模块
│   └── system/                 # 系统模块
│
├── store/                       # 状态管理
├── router/                      # 路由配置
├── utils/                       # 工具函数
├── styles/                      # 样式文件
├── assets/                      # 静态资源
└── types/                       # TypeScript类型定义


## 五、技术选型补充

### 5.1 工作流引擎选型

**方案：内置轻量级流程引擎 + 蓝凌 LBPM 集成（双模式）**

**内置引擎特点：**
- 基于状态机模式 + JSON 配置
- 支持基础审批流程（单审批、多级审批、并行审批）
- 优点：可控性强、易于定制、无额外成本
- 适用场景：简单审批、变更审批等基础流程

**蓝凌 LBPM 集成：**
- 通过 REST API + WebHook 回调集成
- 支持复杂企业级流程
- 优点：功能强大、流程可视化、成熟稳定
- 适用场景：企业级复杂流程场景

**技术栈：**
```xml
<!-- Spring State Machine -->
<dependency>
    <groupId>org.springframework.statemachine</groupId>
    <artifactId>spring-statemachine</artifactId>
    <version>3.2.0</version>
</dependency>
```

### 5.2 规则引擎选型

**方案：Groovy 脚本引擎 + JavaScript 引擎**

**Groovy引擎：**
- 优点：语法简单、与Java无缝集成、动态性强
- 适用：编码规则、校验规则、复杂逻辑规则
- 示例：根据前缀+日期+序列号生成编码

**JavaScript引擎（GraalVM）：**
- 优点：前端可预览、易于调试、性能优秀
- 适用：前端表单校验、动态显隐规则

**技术栈：**
```xml
<!-- Groovy引擎 -->
<dependency>
    <groupId>org.apache.groovy</groupId>
    <artifactId>groovy</artifactId>
    <version>4.0.15</version>
</dependency>
```

### 5.3 消息队列选型

**方案：RabbitMQ**

**理由：**
- 可靠性高，支持消息确认、持久化
- 支持多种消息模式（直连、主题、扇出）
- 管理界面友好，易于监控
- 社区成熟，文档丰富

**应用场景：**
- 数据分发通知
- 异步任务处理
- 系统消息推送

**技术栈：**
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-amqp</artifactId>
</dependency>
```

### 5.4 缓存选型

**方案：Redis**

**应用场景：**
- 数据字典缓存
- 会话管理
- 分布式锁
- 热点数据缓存

**技术栈：**
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>
```

### 5.5 定时任务选型

**方案：Spring Scheduler + Quartz**

- Spring Scheduler：用于简单定时任务
- Quartz：用于复杂定时任务，支持集群、持久化

**技术栈：**
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-quartz</artifactId>
</dependency>
```

### 5.6 文件存储选型

**方案：MinIO / 阿里云OSS**

**应用场景：**
- 导入导出模板文件
- 数据快照文件
- 审计日志文件

**技术栈：**
```xml
<dependency>
    <groupId>io.minio</groupId>
    <artifactId>minio</artifactId>
    <version>8.5.7</version>
</dependency>
```

### 5.7 前端组件库选型

**方案：Element Plus + 自定义组件**

**表单设计器：**
- 推荐：FormMaking / FormGenerator（开源）
- 或自研：基于 JSON Schema + Vue 3

**流程设计器：**
- 推荐：bpmn-js（开源，BPMN 2.0标准）
- 或自研：基于 SVG + Vue 3

**代码编辑器：**
- 推荐：Monaco Editor（VS Code编辑器引擎）

### 5.8 其他技术组件

**API文档：**
```xml
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.3.0</version>
</dependency>
```

**Excel处理：**
```xml
<dependency>
    <groupId>com.alibaba</groupId>
    <artifactId>easyexcel</artifactId>
    <version>3.3.3</version>
</dependency>
```

**JSON处理：**
```xml
<dependency>
    <groupId>com.alibaba.fastjson2</groupId>
    <artifactId>fastjson2</artifactId>
    <version>2.0.43</version>
</dependency>
```

**HTTP客户端：**
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-webflux</artifactId>
</dependency>
```
