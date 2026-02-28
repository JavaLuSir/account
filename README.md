# account - 家庭账本管理系统

一个简洁的移动端优先的家庭收支记账应用。

## 功能特性

- **账户管理** - 新增、删除账户，支持资产/负债分类
- **流水记录** - 记录收入和支出，按类别分组
- **数据统计** - 实时显示收入/支出汇总，饼图可视化
- **移动端适配** - 底部Tab导航，响应式布局

## 技术栈

- 后端：Spring Boot 2.3.4 + MySQL + Druid
- 前端：Bootstrap 5 + ECharts 5
- 移动端：原生CSS布局，底部Tab导航

## 快速开始

### 1. 环境要求

- JDK 1.8+
- Maven 3.6+
- MySQL 5.7+

### 2. 配置数据库

修改 `src/main/resources/application.yml` 中的数据库配置：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/account?useUnicode=true&characterEncoding=utf-8
    username: your_username
    password: your_password
```

### 3. 启动项目

```bash
mvn clean package
java -jar target/account-0.0.1.jar
```

访问 http://localhost:8080/account

## 页面功能

### 流水页面
- 饼图：按类别显示支出分布
- 收入/支出汇总：绿色显示收入，红色显示支出
- 筛选：按账户、日期范围筛选
- 操作：添加流水、删除流水

### 概览页面
- 总资产、总负债、净资产统计
- 账户列表管理

## API 接口

| 方法 | 路径 | 描述 |
|------|------|------|
| GET | /account/rest/account | 获取账户列表 |
| POST | /account/rest/addaccount | 添加账户 |
| POST | /account/rest/delaccount | 删除账户 |
| GET | /account/rest/waterall | 获取所有流水 |
| POST | /account/rest/adddetail | 添加流水 |
| POST | /account/rest/deldetail | 删除流水 |
| GET | /account/rest/category | 获取类别列表 |
| GET | /account/rest/categorystats | 类别统计 |

## 项目结构

```
account/
├── src/main/
│   ├── java/com/luxinx/
│   │   ├── bean/          # 实体类
│   │   ├── controller/    # 控制器
│   │   ├── service/       # 业务逻辑
│   │   └── util/         # 工具类
│   └── resources/
│       ├── static/        # 前端页面
│       └── application.yml
├── pom.xml
└── README.md
```

## License

MIT
