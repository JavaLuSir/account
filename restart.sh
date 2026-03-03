#!/bin/bash

# 一键重启 Account 服务

APP_NAME="account-0.0.1.jar"
APP_DIR="/root/account"
LOG_FILE="$APP_DIR/app.log"

# 查找正在运行的Java进程
PID=$(ps aux | grep "$APP_NAME" | grep -v grep | awk '{print $2}')

if [ -n "$PID" ]; then
    echo "服务正在运行 (PID: $PID)，正在停止..."
    kill $PID
    sleep 2
fi

# 再次检查，确保已停止
PID=$(ps aux | grep "$APP_NAME" | grep -v grep | awk '{print $2}')
if [ -n "$PID" ]; then
    echo "强制停止服务..."
    kill -9 $PID
    sleep 1
fi

echo "启动服务..."
cd $APP_DIR
nohup java -jar target/$APP_NAME > $LOG_FILE 2>&1 &

# 等待服务启动
sleep 3

# 检查服务是否启动成功
if curl -s http://127.0.0.1:8080/account/rest/account > /dev/null 2>&1; then
    echo "服务启动成功！"
else
    echo "服务启动可能失败，请检查日志: tail -50 $LOG_FILE"
fi
