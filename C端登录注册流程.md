# 烟火邻里 — C 端手机号登录/注册流程

> 版本：v1.0 | 更新日期：2026-03-16

---

## 一、总体架构

```
┌─────────────────────────────────────────────────────────────────┐
│                       前端 (Vue 3 + Vite)                        │
│  Login.vue → api.ts → Vite Proxy(:3001 → :8080)                │
│  auth store(Pinia) ← localStorage(token)                        │
│  路由守卫 → meta.requiresAuth → 拦截跳转 /login                  │
└──────────────────────────┬──────────────────────────────────────┘
                           │ HTTP (JSON)
                           ▼
┌─────────────────────────────────────────────────────────────────┐
│                    后端 (Spring Boot 2.7)                         │
│  ApiSecurityConfig → FilterChain                                │
│  ┌───────────────────────────────────────────────────────┐      │
│  │ ApiJwtAuthenticationFilter (OncePerRequestFilter)     │      │
│  │  解析 Authorization → Redis 获取会话 → SecurityContext  │      │
│  └───────────────────────────────────────────────────────┘      │
│  Controller → Service → Mapper                                  │
└──────────┬────────────────────────────┬─────────────────────────┘
           │                            │
           ▼                            ▼
     ┌──────────┐                ┌─────────────┐
     │  MySQL   │                │    Redis     │
     │ ums_member│                │ 验证码/会话   │
     └──────────┘                └─────────────┘
           │
           ▼
     ┌──────────────┐
     │ 阿里云短信 SDK │
     │ (dypnsapi)    │
     └──────────────┘
```

---

## 二、数据库设计

### 2.1 ums_member 表

```sql
CREATE TABLE `ums_member` (
  `id`          bigint(20)   NOT NULL AUTO_INCREMENT,
  `phone`       varchar(20)  NOT NULL COMMENT '手机号（唯一登录凭证）',
  `nickname`    varchar(64)  DEFAULT NULL COMMENT '昵称',
  `avatar`      varchar(500) DEFAULT NULL COMMENT '头像URL',
  `status`      int(1)       NOT NULL DEFAULT 1 COMMENT '0-禁用 1-启用',
  `create_time` datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_phone` (`phone`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='C端会员表';
```

### 2.2 Redis Key 设计

| Key 模式 | 值类型 | TTL | 用途 |
|----------|--------|-----|------|
| `auth:code:phone:{phone}` | String（6位数字） | 5 分钟 | 短信验证码 |
| `sms:limit:interval:{phone}` | String("1") | 60 秒 | 同号 60 秒内仅发一次 |
| `sms:limit:daily:{phone}` | String(计数) | 当日剩余秒数 | 每日最多 10 次 |
| `api:member:info:{phone}` | JSON(ApiMemberDetails) | 30 分钟（滑动续期） | 登录会话 |

---

## 三、完整时序图

```
用户                   前端(Vue)                后端(API)               Redis            阿里云SMS          MySQL
 │                       │                       │                      │                  │               │
 │ 1.输入手机号            │                       │                      │                  │               │
 │ 2.点击"获取验证码"       │                       │                      │                  │               │
 │──────────────────────>│                       │                      │                  │               │
 │                       │ 3.前端校验手机号格式      │                      │                  │               │
 │                       │ 4.POST /api/sms/       │                      │                  │               │
 │                       │   sendVerifyCode       │                      │                  │               │
 │                       │──────────────────────>│                      │                  │               │
 │                       │                       │ 5.校验手机号格式        │                  │               │
 │                       │                       │ 6.执行 Lua 限流脚本    │                  │               │
 │                       │                       │──────────────────────>│                  │               │
 │                       │                       │ 7.限流通过             │                  │               │
 │                       │                       │<──────────────────────│                  │               │
 │                       │                       │ 8.生成6位随机验证码     │                  │               │
 │                       │                       │ 9.调用阿里云发送        │                  │               │
 │                       │                       │─────────────────────────────────────────>│               │
 │                       │                       │ 10.发送成功            │                  │               │
 │                       │                       │<─────────────────────────────────────────│               │
 │                       │                       │ 11.验证码存入 Redis     │                  │               │
 │                       │                       │  (5分钟有效)           │                  │               │
 │                       │                       │──────────────────────>│                  │               │
 │                       │ 12.返回成功             │                      │                  │               │
 │                       │<──────────────────────│                      │                  │               │
 │ 13.开始60秒倒计时       │                       │                      │                  │               │
 │<──────────────────────│                       │                      │                  │               │
 │                       │                       │                      │                  │               │
 │ 14.输入验证码           │                       │                      │                  │               │
 │ 15.点击"登录/注册"      │                       │                      │                  │               │
 │──────────────────────>│                       │                      │                  │               │
 │                       │ 16.前端校验格式          │                      │                  │               │
 │                       │ 17.POST /api/member/   │                      │                  │               │
 │                       │   loginByPhone         │                      │                  │               │
 │                       │  {phone, verifyCode}   │                      │                  │               │
 │                       │──────────────────────>│                      │                  │               │
 │                       │                       │ 18.@Validated 参数校验 │                  │               │
 │                       │                       │ 19.从 Redis 取验证码   │                  │               │
 │                       │                       │──────────────────────>│                  │               │
 │                       │                       │<──────────────────────│                  │               │
 │                       │                       │ 20.比对验证码           │                  │               │
 │                       │                       │ 21.查询 ums_member     │                  │               │
 │                       │                       │──────────────────────────────────────────────────────────>│
 │                       │                       │<──────────────────────────────────────────────────────────│
 │                       │                       │ 22.不存在则自动注册     │                  │               │
 │                       │                       │  INSERT ums_member    │                  │               │
 │                       │                       │──────────────────────────────────────────────────────────>│
 │                       │                       │ 23.检查账号状态         │                  │               │
 │                       │                       │ 24.JwtTokenUtil        │                  │               │
 │                       │                       │  生成JWT(sub=phone)   │                  │               │
 │                       │                       │ 25.ApiMemberDetails    │                  │               │
 │                       │                       │  存入Redis(30min)     │                  │               │
 │                       │                       │──────────────────────>│                  │               │
 │                       │                       │ 26.删除已用验证码       │                  │               │
 │                       │                       │──────────────────────>│                  │               │
 │                       │ 27.返回{token,member}   │                      │                  │               │
 │                       │<──────────────────────│                      │                  │               │
 │                       │ 28.token存localStorage │                      │                  │               │
 │                       │ 29.Pinia更新auth state │                      │                  │               │
 │ 30.跳转目标页(或首页)   │                       │                      │                  │               │
 │<──────────────────────│                       │                      │                  │               │
 │                       │                       │                      │                  │               │
 │  ===== 后续已认证请求 =====                     │                      │                  │               │
 │                       │                       │                      │                  │               │
 │ 31.访问需要登录的页面    │                       │                      │                  │               │
 │──────────────────────>│                       │                      │                  │               │
 │                       │ 32.GET /api/member/info│                      │                  │               │
 │                       │  Header: Bearer {jwt}  │                      │                  │               │
 │                       │──────────────────────>│                      │                  │               │
 │                       │                       │ 33.ApiJwtFilter 拦截   │                  │               │
 │                       │                       │  解析token→取phone    │                  │               │
 │                       │                       │ 34.Redis取会话         │                  │               │
 │                       │                       │──────────────────────>│                  │               │
 │                       │                       │<──────────────────────│                  │               │
 │                       │                       │ 35.validateToken       │                  │               │
 │                       │                       │ 36.写入SecurityContext │                  │               │
 │                       │                       │ 37.滑动续期30min       │                  │               │
 │                       │                       │──────────────────────>│                  │               │
 │                       │                       │ 38.Controller处理      │                  │               │
 │                       │ 39.返回会员信息          │                      │                  │               │
 │                       │<──────────────────────│                      │                  │               │
 │ 40.展示用户信息         │                       │                      │                  │               │
 │<──────────────────────│                       │                      │                  │               │
```

---

## 四、后端实现详解

### 4.1 模块职责与文件清单

按照项目 `MODULE-ARCH.mdc` 模块架构规范，各层职责如下：

| 层级 | 模块 | 新增/修改文件 | 职责 |
|------|------|-------------|------|
| 数据词典 | fireworks-model | `UmsMember.java` | 会员实体 |
| | | `MemberLoginParam.java` | 登录请求 DTO（含 JSR-303 校验） |
| | | `MemberLoginVO.java` | 登录响应 VO（token + member） |
| | | `RedisKeyConstant.java` (+1常量) | Redis Key 统一管理 |
| 通用层 | fireworks-common | `ApiMemberDetails.java` | UserDetails 实现（封装 UmsMember） |
| 业务层 | fireworks-service | `UmsMemberMapper.java` | MyBatis-Plus Mapper |
| | | `UmsMemberService.java` | 会员服务接口 |
| | | `UmsMemberServiceImpl.java` | 验证码校验+自动注册+JWT生成 |
| | | `JwtTokenUtil.java` (修改) | 新增 `generateToken(String)` 重载 |
| API 层 | fireworks-api | `ApiSecurityConfig.java` | Spring Security 配置 |
| | | `ApiJwtAuthenticationFilter.java` | JWT 认证过滤器 |
| | | `ApiRestAuthenticationEntryPoint.java` | 401 JSON 响应 |
| | | `ApiMemberLoginController.java` | 登录/信息/登出接口 |
| | | `GlobalExceptionHandler.java` (修改) | 新增校验异常处理 |
| | | `WebConfig.java` (修改) | CORS 迁移至 SecurityConfig |
| | | `build.gradle` (修改) | +starter-security, +starter-validation |

### 4.2 Spring Security 配置详解（ApiSecurityConfig）

```
HTTP 请求
    │
    ▼
CorsFilter (Spring Security 内置)
    │
    ▼
ApiJwtAuthenticationFilter (自定义，在 UsernamePasswordAuthenticationFilter 之前)
    │  解析 Authorization: Bearer {token}
    │  → JwtTokenUtil.getUsernameFromToken(token)
    │  → RedisUtil.get(api:member:info:{phone})
    │  → 校验通过: SecurityContextHolder.setAuthentication()
    │  → 滑动续期 Redis 会话
    │
    ▼
FilterSecurityInterceptor (Spring Security 内置)
    │  按 authorizeRequests() 规则决定:
    │  ├─ permitAll → 直接放行
    │  └─ authenticated → 检查 SecurityContext
    │     ├─ 有认证信息 → 放行到 Controller
    │     └─ 无认证信息 → ApiRestAuthenticationEntryPoint → 401 JSON
    │
    ▼
DispatcherServlet → Controller
```

**公开接口 vs 需认证接口：**

```java
// 公开接口（无需 Token）
.antMatchers(HttpMethod.POST, "/api/sms/sendVerifyCode").permitAll()
.antMatchers(HttpMethod.POST, "/api/member/loginByPhone").permitAll()
.antMatchers(HttpMethod.GET, "/api/category/**").permitAll()
.antMatchers(HttpMethod.GET, "/api/product/**").permitAll()

// 需认证接口（必须携带有效 Token）
.antMatchers("/api/member/**").authenticated()
.antMatchers("/api/cart/**").authenticated()
.antMatchers("/api/order/**").authenticated()
```

> 注意规则顺序：`POST /api/member/loginByPhone` 的 permitAll 在 `/api/member/**` 的 authenticated 之前，Spring Security 按声明顺序匹配，先匹配到的规则生效。

### 4.3 登录服务核心逻辑（UmsMemberServiceImpl.loginByPhone）

```
输入: phone + verifyCode
    │
    ├─ 1. Redis 取验证码 → 不存在: "验证码已过期"
    │                    → 不匹配: "验证码错误"
    │
    ├─ 2. 查 ums_member(phone)
    │     └─ 不存在 → INSERT 新记录（自动注册，昵称="用户XXXX"）
    │
    ├─ 3. 检查 status → !=1: "账号已被禁用"
    │
    ├─ 4. JwtTokenUtil.generateToken(phone)
    │     └─ JWT payload: { sub: phone, created: now, exp: now+86400s }
    │     └─ 签名算法: HS512
    │
    ├─ 5. ApiMemberDetails(member) → RedisUtil.set(30min)
    │
    ├─ 6. Redis 删除已使用的验证码（一次性消费）
    │
    └─ 7. 返回 MemberLoginVO { token, tokenHead, member }
```

### 4.4 JWT Token 结构

```
Header:  { "alg": "HS512" }
Payload: { "sub": "13800138000", "created": 1710576000000, "exp": 1710662400 }
Signature: HMACSHA512(base64(header) + "." + base64(payload), secret)
```

- **subject**: 手机号（唯一标识）
- **expiration**: 签发后 86400 秒（24小时）— 绝对上限
- **实际会话**：由 Redis TTL 控制（30 分钟滑动窗口），比 JWT exp 更严格

### 4.5 接口汇总

| 接口 | 方法 | 鉴权 | 请求参数 | 返回值 |
|------|------|------|---------|-------|
| `/api/sms/sendVerifyCode` | POST | 无需 | `phone` (query) | `Result<Void>` |
| `/api/member/loginByPhone` | POST | 无需 | `{phone, verifyCode}` (JSON body) | `Result<MemberLoginVO>` |
| `/api/member/info` | GET | 需要 | 无（Token 中解析） | `Result<UmsMember>` |
| `/api/member/logout` | POST | 需要 | 无 | `Result<Void>` |

---

## 五、前端实现详解

### 5.1 文件清单

| 文件 | 职责 |
|------|------|
| `src/lib/api.ts` | 统一请求封装（fetch + Token 注入 + 401 拦截） |
| `src/stores/auth.ts` | Pinia 认证状态管理（token + member + localStorage） |
| `src/views/Login.vue` | 登录页面（手机号输入 + 验证码倒计时 + 表单提交） |
| `src/router/index.ts` | 路由守卫（meta.requiresAuth） |
| `src/components/Navbar.vue` | 顶部导航（登录态/未登录态切换） |
| `src/App.vue` | 应用初始化（恢复登录态） |

### 5.2 请求封装核心逻辑（api.ts）

```
每个请求
    │
    ├─ 从 localStorage 读取 token
    │  └─ 存在 → 注入 Header: Authorization: Bearer {token}
    │
    ├─ 发送 fetch 请求
    │
    ├─ 解析响应 JSON
    │  ├─ code === 401 → 清除 token → 跳转 /login?redirect=当前路径
    │  ├─ code !== 200 → throw Error(message)
    │  └─ code === 200 → 返回 data
    │
    └─ 调用方通过 try/catch 处理错误
```

### 5.3 验证码按钮状态机

```
┌──────────────┐   点击(手机号合法)   ┌──────────────┐   API 成功   ┌──────────────────────┐
│  初始态       │ ─────────────────> │  发送中       │ ──────────> │  倒计时态             │
│  "获取验证码"  │                    │  "发送中..."   │             │  "重新获取(60s)"       │
│  绿色可点击    │                    │  灰色禁用      │             │  "重新获取(59s)"       │
└──────────────┘                    └──────┬───────┘             │  ...                  │
       ▲                                    │ API 失败             │  "重新获取(1s)"        │
       │                                    │                     │  灰色禁用              │
       │                                    ▼                     └──────────┬───────────┘
       │                            ┌──────────────┐                        │ countdown=0
       │                            │  错误提示     │                        │
       │                            │  按钮恢复可用  │                        ▼
       │                            └──────────────┘               ┌──────────────┐
       │                                                           │  恢复初始态    │
       └───────────────────────────────────────────────────────────│  "获取验证码"  │
                                                                   └──────────────┘
```

### 5.4 路由守卫逻辑

```
beforeEach(to, from, next)
    │
    ├─ to.meta.requiresAuth === true ?
    │  ├─ YES → localStorage.getItem('token') 存在?
    │  │        ├─ YES → next() (放行)
    │  │        └─ NO  → next({ name: 'Login', query: { redirect: to.fullPath } })
    │  │
    │  └─ NO → next() (放行)
```

需要登录的路由：`/cart`、`/checkout`

### 5.5 登录态恢复（App.vue）

```
应用启动 (App.vue setup)
    │
    ├─ authStore.isLoggedIn ?  (检查 localStorage 中的 token)
    │  ├─ YES → authStore.fetchInfo()
    │  │        └─ GET /api/member/info
    │  │           ├─ 成功 → member 数据填充
    │  │           └─ 失败(401) → clearAuth() 清除过期 token
    │  └─ NO → 不做任何操作
```

---

## 六、安全机制汇总

| 防护点 | 当前实现 | 层级 |
|--------|---------|------|
| 短信轰炸 | Redis Lua 原子限流：60s/次，10次/日 | 后端 Service |
| 手机号格式 | 前端正则 + 后端正则 + @Validated 双重校验 | 前后端 |
| 验证码有效期 | Redis TTL 5 分钟自动过期 | 后端 Redis |
| 验证码一次性 | 登录成功后 Redis 删除验证码 | 后端 Service |
| CSRF | Spring Security 禁用（STATELESS 无状态 JWT） | 后端 Security |
| 会话管理 | Redis 滑动窗口 30 分钟，JWT 24 小时绝对上限 | 后端 Filter |
| 未认证访问 | SecurityConfig + 401 JSON 响应 | 后端 Security |
| Token 过期 | 前端 401 拦截 → 清除 token → 跳转登录页 | 前端 api.ts |
| XSS(token) | localStorage 存储（同源策略保护） | 前端 |
| CORS | Spring Security CorsConfigurationSource 统一管理 | 后端 Security |

---

## 七、风险分析与企业级改进建议

### 7.1 短信验证码相关风险

#### 风险 1：验证码暴力破解

**当前状态**：6 位数字验证码，理论上 100 万种组合。若接口无校验次数限制，攻击者可在 5 分钟有效期内穷举。

**改进建议**：
- 增加验证码校验失败次数限制（Redis 计数器 `auth:code:fail:{phone}`），连续错误 5 次后锁定该手机号 15 分钟
- 引入图形验证码 / 滑块验证码前置（如极验、腾讯云天御），人机识别通过后才允许发送短信
- 日志监控：同 IP 短时间内对不同手机号发起大量验证，触发告警

#### 风险 2：短信轰炸（变种绕过）

**当前状态**：Lua 脚本限流以手机号为维度（60s/次、10次/日），但无 IP 维度限制。

**改进建议**：
- 增加 IP 维度限制：同一 IP 每小时最多发 20 条（Redis `sms:limit:ip:{ip}`）
- 增加设备指纹限制：配合前端生成唯一设备 ID
- 接入风控平台（阿里云、腾讯云风控），异常请求直接拦截
- 考虑虚拟号段屏蔽（170/171 等虚拟运营商号段可选择性拦截）

#### 风险 3：验证码窃听 / 钓鱼

**当前状态**：短信验证码明文发送到手机，无法防止 SIM 卡劫持或短信嗅探。

**改进建议**（大厂方案）：
- 优先推荐一键登录（阿里云号码认证 / 极光认证），运营商通道级认证，不经过短信
- 敏感操作（改手机号、支付等）增加二次验证
- 登录通知：登录成功后推送消息通知用户（新设备首次登录）

### 7.2 JWT Token 相关风险

#### 风险 4：JWT Secret 硬编码

**当前状态**：`application.yml` 中 `jwt.secret` 为固定字符串，多环境共用。

**改进建议**：
- 不同环境使用不同 secret，通过环境变量 / 配置中心（Nacos/Apollo）注入
- secret 长度至少 256 位，使用随机生成的强密钥
- 定期轮换 secret（配合 Token Refresh 机制）

#### 风险 5：Token 泄露无法主动失效

**当前状态**：JWT 本身无状态，虽然用 Redis 做了会话控制（30min 滑动窗口），但若 Token 泄露，攻击者可在 Redis 有效期内冒用。

**改进建议**：
- 引入 Token Refresh 机制（双 Token）：
  - `accessToken`：短有效期（15 分钟），用于接口认证
  - `refreshToken`：长有效期（7 天），仅用于换取新 accessToken
  - accessToken 过期后前端自动用 refreshToken 续期，用户无感
- Redis 中增加 Token 黑名单（logout 时将 JWT 的 jti 加入黑名单，TTL = JWT 剩余有效期）
- 踢人下线：管理后台可删除 Redis 中指定用户的会话

#### 风险 6：Token 存储在 localStorage

**当前状态**：Token 存 `localStorage`，可被同源下的 XSS 脚本读取。

**改进建议**：
- 方案 A（推荐）：改用 `httpOnly + secure + sameSite=strict` 的 Cookie 存储，前端无法通过 JS 读取
- 方案 B：保持 localStorage，但加强 CSP（Content-Security-Policy）防 XSS
- 无论哪种方案，必须做好 XSS 防护（Vue 3 默认模板转义已覆盖大部分场景）

### 7.3 登录流程本身的风险

#### 风险 7：手机号枚举

**当前状态**：登录接口对"手机号不存在"时直接自动注册，不泄露是否已注册。但发送验证码接口对任何手机号都会返回成功（只要限流通过），无法通过此判断注册状态。

**当前评估**：**低风险**，自动注册模式本身隐藏了枚举信息。保持即可。

#### 风险 8：并发注册竞态条件

**当前状态**：`loginByPhone` 先查后插入，若同一手机号并发请求，可能产生两条记录（虽然 `uk_phone` 唯一索引会让第二条 INSERT 报错）。

**改进建议**：
- 在 Service 中 catch `DuplicateKeyException`，捕获后重新查询（幂等保护）
- 或使用 `INSERT ... ON DUPLICATE KEY UPDATE` 替代先查后插
- 或在登录时使用分布式锁 `SETNX sms:login:lock:{phone}` 保证串行

#### 风险 9：登录接口无限重试

**当前状态**：登录接口 `/api/member/loginByPhone` 无速率限制。

**改进建议**：
- 登录接口增加 IP + 手机号维度的限速（如 1 分钟最多 10 次尝试）
- 连续失败 N 次后要求图形验证码
- 接入 API Gateway（如 Spring Cloud Gateway / Nginx rate_limit）统一限流

### 7.4 会话管理风险

#### 风险 10：多设备登录无感知

**当前状态**：同一手机号多次登录，Redis 中 `api:member:info:{phone}` 只有一份，最后一次登录的 JWT 有效。但旧 JWT 仍然可以正常使用（因为 Redis value 会被新登录覆盖，但旧 JWT 解析出的 phone 去 Redis 取到的也是最新数据）。

**改进建议**：
- Redis Key 中加入 Token 版本号或 jti，每次登录生成新版本，旧版本 Token 不匹配则拒绝
- 或使用 `Set` 类型存储所有活跃 Token，支持"踢掉其他设备"功能
- 提供"登录设备管理"页面，用户可主动下线指定设备

#### 风险 11：缺少强制下线机制

**当前状态**：管理后台无法强制 C 端用户下线。

**改进建议**：
- admin 模块增加接口：修改 `ums_member.status = 0` 后同时删除 Redis 会话
- 用户修改手机号时清除所有旧会话

### 7.5 前端安全风险

#### 风险 12：前端倒计时可绕过

**当前状态**：60 秒倒计时仅前端控制，用户刷新页面即可重新发送。

**当前评估**：**低风险**，后端 Redis Lua 限流是真正的防线，前端倒计时仅为 UX 优化。后端已有 60 秒间隔限制，绕过前端倒计时也无法多次发送。

**可选改进**：
- 将倒计时剩余秒数写入 `sessionStorage`，刷新页面后恢复倒计时
- 或后端在 60 秒间隔限流响应中返回剩余等待秒数，前端据此显示

#### 风险 13：前端 Token 刷新无过渡

**当前状态**：Token 过期后直接跳转登录页，用户正在操作的数据丢失。

**改进建议**：
- 引入 Token Refresh 静默续期（见风险 5）
- 401 响应时先尝试用 refreshToken 续期，续期成功后重放失败请求
- 仅当 refreshToken 也过期时才跳转登录页
- 前端操作数据持久化到 sessionStorage，登录后自动恢复

### 7.6 基础设施层面

#### 改进 14：HTTPS

**必须项**：生产环境所有接口必须走 HTTPS，防止 Token 被中间人截取。

#### 改进 15：日志与审计

**改进建议**：
- 登录成功/失败日志入库（`ums_member_login_log` 表），记录 IP、设备信息、时间
- 异常登录行为告警（如异地登录、高频登录失败）
- 接入 ELK 或类似日志分析平台

#### 改进 16：API 幂等性

**改进建议**：
- 登录接口添加请求幂等 token（前端生成 UUID，后端 Redis 去重），防止网络重试导致重复注册

#### 改进 17：灰度与降级

**改进建议**：
- 短信服务不可用时提供降级方案（如语音验证码备选通道）
- 多云短信服务商（阿里云 + 腾讯云）互备，自动切换

---

## 八、企业级登录的进阶功能（后续迭代方向）

| 功能 | 说明 | 优先级 |
|------|------|--------|
| Token Refresh 双 Token | accessToken 15min + refreshToken 7d，无感续期 | P0 |
| 图形验证码前置 | 极验/腾讯天御，人机识别后才发短信 | P0 |
| 验证码暴力破解防护 | 校验失败 5 次锁定手机号 15 分钟 | P0 |
| 登录日志 | ums_member_login_log 表，记录设备/IP/时间 | P1 |
| 一键登录 | 阿里云号码认证/极光认证，运营商级免短信登录 | P1 |
| 多设备管理 | 查看/踢掉其他设备，Redis Set 存活跃会话 | P1 |
| 微信/支付宝登录 | OAuth2 接入，绑定手机号 | P1 |
| IP 维度限流 | 同 IP 每小时最多发 20 条短信 | P1 |
| Cookie 存储 Token | httpOnly + secure + sameSite，防 XSS 窃取 | P2 |
| JWT Secret 轮换 | 配置中心管理，定期轮换 | P2 |
| 账号合并 | 多种登录方式绑定同一账号 | P2 |
| 生物识别 | 指纹/Face ID 快捷登录（移动端） | P3 |

---

## 九、快速联调 Checklist

- [ ] 执行 `doc/sql/ums_member_init.sql` 建表
- [ ] Gradle sync / refresh（fireworks-api 新增了两个依赖）
- [ ] 确认 Redis 服务运行（127.0.0.1:6379）
- [ ] 确认阿里云短信配置（环境变量或 application-local.yml）
- [ ] 启动后端：`FireworksApiApplication`（端口 8080）
- [ ] 启动前端：`npm run dev`（端口 3001，代理到 8080）
- [ ] 打开 http://localhost:3001/login
- [ ] 输入手机号 → 获取验证码 → 输入验证码 → 登录
- [ ] 验证 Navbar 显示昵称 + 退出按钮
- [ ] 访问 /cart 未登录时自动跳转 /login
- [ ] 点击退出 → Navbar 恢复为 "登录/注册"

