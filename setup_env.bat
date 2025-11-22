@echo off
REM ==========================================================
REM Team20 环境初始化脚本 (Windows) - Lab1
REM 对应 Linux/Mac 的 setup_team20_env.sh
REM 功能:
REM   - Git 行尾 & 用户检查
REM   - 创建 .editorconfig / .gitattributes
REM   - 检查 / 生成 Maven Wrapper
REM   - 创建 VSCode 设置
REM   - 可选生成 verify_env.bat
REM 环境变量:
REM   TEAM20_NON_INTERACTIVE=1  -> 跳过交互
REM   TEAM20_GIT_NAME / TEAM20_GIT_EMAIL -> 非交互下设置 Git 用户
REM ==========================================================

SETLOCAL ENABLEDELAYEDEXPANSION
SET "NON_INTERACTIVE=%TEAM20_NON_INTERACTIVE%"
IF "%NON_INTERACTIVE%"=="" SET "NON_INTERACTIVE=0"

ECHO.
ECHO ==========================================
ECHO   Team20 环境初始化 (Windows)
ECHO ==========================================
ECHO.

REM 1. Git 检查与配置
WHERE git >NUL 2>&1
IF ERRORLEVEL 1 (
  ECHO [ERROR] 未检测到 Git，请先安装 Git。
) ELSE (
  FOR /F "tokens=1-3" %%A IN ('git --version') DO SET "GIT_VER=%%C"
  ECHO [INFO] Git 已安装: %GIT_VER%
  REM 行尾符策略（Windows 推荐 true）
  git config --global core.autocrlf true >NUL 2>&1
  ECHO [OK] 行尾符策略 core.autocrlf=true

  FOR /F "usebackq delims=" %%A IN (`git config user.name 2^>NUL`) DO SET "GIT_NAME=%%A"
  FOR /F "usebackq delims=" %%A IN (`git config user.email 2^>NUL`) DO SET "GIT_EMAIL=%%A"

  IF "%GIT_NAME%"=="" (
    IF "%NON_INTERACTIVE%"=="1" (
      IF NOT "%TEAM20_GIT_NAME%"=="" IF NOT "%TEAM20_GIT_EMAIL%"=="" (
        git config --global user.name "%TEAM20_GIT_NAME%"
        git config --global user.email "%TEAM20_GIT_EMAIL%"
        ECHO [OK] Git 用户信息已通过环境变量设置
      ) ELSE (
        ECHO [WARN] 非交互模式下未提供 TEAM20_GIT_NAME / TEAM20_GIT_EMAIL
      )
    ) ELSE (
      ECHO [WARN] Git 用户信息未配置
      SET /P IN_NAME=请输入你的名字:
      SET /P IN_MAIL=请输入你的邮箱:
      git config --global user.name "%IN_NAME%"
      git config --global user.email "%IN_MAIL%"
      ECHO [OK] Git 用户信息已配置
    )
  ) ELSE (
    ECHO [OK] Git 用户: %GIT_NAME% ^<%GIT_EMAIL%^>
  )
)

ECHO.

REM 2. 创建 .editorconfig
IF NOT EXIST ".editorconfig" (
  > ".editorconfig" ECHO root = true
  >> ".editorconfig" ECHO.
  >> ".editorconfig" ECHO [*]
  >> ".editorconfig" ECHO charset = utf-8
  >> ".editorconfig" ECHO end_of_line = lf
  >> ".editorconfig" ECHO insert_final_newline = true
  >> ".editorconfig" ECHO trim_trailing_whitespace = true
  >> ".editorconfig" ECHO.
  >> ".editorconfig" ECHO [*.java]
  >> ".editorconfig" ECHO indent_style = space
  >> ".editorconfig" ECHO indent_size = 4
  >> ".editorconfig" ECHO max_line_length = 120
  >> ".editorconfig" ECHO.
  >> ".editorconfig" ECHO [*.{xml,json,yml,yaml}]
  >> ".editorconfig" ECHO indent_style = space
  >> ".editorconfig" ECHO indent_size = 2
  >> ".editorconfig" ECHO.
  >> ".editorconfig" ECHO [*.md]
  >> ".editorconfig" ECHO trim_trailing_whitespace = false
  >> ".editorconfig" ECHO.
  >> ".editorconfig" ECHO [*.{bat,cmd}]
  >> ".editorconfig" ECHO indent_style = space
  >> ".editorconfig" ECHO indent_size = 2
  >> ".editorconfig" ECHO end_of_line = crlf
  ECHO [OK] .editorconfig 已创建
) ELSE (
  ECHO [INFO] .editorconfig 已存在
)

ECHO.

REM 3. 创建 .gitattributes
IF NOT EXIST ".gitattributes" (
  > ".gitattributes" ECHO * text=auto
  >> ".gitattributes" ECHO *.java text eol=lf
  >> ".gitattributes" ECHO *.xml text eol=lf
  >> ".gitattributes" ECHO *.properties text eol=lf
  >> ".gitattributes" ECHO *.md text eol=lf
  >> ".gitattributes" ECHO *.sh text eol=lf
  >> ".gitattributes" ECHO *.json text eol=lf
  >> ".gitattributes" ECHO *.yml text eol=lf
  >> ".gitattributes" ECHO *.yaml text eol=lf
  >> ".gitattributes" ECHO *.bat text eol=crlf
  >> ".gitattributes" ECHO *.cmd text eol=crlf
  >> ".gitattributes" ECHO *.jar binary
  >> ".gitattributes" ECHO mvnw text eol=lf
  >> ".gitattributes" ECHO mvnw.cmd text eol=crlf
  ECHO [OK] .gitattributes 已创建
) ELSE (
  ECHO [INFO] .gitattributes 已存在
)

ECHO.

REM 4. 检查 / 生成 Maven Wrapper
IF NOT EXIST "mvnw.cmd" (
  WHERE mvn >NUL 2>&1
  IF ERRORLEVEL 1 (
    ECHO [ERROR] 未安装 Maven，且缺少 mvnw.cmd，无法生成 Wrapper
  ) ELSE (
    ECHO [INFO] 生成 Maven Wrapper...
    mvn -q -B wrapper:wrapper -Dmaven=3.9.9
    IF EXIST mvnw.cmd (
      ECHO [OK] Maven Wrapper 已生成
    ) ELSE (
      ECHO [ERROR] Maven Wrapper 生成失败
    )
  )
) ELSE (
  ECHO [OK] Maven Wrapper 已存在
)

ECHO.

REM 5. VSCode settings
IF NOT EXIST ".vscode" (
  MD ".vscode" >NUL 2>&1
)
IF NOT EXIST ".vscode\settings.json" (
  > ".vscode\settings.json" ECHO {
  >> ".vscode\settings.json" ECHO   "java.configuration.updateBuildConfiguration": "automatic",
  >> ".vscode\settings.json" ECHO   "maven.executable.preferMavenWrapper": true,
  >> ".vscode\settings.json" ECHO   "files.encoding": "utf8",
  >> ".vscode\settings.json" ECHO   "files.eol": "\n",
  >> ".vscode\settings.json" ECHO   "editor.formatOnSave": true,
  >> ".vscode\settings.json" ECHO   "editor.tabSize": 4,
  >> ".vscode\settings.json" ECHO   "editor.insertSpaces": true
  >> ".vscode\settings.json" ECHO }
  ECHO [OK] VSCode settings 已创建
) ELSE (
  ECHO [INFO] VSCode settings 已存在
)

ECHO.

REM 6. 创建 verify_env.bat（如果不存在）
IF NOT EXIST "verify_env.bat" (
  > verify_env.bat ECHO @echo off
  >> verify_env.bat ECHO REM 调用环境验证
  >> verify_env.bat ECHO IF EXIST setup_team20_env2.sh (
  >> verify_env.bat ECHO   bash setup_team20_env2.sh
  >> verify_env.bat ECHO ) ELSE (
  >> verify_env.bat ECHO   echo 缺少 setup_team20_env2.sh
  >> verify_env.bat ECHO )
  ECHO [OK] verify_env.bat 已创建
) ELSE (
  ECHO [INFO] verify_env.bat 已存在
)

ECHO.

REM 7. Java 版本提示
WHERE java >NUL 2>&1
IF ERRORLEVEL 1 (
  ECHO [WARN] 未检测到 Java (需要 JDK 17+)
) ELSE (
  FOR /F "usebackq tokens=2 delims==" %%J IN (`"wmic javahlp 2>nul"`) DO REM dummy suppress
  FOR /F "tokens=2 delims==" %%J IN ('"java -XshowSettings:properties -version 2>&1 | findstr java.version"') DO SET "JVER=%%J"
  IF NOT DEFINED JVER (
    FOR /F "tokens=*" %%J IN ('java -version 2^>^&1 ^| findstr /i "version"') DO SET "JVER=%%J"
  )
  ECHO [INFO] Java: %JVER%
)

ECHO.
ECHO ==========================================
ECHO   初始化完成
ECHO ==========================================
ECHO 下一步:
ECHO   1. verify_env.bat (或运行 bash setup_team20_env2.sh)
ECHO   2. build.bat run
ECHO   3. mvnw.cmd test
ECHO.

EXIT /B 0