# 小R同学伙伴匹配系统 (Rynq Partner Matching)

一个基于 Spring Boot + Vue3 的前后端分离的伙伴匹配系统。用户可以注册登录、发布标签、寻找相似伙伴、加入队伍，并支持 WebSocket 实时私聊。

## 🛠 技术栈

### 后端 (Minded-Backend)
- **核心框架**：Java 17 / Spring Boot 2.6
- **数据访问**：MyBatis-Plus / MySQL 8
- **缓存与并发**：Redis / Redisson (分布式锁)
- **实时通信**：WebSocket
- **其他**：Spring AOP / EasyExcel / 阿里云 OSS

### 前端 (Minded-frontend)
- **核心框架**：Vue 3 / Vite / TypeScript
- **UI 组件库**：Vant UI
- **网络请求**：Axios

## 🚀 本地运行指南

### 1. 后端运行
1. 导入 `Minded-Backend` 到 IDEA。
2. 数据库：创建 MySQL 数据库 `minded`，并导入表结构和基础数据。
3. 配置：复制 `application-example.yml` 为 `application.yml`，填入你的本地数据库密码、Redis 密码以及阿里云 OSS / 163邮箱的配置。
4. 运行 `MindedApplication` 启动类。

### 2. 前端运行
1. 进入 `Minded-frontend` 目录。
2. 安装依赖：`npm install`
3. 启动项目：`npm run dev`
4. 访问：`http://localhost:5173`

## 🔗 接口文档
后端启动成功后，访问 Knife4j/Swagger 接口文档：
`http://localhost:8080/api/doc.html`
