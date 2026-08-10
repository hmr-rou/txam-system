# 大学英语四级考试成绩管理系统

## 项目概述

基于 Spring Boot + Vue 3 的 CET-4 成绩管理系统，支持管理员对学生成绩的增删改查、Excel 批量导入导出、多条件检索，以及学生自助查分。采用前后端分离架构，兼具 Thymeleaf 服务端渲染方案作为降级适配。

## 核心贡献

### 后端

**项目架构搭建**
- 从零搭建 **Spring Boot + MyBatis-Plus** 项目骨架：分层架构（Controller → Service → Mapper）、统一配置、拦截器注册

**密码安全设计**
- 设计 **SHA-256 + 随机盐** 密码方案，兼容旧系统无 salt 明文密码的平滑升级：`login()` 依次尝试含盐查询 → 无盐降级查询 → 明文比对

**数据查询与批量写入优化**
- 使用 **LambdaQueryWrapper** 构建 6 字段动态组合模糊查询，避免硬编码 SQL 拼接
- 自定义 **批量插入 SQL**（`<foreach>` 动态 SQL）将 Excel 导入性能从逐条 insert 优化为单条 SQL

**Excel 导入导出闭环**
- 基于 **Apache POI** 实现完整流程：模板下载（含示例行）→ 导入校验（类型/范围/必填/日期格式）→ 错误行反馈 → 导出支持按查询条件过滤

**权限控制**
- 自定义 **Spring Interceptor** 实现双角色权限控制：区分普通请求与 AJAX 请求（`X-Requested-With` + `Sec-Fetch-Mode`），后者返回 JSON 而非重定向

### 前端

**技术演进**
- 主导从 **Thymeleaf SSR → Vue 3 SPA** 的技术演进：将 570 行混合 Java 脚本的 JSP 重构为 220 行 Vue 单文件组件，代码量减少 60%

**状态管理**
- 使用 **Vue 3 Composition API**（`ref` / `reactive` / `onMounted`）管理组件状态，替代 Thymeleaf 中散落在 DOM 和脚本中的隐式状态

**组件化与 UI 标准化**
- 引入 **Element Plus** 组件库标准化 UI（`el-table` 带 loading/分页、`el-dialog` 模态交互、`el-upload` 拖拽上传、`el-date-picker` 日期选择），替换手写的 550 行 CSS + HTML

**请求层与路由层**
- 实现 **Axios 拦截器** 统一处理 401/403 错误（自动跳转登录页）和网络异常提示
- **Vue Router 导航守卫** 根据 `sessionStorage` 中的角色信息拦截未授权访问

**前后端协作**
- 后端新增 **REST API**（`/api/login`、`/api/admin/scores`、`/api/student/scores` 等），前端通过 Vite proxy 代理跨域请求

### 测试

**测试总览**
- 编写 **27 个单元测试**（JUnit 5 + Mockito + MockMvc），覆盖 Controller / Service / Util 三层，BUILD SUCCESS

**PasswordUtils — 加密切底验证**
- 7 个测试覆盖正常/异常/边界：盐随机性、哈希确定性、正确验证、错误密码拒绝、错误盐拒绝

**UserService — 核心业务路径**
- 6 个测试覆盖：登录成功/失败、用户不存在、无 salt 降级、改密成功/失败
- 使用真实 PasswordUtils 而非 mock static，保证加密逻辑在测试中真实执行

**Cet4ScoreService — CRUD 全链路**
- 8 个测试覆盖：新增/查询/更新/删除/批量导入，通过 Mockito 隔离 Mapper 层

**LoginController — Web 层验证**
- 6 个测试覆盖：视图渲染、角色重定向、错误反馈、登出
- 使用 Standalone MockMvc 避免加载完整 Spring 上下文

### 技术栈

| 层级 | 技术 |
|------|------|
| 后端框架 | Spring Boot 2.7.18 |
| ORM | MyBatis-Plus 3.5.5 |
| 数据库 | MySQL 5.7+ |
| 前端（主方案） | Vue 3 + Vite + Element Plus + Vue Router + Axios |
| 前端（备方案） | Thymeleaf + 原生 JS + CSS |
| 文件处理 | Apache POI 5.2.5（Excel 读写） |
| 安全 | SHA-256 + 随机盐密码哈希 |
| 测试 | JUnit 5 + Mockito + MockMvc |
| 构建 | Maven（后端）/ npm（前端） |

### 功能模块

```
├── 登录模块
│   ├── 身份证号 + 密码登录
│   └── 角色区分（管理员 / 学生）→ 不同主页
│
├── 管理员端
│   ├── 成绩 CRUD（录入 / 修改 / 删除）
│   ├── 多条件组合查询（身份证号、准考证号、学校、学院、专业、班级）
│   ├── Excel 批量导入（模板下载、数据校验、错误行反馈）
│   ├── Excel 导出（支持按查询条件导出）
│   ├── 自动同步学生账号
│   └── 修改密码
│
├── 学生端
│   ├── 查看个人成绩单（按考试时间倒序）
│   └── 修改密码
│
└── 安全机制
    ├── Spring Interceptor 登录拦截 + 角色校验
    ├── AJAX 请求返回 JSON 错误（不跳转）
    ├── SHA-256 + Salt 密码加密
    └── 兼容旧数据（无 salt）的平滑升级
```

---

## 项目结构

```
txam/
├── pom.xml
├── README.md
├── src/
│   ├── main/java/hmr/
│   │   ├── Application.java              # Spring Boot 入口
│   │   ├── config/
│   │   │   └── WebConfig.java            # 拦截器配置
│   │   ├── interceptor/
│   │   │   └── LoginInterceptor.java     # 登录 + 角色拦截
│   │   ├── controller/
│   │   │   ├── LoginController.java      # 登录 / 登出 / 用户信息 API
│   │   │   ├── AdminController.java      # 管理员 CRUD + 导入导出
│   │   │   └── StudentController.java    # 学生查分
│   │   ├── service/
│   │   │   ├── UserService.java          # 用户认证 / 密码管理
│   │   │   └── Cet4ScoreService.java     # 成绩业务逻辑
│   │   ├── mapper/
│   │   │   ├── UserMapper.java           # 用户 SQL（含密码更新）
│   │   │   └── Cet4ScoreMapper.java      # 成绩 SQL（含批量插入）
│   │   ├── javabean/
│   │   │   ├── User.java                 # 用户实体
│   │   │   └── Cet4Score.java            # 成绩实体
│   │   └── utils/
│   │       ├── PasswordUtils.java        # SHA-256 + Salt 工具
│   │       └── ExcelUtil.java            # Excel 导入导出工具
│   ├── main/resources/
│   │   ├── application.yml               # 数据库 / Thymeleaf / 文件上传配置
│   │   ├── templates/                    # Thymeleaf 模板（备选方案）
│   │   │   ├── login.html
│   │   │   ├── admin_home.html
│   │   │   └── student_home.html
│   │   └── static/css/common.css         # 公共样式
│   └── test/java/hmr/
│       ├── utils/PasswordUtilsTest.java  # 密码工具测试（7 cases）
│       ├── service/
│       │   ├── UserServiceTest.java      # 用户服务测试（6 cases）
│       │   └── Cet4ScoreServiceTest.java # 成绩服务测试（8 cases）
│       └── controller/
│           └── LoginControllerTest.java  # 登录控制器测试（6 cases）

txam-frontend/                              # Vue 3 前端（主方案）
├── package.json
├── vite.config.js                          # Vite 配置 + 后端代理
├── index.html
└── src/
    ├── main.js                             # 入口：挂载 Element Plus + Router
    ├── App.vue                             # 根组件 <router-view />
    ├── api/index.js                        # Axios 实例 + 响应拦截器
    ├── router/index.js                     # 路由表 + 导航守卫
    └── views/
        ├── Login.vue                       # 登录页
        ├── AdminHome.vue                   # 管理员主页
        └── StudentHome.vue                 # 学生主页
```

---

## 后端

### 数据库表结构

```sql
-- 用户表
CREATE TABLE user (
    id INT AUTO_INCREMENT PRIMARY KEY,
    id_card_number VARCHAR(18) UNIQUE NOT NULL,
    name VARCHAR(50) NOT NULL,
    password VARCHAR(255) NOT NULL,
    salt VARCHAR(64),
    role VARCHAR(20) DEFAULT 'student'
);

-- 成绩表
CREATE TABLE cet4_score (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    school VARCHAR(100),
    college VARCHAR(100),
    major VARCHAR(100),
    class_name VARCHAR(50),
    id_card_number VARCHAR(18) NOT NULL,
    admission_no VARCHAR(50),
    score DECIMAL(5,1),
    exam_time DATE
);
```

### API 文档

#### 页面路由（Thymeleaf SSR）

| 方法 | URL | 返回 | 说明 |
|------|-----|------|------|
| GET | `/` `/login` | HTML | 登录页 |
| GET | `/admin/home` | HTML | 管理员主页（需 admin） |
| GET | `/student/home` | HTML | 学生主页（需登录） |

#### REST API（Vue 3 SPA）

| 方法 | URL | 返回 | 说明 |
|------|-----|------|------|
| POST | `/api/login` | JSON | 登录：`{success, user: {name, role, idCardNumber}}` |
| GET | `/api/currentUser` | JSON | 获取当前会话用户 |
| GET | `/api/admin/scores` | JSON Array | 全部成绩列表 |
| GET | `/api/admin/scores/search?…` | JSON Array | 多条件查询（6 个可选参数） |
| GET | `/api/student/scores` | JSON Array | 当前学生的成绩 |
| POST | `/admin/save` | JSON | 新增/修改成绩（form-urlencoded） |
| GET | `/admin/getScore?id=` | JSON | 获取单条成绩 |
| GET | `/admin/delete?id=` | 302 | 删除成绩 |
| POST | `/admin/import` | JSON | 批量导入（multipart/form-data） |
| GET | `/admin/export?…` | File | 导出 Excel |
| GET | `/admin/downloadTemplate` | File | 下载导入模板 |
| POST | `/admin/changePassword` | 302 | 管理员改密 |
| POST | `/student/changePassword` | 302 | 学生改密 |
| GET | `/logout` | 302 | 退出登录 |

### 核心设计

**密码安全（PasswordUtils.java）**

```
注册/改密：用户密码 + 随机16字节盐 → SHA-256 → 存储 hash + salt
登录验证：输入密码 + 取出 salt → SHA-256 → 比对存储的 hash
兼容旧数据：salt 为 null 时降级为明文比对
```

**登录兼容（UserService.login）**

```
1. 先查含 salt 列 → 成功 → SHA-256 验证
2. 查不到 → 降级查无 salt 列 → 明文比对
3. 改密时尝试写 salt 列 → 失败则降级写明文
```

**批量导入优化（Cet4ScoreMapper.batchInsert）**

```xml
<!-- 单条 SQL 完成批量插入，性能远优于逐条 insert -->
INSERT INTO cet4_score (...) VALUES
  (?, ?, ?, ...),
  (?, ?, ?, ...),
  ...
```

**拦截器（LoginInterceptor）**

```
/admin/** → 未登录 → 跳转 /login
/admin/** → 角色非 admin → AJAX 返回 JSON 403 / 普通请求跳转
/student/** → 未登录 → 跳转 /login
```

---

## 前端

### 项目背景

本项目为一个典型的 **B 端管理后台**，面向两类用户——管理员（成绩管理）和学生（查分）。核心交互模式为"**列表 + 弹窗**"：主页面以表格呈现数据，增/改/导入等操作通过模态弹窗完成，避免页面跳转带来的上下文丢失。

前端经历了两次迭代：

| 版本 | 技术方案 | 特点 |
|------|---------|------|
| v1 | Thymeleaf SSR + 原生 JS + 自研 CSS | 服务端渲染，零前端依赖，兼容性好 |
| v2 | Vue 3 SPA + Element Plus | 前后端分离，组件化，开发效率高 |

**v1 → v2 的演进动因**：

- Thymeleaf 模板中混合了大量 Java 脚本（`<% %>`）和 JSTL 标签，业务逻辑与视图耦合，维护成本随页面复杂度上升
- 手写的模态框、表格、表单校验代码重复度高，新增一个弹窗需要从头写 HTML + CSS + JS
- 前端状态散落在 DOM 和全局变量中，搜索条件、选中行、弹窗显隐等状态管理混乱
- 交互反馈依赖 `alert()` 和整页刷新，用户体验不够流畅

迁移到 Vue 3 后，核心变化：

| 维度 | v1（Thymeleaf） | v2（Vue 3 + Element Plus） |
|------|----------------|---------------------------|
| 渲染模式 | 服务端渲染，每次操作整页刷新 | 客户端渲染，数据通过 API 异步加载 |
| 状态管理 | 无，DOM 即状态 | `ref` / `reactive` 响应式数据 |
| UI 组件 | 手写 CSS + HTML（~550 行） | Element Plus 组件库（`el-table` / `el-dialog` / `el-upload` 等） |
| 表单校验 | 后端校验 + `alert()` 提示 | 前端即时校验 + `ElMessage` 反馈 |
| 路由 | 后端 Controller 跳转 | Vue Router 前端路由 + 导航守卫 |
| 代码量 | admin_home.jsp ~570 行 | AdminHome.vue ~220 行 |
| 可复用性 | 低，样式散落各处 | 高，组件可插拔 |

### Vue 3 方案（`txam-frontend/`）

**路由设计**

| 路径 | 组件 | 守卫 |
|------|------|------|
| `/login` | Login.vue | 无需登录 |
| `/admin/home` | AdminHome.vue | role = admin |
| `/student/home` | StudentHome.vue | 已登录 |

**组件树**

```
App.vue
└── <router-view>
    ├── Login.vue
    │   └── el-form + el-input + el-button
    │
    ├── AdminHome.vue
    │   ├── el-header（导航栏）
    │   ├── el-button 操作栏（录入/查询/导入/导出）
    │   ├── el-form（多条件搜索面板，展开/收起）
    │   ├── el-table（成绩列表，loading/分页）
    │   ├── el-dialog（录入/编辑成绩）
    │   ├── el-dialog（批量导入 + el-upload）
    │   └── el-dialog（修改密码）
    │
    └── StudentHome.vue
        ├── el-header
        ├── el-card + el-table（成绩单）
        ├── el-empty（空状态）
        └── el-dialog（修改密码）
```

**关键实现**

| 功能 | 实现方式 |
|------|---------|
| 全局错误处理 | Axios 响应拦截器：401 → 跳转登录，网络异常 → 统一提示 |
| 路由守卫 | `router.beforeEach` 检查 `sessionStorage.user` 是否存在、角色是否匹配 |
| 登录流程 | `POST /api/login` → 存入 `sessionStorage` → Vue Router 跳转 |
| 搜索面板 | `v-show` 控制显隐，`reactive` 绑定 6 个字段，查询/重置两个操作 |
| 成绩编辑 | `openEdit(id)` → `GET /admin/getScore?id=` → 数据回填 `el-dialog` |
| 批量导入 | `el-upload` 选择文件 → `FormData` → `POST /admin/import` → 展示成功数/错误行 |
| 导出 Excel | 构建查询参数 → `window.open('/admin/export?' + params)` |
| 成绩等级 | `el-tag` + 三元表达式：≥ 425 绿色（通过），< 425 红色（未通过） |
| 跨域 | Vite `proxy` 将 `/admin/*` `/login` `/api/*` 转发至 `localhost:8080` |

### Thymeleaf 方案（备选，`src/main/resources/templates/`）

用于不支持 JavaScript 的场景或降级方案，直接服务端渲染：

- `login.html` — 登录表单，`th:action="@{/login}"`
- `admin_home.html` — 完整管理后台，原生 JS + Fetch API 实现异步交互，自建 CSS 组件库（模态框、表格、搜索面板、按钮系统）
- `student_home.html` — 学生查分页，`th:each` 遍历成绩列表，`th:class` 动态设置通过/未通过样式

---

## 测试

### 测试概览

27 个单元测试，全部通过，无失败无跳过。

```
Tests run: 27, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

### 测试分层

```
┌─────────────────────────────┐
│  LoginControllerTest (6)    │  ← Web 层：MockMvc Standalone
│  - 登录页渲染                 │
│  - 密码错误 / 角色重定向        │
│  - 登出                       │
├─────────────────────────────┤
│  UserServiceTest (6)        │  ← Service 层：Mockito Mock Mapper
│  Cet4ScoreServiceTest (8)   │
│  - 登录成功/失败/用户不存在      │
│  - 修改密码/旧数据兼容          │
│  - CRUD 增删改查              │
│  - 批量导入                   │
├─────────────────────────────┤
│  PasswordUtilsTest (7)      │  ← Util 层：纯单元测试
│  - 盐生成/随机性               │
│  - 哈希确定性                  │
│  - 正确验证/错误拒绝            │
└─────────────────────────────┘
```

### 测试用例清单

**PasswordUtilsTest（7 cases）**

| # | 用例 | 验证点 |
|---|------|--------|
| 1 | 生成盐长度正确 | `length >= 16` |
| 2 | 每次盐不同 | `assertNotEquals` |
| 3 | 相同输入哈希一致 | 幂等性 |
| 4 | 不同密码哈希不同 | 碰撞抵抗 |
| 5 | 正确密码验证通过 | `verify() = true` |
| 6 | 错误密码验证拒绝 | `verify() = false` |
| 7 | 错误盐验证拒绝 | `verify() = false` |

**UserServiceTest（6 cases）**

| # | 用例 | Mock 策略 |
|---|------|----------|
| 1 | 凭据正确返回用户 | mock Mapper 返回带 salt 用户 |
| 2 | 密码错误返回 null | mock Mapper 返回用户，真实 PasswordUtils 验证 |
| 3 | 用户不存在返回 null | mock Mapper 返回 null |
| 4 | 无 salt 列降级兼容 | mock 查 salt 返回 null → 降级无 salt → 明文比对 |
| 5 | 改密成功 | mock 旧密码验证通过 → 更新 DB |
| 6 | 改密失败（原密码错） | mock 旧密码验证失败 → 不调用更新 |

**Cet4ScoreServiceTest（8 cases）**

| # | 用例 | 验证点 |
|---|------|--------|
| 1 | 新增成功 | `insert > 0` → `true` |
| 2 | 新增失败 | `insert = 0` → `false` |
| 3 | 按身份证号查询 | 返回正确列表 |
| 4 | 条件查询全空 | 仍可正常查询 |
| 5 | 条件查询带关键词 | Mapper 被调用一次 |
| 6 | 更新成功 | `updateById > 0` → `true` |
| 7 | 删除成功 | `deleteById > 0` → `true` |
| 8 | 批量导入 | 调用 `batchInsert` |

**LoginControllerTest（6 cases）**

| # | 用例 | HTTP 验证 |
|---|------|----------|
| 1 | GET /login → 200 | `view().name("login")` |
| 2 | GET / → 200 | `view().name("login")` |
| 3 | 密码错误 | `model().attributeExists("error")` |
| 4 | 管理员登录 | `redirectedUrl("/admin/home")` |
| 5 | 学生登录 | `redirectedUrl("/student/home")` |
| 6 | 登出 | `redirectedUrl("/login")` |

---

## 运行指南

### 环境要求

- JDK 11+
- MySQL 5.7+
- Node.js 18+
- Maven 3.6+

### 1. 初始化数据库

```sql
CREATE DATABASE txam DEFAULT CHARACTER SET utf8mb4;
-- 表结构见上文「数据库表结构」
```

### 2. 配置数据库连接

编辑 `txam/src/main/resources/application.yml`：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/txam?...
    username: root
    password: your_password
```

### 3. 启动后端

```bash
cd txam
mvn spring-boot:run
# 后端运行在 http://localhost:8080
```

### 4. 启动前端（Vue 3）

```bash
cd txam-frontend
npm install
npm run dev
# 前端运行在 http://localhost:3000
```

### 5. 运行测试

```bash
cd txam
mvn test
```

### 6. 打包部署

```bash
# 后端
cd txam && mvn package -DskipTests

# 前端
cd txam-frontend && npm run build
# 输出到 dist/，可部署到 Nginx
```

---

## 默认账号

| 角色 | 身份证号 | 密码 |
|------|---------|------|
| 管理员 | admin | 123456 |
| 学生 | 导入成绩时自动创建 | 123456 |
