@echo off
chcp 65001 >nul
setlocal enabledelayedexpansion

echo 数据库连接检查脚本
echo 用于验证远程数据库连接是否正常
echo.

:: 默认配置
set DB_HOST=localhost
set DB_PORT=3306
set DB_NAME=training_system
set DB_USER=root
set DB_PASS=

:: 读取配置文件中的数据库连接信息
if exist "..\src\main\resources\application.properties" (
    echo 正在读取数据库配置...
    
    for /f "tokens=1,2 delims==" %%a in ('findstr "spring.datasource.url" ..\src\main\resources\application.properties') do (
        set URL=%%b
        set URL=!URL:jdbc:mysql://=!
        for /f "tokens=1,2 delims=/" %%c in ("!URL!") do (
            for /f "tokens=1,2 delims=:" %%e in ("%%c") do (
                set DB_HOST=%%e
                set DB_PORT=%%f
            )
            set DB_NAME=%%d
        )
    )
    
    for /f "tokens=1,2 delims==" %%a in ('findstr "spring.datasource.username" ..\src\main\resources\application.properties') do (
        set DB_USER=%%b
    )
    
    for /f "tokens=1,2 delims==" %%a in ('findstr "spring.datasource.password" ..\src\main\resources\application.properties') do (
        set DB_PASS=%%b
    )
)

echo 数据库连接信息:
echo 主机: %DB_HOST%
echo 端口: %DB_PORT%
echo 数据库: %DB_NAME%
echo 用户: %DB_USER%
echo.

:: 检查数据库连接
echo 正在测试数据库连接...
mysql -h%DB_HOST% -P%DB_PORT% -u%DB_USER% -p%DB_PASS% -e "SELECT 1;" 2>nul

if %errorlevel% equ 0 (
    echo ✅ 数据库连接成功
    
    :: 检查数据库是否存在
    mysql -h%DB_HOST% -P%DB_PORT% -u%DB_USER% -p%DB_PASS% -e "USE %DB_NAME%; SELECT '数据库存在' AS status;" 2>nul
    
    if %errorlevel% equ 0 (
        echo ✅ 数据库 '%DB_NAME%' 存在
        
        :: 检查表是否存在
        mysql -h%DB_HOST% -P%DB_PORT% -u%DB_USER% -p%DB_PASS% -N -e "USE %DB_NAME%; SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = '%DB_NAME%' AND table_name = 'course_question_settings';" 2>nul > temp_result.txt
        set /p TABLE_EXISTS=<temp_result.txt
        del temp_result.txt
        
        if "%TABLE_EXISTS%" gtr "0" (
            echo ✅ 表 'course_question_settings' 已存在
            
            :: 显示表结构
            echo.
            echo 表结构预览:
            mysql -h%DB_HOST% -P%DB_PORT% -u%DB_USER% -p%DB_PASS% -e "USE %DB_NAME%; DESCRIBE course_question_settings;" 2>nul
            
            :: 显示记录数
            mysql -h%DB_HOST% -P%DB_PORT% -u%DB_USER% -p%DB_PASS% -N -e "USE %DB_NAME%; SELECT COUNT(*) FROM course_question_settings;" 2>nul > temp_result.txt
            set /p COUNT=<temp_result.txt
            del temp_result.txt
            echo.
            echo 当前记录数: %COUNT%
        ) else (
            echo ⚠️ 表 'course_question_settings' 不存在，需要执行初始化脚本
        )
    ) else (
        echo ❌ 数据库 '%DB_NAME%' 不存在，需要创建数据库
    )
) else (
    echo ❌ 数据库连接失败
    echo 请检查:
    echo 1. 数据库服务器是否运行
    echo 2. 防火墙是否允许连接
    echo 3. 用户名密码是否正确
    echo 4. 数据库用户是否有远程访问权限
)

pause