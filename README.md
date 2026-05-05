# 外卖订单系统 - 接口自动化测试项目

## 项目简介

针对外卖平台核心业务（下单、支付、取消、配送、完成）构建的完整工程，包含**后端服务**、**前端页面**和**接口自动化测试**三个子项目。测试工程替代手工回归测试，保障日常版本迭代的质量与效率。

## 项目结构

```
dianshang-test/
├── pom.xml                         # 后端服务 Maven 配置
├── src/                            # 后端服务源码
│   └── main/java/com/example/order/
│       ├── controller/             # REST 接口层
│       ├── service/impl/           # 业务逻辑层
│       ├── manager/impl/           # 通用处理层（幂等、库存）
│       ├── client/impl/            # 外部服务调用层（支付网关、推送）
│       ├── mapper/                 # 数据访问层（MyBatis Plus）
│       ├── model/
│       │   ├── dto/                # 请求参数对象
│       │   ├── vo/                 # 响应视图对象
│       │   ├── bo/                 # 业务传递对象
│       │   ├── entity/             # 数据库实体（DO）
│       │   └── enums/              # 枚举定义
│       ├── common/                 # 统一响应、异常、工具类
│       ├── config/                 # 全局配置
│       └── interceptor/            # 请求拦截器（链路追踪）
├── order-web/                      # 前端工程（Vue 3 + Element Plus）
│   ├── src/
│   │   ├── api/                    # API 请求封装
│   │   ├── views/                  # 页面组件
│   │   ├── router/                 # 路由配置
│   │   ├── store/                  # Pinia 状态管理
│   │   └── components/             # 通用组件
│   └── package.json
└── order-test/                     # 测试自动化工程
    ├── src/
    │   ├── main/java/.../config/   # RestAssured + WireMock + 数据源配置
    │   ├── main/java/.../client/   # API 客户端封装（@Step 注解）
    │   ├── main/java/.../data/     # 测试数据 Mapper
    │   └── test/java/.../
    │       ├── base/               # BaseTest 基类
    │       ├── order/              # 下单、取消、查询测试
    │       ├── payment/            # 支付、查询测试
    │       ├── idempotency/        # 幂等性测试
    │       └── flow/               # 状态流转全链路测试
    └── pom.xml
```

## 技术栈

### 后端服务
| 技术 | 版本 | 说明 |
|------|------|------|
| Spring Boot | 2.7.18 | 基础框架 |
| MyBatis Plus | 3.5.5 | ORM + 乐观锁 + 逻辑删除 + 自动填充 |
| H2 / MySQL | - | 开发用 H2 内存库，生产用 MySQL |
| Knife4j | 4.3.0 | Swagger 接口文档 |
| Lombok | - | 简化 POJO |

### 前端工程
| 技术 | 版本 | 说明 |
|------|------|------|
| Vue | 3.4 | 前端框架 |
| Vite | 5.2 | 构建工具 |
| Element Plus | 2.6 | UI 组件库 |
| Pinia | 2.1 | 状态管理 |
| Axios | 1.6 | HTTP 客户端 |

### 测试自动化
| 技术 | 版本 | 说明 |
|------|------|------|
| JUnit 5 | - | 测试框架 |
| RestAssured | 5.3.2 | HTTP 接口测试 |
| WireMock | 2.35.1 | 外部服务 Mock |
| MyBatis | 2.3.1 | 测试数据管理 |
| Allure | 2.24.0 | 可视化测试报告 |

## 核心业务

### 订单状态机

```
待支付(0) ──→ 已支付(1) ──→ 配送中(2) ──→ 已完成(3)
   │              │
   ▼              ▼
已取消(4)     已取消(4) + 自动退款
```

### API 接口

| 方法 | 路径 | 说明 | 请求头 |
|------|------|------|--------|
| POST | `/api/orders` | 创建订单 | X-User-Id, Idempotency-Key |
| GET | `/api/orders/{orderNo}` | 查询订单详情 | - |
| GET | `/api/orders/user/{userId}` | 查询用户订单列表 | - |
| POST | `/api/orders/cancel` | 取消订单 | X-User-Id |
| POST | `/api/orders/{orderNo}/deliver` | 开始配送 | - |
| POST | `/api/orders/{orderNo}/complete` | 确认送达 | - |
| POST | `/api/payments` | 订单支付 | X-User-Id, Idempotency-Key |
| GET | `/api/payments/order/{orderNo}` | 查询支付记录 | - |

### 企业级特性

- **幂等性**：`Idempotency-Key` 请求头 + `t_idempotency` 唯一约束去重，防止重复下单/支付
- **并发安全**：库存扣减使用 `SELECT FOR UPDATE` 行锁，订单状态流转使用 CAS + version 乐观锁
- **外部服务隔离**：`PaymentGatewayClient` / `PushNotificationClient` 接口抽象，测试环境用 WireMock Mock
- **链路追踪**：MDC traceId 贯穿请求全链路，响应体统一携带 traceId
- **统一异常体系**：`ResponseCode` 错误码枚举 + `BizException` + `GlobalExceptionHandler`
- **VO/BO/DO 分层**：接口层 VO、业务层 BO、数据层 DO 严格分离
- **Profile 分离**：`dev`（H2）/ `prod`（MySQL）环境配置隔离

## 快速启动

### 环境要求

- JDK 11+
- Maven 3.6+
- Node.js 16+（前端）

### 1. 启动后端服务

```bash
# 项目根目录
mvn clean spring-boot:run
```

启动后访问：
- 接口文档：http://localhost:8080/doc.html
- H2 控制台：http://localhost:8080/h2-console（JDBC URL: `jdbc:h2:mem:order_db`）

### 2. 启动前端

```bash
cd order-web
npm install
npm run dev
```

浏览器访问 http://localhost:5173

### 3. 运行测试

```bash
# 确保后端服务已启动

cd order-test
mvn clean test
```

### 4. 生成 Allure 测试报告

```bash
cd order-test
mvn allure:serve
```

## 测试用例覆盖

| 测试类 | 用例数 | 覆盖范围 |
|--------|--------|----------|
| CreateOrderTest | 8 | 正常下单、多商品、店铺关闭、商品下架、库存不足、参数校验、用户不存在、商品不属于店铺 |
| CancelOrderTest | 7 | 取消待支付/已支付/配送中/已完成、重复取消、非本人操作、订单不存在 |
| OrderQueryTest | 6 | 订单号查询、不存在查询、用户列表、空列表、用户不存在、已取消详情 |
| PayOrderTest | 8 | 余额/微信/支付宝支付、余额不足、重复支付、已取消支付、订单不存在、非本人 |
| PaymentQueryTest | 3 | 支付记录查询、不存在、已退款状态 |
| IdempotencyTest | 4 | 下单幂等、支付幂等、不同键不同订单、无键多次创建 |
| OrderStatusFlowTest | 7 | 完整生命周期、退款余额恢复、非法状态流转（4项）、库存恢复 |
| **合计** | **43** | 覆盖正逆向全流程 |

## 数据库表结构

| 表名 | 说明 |
|------|------|
| t_user | 用户表（余额、version 乐观锁） |
| t_shop | 店铺表（营业状态） |
| t_product | 商品表（库存、version 乐观锁、上下架） |
| t_order | 订单表（状态机、version 乐观锁、逻辑删除） |
| t_order_item | 订单明细表 |
| t_payment | 支付表（支付方式、第三方流水号、逻辑删除） |
| t_idempotency | 幂等记录表（唯一约束去重） |

## 测试数据

系统启动时自动初始化：

| 数据 | 说明 |
|------|------|
| 用户 ID=1 张三 | 余额 1000.00，正常用户 |
| 用户 ID=2 李四 | 余额 200.00 |
| 用户 ID=3 | 余额 0.50，余额不足测试用户 |
| 店铺 ID=1 好味道餐厅 | 营业中 |
| 店铺 ID=2 休息中餐厅 | 已休息 |
| 商品 ID=1 宫保鸡丁 | ¥28，库存 100 |
| 商品 ID=2 鱼香肉丝 | ¥25，库存 50 |
| 商品 ID=4 库存不足菜品 | ¥30，库存 0 |
| 商品 ID=5 已下架菜品 | 已下架 |

## WireMock 配置

测试运行时自动启动两个 WireMock 服务：

| 服务 | 端口 | Mock 接口 |
|------|------|-----------|
| 支付网关 | 9090 | `POST /api/v1/pay` → 返回 transactionId |
| 支付网关 | 9090 | `POST /api/v1/refund` → 返回 refundId |
| 推送服务 | 9091 | `POST /api/v1/push` → 200 OK |
