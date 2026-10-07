# lele-inventory (Spring Boot)

一个用于代购“入库 -> 转运 -> 出库”的简易 Web 管理系统（手动录入）。

## 运行前准备

1. 安装并启动 MySQL。
2. 创建数据库：
   - `CREATE DATABASE lele_inventory;`
3. 修改 `src/main/resources/application.yml` 中的账号密码（默认 `root / change_me`）。

> 本项目使用 `spring.jpa.hibernate.ddl-auto=update`，启动时会自动创建/更新表结构。

## 启动

在项目根目录执行（需要本机已安装 Maven）：

```bash
mvn spring-boot:run
```

启动后访问：`http://localhost:8080`

## React 前端（可选）

如果你想用 React 独立前端：
1. 运行后端 `http://localhost:8080`
2. 进入 `frontend/` 目录安装并启动：
   ```bash
   cd frontend
   npm install
   npm run dev
   ```
3. 访问 `http://localhost:5173`

## 登录

默认内存账号：
- `admin / admin`

## 录入流程

1. 菜单里先建基础数据：`品类` -> `商品` -> `购买平台` -> `客户`
2. 创建 `入库单`：同时录入该单下的若干 `item`
3. 创建 `转运单`：页面只允许选择当前状态为 `INBOUND` 的 `item`
4. 创建 `出库单`：页面只允许选择当前状态为 `IN_TRANSITION` 的 `item`

后端对流转顺序做了校验：不满足顺序的 `itemIds` 会直接报错提示。

