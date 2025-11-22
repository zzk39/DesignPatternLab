#!/bin/bash
#
# Team20 环境初始化脚本 (Lab1)
# 用于首次或重复运行，确保开发环境与项目规范一致。
# 幂等：再次运行不会破坏已有配置。
#
# 可用环境变量：
#   TEAM20_NON_INTERACTIVE=1   跳过交互（CI / Codespaces）
#   TEAM20_GIT_NAME / TEAM20_GIT_EMAIL  提前提供 Git 用户信息
#

set -euo pipefail

GREEN='\033[0;32m'; YELLOW='\033[1;33m'; RED='\033[0;31m'; BLUE='\033[0;34m'; NC='\033[0m'
info(){ echo -e "${BLUE}[INFO]${NC} $*"; }
ok(){ echo -e "${GREEN}✓${NC} $*"; }
warn(){ echo -e "${YELLOW}!${NC} $*"; }
err(){ echo -e "${RED}✗${NC} $*"; }

NON_INTERACTIVE="${TEAM20_NON_INTERACTIVE:-0}"

# 检测平台
detect_platform() {
  case "$(uname -s)" in
    Linux*)
      if grep -qi microsoft /proc/version 2>/dev/null; then PLATFORM="WSL"
      elif [ -f "/.dockerenv" ]; then PLATFORM="Docker"
      else PLATFORM="Linux"; fi
      ;;
    Darwin*) PLATFORM="macOS";;
    CYGWIN*) PLATFORM="Cygwin";;
    MINGW*|MSYS*) PLATFORM="Windows-GitBash";;
    *) PLATFORM="Unknown";;
  esac
}
detect_platform

echo ""
echo "=========================================="
echo "  Team20 环境初始化 - $PLATFORM"
echo "=========================================="
echo ""

# 1. Git 配置
info "检查 Git 配置..."
if command -v git &>/dev/null; then
  # 行尾符策略
  if [[ "$PLATFORM" == "Linux" || "$PLATFORM" == "WSL" || "$PLATFORM" == "macOS" || "$PLATFORM" == "Docker" ]]; then
    git config --global core.autocrlf input 2>/dev/null || true
    ok "行尾符策略 (autocrlf=input)"
  else
    git config --global core.autocrlf true 2>/dev/null || true
    ok "行尾符策略 (autocrlf=true)"
  fi
  USER_SET=$(git config user.name || true)
  EMAIL_SET=$(git config user.email || true)
  if [[ -z "$USER_SET" || -z "$EMAIL_SET" ]]; then
    if [ "$NON_INTERACTIVE" = "1" ]; then
      if [[ -n "${TEAM20_GIT_NAME:-}" && -n "${TEAM20_GIT_EMAIL:-}" ]]; then
        git config --global user.name "$TEAM20_GIT_NAME"
        git config --global user.email "$TEAM20_GIT_EMAIL"
        ok "Git 用户信息已通过环境变量设置"
      else
        warn "Git 用户信息缺失（非交互模式下未提供 TEAM20_GIT_NAME/TEAM20_GIT_EMAIL）"
      fi
    else
      warn "Git 用户信息未配置"
      read -rp "输入你的名字: " IN_NAME
      read -rp "输入你的邮箱: " IN_EMAIL
      git config --global user.name "$IN_NAME"
      git config --global user.email "$IN_EMAIL"
      ok "Git 用户信息已配置"
    fi
  else
    ok "Git 用户: $USER_SET <$EMAIL_SET>"
  fi
else
  err "Git 未安装"
fi
echo ""

# 2. .editorconfig
info "创建/校验 .editorconfig..."
if [ ! -f ".editorconfig" ]; then
cat > .editorconfig <<'EOF'
root = true

[*]
charset = utf-8
end_of_line = lf
insert_final_newline = true
trim_trailing_whitespace = true

[*.java]
indent_style = space
indent_size = 4
max_line_length = 120

[*.{xml,json,yml,yaml}]
indent_style = space
indent_size = 2

[*.md]
trim_trailing_whitespace = false

[*.{sh,bash}]
indent_style = space
indent_size = 2
end_of_line = lf

[*.{bat,cmd}]
indent_style = space
indent_size = 2
end_of_line = crlf
EOF
  ok ".editorconfig 已创建"
else
  warn ".editorconfig 已存在，未覆盖"
fi
echo ""

# 3. .gitattributes
info "创建/校验 .gitattributes..."
if [ ! -f ".gitattributes" ]; then
cat > .gitattributes <<'EOF'
* text=auto

# 源代码统一 LF
*.java text eol=lf
*.xml text eol=lf
*.properties text eol=lf
*.md text eol=lf
*.sh text eol=lf
*.json text eol=lf
*.yml text eol=lf
*.yaml text eol=lf

# Windows 脚本 CRLF
*.bat text eol=crlf
*.cmd text eol=crlf

# 二进制
*.jar binary
*.class binary
*.png binary
*.jpg binary

# Maven Wrapper
mvnw text eol=lf
mvnw.cmd text eol=crlf
EOF
  ok ".gitattributes 已创建"
else
  warn ".gitattributes 已存在，未覆盖"
fi
echo ""

# 4. Maven Wrapper
info "检查 Maven Wrapper..."
if [ ! -f "mvnw" ]; then
  if command -v mvn &>/dev/null; then
    warn "Maven Wrapper 不存在，开始生成..."
    mvn -q -B wrapper:wrapper -Dmaven=3.9.9 || mvn -q -B wrapper:wrapper
    chmod +x mvnw || true
    ok "Maven Wrapper 已生成"
  else
    err "系统未安装 Maven，无法生成 mvnw（请先安装或在另一机生成并提交）"
  fi
else
  chmod +x mvnw 2>/dev/null || true
  ok "Maven Wrapper 已存在"
fi
echo ""

# 5. VS Code settings
info "配置 VS Code 设置..."
mkdir -p .vscode
if [ ! -f ".vscode/settings.json" ]; then
cat > .vscode/settings.json <<'EOF'
{
  "java.configuration.updateBuildConfiguration": "automatic",
  "maven.executable.preferMavenWrapper": true,
  "files.encoding": "utf8",
  "files.eol": "\n",
  "editor.formatOnSave": true,
  "editor.tabSize": 4,
  "editor.insertSpaces": true,
  "[java]": {
    "editor.defaultFormatter": "redhat.java",
    "editor.tabSize": 4
  },
  "[xml]": {
    "editor.tabSize": 2
  },
  "[json]": {
    "editor.tabSize": 2
  }
}
EOF
  ok ".vscode/settings.json 已创建"
else
  warn ".vscode/settings.json 已存在"
fi
echo ""

# 6. 标准化行尾（仅限有工具时）
info "标准化行尾 (Java / mvnw)..."
if command -v dos2unix &>/dev/null; then
  find . -type f -name "*.java" -exec dos2unix {} + 2>/dev/null || true
  [ -f mvnw ] && dos2unix mvnw 2>/dev/null || true
  ok "行尾已标准化 (dos2unix)"
else
  warn "dos2unix 不存在，跳过行尾转换"
fi
echo ""

# 7. Java 环境提示
info "检测 Java 版本..."
JAVA_OK=0
if command -v java &>/dev/null; then
  JV=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}')
  JMAJOR=$(echo "$JV" | cut -d'.' -f1)
  if [[ "$JMAJOR" -ge 17 ]]; then
    ok "Java 版本满足要求: $JV"
    JAVA_OK=1
  else
    err "Java 版本过低: $JV (需要 >= 17)"
  fi
else
  err "未检测到 java 命令"
fi
if [ "$JAVA_OK" -ne 1 ]; then
  case "$PLATFORM" in
    Linux|WSL|Docker)
      echo "安装建议: sudo apt update && sudo apt install -y openjdk-17-jdk"
      ;;
    macOS)
      echo "安装建议: brew install openjdk@17"
      echo "并在 shell 配置文件中添加: export JAVA_HOME=\$(/usr/libexec/java_home -v 17)"
      ;;
    Windows-GitBash)
      echo "安装建议: 使用 Adoptium (https://adoptium.net/) 或 Scoop: scoop install temurin17"
      ;;
  esac
fi
echo ""

# 8. 可选生成 Windows build.bat（存在则跳过）
if [ ! -f "build.bat" ]; then
  info "生成 Windows build.bat（简化运行）..."
cat > build.bat <<'EOF'
@echo off
REM Team20 Windows build/run helper
SETLOCAL ENABLEDELAYEDEXPANSION
cd /d "%~dp0"

if exist mvnw.cmd (
  set "MVNW=mvnw.cmd"
) else (
  set "MVNW=mvnw.cmd"
)

if "%~1"=="run" (
  call "%MVNW%" -DskipTests install
  call "%MVNW%" -DskipTests -pl core exec:java -Dexec.mainClass="com.team20.editor.Main"
  goto :eof
)

echo Usage: build.bat run
EOF
  ok "build.bat 已创建"
else
  warn "build.bat 已存在"
fi
echo ""

# 9. 生成 verify_env.sh（如不存在）
if [ ! -f "verify_env.sh" ]; then
  info "生成 verify_env.sh (调用 setup_team20_env2.sh)..."
cat > verify_env.sh <<'EOF'
#!/bin/bash
set -e
if [ -f "./setup_team20_env2.sh" ]; then
  bash ./setup_team20_env2.sh
else
  echo "setup_team20_env2.sh 不存在"
  exit 1
fi
EOF
  chmod +x verify_env.sh
  ok "verify_env.sh 已创建"
else
  warn "verify_env.sh 已存在"
fi
echo ""

# 10. 输出总结
echo "=========================================="
echo "  初始化完成"
echo "=========================================="
ok "环境基础配置已完成。"
echo ""
echo "下一步建议:"
echo "  1. 验证: ./verify_env.sh"
echo "  2. 编译: ./mvnw clean package"
echo "  3. 运行: ./build.sh run"
echo "  4. 测试: ./mvnw test"
echo ""
if [ "$JAVA_OK" -ne 1 ]; then
  warn "Java 未满足版本要求，需先安装再继续开发。"
fi

# 自动运行验证（可选）
if [ -f "./setup_team20_env2.sh" ]; then
  info "自动运行验证脚本 (setup_team20_env2.sh)..."
  bash ./setup_team20_env2.sh || true
fi