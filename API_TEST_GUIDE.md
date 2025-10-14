# 后台管理系统API测试指南

## 基本信息
- **服务地址**: http://localhost:8080
- **Swagger文档**: http://localhost:8080/swagger-ui.html
- **H2控制台**: http://localhost:8080/h2-console

## 1. 管理员登录

### 登录获取Token
```bash
curl -X POST http://localhost:8080/api/admin/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "admin123"
  }'
```

**响应示例**:
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "userType": "ADMIN",
  "user": {
    "id": 1,
    "username": "admin",
    "email": "admin@maternity.com",
    "name": "System Administrator",
    "role": "SUPER_ADMIN",
    "enabled": true
  }
}
```

## 2. 订单管理API

### 2.1 查看所有订单（分页）
```bash
curl -X GET "http://localhost:8080/api/admin/orders?page=0&size=10" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### 2.2 查看订单详情
```bash
curl -X GET http://localhost:8080/api/admin/orders/1 \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### 2.3 按状态查询订单
```bash
# 可用状态: PENDING, CONFIRMED, IN_PROGRESS, COMPLETED, CANCELLED
curl -X GET http://localhost:8080/api/admin/orders/status/CONFIRMED \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### 2.4 查询特定宝妈的订单
```bash
curl -X GET http://localhost:8080/api/admin/orders/mother/1 \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### 2.5 更新订单状态
```bash
curl -X PUT http://localhost:8080/api/admin/orders/1/status \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "status": "COMPLETED"
  }'
```

### 2.6 获取订单统计
```bash
curl -X GET http://localhost:8080/api/admin/orders/stats \
  -H "Authorization: Bearer YOUR_TOKEN"
```

**响应示例**:
```json
{
  "totalOrders": 2,
  "pendingOrders": 0,
  "confirmedOrders": 1,
  "inProgressOrders": 1,
  "completedOrders": 0,
  "cancelledOrders": 0
}
```

## 3. 用户管理API

### 3.1 查看所有用户（分页）
```bash
curl -X GET "http://localhost:8080/api/admin/users?page=0&size=10" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### 3.2 查看用户详情
```bash
curl -X GET http://localhost:8080/api/admin/users/1 \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### 3.3 查看所有宝妈
```bash
curl -X GET http://localhost:8080/api/admin/users/mothers \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### 3.4 查看所有月嫂
```bash
curl -X GET http://localhost:8080/api/admin/users/matrons \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### 3.5 按姓名搜索用户
```bash
curl -X GET "http://localhost:8080/api/admin/users/search/name?name=Demo" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### 3.6 按手机号搜索用户
```bash
curl -X GET "http://localhost:8080/api/admin/users/search/phone?phone=138" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### 3.7 获取用户统计
```bash
curl -X GET http://localhost:8080/api/admin/users/stats \
  -H "Authorization: Bearer YOUR_TOKEN"
```

**响应示例**:
```json
{
  "totalUsers": 6,
  "totalMothers": 2,
  "totalMatrons": 4
}
```

### 3.8 查看所有月嫂档案
```bash
curl -X GET http://localhost:8080/api/admin/users/matron-profiles \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### 3.9 查看特定月嫂档案
```bash
curl -X GET http://localhost:8080/api/admin/users/matron-profiles/1 \
  -H "Authorization: Bearer YOUR_TOKEN"
```

## 4. 删除操作（需要SUPER_ADMIN权限）

### 4.1 删除订单
```bash
curl -X DELETE http://localhost:8080/api/admin/orders/1 \
  -H "Authorization: Bearer SUPER_ADMIN_TOKEN"
```

### 4.2 删除用户
```bash
curl -X DELETE http://localhost:8080/api/admin/users/1 \
  -H "Authorization: Bearer SUPER_ADMIN_TOKEN"
```

## 5. 使用Postman测试

1. **导入到Postman**:
   - 创建新的Collection: "Maternity Admin API"
   - 添加环境变量:
     - `baseUrl`: http://localhost:8080
     - `adminToken`: (登录后获取的token)

2. **设置Authorization**:
   - Type: Bearer Token
   - Token: {{adminToken}}

3. **测试流程**:
   1. 先调用登录API获取token
   2. 将token保存到环境变量
   3. 测试其他API端点

## 6. 浏览器测试（Swagger UI）

访问: http://localhost:8080/swagger-ui.html

1. 找到 "Admin Authentication" 分组
2. 点击 "POST /api/admin/auth/login"
3. 点击 "Try it out"
4. 输入用户名密码后执行
5. 复制返回的token
6. 点击页面右上角 "Authorize" 按钮
7. 输入: `Bearer YOUR_TOKEN`
8. 现在可以测试所有后台管理API

## 7. 权限测试

### 测试未授权访问
```bash
# 不带token访问（应该返回401）
curl -X GET http://localhost:8080/api/admin/orders
```

### 测试普通管理员权限
```bash
# 使用manager账户登录
curl -X POST http://localhost:8080/api/admin/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "manager",
    "password": "manager123"
  }'

# 尝试删除订单（应该返回403 Forbidden）
curl -X DELETE http://localhost:8080/api/admin/orders/1 \
  -H "Authorization: Bearer MANAGER_TOKEN"
```

## 8. 数据库查看

访问 H2控制台: http://localhost:8080/h2-console
- JDBC URL: `jdbc:h2:mem:maternitydb`
- User Name: `sa`
- Password: (留空)

可以执行SQL查询：
```sql
-- 查看所有管理员
SELECT * FROM admins;

-- 查看所有订单
SELECT * FROM orders;

-- 查看所有用户
SELECT * FROM users;

-- 查看月嫂档案
SELECT * FROM matron_profiles;
```

## 9. 示例测试脚本

保存为 `test_admin_api.sh`:

```bash
#!/bin/bash

BASE_URL="http://localhost:8080"

# 1. 登录获取token
echo "=== 管理员登录 ==="
LOGIN_RESPONSE=$(curl -s -X POST $BASE_URL/api/admin/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}')

echo $LOGIN_RESPONSE | jq .

TOKEN=$(echo $LOGIN_RESPONSE | jq -r .token)

echo -e "\n获取到的Token: $TOKEN\n"

# 2. 获取订单统计
echo "=== 订单统计 ==="
curl -s -X GET $BASE_URL/api/admin/orders/stats \
  -H "Authorization: Bearer $TOKEN" | jq .

# 3. 获取用户统计
echo -e "\n=== 用户统计 ==="
curl -s -X GET $BASE_URL/api/admin/users/stats \
  -H "Authorization: Bearer $TOKEN" | jq .

# 4. 获取所有宝妈
echo -e "\n=== 所有宝妈 ==="
curl -s -X GET $BASE_URL/api/admin/users/mothers \
  -H "Authorization: Bearer $TOKEN" | jq .

# 5. 获取所有月嫂
echo -e "\n=== 所有月嫂 ==="
curl -s -X GET $BASE_URL/api/admin/users/matrons \
  -H "Authorization: Bearer $TOKEN" | jq .

# 6. 获取所有订单
echo -e "\n=== 所有订单（第一页） ==="
curl -s -X GET "$BASE_URL/api/admin/orders?page=0&size=10" \
  -H "Authorization: Bearer $TOKEN" | jq .
```

运行测试脚本：
```bash
chmod +x test_admin_api.sh
./test_admin_api.sh
```

## 10. 功能清单验证

- [x] 管理员登录认证
- [x] 查看所有订单（分页）
- [x] 查看订单详情
- [x] 按状态筛选订单
- [x] 按宝妈ID查询订单
- [x] 更新订单状态
- [x] 订单统计信息
- [x] 查看所有用户（分页）
- [x] 查看用户详情
- [x] 查看所有宝妈
- [x] 查看所有月嫂
- [x] 按姓名搜索用户
- [x] 按手机号搜索用户
- [x] 用户统计信息
- [x] 查看月嫂档案
- [x] 权限控制（ADMIN vs SUPER_ADMIN）
- [x] JWT Token认证
