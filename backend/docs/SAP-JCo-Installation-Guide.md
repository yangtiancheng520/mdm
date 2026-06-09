# SAP JCo 下载和安装指南

## 一、下载 SAP JCo

### 方法1：SAP 官方网站（推荐）

#### 步骤1：注册/登录 SAP 账号
1. 访问：https://accounts.sap.com/
2. 注册 SAP 账号（免费）
3. 登录账号

#### 步骤2：下载 JCo
1. 访问：https://support.sap.com/en/product/connectors/jco.html
2. 点击 "Download" 链接
3. 选择适合你操作系统的版本：
   - **Windows 64位**: `sapjco3-ntamd64-3.1.2.zip`
   - **Linux 64位**: `sapjco3-linuxx86_64-3.1.2.tgz`
   - **Mac 64位**: `sapjco3-darwinintel64-3.1.2.tgz`

#### 步骤3：解压文件
解压后会得到以下文件：
- `sapjco3.jar` - Java 库文件（必需）
- `sapjco3.dll` (Windows) 或 `libsapjco3.so` (Linux) - 本地库文件（必需）
- `README.txt` - 说明文档
- `CHANGELOG.txt` - 更新日志

### 方法2：SAP 开发者试用版

如果无法从官方网站下载，可以尝试：
1. 访问 SAP 开发者中心：https://developers.sap.com/
2. 注册开发者账号（免费）
3. 在试用下载区寻找 SAP JCo

### 方法3：从已有项目获取

如果你有其他 SAP 项目，可以从那里复制 `sapjco3.jar` 文件。

---

## 二、安装 SAP JCo 到本地 Maven 仓库

### Windows 系统：

#### 方法1：使用批处理脚本
```batch
cd backend
install-sapjco.bat
```

#### 方法2：手动安装
```batch
cd backend
mvn install:install-file ^
  -Dfile=sapjco3.jar ^
  -DgroupId=com.sap ^
  -DartifactId=sapjco3 ^
  -Dversion=3.1.2 ^
  -Dpackaging=jar
```

### Linux/Mac 系统：

```bash
cd backend
mvn install:install-file \
  -Dfile=sapjco3.jar \
  -DgroupId=com.sap \
  -DartifactId=sapjco3 \
  -Dversion=3.1.2 \
  -Dpackaging=jar
```

---

## 三、配置本地库文件（重要！）

### Windows 系统：
1. 将 `sapjco3.dll` 复制到以下任一目录：
   - `C:\Windows\System32`（推荐）
   - 或项目的 `backend` 目录
   - 或添加到系统环境变量 `PATH` 中

### Linux 系统：
```bash
# 复制到系统库目录
sudo cp libsapjco3.so /usr/lib/

# 或设置环境变量
export LD_LIBRARY_PATH=/path/to/libsapjco3.so:$LD_LIBRARY_PATH
```

### Mac 系统：
```bash
# 复制到系统库目录
sudo cp libsapjco3.jnilib /usr/lib/

# 或设置环境变量
export DYLD_LIBRARY_PATH=/path/to/libsapjco3.jnilib:$DYLD_LIBRARY_PATH
```

---

## 四、验证安装

### 1. 检查 Maven 仓库
```bash
# 查看本地仓库中的 SAP JCo
ls ~/.m2/repository/com/sap/sapjco3/3.1.2/
```

### 2. 运行测试
```bash
cd backend
mvn clean compile
```

如果编译成功，说明 SAP JCo 已正确安装。

---

## 五、常见问题

### Q1: 没有 SAP 账号怎么办？
**A:** SAP 账号注册是免费的，访问 https://accounts.sap.com/ 即可注册。

### Q2: 下载需要授权怎么办？
**A:** 某些下载需要 SAP S-User 账号（企业账号）。如果无法获取，可以：
- 联系公司的 SAP 管理员
- 使用 SAP 社区账号尝试下载试用版
- 暂时禁用 SAP 连接器功能

### Q3: 编译时提示找不到 sapjco3.jar 怎么办？
**A:** 确保已正确安装到本地 Maven 仓库：
```bash
mvn install:install-file -Dfile=sapjco3.jar -DgroupId=com.sap -DartifactId=sapjco3 -Dversion=3.1.2 -Dpackaging=jar
```

### Q4: 运行时提示找不到 sapjco3.dll 怎么办？
**A:** 确保本地库文件已正确配置：
- Windows: 复制到 `C:\Windows\System32`
- Linux: 复制到 `/usr/lib/` 或设置 `LD_LIBRARY_PATH`
- Mac: 复制到 `/usr/lib/` 或设置 `DYLD_LIBRARY_PATH`

### Q5: 可以暂时不安装 SAP JCo 吗？
**A:** 可以。修改 `backend/src/main/java/com/vueadmin/service/distribution/connector/ConnectorFactory.java`：

```java
switch (systemType) {
    case "SAP":
        throw new BusinessException("SAP连接器暂未启用，需要安装SAP JCo库");
    case "HTTP":
        return new HttpConnector(config, connectionConfig);
    case "DATABASE":
        return new DatabaseConnector(config, connectionConfig);
    default:
        throw new BusinessException("不支持的系统类型: " + systemType);
}
```

这样项目可以正常启动，只是 SAP 功能不可用。

---

## 六、参考链接

- SAP JCo 官方文档：https://help.sap.com/doc/saphelp_nw75/7.5.0/en-US/48/87ed6c31b72cbfe10000000a42189d/frameset.htm
- SAP 连接器主页：https://support.sap.com/en/product/connectors.html
- SAP 开发者中心：https://developers.sap.com/
