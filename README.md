# proxy-base
一个基于 Netty 的轻量级 HTTPS 代理服务器。

## 核心功能
1. HTTP/HTTPS 代理
   - 支持 HTTP/HTTPS 协议
   - 支持 CONNECT 方法
   - 支持基本的身份认证（可配置）

2. 域名控制
   - 支持域名白名单配置
   - 默认支持常见 AI 服务域名

## 技术栈
- Spring Boot 2.7.x
- Netty 4.1.x
- Java 8

## 快速开始
1. 环境要求
   - JDK 8+
   - Maven 3.6+

2. 构建运行
   ```bash
   mvn clean package
   java -jar target/proxy-base-1.0-SNAPSHOT.jar
   ```

3. 配置代理
   - 代理地址：127.0.0.1
   - 代理端口：8080
   - 认证信息（可选）：
     - 用户名：admin
     - 密码：admin

## 配置说明
配置文件位于 `src/main/resources/application.yml`

主要配置项：
```yaml
proxy:
  port: 8080                # 代理服务器端口
  authentication: false     # 是否启用认证
  username: admin          # 认证用户名
  password: admin          # 认证密码
  enable-ssl: true         # 启用 SSL 支持
  allowed-hosts:           # 允许访问的域名
    - "*.cursor.sh"
    - "*.cursor.so"
    - "*.openai.com"
    # ... 其他域名
```

## 项目结构
```
src/main/java/com/tootoo/
├── TooTooApplication.java          # 应用入口
├── config/
│   └── ProxyConfig.java           # 配置类
└── proxy/
    ├── ProxyServer.java           # 代理服务器核心
    ├── frontend/                  # 前端处理器
    ├── backend/                   # 后端处理器
    ├── ssl/                      # SSL 处理
    └── util/                     # 工具类
```

## 版本说明
当前版本: 1.0.0-base
- 实现基本的代理功能
- 支持 HTTP/HTTPS
- 支持基本认证
- 支持域名白名单
