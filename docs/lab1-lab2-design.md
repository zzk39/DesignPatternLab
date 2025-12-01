# Lab1 & Lab2 模块需求设计文档

## 一、项目概述

本项目是一个基于命令行的多文本编辑器，支持同时打开多个文本文件，提供工作区管理、日志记录、状态持久化等功能。项目采用模块化设计，使用设计模式实现可扩展架构。

## 二、Lab1 功能需求

### 2.1 工作区模块 (Workspace)

#### 2.1.1 核心职责
- 管理当前会话的全局状态
- 协调命令与具体编辑器之间的交互
- 状态持久化与恢复
- 发布事件供日志记录等模块订阅

#### 2.1.2 设计要点
- 工作区中可以有多个编辑中的文件(Editor)
- 有一个当前的活动文件(Active Editor)
- 每个Editor有独立的undo/redo状态

#### 2.1.3 持久化内容
- 打开的文件列表
- 当前活动文件
- 文件修改状态(modified标记)
- 日志开关状态

#### 2.1.4 使用的设计模式
- **备忘录模式 (Memento)**：用于工作区状态的持久化和恢复
- **观察者模式 (Observer)**：用于事件通知机制

### 2.2 编辑器模块 (Editor)

#### 2.2.1 文本编辑器 (TextEditor)

##### 功能职责
- 支持基本文本编辑操作：追加(append)、插入(insert)、删除(delete)、替换(replace)
- 支持显示操作：指定行号范围显示文本内容(show)
- 所有编辑操作后自动标记文件为已修改

##### 数据结构
- 使用行数组(`List<String>`)存储文本
- 每个元素是一行
- 保存时用换行符连接各行

##### 使用的设计模式
- **命令模式 (Command)**：实现undo/redo功能
- **装饰器模式 (Decorator)**

### 2.3 日志模块 (Logging)

#### 2.3.1 核心功能
- 记录每一次命令执行，包括执行的时间戳
- 持久化到日志文件中

#### 2.3.2 功能职责
- 若文件第一行是 `# log`，则打开该文件时自动启用日志记录
- 记录每一条命令的执行内容与时间戳
- 支持日志开关，可通过命令手动启用/关闭
- 日志写入 `.filename.log` 文件，永久保存

#### 2.3.3 使用的设计模式
- **观察者模式 (Observer)**：日志模块作为观察者监听命令执行事件

### 2.4 Lab1 命令列表

#### 工作区命令
| 命令 | 功能 | 必需参数 | 可选参数 |
|------|------|---------|---------|
| `load <file>` | 加载文件 | 文件路径 | - |
| `save [file\|all]` | 保存文件 | - | file/all |
| `init <file> [with-log]` | 创建新缓冲区 | 文件 | with-log |
| `close [file]` | 关闭文件 | - | file |
| `edit <file>` | 切换活动文件 | 文件 | - |
| `editor-list` | 显示文件列表 | - | - |
| `dir-tree [path]` | 显示目录树 | - | path |
| `undo` | 撤销 | - | - |
| `redo` | 重做 | - | - |
| `exit` | 退出程序 | - | - |

#### 文本编辑命令
| 命令 | 功能 | 适用文件 |
|------|------|---------|
| `append "text"` | 追加文本 | .txt |
| `insert <line:col> "text"` | 插入文本 | .txt |
| `delete <line:col> <len>` | 删除字符 | .txt |
| `replace <line:col> <len> "text"` | 替换文本 | .txt |
| `show [start:end]` | 显示内容 | .txt |

#### 日志命令
| 命令 | 功能 |
|------|------|
| `log-on [file]` | 启用日志 |
| `log-off [file]` | 关闭日志 |
| `log-show [file]` | 显示日志 |

## 三、Lab2 功能需求

### 3.1 XML编辑器 (XmlEditor)

#### 3.1.1 功能职责
- 支持元素级编辑操作：
  - 插入元素(insert-before)
  - 追加子元素(append-child)
  - 修改元素ID(edit-id)
  - 修改元素文本(edit-text)
  - 删除元素(delete)
- 支持树形结构的可视化输出(xml-tree)
- 支持拼写检查功能

#### 3.1.2 数据结构
- 解析XML文件为树形结构(DOM树)
- 内部维护元素节点，并建立 `id -> element` 的映射
- 保存时序列化回XML格式

#### 3.1.3 使用的设计模式
- **命令模式 (Command)**：实现undo/redo功能
- **组合模式 (Composite)**：表示XML树形结构
- **装饰器模式 (Decorator)**：自动标记文件修改状态

### 3.2 统计模块 (Statistics)

#### 3.2.1 核心功能
- 记录每个文件在当前会话(Session)中的编辑时长
- 以可读格式显示时长

#### 3.2.2 时长计算规则
- **开始计时**：当文件成为活动文件时
- **停止计时**：当切换到其他文件、关闭文件或退出程序时
- **累计时长**：一个会话中，文件每次成为活动文件都会累计时长
- **重置时长**：文件关闭后，如果再次打开，时长重置为0

#### 3.2.3 时长格式规范
| 时长范围 | 显示格式 | 示例 |
|----------|----------|------|
| < 1分钟 | X秒 | `45秒` |
| 1-59分钟 | X分钟 | `25分钟` |
| 1-23小时 | X小时Y分钟 | `2小时15分钟` |
| ≥ 24小时 | X天Y小时 | `1天3小时` |

#### 3.2.4 使用的设计模式
- **装饰器模式 (Decorator)**：为每个文件名添加时长信息
- **观察者模式 (Observer)**：监听文件切换事件

### 3.3 拼写检查模块 (Spell Checking)

#### 3.3.1 核心功能
- 对编辑器中的文本内容进行拼写检查
- 报告拼写错误及建议

#### 3.3.2 设计考察点
- **依赖隔离**：第三方库依赖被限制在适配器内
- **接口抽象**：定义清晰接口，编辑器依赖接口而非实现
- **依赖注入**：依赖从外部传入，而非内部创建
- **可测试性**：使用Mock对象测试，无需真实库

#### 3.3.3 使用的设计模式
- **适配器模式 (Adapter)**

### 3.4 日志增强

#### 3.4.1 新增功能
- 通过文件首行的 `# log` 行为该文件启用日志
- 支持 `-e <cmd>` 参数过滤不需要记录的命令

#### 3.4.2 语法规则
- `# log`：启用该文件的日志记录
- `# log -e <cmd> [-e <cmd> ...]`：排除指定命令的日志记录

### 3.5 Lab2 新增命令

#### XML编辑命令
| 命令 | 功能 | 适用文件 |
|------|------|---------|
| `insert-before <tag> <newId> <targetId> ["text"]` | 插入元素 | .xml |
| `append-child <tag> <newId> <parentId> ["text"]` | 追加子元素 | .xml |
| `edit-id <oldId> <newId>` | 修改元素ID | .xml |
| `edit-text <elementId> ["text"]` | 修改元素文本 | .xml |
| `delete <elementId>` | 删除元素 | .xml |
| `xml-tree [file]` | 显示XML树 | .xml |

#### 拼写检查命令
| 命令 | 功能 | 适用文件 |
|------|------|---------|
| `spell-check [file]` | 拼写检查 | .txt .xml |

## 四、模块架构设计

### 4.1 项目模块结构

```
text-editor-parent (聚合 POM)
├── core                    # 核心模块 (SPI, 域模型, 平台代码)
├── plugins/
│   ├── core-impl          # 核心实现插件 (文本编辑器, 工作区命令, 日志命令)
│   ├── xml-impl           # XML编辑器插件
│   ├── stats-impl         # 统计模块插件
│   ├── spellcheck-simple  # 拼写检查插件
│   └── cli-jline          # 命令行界面插件
```

### 4.2 核心模块 (core)

#### 包结构
```
com.team20.editor/
├── domain/
│   ├── command/           # 命令接口和基类
│   ├── editor/            # 编辑器接口和抽象类
│   └── workspace/         # 工作区相关类
├── infrastructure/
│   ├── event/             # 事件系统 (观察者模式实现)
│   └── persistence/       # 持久化管理
├── extension/             # 扩展点接口 (SPI)
├── bootstrap/             # 应用启动和上下文
└── util/                  # 工具类
```

### 4.3 设计模式应用

#### 4.3.1 命令模式 (Command Pattern)
- **Command接口**：定义execute方法
- **UndoableCommand接口**：扩展Command，增加undo/redo方法
- **具体命令类**：实现各种编辑操作

#### 4.3.2 观察者模式 (Observer Pattern)
- **EventBus**：事件总线，管理事件发布和订阅
- **EventListener**：事件监听器接口
- **Event**：事件基类
- **CommandEvent/WorkspaceEvent**：具体事件类型

#### 4.3.3 备忘录模式 (Memento Pattern)
- **WorkspaceState**：工作区状态的快照
- **WorkspaceMemento**：工作区备忘录
- **PersistenceManager**：负责状态的保存和恢复

#### 4.3.4 组合模式 (Composite Pattern)
- **XmlEditor**：使用DOM树结构管理XML元素
- **Element节点**：支持嵌套子元素

#### 4.3.5 适配器模式 (Adapter Pattern)
- **SpellChecker接口**：定义拼写检查抽象
- **LanguageToolHttpSpellChecker**：适配LanguageTool API

#### 4.3.6 装饰器模式 (Decorator Pattern)
- **AbstractUndoableTextCommand**：为文本命令添加undo/redo能力
- **统计装饰**：为文件列表添加时长显示

## 五、测试策略

### 5.1 单元测试覆盖

#### Core模块测试
- Workspace类测试
- WorkspaceState类测试
- EventBus/事件系统测试
- PersistenceManager测试

#### Core-impl模块测试
- TextEditor测试
- 文本编辑命令测试
- 工作区命令测试
- 日志命令测试

#### XML-impl模块测试
- XmlEditor测试
- XML编辑命令测试

#### Stats-impl模块测试
- SessionStatisticsListener测试
- 时长格式化测试

#### Spellcheck-simple模块测试
- SpellChecker接口测试
- Mock测试

### 5.2 测试框架
- JUnit 5：单元测试框架
- Mockito：Mock框架

## 六、总结

本项目通过模块化设计和设计模式的合理应用，实现了一个可扩展、可维护的命令行文本编辑器。核心亮点包括：

1. **模块化架构**：核心功能与具体实现分离，通过SPI机制实现扩展
2. **设计模式应用**：命令模式、观察者模式、备忘录模式等的合理运用
3. **可测试性**：清晰的接口定义便于单元测试和Mock测试
4. **可扩展性**：插件化架构支持功能的灵活扩展
