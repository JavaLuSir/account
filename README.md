# account - 家庭账本管理系统

一个简单的 Web 端账本应用，支持账户管理、流水记录和定时提醒功能。

## 功能特性

- **账户管理** - 新增、修改、删除账户
- **流水记录** - 记录收入和支出
- **资产统计** - 查看总资产和总负债
- **定时提醒** - 定时推送账目提醒
- **移动端适配** - 支持手机浏览器访问

## 技术栈

- Spring Boot 2.3.4
- MySQL 数据库
- Druid 数据库连接池
- 前端：MUI + jQuery + ECharts

## 快速开始

### 1. 环境要求

- JDK 1.8+
- Maven 3.6+
- MySQL 5.7+

### 2. 配置数据库

在 MySQL 中创建数据库并导入 SQL 脚本：

```sql
CREATE DATABASE account DEFAULT CHARACTER SET utf8mb4;
-- 导入项目中的 sql 脚本
```

修改 `src/main/resources/application.yml` 中的数据库配置：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/account?useUnicode=true&characterEncoding=utf-8
    username: your_username
    password: your_password
```

### 3. 配置登录信息

修改 `src/main/resources/application.properties`：

```properties
account.username=admin
account.password=123456
```

### 4. 启动项目

```bash
mvn spring-boot:run
```

访问 http://localhost:8080/account

## 项目结构

```
account/
├── src/main/
│   ├── java/com/luxinx/
│   │   ├── account/       # 启动类
│   │   ├── bean/          # 实体类
│   │   ├── config/        # 配置类
│   │   ├── controller/    # 控制器
│   │   ├── cron/          # 定时任务
│   │   ├── dao/           # 数据访问层
│   │   ├── service/       # 业务逻辑
│   │   └── util/          # 工具类
│   └── resources/
│       ├── static/        # 静态资源
│       ├── application.yml
│       └── application.properties
├── pom.xml
└── README.md
```

## API 接口

| 方法 | 路径 | 描述 |
|------|------|------|
| GET/POST | /account/list | 账户列表 |
| GET/POST | /account/add | 添加账户 |
| GET/POST | /account/update | 更新账户 |
| GET/POST | /account/delete | 删除账户 |
| GET/POST | /water/list | 流水列表 |
| GET/POST | /water/add | 添加流水 |
| GET/POST | /water/delete | 删除流水 |

## 页面说明

- `index.html` - 主页面（资产概览、账户管理、流水记录）
- `test.html` - 测试页面

##  License

MIT
