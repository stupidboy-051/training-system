#!/bin/bash

# 数据库连接检查脚本
# 用于验证远程数据库连接是否正常

# 默认配置
DB_HOST="localhost"
DB_PORT="3306"
DB_NAME="training_system"
DB_USER="root"
DB_PASS=""

# 读取配置文件中的数据库连接信息
if [ -f "../src/main/resources/application.properties" ]; then
    echo "正在读取数据库配置..."
    DB_HOST=$(grep -E '^spring.datasource.url=' ../src/main/resources/application.properties | sed 's/.*\/\/\([^:]*\).*/\1/')
    DB_PORT=$(grep -E '^spring.datasource.url=' ../src/main/resources/application.properties | sed 's/.*:\([0-9]*\).*/\1/')
    DB_NAME=$(grep -E '^spring.datasource.url=' ../src/main/resources/application.properties | sed 's/.*\/\([^?]*\).*/\1/')
    DB_USER=$(grep -E '^spring.datasource.username=' ../src/main/resources/application.properties | sed 's/spring.datasource.username=//')
    DB_PASS=$(grep -E '^spring.datasource.password=' ../src/main/resources/application.properties | sed 's/spring.datasource.password=//')
fi

echo "数据库连接信息:"
echo "主机: $DB_HOST"
echo "端口: $DB_PORT"
echo "数据库: $DB_NAME"
echo "用户: $DB_USER"

# 检查数据库连接
echo "正在测试数据库连接..."
mysql -h$DB_HOST -P$DB_PORT -u$DB_USER -p$DB_PASS -e "SELECT 1;" 2>/dev/null

if [ $? -eq 0 ]; then
    echo "✅ 数据库连接成功"
    
    # 检查数据库是否存在
    mysql -h$DB_HOST -P$DB_PORT -u$DB_USER -p$DB_PASS -e "USE $DB_NAME; SELECT '数据库存在' AS status;" 2>/dev/null
    
    if [ $? -eq 0 ]; then
        echo "✅ 数据库 '$DB_NAME' 存在"
        
        # 检查表是否存在
        TABLE_EXISTS=$(mysql -h$DB_HOST -P$DB_PORT -u$DB_USER -p$DB_PASS -N -e "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = '$DB_NAME' AND table_name = 'course_question_settings';" 2>/dev/null)
        
        if [ "$TABLE_EXISTS" -gt 0 ]; then
            echo "✅ 表 'course_question_settings' 已存在"
            
            # 显示表结构
            echo "表结构预览:"
            mysql -h$DB_HOST -P$DB_PORT -u$DB_USER -p$DB_PASS -e "USE $DB_NAME; DESCRIBE course_question_settings;" 2>/dev/null
            
            # 显示记录数
            COUNT=$(mysql -h$DB_HOST -P$DB_PORT -u$DB_USER -p$DB_PASS -N -e "USE $DB_NAME; SELECT COUNT(*) FROM course_question_settings;" 2>/dev/null)
            echo "当前记录数: $COUNT"
        else
            echo "⚠️ 表 'course_question_settings' 不存在，需要执行初始化脚本"
        fi
    else
        echo "❌ 数据库 '$DB_NAME' 不存在，需要创建数据库"
    fi
else
    echo "❌ 数据库连接失败"
    echo "请检查:"
    echo "1. 数据库服务器是否运行"
    echo "2. 防火墙是否允许连接"
    echo "3. 用户名密码是否正确"
    echo "4. 数据库用户是否有远程访问权限"
fi