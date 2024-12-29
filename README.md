# too-too
一个功能强大的 HTTPS 代理服务器，专为创业公司设计。

## 项目功能
1. HTTPS 代理服务
   - 支持 HTTPS 协议代理
   - 支持基本的身份认证
   - 支持自定义代理规则

2. 流量监控与分析
   - 实时流量监控
   - 请求统计（按域名、时间段等维度）
   - 带宽使用统计
   - 响应时间统计

3. 数据可视化
   - 流量使用趋势图
   - 热门域名统计
   - 带宽使用报告
   - 异常请求分析

## 技术架构
### 后端技术栈
- Spring Boot 3.x
- Netty (用于代理服务器实现)
- Spring Data JPA (数据持久化)
- H2 Database (开发环境)
- MySQL (生产环境)

### 主要模块
1. proxy-core: 代理服务器核心模块
2. monitoring: 监控统计模块
3. analytics: 数据分析模块
4. web-api: REST API 接口模块

## 快速开始
1. 环境要求
   - JDK 8
   - Maven 3.6+
   - MySQL 8.0+ (生产环境)

2. 构建运行
   ```bash
   mvn clean install
   java -jar too-too-server/target/too-too-server.jar
   ```

3. 配置代理
   默认代理端口：8080
   默认管理界面：http://localhost:8081

## 配置说明
配置文件位于 `src/main/resources/application.yml` # proxy-base
