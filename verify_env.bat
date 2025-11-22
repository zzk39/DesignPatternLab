@echo off
REM ==================================================
REM Team20 环境验证 (Windows 简化版)
REM 更详细验证可在 bash 中运行 setup_team20_env2.sh
REM ==================================================

SETLOCAL ENABLEDELAYEDEXPANSION
SET "ERRORS=0"
SET "WARNINGS=0"

ECHO.
ECHO ==========================================
ECHO   Team20 环境验证 (Windows)
ECHO ==========================================
ECHO.

REM 1. Java
ECHO [CHECK] Java
WHERE java >NUL 2>&1
IF ERRORLEVEL 1 (
  ECHO   ✗ 未检测到 Java
  SET /A ERRORS+=1
) ELSE (
  FOR /F "tokens=*" %%L IN ('java -version 2^>^&1 ^| findstr /i "version"') DO SET "JAVA_LINE=%%L"
  ECHO   ✓ %JAVA_LINE%
)

REM 2. Maven / Wrapper
ECHO [CHECK] Maven / Wrapper
IF EXIST mvnw.cmd (
  ECHO   ✓ mvnw.cmd 存在
) ELSE (
  WHERE mvn >NUL 2>&1
  IF ERRORLEVEL 1 (
    ECHO   ✗ 缺少 mvnw.cmd 且系统未安装 Maven
    SET /A ERRORS+=1
  ) ELSE (
    ECHO   ! 系统 Maven 存在，但缺少 mvnw.cmd (建议生成)
    SET /A WARNINGS+=1
  )
)

REM 3. Git
ECHO [CHECK] Git
WHERE git >NUL 2>&1
IF ERRORLEVEL 1 (
  ECHO   ✗ Git 未安装
  SET /A ERRORS+=1
) ELSE (
  FOR /F "tokens=1-3" %%A IN ('git --version') DO SET "GIT_V=%%C"
  ECHO   ✓ Git: %GIT_V%
  FOR /F "usebackq delims=" %%A IN (`git config user.name 2^>NUL`) DO SET "GU=%%A"
  FOR /F "usebackq delims=" %%A IN (`git config user.email 2^>NUL`) DO SET "GE=%%A"
  IF "%GU%"=="" (
    ECHO   ! Git 用户信息未配置
    SET /A WARNINGS+=1
  ) ELSE (
    ECHO   ✓ Git 用户: %GU% ^<%GE%^>
  )
)

REM 4. 项目关键文件
ECHO [CHECK] 项目文件
IF EXIST pom.xml (ECHO   ✓ pom.xml) ELSE (ECHO   ✗ pom.xml 缺失 & SET /A ERRORS+=1)
IF EXIST .editorconfig (ECHO   ✓ .editorconfig) ELSE (ECHO   ! 缺失 .editorconfig & SET /A WARNINGS+=1)
IF EXIST .gitattributes (ECHO   ✓ .gitattributes) ELSE (ECHO   ! 缺失 .gitattributes & SET /A WARNINGS+=1)
IF EXIST build.sh (ECHO   ✓ build.sh) ELSE (ECHO   ! 缺失 build.sh & SET /A WARNINGS+=1)
IF EXIST build.bat (ECHO   ✓ build.bat) ELSE (ECHO   ! 缺失 build.bat & SET /A WARNINGS+=1)

REM 5. workspace/logs
ECHO [CHECK] 工作区与日志
IF EXIST .workspace.state (ECHO   ✓ .workspace.state 存在) ELSE (ECHO   ! 尚未生成 .workspace.state)
FOR /F "delims=" %%L IN ('dir /b /a-d ".?.*.log" 2^>NUL') DO SET "HASLOG=1"
IF DEFINED HASLOG (
  ECHO   ✓ 检测到日志文件 (.filename.log)
) ELSE (
  ECHO   ! 未发现日志文件
)

REM 6. Devcontainer
ECHO [CHECK] DevContainer
IF EXIST ".devcontainer\devcontainer.json" (
  ECHO   ✓ devcontainer.json 存在
) ELSE (
  ECHO   ! 缺失 .devcontainer\devcontainer.json (推荐添加)
  SET /A WARNINGS+=1
)

ECHO.
ECHO ==========================================
ECHO   验证结果
ECHO ==========================================
ECHO 错误: %ERRORS%
ECHO 警告: %WARNINGS%
ECHO.

IF %ERRORS% EQU 0 (
  ECHO ✓ 环境验证通过
  ECHO 下一步:
  ECHO   build.bat run
  ECHO   mvnw.cmd test
  EXIT /B 0
) ELSE (
  ECHO ✗ 存在需要处理的错误，请修复后重试
  EXIT /B 1
)