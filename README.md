# DesignPatternLab
For course: *Advanced Software Development Techniques 25*

# Team20 Text Editor

> Command-line modular text editor | Design Patterns Practice Project  
> **Team**: Team20 | **Maintainer**: @team20 | **Updated**: 2025-12-01

---

## 📋 Lab2 功能概述

Lab2 在 Lab1 基础上实现了一个**基于命令行的多文件编辑器**，包含**纯文本编辑器**与**XML编辑器**两类，并增加**编辑时长统计**、**拼写检查**两大模块。

### 核心功能

- **工作区管理**：支持同时打开多个文本文件和XML文件，管理活动文件和编辑器状态
- **文本编辑**：基本的文本编辑操作（追加、插入、删除、替换、显示）
- **XML编辑**：支持元素级编辑操作（插入元素、追加子元素、修改元素ID、修改元素文本、删除元素）
- **XML树形显示**：支持树形结构可视化输出
- **撤销/重做**：支持编辑操作的撤销和重做
- **编辑时长统计**：记录每个文件在当前会话中的编辑时长
- **拼写检查**：扫描文档文本内容并输出拼写错误报告
- **日志记录**：可选的命令执行日志，支持 `.filename.log` 格式，支持日志过滤
- **状态持久化**：工作区状态自动保存到 `.workspace.state`，下次启动时恢复

---

## 🚀 快速开始

### 前置要求
- Java 17 或更高版本
- Maven 3.6 或更高版本

### 1️⃣ 克隆项目
```bash
git clone https://github.com/zzk39/DesignPatternLab.git
cd DesignPatternLab
#请根据页面最下方脚本配置环境
```

### 2️⃣ 编译项目
```bash
# Linux/macOS/WSL
./mvnw clean package

# Windows
mvnw.cmd clean package
```

### 3️⃣ 运行程序
```bash
# Linux/macOS/WSL
./build.sh run

# Windows
build.bat run
```

或者直接使用 Maven：
```bash
mvn exec:java -pl core -Dexec.mainClass="com.team20.editor.Main"
```

### 4️⃣ 运行测试
```bash
# 运行所有测试
mvn test

# 运行特定模块的测试
mvn test -pl core
mvn test -pl plugins/core-impl
```

---

## 📝 命令速查表

### 工作区命令
| 命令 | 功能 | 示例 |
|------|------|------|
| `load <file>` | 加载文件 | `load test.txt` |
| `save [file\|all]` | 保存文件 | `save` / `save test.txt` / `save all` |
| `init <text\|xml> [with-log]` | 创建新缓冲区 | `init text` / `init xml with-log` |
| `close [file]` | 关闭文件 | `close` / `close test.txt` |
| `edit <file>` | 切换活动文件 | `edit test.txt` |
| `editor-list` | 显示文件列表（含时长） | `editor-list` |
| `dir-tree [path]` | 显示目录树 | `dir-tree` / `dir-tree src` |
| `undo` | 撤销 | `undo` |
| `redo` | 重做 | `redo` |
| `exit` | 退出程序 | `exit` |

### 文本编辑命令

| 命令 | 功能 | 示例 |
|------|------|------|
| `append "text"` | 追加文本 | `append "Hello World"` |
| `insert <line:col> "text"` | 插入文本 | `insert 1:1 "Hello"` |
| `delete <line:col> <len>` | 删除字符 | `delete 1:1 5` |
| `replace <line:col> <len> "text"` | 替换文本 | `replace 1:1 5 "Hi"` |
| `show [start:end]` | 显示内容 | `show` / `show 1:10` |

### 日志命令

| 命令 | 功能 | 示例 |
|------|------|------|
| `log-on [file]` | 启用日志 | `log-on` / `log-on test.txt` |
| `log-off [file]` | 关闭日志 | `log-off` |
| `log-show [file]` | 显示日志 | `log-show` |

### XML编辑命令

| 命令 | 功能 | 示例 |
|------|------|------|
| `insert-before <tag> <newId> <targetId> ["text"]` | 在目标元素前插入元素 | `insert-before book newBook book1` |
| `append-child <tag> <newId> <parentId> ["text"]` | 追加子元素 | `append-child price price4 book1 "29.99"` |
| `edit-id <oldId> <newId>` | 修改元素ID | `edit-id book1 book001` |
| `edit-text <elementId> ["text"]` | 修改元素文本 | `edit-text title1 "New Title"` |
| `delete <elementId>` | 删除元素 | `delete book1` |
| `xml-tree [file]` | 显示XML树形结构 | `xml-tree` / `xml-tree data.xml` |

### 拼写检查命令

| 命令 | 功能 | 示例 |
|------|------|------|
| `spell-check [file]` | 拼写检查 | `spell-check` / `spell-check test.txt` |

---

## 💡 使用示例

### 示例 1: 创建并编辑文本文件
```bash
> init text
Created new buffer: untitled.txt

> append "Hello, World!"
Appended line.

> append "Welcome to Team20 Editor"
Appended line.

> show
1: Hello, World!
2: Welcome to Team20 Editor

> save hello.txt
Saved: hello.txt
```

### 示例 2: 创建并编辑XML文件
```bash
> init xml
Created new buffer: untitled.xml

> xml-tree
root [id="root"]

> append-child book book1 root
Appended child: book

> append-child title title1 book1 "My Book"
Appended child: title

> xml-tree
root [id="root"]
└── book [id="book1"]
    └── title [id="title1"]
        └── "My Book"

> save books.xml
Saved: books.xml
```

### 示例 3: 编辑时长统计
```bash
> load file1.txt
Loaded: file1.txt

> load file2.xml
Loaded: file2.xml

> editor-list
* file2.xml (45秒)
  file1.txt (2分钟)

> edit file1.txt
Switched to: file1.txt

> editor-list
* file1.txt (2小时15分钟)
  file2.xml (45秒)
```

### 示例 4: 启用日志记录
```bash
> init text with-log
Created new buffer: untitled.txt (logging enabled)

> append "First line"
Appended line.

> save log-test.txt
Saved: log-test.txt

> log-show
session start at 20251201 18:05:00
20251201 18:05:05 append "First line"
20251201 18:05:10 save
```

### 示例 5: 日志过滤功能
```bash
# 文件首行写入: # log -e append -e delete
# 表示不记录该文件的 append 与 delete 命令日志
```

### 示例 6: 拼写检查
```bash
> load document.txt
Loaded: document.txt

> spell-check
拼写检查结果:
第1行，第5列: "recieve" -> 建议: receive
第3行，第12列: "occured" -> 建议: occurred

> load books.xml
Loaded: books.xml

> spell-check
拼写检查结果:
元素 title1: "Itallian" -> 建议: Italian
元素 author2: "Rowlling" -> 建议: Rowling
```

### 示例 7: 多文件编辑
```bash
> load file1.txt
Loaded: file1.txt

> load file2.xml
Loaded: file2.xml

> editor-list
* file2.xml (45秒)
  file1.txt (30秒)

> edit file1.txt
Switched to: file1.txt

> editor-list
* file1.txt (1分钟)
  file2.xml (45秒)
```

---

## 📊 Lab2 新增模块说明

### 1. XML编辑器 (XmlEditor)

XML编辑器支持元素级编辑操作，将XML文件解析为树形结构（DOM树），并支持以下功能：

**功能特性**:
- 支持元素级编辑操作：`insert-before`、`append-child`、`edit-id`、`edit-text`、`delete`
- 支持树形结构可视化输出 (`xml-tree`)
- 支持拼写检查功能
- 支持撤销/重做操作

**数据结构**:
- 解析XML文件为树形结构（DOM树）
- 内部维护 `id -> element` 的映射以支持快速查找
- 每个元素必须有唯一的 `id` 属性用于命令操作中的元素定位

**XML文件示例**:
```xml
<?xml version="1.0" encoding="UTF-8"?>
<bookstore id="root">
    <book id="book1" category="COOKING">
        <title id="title1" lang="en">Everyday Italian</title>
        <author id="author1">Giada De Laurentiis</author>
        <year id="year1">2005</year>
        <price id="price1">30.00</price>
    </book>
</bookstore>
```

### 2. 统计模块 (Statistics)

统计模块记录每个文件在当前会话(Session)中的编辑时长，并以可读格式显示。

**会话定义**:
- 从程序启动到退出的一次完整运行周期
- 每次启动程序开始新的会话，所有文件的编辑时长重置为0
- 工作区状态恢复不会恢复编辑时长

**时长计算规则**:
- 开始计时：当文件成为活动文件时（通过 `load` 或 `edit` 命令）
- 停止计时：当切换到其他文件、关闭文件或退出程序时
- 累计时长：一个会话中，文件每次成为活动文件都会累计时长
- 重置时长：文件关闭后，如果再次打开，时长重置为0

**时长格式规范**:

| 时长范围 | 显示格式 | 示例 |
|---------|---------|------|
| < 1分钟 | X秒 | `45秒` |
| 1-59分钟 | X分钟 | `25分钟` |
| 1-23小时 | X小时Y分钟 | `2小时15分钟` |
| ≥ 24小时 | X天Y小时 | `1天3小时` |

### 3. 拼写检查模块 (Spell Checking)

拼写检查模块对编辑器中的文本内容进行拼写检查，并报告错误。

**功能特性**:
- 支持文本文件和XML文件的拼写检查
- 对于文本文件：报告行号、列号和拼写建议
- 对于XML文件：报告元素ID和拼写建议
- 第三方库依赖被限制在适配器内，实现依赖隔离

**输出格式示例**:

文本文件:
```
拼写检查结果:
第1行，第5列: "recieve" -> 建议: receive
第3行，第12列: "occured" -> 建议: occurred
```

XML文件:
```
拼写检查结果:
元素 title1: "Itallian" -> 建议: Italian
元素 author2: "Rowlling" -> 建议: Rowling
```

### 4. 日志增强

日志模块新增"按文件首行配置的日志过滤"能力。

**语法规则**:
- `# log`：启用该文件的日志记录（保持原有行为）
- `# log -e <cmd> [-e <cmd> ...]`：排除指定命令的日志记录

**示例**:
```
# log -e append -e delete
```
表示不记录该文件的 `append` 与 `delete` 命令日志。

**行为说明**:
- 过滤仅作用于该文件的日志记录
- 适用于文本编辑命令与XML编辑命令
- 未识别或不存在的命令名将被忽略，并在日志模块内以告警方式提示
- 日志写入失败仅提示警告，不阻断编辑流程

---

## 📂 文件存储位置

### 工作区状态
- **文件**: `.workspace.state`
- **位置**: 程序运行目录
- **内容**: 打开的文件列表、当前活动文件、日志开关状态等
- **格式**: JSON

### 日志文件
- **文件**: `.filename.log` (例如 `.test.txt.log`)
- **位置**: 程序运行目录
- **内容**: 命令执行历史和时间戳
- **格式**: 纯文本，每行一条命令记录

### 遗留标记迁移
- 旧版本使用 `.filename.log.enabled` 文件标记日志开关
- Lab1 启动时自动迁移到工作区状态，并删除旧标记文件

---

## 🏗️ 项目结构

```
DesignPatternLab/
├── core/                           # 核心模块
│   └── src/
│       ├── main/java/com/team20/editor/
│       │   ├── bootstrap/          # 应用程序引导和上下文
│       │   ├── domain/             # 领域模型
│       │   │   ├── command/        # 命令模式实现
│       │   │   ├── editor/         # 编辑器接口和实现
│       │   │   └── workspace/      # 工作区管理
│       │   ├── extension/          # 扩展点和 SPI
│       │   ├── infrastructure/     # 基础设施（事件、持久化）
│       │   ├── monitoring/         # 日志和监控
│       │   └── Main.java           # 程序入口
│       └── test/                   # 单元测试
├── plugins/                        # 插件模块
│   ├── core-impl/                  # 核心命令实现
│   └── cli-jline/                  # JLine CLI 插件
├── README.md                       # 本文档
├── pom.xml                         # Maven 项目配置
└── build.sh / build.bat           # 构建脚本
```

---

## 🧪 测试

项目使用 JUnit 5 进行单元测试和集成测试。

### 运行测试
```bash
# 运行所有测试
mvn test

# 运行指定模块测试
mvn test -pl core
mvn test -pl plugins/core-impl

# 运行特定测试类
mvn test -Dtest=WorkspaceTest

# 查看测试覆盖率
mvn test jacoco:report
```

### 测试结构
- `core/src/test/` - 核心模块测试
- `plugins/*/src/test/` - 插件模块测试

---

## 🔧 开发环境设置

### 方法 1: Docker + VS Code（推荐）

1. 安装 Docker 和 VS Code
2. 安装 VS Code 扩展：Dev Containers
3. 打开项目文件夹
4. 选择 "Reopen in Container"
5. 容器会自动配置 Java 17 和 Maven

### 方法 2: 本地环境

#### Linux/macOS/WSL
```bash
# 配置环境
./setup_env.sh

# 验证环境
./verify_env.sh

# 运行程序
./build.sh run
```

#### Windows
```cmd
REM 配置环境
setup_env.bat

REM 验证环境
verify_env.bat

REM 运行程序
build.bat run
```

---

## 📚 设计模式应用

### Lab1 应用的设计模式

1. **命令模式 (Command Pattern)**: 实现可撤销/重做的编辑操作
2. **备忘录模式 (Memento Pattern)**: 工作区状态的持久化和恢复
3. **观察者模式 (Observer Pattern)**: 事件系统和日志监听
4. **工厂模式 (Factory Pattern)**: 编辑器和命令的创建
5. **单例模式 (Singleton Pattern)**: 命令注册表和应用程序上下文
6. **策略模式 (Strategy Pattern)**: 序列化和日志输出策略
7. **适配器模式 (Adapter Pattern)**: 树形视图适配器

### Lab2 新增应用的设计模式

1. **组合模式 (Composite Pattern)**: 表示XML树形结构
2. **装饰器模式 (Decorator Pattern)**: 
   - 自动标记文件修改状态
   - 在显示文件列表时为每个文件名添加时长信息
3. **适配器模式 (Adapter Pattern)**: 拼写检查第三方库适配器，实现依赖隔离
4. **观察者模式 (Observer Pattern)**: 监听文件切换事件，自动更新时长统计

---

## 🤝 贡献指南

1. Fork 项目
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 开启 Pull Request

---

## 📄 许可证

本项目仅供课程学习使用。

---

## 📞 联系方式

- **项目仓库**: https://github.com/zzk39/DesignPatternLab
- **课程**: Advanced Software Development Techniques 25
- **团队**: Team20
