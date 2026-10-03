# Mall Backend

一个基于 Spring Boot + MySQL + MyBatis + Redis 的电商后台，包含用户、商品、订单三个模块。

项目目标是练习后端分层、数据库事务、并发扣库存、异常处理和缓存设计，不追求功能数量，追求每一条链路都跑通、能讲清楚。

## 技术栈

- Java 21
- Spring Boot 4.1.1
- MySQL 9.5
- MyBatis 4.1.0
- Redis 7.x
- Maven

## 功能

### 用户模块
- 查询所有商品
- 按 ID 查询商品（带 Redis 缓存）
- 新增商品
- 修改商品（修改后清除缓存）
- 删除商品（删除后清除缓存）

### 商品模块
- 查询所有商品
- 按 ID 查询商品
- 新增商品
- 修改商品
- 删除商品

### 订单模块
- 下单（支持一次购买多个商品，事务保证原子性）
- 查询所有订单
- 按 ID 查询订单
- 查询某订单的所有订单项

## 项目结构
```text
src/main/java/com/example/mallbackend/
├── controller/ 接口层
├── service/ 业务逻辑层
├── mapper/ 数据访问层（MyBatis）
├── entity/ 数据库实体
├── dto/ 请求/响应对象
└── exception/ 业务异常与全局异常处理
```

## 快速开始

### 1. 建库建表

```sql
CREATE DATABASE IF NOT EXISTS mall DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE mall;

CREATE TABLE IF NOT EXISTS `user` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `username` VARCHAR(50) NOT NULL UNIQUE,
    `password` VARCHAR(100) NOT NULL,
    `phone` VARCHAR(20),
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `product` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `name` VARCHAR(100) NOT NULL,
    `price` DECIMAL(10,2) NOT NULL,
    `stock` INT NOT NULL DEFAULT 0,
    `description` VARCHAR(500),
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `orders` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `user_id` BIGINT NOT NULL,
    `total_price` DECIMAL(10,2) NOT NULL,
    `status` VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `order_item` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `order_id` BIGINT NOT NULL,
    `product_id` BIGINT NOT NULL,
    `product_name` VARCHAR(100) NOT NULL,
    `quantity` INT NOT NULL,
    `price` DECIMAL(10,2) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

### 2.启动 Redis

项目默认连接 ```localhost:6379```，如需修改，改 ```application.properties``` 里的 ```spring.data.redis.host``` 和 ```spring.data.redis.port```。

### 3.配置数据库

复制 ```application.properties.example``` 为 ```application.properties```填入自己的数据库密码：

```text
spring.datasource.url=jdbc:mysql://localhost:3306/mall?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true
spring.datasource.username=root
spring.datasource.password=你的密码
```

### 4.启动

```bash
mvn spring-boot:run
```

或直接在 IDEA 里运行 ```MallBackendApplication```

## 接口示例

### 新增用户

```text
POST /users
Content-Type: application/json

{
    "username": "testuser",
    "password": "abc123",
    "phone": "13900000000"
}
```

### 新增商品

```text
POST /products
Content-Type: application/json

{
    "name": "机械键盘",
    "price": "299.00",
    "stock": 100,
    "description": "87键 青轴 白色背光"
}
```

### 查询商品（带缓存）

```text
GET /products/1
```

### 下单

```text
POST /orders
Content-Type: application/json

{
    "userId": 1,
    "items": [
        {"productId": 1, "quantity": 2},
        {"productId": 3, "quantity": 1}
    ]
}
```

返回:

```json
{
    "id": 1,
    "userId": 1,
    "totalPrice": 687.50,
    "status": "PENDING",
    "createTime": "2026-10-01T18:59:51"
}
```

### 查询订单项

```text
GET /orders/1/items
```

## 核心实现

### 1.分层结构

```text
Controller → Service → Mapper
```

- Controller 只负责接收请求、返回响应;
- Service 承载业务逻辑和事务边界;
- Mapper 只负责单表 SQL。

### 2.事务与原子扣库存

下单流程包含多步操作：校验用户、校验商品、计算总价、插入订单、插入订单项、扣减库存。

整个流程用 @Transactional 包裹，任何一步失败都抛出异常，由 Spring 回滚全部操作，不会出现“订单建了但库存没扣”或“库存扣了但订单没建”的中间状态。

扣库存不用“先查再改”，而是用一条原子 SQL:

```sql
UPDATE product SET stock = stock - #{quantity}
WHERE id = #{id} AND stock >= #{quantity}
```

- 检查和扣减在同一条 SQL 内完成;
- WHERE stock >= quantity 保证库存不足时更新 0 行;
- 靠数据库行锁防止并发超卖，而不是靠应用层"查一下库存够不够"。

### 3.订单项快照

```order_item``` 表除了 ```product_id```, 还保存了下单时的 ```product_name``` 和 ```price```。

商品后续改名、调价、下架，都不影响历史订单的显示。订单是历史事实，必须记录成交时的值。

### 4.全局异常处理

自定义业务异常继承 ```BusinessException```，由 ```GlobalExceptionHandler``` 统一捕获，转成对应的 HTTP 状态码和 JSON:

```json
{
    "code": 404,
    "message": "商品 2 不存在"
}
```

- 用户/商品/订单不存在 → 404
- 库存不足 → 400
- 其他业务异常 → 500

### 5.Redis 缓存

商品详情使用 Redis 缓存，采用 Cache Aside 模式：先更新数据库，再删除缓存。

解决了缓存的四个经典问题：

缓存穿透：查询不存在的 id 时，MySQL 查不到，缓存 "null" 字符串，设短过期时间（2~4 分钟），避免同一无效 id 反复打到数据库。

缓存击穿：热点 key 失效瞬间，大量请求同时打到数据库。使用按 key 加锁 + 双重检查解决：

- 每个 key 对应一个 ReentrantLock，存在 ConcurrentHashMap 中；
- 未命中的请求先抢锁，只有一个线程去查库；
- 拿到锁后再次检查缓存（双重检查），防止等锁期间其他线程已写回；
- 其他线程等锁释放后重新查缓存，命中直接返回。

缓存雪崩：过期时间加随机偏移（10 + [0,5] 分钟），避免大量 key 同时失效。

缓存一致性：修改或删除商品后，主动删除对应的 Redis key，下次查询重新从数据库加载。

## 已知限制

1. 未实现登录鉴权，接口可直接访问；
2. 订单状态目前只写入 PENDING，未实现状态流转；
3. 未实现分页，列表接口一次返回全部数据；
4. 未实现商品搜索、订单筛选等查询功能。
5. 锁池未做清理，商品数量有界时可接受，无限增长的 key 场景应改用固定分片锁（如 Guava Striped）或引用计数方案；
6. 未实现单元测试与集成测试。

## License

仅用于个人学习，无商业用途。