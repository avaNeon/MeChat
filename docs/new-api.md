# MeChat 新增接口文档

## 调用流程指南

### 群聊完整流程

```text
1. POST /accounts/register        -> 注册账号
2. POST /accounts/login           -> 登录，拿到 token 和 userId
3. POST /friends                  -> 添加好友（需要知道对方 userId 或 nickname）
4. POST /friends/requests/{id}/accept  -> 对方同意后成为好友
5. POST /groups                   -> 创建群聊，拉入已添加的好友
6. POST /groups/{groupId}/messages -> 在群中发送消息，所有成员实时收到
7. GET  /groups/{groupId}/messages -> 查看群聊历史消息
```

### 发送图片消息流程

```text
1. POST /messages/images          -> 上传图片，拿到 imgPath
2. POST /messages/send 或 POST /groups/{groupId}/messages  -> 发送时传入 imgPath
3. GET  /messages/images/{path}   -> 客户端根据 imgPath 加载图片
```

> **注意**：必须先上传图片拿到 `imgPath`，才能在发送消息时传入该路径。单聊和群聊发送图片流程一样。

---

## 群聊接口

### 查询我的群聊列表

```http
GET /groups
token: <token>
```

响应 `data`:

```json
[
  {
    "id": "<groupId>",
    "groupName": "我们的小群",
    "owner": {
      "userId": "<userId>",
      "nickname": "群主",
      "avatar": "<avatarFileName>"
    },
    "avatar": null,
    "memberCount": 3,
    "createTime": "2026-06-13T10:30:00"
  }
]
```

### 创建群聊

```http
POST /groups
token: <token>
Content-Type: application/json
```

请求体:

```json
{
  "groupName": "我们的小群",
  "memberUserIds": [739093770475929601, 739093770475929602]
}
```

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| groupName | String | 是 | 群名称，最长 255 个字符 |
| memberUserIds | List\<Long\> | 是 | 初始成员用户 ID 列表 |

响应 `data`:

```json
{
  "id": "<groupId>",
  "groupName": "我们的小群",
  "owner": { "userId": "<userId>", "nickname": "neon", "avatar": null },
  "avatar": null,
  "memberCount": 3,
  "createTime": "2026-06-13T10:30:00"
}
```

> 创建者自动成为群主。初始成员必须为创建者的好友。创建者如果在 memberUserIds 中会被自动去重。

### 拉人进群

```http
POST /groups/{groupId}/members
token: <token>
Content-Type: application/json
```

请求体（直接传用户 ID 列表）:

```json
[739093770475929603, 739093770475929604]
```

> 任何群成员都可以拉人。被拉用户必须为操作者的好友。已在群中的用户不会重复加入。

### 踢人出群

```http
DELETE /groups/{groupId}/members/{userId}
token: <token>
```

> 只有群主可以踢人。不能踢群主自己。踢出最后一名成员后群自动解散。

### 查询群成员

```http
GET /groups/{groupId}/members
token: <token>
```

响应 `data`:

```json
[
  {
    "user": { "userId": "<userId>", "nickname": "neon", "avatar": "<avatarFileName>" },
    "role": 1,
    "joinTime": "2026-06-13T10:30:00"
  },
  {
    "user": { "userId": "<userId>", "nickname": "friend", "avatar": "<avatarFileName>" },
    "role": 0,
    "joinTime": "2026-06-13T10:30:01"
  }
]
```

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| user | AccountUserVO | 成员用户信息 |
| role | Int | 0 普通成员，1 群主 |
| joinTime | String | 加入时间 |

### 发送群聊消息

```http
POST /groups/{groupId}/messages
token: <token>
Content-Type: application/json
```

文本消息:

```json
{
  "clientMessageId": "group-msg-001",
  "messageType": 1,
  "content": "大家好"
}
```

图片消息:

```json
{
  "clientMessageId": "group-msg-002",
  "messageType": 2,
  "content": "看这张图",
  "imgPath": "file/msg/191481802202943488_595cf4215583482b80fdb1c7ed80cab9.jpg"
}
```

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| clientMessageId | String | 否 | 客户端消息ID，用于幂等 |
| messageType | Int | 是 | 1 文本，2 图片 |
| content | String | 否 | 文本内容，图片消息可为空 |
| imgPath | String | 否 | 图片路径，先调用上传图片接口获取 |

响应 `data`:

```json
{
  "id": 100,
  "conversationId": "<convId>",
  "groupId": "<groupId>",
  "senderId": "<userId>",
  "receiverId": 0,
  "clientMessageId": "group-msg-001",
  "messageType": 1,
  "content": "大家好",
  "imgPath": null,
  "sendTime": "2026-06-13T11:00:00"
}
```

> 所有群成员通过 WebSocket 实时收到推送，推送格式和单聊一致（`type: "message"`），客户端根据 `groupId` 区分群消息。

### 查询群聊消息历史

```http
GET /groups/{groupId}/messages?beforeMessageId=<msgId>&limit=30
token: <token>
```

| 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| beforeMessageId | Long | 否 | 只查该 ID 之前的消息，首次翻页不传 |
| limit | Int | 否 | 默认 30，最大 100 |

响应 `data`:

```json
{
  "messages": [
    {
      "id": 100,
      "conversationId": "<convId>",
      "groupId": "<groupId>",
      "senderId": "<userId>",
      "receiverId": 0,
      "clientMessageId": "group-msg-001",
      "messageType": 1,
      "content": "大家好",
      "imgPath": null,
      "sendTime": "2026-06-13T11:00:00"
    }
  ],
  "hasMore": false
}
```

> 消息按发送时间正序排列。群成员才能查看。

### 标记群聊已读

```http
POST /groups/{groupId}/read
token: <token>
```

> 标记后该群的未读消息计数清零。

### 会话列表（群聊入口）

群聊会话自动出现在 `GET /conversations` 返回列表中：

```json
{
  "conversationId": "<convId>",
  "type": 1,
  "groupId": "<groupId>",
  "groupName": "我们的小群",
  "groupAvatar": null,
  "peer": null,
  "lastMessage": { "content": "大家好", ... },
  "lastMessageTime": "2026-06-13T11:00:00",
  "unreadCount": 3
}
```

> `type=1` 表示群聊，客户端据此展示群名/群头像而非对端用户。

---

## 聊天图片接口

### 上传聊天图片

```http
POST /messages/images
token: <token>
Content-Type: multipart/form-data
```

表单字段:

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- |
| imageFile | File | 是 | 图片文件，支持 jpg/jpeg/png/gif/webp |

响应 `data`:

```text
file/msg/191481802202943488_595cf4215583482b80fdb1c7ed80cab9.jpg
```

> 返回相对路径，客户端保存后，发送消息时传入 `imgPath`。图片和头像共用每日 30MB 限额。

### 下载聊天图片

```http
GET /messages/images/file/msg/191481802202943488_595cf4215583482b80fdb1c7ed80cab9.jpg
token: <token>
```

响应: 二进制图片数据，`Content-Type: image/jpeg` 等。

> 路径直接拼接在上传接口返回值后面。需要传 token。

Android 加载示例:

```kotlin
Glide.with(context)
    .load("http://<host>:8080/messages/images/$imgPath")
    // 需要自定义 OkHttp 拦截器添加 token header
    .into(imageView)
```

---

## 发送消息（更新）

单聊和群聊消息响应均新增 `groupId` 字段（群聊时有值）。群聊 WebSocket 推送格式相同，`type` 仍是 `"message"`，客户端根据 `groupId != null` 判断为群消息。

### 单聊发送

```http
POST /messages/send
token: <token>
Content-Type: application/json
```

图片消息请求体:

```json
{
  "receiverId": 739093770475929601,
  "clientMessageId": "client-msg-002",
  "messageType": 2,
  "content": "看这张图",
  "imgPath": "file/msg/191481802202943488_595cf4215583482b80fdb1c7ed80cab9.jpg"
}
```

| 新增/变更字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| messageType | Int | 是 | 1 文本，2 图片（新增） |
| content | String | 否 | 图片消息可为空（变更） |
| imgPath | String | 否 | 图片路径，通过上传图片接口获取（新增） |

响应新增 `groupId` 和 `imgPath` 字段:

```json
{
  "id": 1,
  "conversationId": "...",
  "groupId": null,
  "senderId": "...",
  "receiverId": "...",
  "clientMessageId": "client-msg-002",
  "messageType": 2,
  "content": "看这张图",
  "imgPath": "file/msg/191481802202943488_595cf4215583482b80fdb1c7ed80cab9.jpg",
  "sendTime": "2026-06-13T10:30:00"
}
```

> `groupId` 单聊时为 null，群聊时有值。客户端可通过 `groupId != null` 判断消息来源。

---

## 新增错误码

| 错误码 | 说明 |
| --- | --- |
| 3004 | 上传超过每日限额（头像+图片共用 30MB） |
| 3005 | 头像/图片格式不支持 |
| 3101 | 图片上传失败 |
| 3102 | 图片不存在 |
| 3103 | 图片文件不能为空 |
| 3104 | 图片格式不支持 |
| 4101 | 不是好友，无法拉入群聊 |
| 4102 | 群聊不存在 |
| 4103 | 你不是群成员，无权操作 |
| 4104 | 只有群主才能踢人 |
| 4105 | 不能踢出群主或自己 |
| 4106 | 该用户不在群中 |
| 4107 | 群聊会话不存在 |
