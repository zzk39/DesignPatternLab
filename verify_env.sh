#!/bin/bash
#
# Team20 环境验证脚本 (Lab1)
# 不做任何写操作，仅检测并报告。
# 退出码：0 = 通过；非0 = 有错误需处理
#
set -euo pipefail

GREEN='\033[0;32m'; YELLOW='\033[1;33m'; RED='\033[0;31m'; BLUE='\033[0;34m'; NC='\033[0m'
ok(){ echo -e "${GREEN}✓${NC} $*"; }
warn(){ echo -e "${YELLOW}!${NC} $*"; }
err(){ echo -e "${RED}✗${NC} $*"; }
info(){ echo -e "${BLUE}[INFO]${NC} $*"; }

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

ERRORS=0
WARNINGS=0

echo ""
echo "=========================================="
echo "  Team20 环境验证 - $PLATFORM"
echo "=========================================="
echo ""

# 1. Java
echo "1. Java"
echo "----------------------------------------"
if command -v java &>/dev/null; then
  JV=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}')
  MAJOR=$(echo "$JV" | cut -d'.' -f1)
  if [[ "$MAJOR" -ge 17 ]]; then ok "Java $JV"
  else err "Java 版本过低: $JV (>=17)"; ((ERRORS++)); fi
  if [[ -n "${JAVA_HOME:-}" ]]; then ok "JAVA_HOME: $JAVA_HOME"
  else warn "JAVA_HOME 未设置"; ((WARNINGS++)); fi
else
  err "未检测到 Java"; ((ERRORS++))
fi
echo ""

# 2. Maven
echo "2. Maven"
echo "----------------------------------------"
MVN_CMD=""
if [ -f "mvnw" ]; then
  if [ -x "mvnw" ]; then ok "Maven Wrapper 存在"
  else warn "mvnw 不可执行 (需要 chmod +x mvnw)"; ((WARNINGS++)); fi
  MVN_CMD="./mvnw"
else
  warn "缺少 mvnw（推荐使用 Maven Wrapper）"; ((WARNINGS++))
fi

if command -v mvn &>/dev/null; then
  MVN_VER=$(mvn -v 2>/dev/null | awk '/Apache Maven/ {print $3}')
  ok "系统 Maven: $MVN_VER"
else
  if [ -z "$MVN_CMD" ]; then
    err "无 Maven 可用 (系统和 Wrapper 都不存在)"; ((ERRORS++))
  fi
fi
echo ""

# 3. Git
echo "3. Git"
echo "----------------------------------------"
if command -v git &>/dev/null; then
  ok "Git: $(git --version | awk '{print $3}')"
  GNAME=$(git config user.name || true)
  GMAIL=$(git config user.email || true)
  if [[ -n "$GNAME" && -n "$GMAIL" ]]; then ok "Git 用户: $GNAME <$GMAIL>"
  else warn "Git 用户未配置 (user.name / user.email)" ; ((WARNINGS++)); fi
  ACRLF=$(git config core.autocrlf || true)
  if [[ "$PLATFORM" == "Linux" || "$PLATFORM" == "WSL" || "$PLATFORM" == "macOS" || "$PLATFORM" == "Docker" ]]; then
    [[ "$ACRLF" == "input" ]] && ok "行尾策略 (input)" || { warn "建议设置: git config --global core.autocrlf input"; ((WARNINGS++)); }
  else
    [[ "$ACRLF" == "true" ]] && ok "行尾策略 (true)" || { warn "建议设置: git config --global core.autocrlf true"; ((WARNINGS++)); }
  fi
else
  err "Git 未安装"; ((ERRORS++))
fi
echo ""

# 4. 核心项目文件
echo "4. 项目结构"
echo "----------------------------------------"
[ -f "pom.xml" ] && ok "pom.xml 存在" || { err "pom.xml 缺失"; ((ERRORS++)); }
[ -f ".editorconfig" ] && ok ".editorconfig 存在" || { warn ".editorconfig 缺失"; ((WARNINGS++)); }
[ -f ".gitattributes" ] && ok ".gitattributes 存在" || { warn ".gitattributes 缺失"; ((WARNINGS++)); }
[ -f "build.sh" ] && ok "build.sh 存在" || { warn "build.sh 缺失"; ((WARNINGS++)); }
[ -f "build.bat" ] && ok "build.bat 存在" || { warn "build.bat 缺失 (Windows 可选)"; ((WARNINGS++)); }

if [ -d "core/src/main/java" ]; then
  COUNT=$(find core/src/main/java -name "*.java" | wc -l | tr -d ' ')
  ok "core Java 文件数: $COUNT"
else
  err "core/src/main/java 缺失"; ((ERRORS++))
fi

# 插件结构
if [ -d "plugins/core-impl" ]; then ok "插件: core-impl"
else warn "缺少 plugins/core-impl"; ((WARNINGS++)); fi
if [ -d "plugins/cli-jline" ]; then ok "插件: cli-jline"
else warn "缺少 plugins/cli-jline"; ((WARNINGS++)); fi
echo ""

# 5. 工作区与日志文件
echo "5. 工作区与日志"
echo "----------------------------------------"
if [ -f ".workspace.state" ]; then ok ".workspace.state 已存在 (将被启动时使用)"
else warn ".workspace.state 未生成（首次运行后会出现）"; ((WARNINGS++)); fi
LOGS=$(ls -1 .*.log 2>/dev/null | wc -l | tr -d ' ')
if [ "$LOGS" -gt 0 ]; then ok "检测到 $LOGS 个日志文件 (.filename.log)"
else warn "未发现日志文件（尚未启用或记录）"; fi
LEGACY=$(ls -1 .*.log.enabled 2>/dev/null | wc -l || echo 0)
if [ "$LEGACY" -gt 0 ]; then warn "发现 $LEGACY 个旧日志标记 (.log.enabled)，启动时会迁移"
fi
echo ""

# 6. DevContainer / Codespaces
echo "6. DevContainer / 容器"
echo "----------------------------------------"
if [ -f ".devcontainer/devcontainer.json" ]; then ok "devcontainer.json 存在"
else warn "缺少 .devcontainer/devcontainer.json（推荐添加用于统一开发环境）"; ((WARNINGS++)); fi
if [ -f "/.dockerenv" ]; then ok "检测到 Docker 容器环境"; fi
if grep -qi codespace /proc/self/cgroup 2>/dev/null || [[ "${CODESPACES:-}" == "true" ]]; then
  ok "GitHub Codespaces 环境"
fi
echo ""

# 7. 编码检查（采样）
echo "7. 编码 (UTF-8 采样)"
echo "----------------------------------------"
SAMPLE=$(find core/src/main/java -name "*.java" | head -5)
NON_UTF8=0
if command -v file &>/dev/null; then
  for f in $SAMPLE; do
    ENC=$(file -b --mime-encoding "$f" 2>/dev/null || echo "unknown")
    if [[ "$ENC" != "utf-8" && "$ENC" != "us-ascii" ]]; then
      err "$f 编码: $ENC"; NON_UTF8=$((NON_UTF8+1))
    fi
  done
  if [ "$NON_UTF8" -eq 0 ]; then ok "采样文件 UTF-8 OK"
  else ((ERRORS++)); fi
else
  warn "file 命令不可用，跳过编码检测"
fi
echo ""

# 8. 依赖与构建快速验证（可选）
echo "8. Maven 快速验证"
echo "----------------------------------------"
if [ -n "$MVN_CMD" ]; then
  if $MVN_CMD -q -DskipTests validate &>/dev/null; then ok "Maven validate 成功"
  else err "Maven validate 失败"; ((ERRORS++)); fi
else
  warn "无有效 Maven 命令，跳过 validate"
fi
echo ""

# 总结
echo "=========================================="
echo "验证结果"
echo "=========================================="
echo "平台: $PLATFORM"
echo "错误: $ERRORS"
echo "警告: $WARNINGS"
echo ""

if [ "$ERRORS" -eq 0 ]; then
  ok "环境验证通过"
  echo ""
  echo "下一步建议:"
  [ -n "$MVN_CMD" ] && echo "  编译: $MVN_CMD clean package"
  echo "  运行: ./build.sh run"
  echo "  测试: $MVN_CMD test"
  echo ""
  exit 0
else
  err "发现 $ERRORS 个错误，请修复后重试"
  echo ""
  exit 1
fi