# MDM 主数据管理平台

一个基于 Spring Boot + Vue 3 的企业级主数据管理系统

## 📋 项目简介

MDM（Master Data Management）主数据管理平台是一个完整的前后端分离权限管理系统，实现了基于角色的访问控制（RBAC），包含用户管理、角色管理、权限管理等核心功能，帮助企业实现数据标准化和统一管理。

## 🛠️ 技术栈

### 后端
- **框架**: Spring Boot 3.2.5
- **Java版本**: 17
- **数据库**: MySQL 8.0+
- **ORM**: Spring Data JPA
- **缓存**: Redis 7.0+
- **构建工具**: Maven

### 前端
- **框架**: Vue 3
- **构建工具**: Vite
- **UI组件库**: Element Plus
- **状态管理**: Pinia
- **路由**: Vue Router
- **语言**: TypeScript

## ✨ 功能特性

- 🔐 用户认证与授权 (JWT)
- 👥 用户管理（增删改查）
- 🎭 角色管理
- 🔑 权限管理（菜单权限、按钮权限）
- 📊 RBAC 权限控制模型
- 🎨 现代化 UI 界面
- 📦 数据标准与模型中心
- 📝 表单与视图设计中心
- 🔄 流程与任务管理中心
- 📈 主数据生命周期管理

## 📦 项目结构

```
mdm/
├── backend/              # Spring Boot 后端
│   ├── src/
│   │   └── main/
│   │       ├── java/     # Java 源代码
│   │       └── resources/
│   │           ├── application.yml
│   │           ├── application-dev.yml
│   │           ├── application-test.yml
│   │           └── application-prod.yml
│   └── pom.xml
├── frontend/             # Vue 3 前端
│   ├── src/
│   │   ├── api/         # API 接口
│   │   ├── components/  # 组件
│   │   ├── router/      # 路由配置
│   │   ├── store/       # 状态管理
│   │   └── views/       # 页面视图
│   └── package.json
├── db/                   # 数据库脚本
│   ├── mdm_schema.sql   # 表结构
│   └── mdm_data.sql     # 初始化数据
├── docs/                 # 项目文档
│   └── mdm-implementation-plan-*.md
├── start-all.bat         # 一键启动脚本
├── start-backend.bat     # 启动后端
├── start-frontend.bat    # 启动前端
└── stop-all.bat          # 停止所有服务
```

## 🚀 快速开始

### 环境要求

- Java 17+
- Maven 3.9+
- Node.js 16+
- MySQL 8.0+
- Redis 7.0+

### 安装步骤

1. **克隆项目**
```bash
git clone https://github.com/yangtiancheng520/mdm.git
cd mdm
```

2. **导入数据库**
```bash
# 创建数据库和表结构
mysql -uroot -padmin < db/mdm_schema.sql

# 导入初始化数据
mysql -uroot -padmin < db/mdm_data.sql
```

3. **启动项目**

**方式一：一键启动（Windows）**
```bash
双击运行 start-all.bat
```

**方式二：手动启动**

启动后端：
```bash
cd backend
mvn spring-boot:run
```

启动前端：
```bash
cd frontend
npm install
npm run dev
```

4. **访问系统**
- 前端地址: http://localhost:9000
- 后端地址: http://localhost:3000
- API文档: http://localhost:3000/swagger-ui.html

### 默认账号

- **账号**: admin
- **密码**: 123456
- **角色**: 超级管理员

## 🔧 配置说明

### 后端配置

修改 `backend/src/main/resources/application-dev.yml`:

```yaml
server:
  port: 3000  # 后端端口

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/mdm
    username: root
    password: admin  # 修改为你的数据库密码
```

### 前端配置

修改 `frontend/vite.config.ts`:

```typescript
export default defineConfig({
  server: {
    port: 9000  // 前端端口
  }
})
```

## 📝 API 接口

### 认证接口
- `POST /api/auth/login` - 用户登录
- `GET /api/auth/info` - 获取用户信息

### 用户接口
- `GET /api/users` - 获取用户列表
- `POST /api/users` - 创建用户
- `PUT /api/users/{id}` - 更新用户
- `DELETE /api/users/{id}` - 删除用户

### 角色接口
- `GET /api/roles` - 获取角色列表
- `POST /api/roles` - 创建角色
- `PUT /api/roles/{id}` - 更新角色
- `DELETE /api/roles/{id}` - 删除角色

### 权限接口
- `GET /api/permissions` - 获取权限列表
- `POST /api/permissions` - 创建权限
- `PUT /api/permissions/{id}` - 更新权限
- `DELETE /api/permissions/{id}` - 删除权限

## 🤝 贡献指南

欢迎提交 Issue 和 Pull Request

## 📄 许可证

MIT License

## 👨‍💻 作者

yangtiancheng520

## 🙏 致谢

感谢以下开源项目：
- [Spring Boot](https://spring.io/projects/spring-boot)
- [Vue.js](https://vuejs.org/)
- [Element Plus](https://element-plus.org/)
- [Vite](https://vitejs.dev/)
