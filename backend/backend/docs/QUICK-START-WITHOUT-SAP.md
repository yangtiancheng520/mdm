# 快速启动方案 - 暂时禁用 SAP 功能

## 问题
项目依赖 SAP JCo 库，但该库需要从 SAP 官网手动下载（需要 SAP 账号）。

## 解决方案

### 方案A：手动下载 SAP JCo（推荐）

1. **注册 SAP 账号**（免费，5分钟）
   - 访问：https://accounts.sap.com/
   - 填写邮箱、姓名等信息
   - 验证邮箱

2. **下载 SAP JCo**
   - 访问：https://support.sap.com/en/product/connectors/jco.html
   - 登录账号
   - 下载 Windows 64位版本：`sapjco3-ntamd64-3.1.2.zip`

3. **安装到项目**
   ```bash
   # 解压文件
   unzip sapjco3-ntamd64-3.1.2.zip

   # 复制 jar 文件到 backend 目录
   cp sapjco3.jar backend/

   # 安装到 Maven 本地仓库
   cd backend
   mvn install:install-file \
     -Dfile=sapjco3.jar \
     -DgroupId=com.sap \
     -DartifactId=sapjco3 \
     -Dversion=3.1.2 \
     -Dpackaging=jar

   # 启动项目
   mvn spring-boot:run
   ```

### 方案B：暂时禁用 SAP 功能（最快）

如果暂时不需要 SAP 功能，可以修改代码跳过 SAP JCo 依赖：

**修改文件：** `backend/src/main/java/com/vueadmin/service/distribution/connector/SapConnector.java`

将依赖 JCo 的代码改为抛出异常的桩实现。

**修改文件：** `backend/src/main/java/com/vueadmin/service/distribution/connector/ConnectorFactory.java`

在创建连接器时，对 SAP 类型抛出提示异常。

**修改文件：** `backend/pom.xml`

将 SAP JCo 依赖注释掉或设置为 optional。

## 建议

如果现在急需启动项目，建议先使用**方案B**（禁用 SAP 功能），等项目启动后再找时间下载安装 SAP JCo。
