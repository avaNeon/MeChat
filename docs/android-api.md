# MeChat Android 接口文档

## 基础信息

Base URL:

```text
http://<server-host>:8080
```

除头像下载接口外，HTTP JSON 接口统一返回:

```json
{
  "code": 0,
  "message": "success",
  "data": {}
}
```

Android 类型对应:

```text
Long    -> Kotlin Long / Java Long
Integer -> Kotlin Int / Java Integer
String  -> Kotlin String / Java String
Boolean -> Kotlin Boolean / Java Boolean
```

需要登录态的 HTTP 接口统一传请求头:

```text
token: <登录后返回的 token>
```

## 账号接口

### 注册账号

```http
POST /accounts/register
Content-Type: application/json
```

请求体:

```json
{
  "nickname": "neon",
  "password": "<password>"
}
```

字段说明:

| 字段 | Android 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| nickname | String | 是 | 昵称 |
| password | String | 是 | 密码 |

响应 `data`:

```json
{
  "userId": "<userId>",
  "nickname": "neon",
  "avatar": null
}
```

### 登录

```http
POST /accounts/login
Content-Type: application/json
```

请求体:

```json
{
  "nickname": "neon",
  "password": "<password>"
}
```

字段说明:

| 字段 | Android 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| nickname | String | 是 | 昵称 |
| password | String | 是 | 密码 |

响应 `data`:

```json
{
  "token": "<token>",
  "user": {
    "userId": "<userId>",
    "nickname": "neon",
    "avatar": "<avatarFileName>"
  }
}
```

客户端需要保存:

| 字段 | Android 类型 | 说明 |
| --- | --- | --- |
| token | String | 后续请求登录态 |
| userId | Long | 当前用户 ID |

### 自动登录

```http
POST /accounts/auto-login
token: <token>
```

请求体: 无

响应 `data` 同登录接口:

```json
{
  "token": "<token>",
  "user": {
    "userId": "<userId>",
    "nickname": "neon",
    "avatar": "<avatarFileName>"
  }
}
```

说明:

```text
如果 token 剩余有效期不超过 1 天，服务端会自动续期到 7 天。
```

### 获取当前用户信息

```http
GET /accounts/me
token: <token>
```

响应 `data`:

```json
{
  "userId": "<userId>",
  "nickname": "neon",
  "avatar": "<avatarFileName>"
}
```

### 上传头像

```http
POST /accounts/avatar
token: <token>
Content-Type: multipart/form-data
```

表单字段:

| 字段 | Android 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| avatarFile | File / MultipartBody.Part | 是 | 头像文件 |

支持格式:

```text
jpg, jpeg, png, gif, webp
```

响应:

```json
{
  "code": 0,
  "message": "success",
  "data": null
}
```

说明:

```text
上传成功后，客户端不需要从返回值取头像信息。
需要刷新头像时，请重新请求 GET /accounts/{userId}/avatar。
```

### 下载头像

```http
GET /accounts/{userId}/avatar
```

示例:

```http
GET /accounts/{userId}/avatar
```

响应:

```text
二进制图片数据
Content-Type: image/jpeg | image/png | image/gif | image/webp
```

说明:

```text
客户端只传 userId。
服务端根据用户头像字段读取图片，不允许客户端直接传文件名。
```

Android 建议:

```text
Glide/Picasso/Coil 直接加载:
http://<server-host>:8080/accounts/{userId}/avatar
```

## 好友接口

### 查询好友列表

```http
GET /friends
token: <token>
```

响应 `data`:

```json
[
  {
    "userId": "<userId>",
    "nickname": "friend",
    "avatar": "<avatarFileName>"
  }
]
```

### 发起好友申请

```http
POST /friends
token: <token>
Content-Type: application/json
```

请求体二选一传 `friendUserId` 或 `friendNickname`，`message` 可选:

```json
{
  "friendUserId": "<userId>",
  "message": "我是 neon，想加你好友"
}
```

或:

```json
{
  "friendNickname": "neon",
  "message": "我是 neon，想加你好友"
}
```

字段说明:

| 字段 | Android 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| friendUserId | Long | 否 | 好友用户 ID，与 friendNickname 二选一 |
| friendNickname | String | 否 | 好友昵称，与 friendUserId 二选一 |
| message | String | 否 | 好友申请留言，最多 255 个字符 |

响应 `data`:

```json
{
  "id": "<requestId>",
  "requester": {
    "userId": "<userId>",
    "nickname": "neon",
    "avatar": "<avatarFileName>"
  },
  "addressee": {
    "userId": "<userId>",
    "nickname": "friend",
    "avatar": "<avatarFileName>"
  },
  "message": "我是 neon，想加你好友",
  "status": 0,
  "createTime": "2026-06-06T11:30:00"
}
```

说明:

```text
不能添加自己为好友。
发起申请后不会立刻成为好友，必须对方同意后才会创建好友关系。
同一对用户只保留一条申请记录；被拒绝后再次申请会刷新留言和状态。
如果对方已经给你发了待处理申请，请先调用同意或拒绝接口处理。
```

### 查询待处理好友申请

```http
GET /friends/requests/pending
token: <token>
```

响应 `data`:

```json
[
  {
    "id": "<requestId>",
    "requester": {
      "userId": "<userId>",
      "nickname": "neon",
      "avatar": "<avatarFileName>"
    },
    "addressee": {
      "userId": "<userId>",
      "nickname": "friend",
      "avatar": "<avatarFileName>"
    },
    "message": "我是 neon，想加你好友",
    "status": 0,
    "createTime": "2026-06-06T11:30:00"
  }
]
```

### 同意好友申请

```http
POST /friends/requests/{requestId}/accept
token: <token>
```

请求体: 无

响应 `data` 为申请发起人的用户信息:

```json
{
  "userId": "<userId>",
  "nickname": "neon",
  "avatar": "<avatarFileName>"
}
```

说明:

```text
只有申请接收人可以同意。
同意成功后双方正式成为好友，可以互相发送单聊消息。
```

### 拒绝好友申请

```http
POST /friends/requests/{requestId}/reject
token: <token>
```

请求体: 无

响应:

```json
{
  "code": 0,
  "message": "success",
  "data": null
}
```

## 会话接口

### 查询会话列表

```http
GET /conversations?limit=50
token: <token>
```

请求参数:

| 参数 | Android 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| limit | Int | 否 | 默认 50，最大 100 |

响应 `data`:

```json
[
  {
    "conversationId": "<conversationId>",
    "peer": {
      "userId": "<userId>",
      "nickname": "friend",
      "avatar": "<avatarFileName>"
    },
    "lastMessage": {
      "id": 1,
      "conversationId": "<conversationId>",
      "senderId": "<userId>",
      "receiverId": "<userId>",
      "clientMessageId": "client-msg-001",
      "messageType": 1,
      "content": "你好",
      "sendTime": "2026-06-04T09:51:00"
    },
    "lastMessageTime": "2026-06-04T09:51:00",
    "unreadCount": 1
  }
]
```

说明:

```text
用于微信首页会话列表。
会话按最后消息时间倒序排列。
unreadCount 表示当前用户在该会话中尚未标记已读的消息数量。
```

### 查询会话历史消息

```http
GET /conversations/{conversationId}/messages?beforeMessageId=<messageId>&limit=30
token: <token>
```

请求参数:

| 参数 | Android 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| beforeMessageId | Long | 否 | 只查询该消息 ID 之前的消息；不传则从最新消息开始 |
| limit | Int | 否 | 默认 30，最大 100 |

响应 `data`:

```json
{
  "messages": [
    {
      "id": 1,
      "conversationId": "<conversationId>",
      "senderId": "<userId>",
      "receiverId": "<userId>",
      "clientMessageId": "client-msg-001",
      "messageType": 1,
      "content": "你好",
      "sendTime": "2026-06-04T09:51:00"
    }
  ],
  "hasMore": false
}
```

说明:

```text
返回消息按发送时间正序排列，客户端可以直接渲染。
向上翻页时，把当前最早一条消息的 id 作为 beforeMessageId。
```

### 标记会话已读

```http
POST /conversations/{conversationId}/read
token: <token>
```

请求体: 无

响应:

```json
{
  "code": 0,
  "message": "success",
  "data": null
}
```

## 消息接口

### 发送单聊消息

```http
POST /messages/send
token: <token>
Content-Type: application/json
```

请求体:

```json
{
  "receiverId": 739093770475929601,
  "clientMessageId": "client-msg-001",
  "messageType": 1,
  "content": "你好"
}
```

字段说明:

| 字段 | Android 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| receiverId | Long | 是 | 接收者用户 ID |
| clientMessageId | String | 是 | 客户端生成的消息 ID，用于弱网重试幂等 |
| messageType | Int | 是 | 消息类型，目前 1 表示文本 |
| content | String | 是 | 消息内容 |

响应 `data`:

```json
{
  "id": 1,
  "conversationId": 739093770475929602,
  "senderId": 739093770475929600,
  "receiverId": 739093770475929601,
  "clientMessageId": "client-msg-001",
  "messageType": 1,
  "content": "你好",
  "sendTime": "2026-06-04T09:51:00"
}
```

客户端建议:

```text
clientMessageId 由 Android 本地生成 UUID。
如果网络重试传同一个 clientMessageId，服务端不会重复入库。
只能给已经互为好友的用户发送单聊消息。
```

### 离线消息同步

```http
POST /messages/offline-sync?limit=50
token: <token>
```

请求参数:

| 参数 | Android 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| limit | Int | 否 | 默认 50，最大 100 |

响应 `data`:

```json
{
  "messages": [
    {
      "id": 1,
      "conversationId": 739093770475929602,
      "senderId": 739093770475929601,
      "receiverId": 739093770475929600,
      "clientMessageId": "client-msg-001",
      "messageType": 1,
      "content": "你好",
      "sendTime": "2026-06-04T09:51:00"
    }
  ],
  "hasMore": false
}
```

说明:

```text
服务端会根据当前用户的同步游标返回未同步消息。
接口成功后，服务端推进对应会话的同步位置。
如果 hasMore=true，客户端应继续调用该接口。
```

## WebSocket 实时接收

连接地址:

```text
ws://<server-host>:8080/ws/messages?token=<token>
```

连接成功后，别人发给当前用户的消息会实时推送。

推送格式:

```json
{
  "type": "message",
  "data": {
    "id": 1,
    "conversationId": 739093770475929602,
    "senderId": 739093770475929601,
    "receiverId": 739093770475929600,
    "clientMessageId": "client-msg-001",
    "messageType": 1,
    "content": "你好",
    "sendTime": "2026-06-04T09:51:00"
  }
}
```

心跳:

```text
客户端发送: ping
服务端返回: pong
```

Android 建议:

```text
App 登录成功后建立 WebSocket。
WebSocket 断开后重连。
重连成功后调用 /messages/offline-sync 补齐断线期间消息。
```

## 错误码

| 错误码 | 说明 |
| --- | --- |
| 0 | 成功 |
| 400 | 请求参数不合法 |
| 401 | 未登录或登录已失效 |
| 1002 | 昵称或密码错误 |
| 1003 | 用户不存在 |
| 1004 | 昵称已存在 |
| 2001 | 不能给自己发送消息 |
| 2002 | 接收者不存在 |
| 2003 | 只能给好友发送消息 |
| 2004 | 会话不存在 |
| 4001 | 不能添加自己为好友 |
| 4002 | 好友不存在 |
| 4003 | 已经是好友 |
| 4004 | 好友申请不存在 |
| 4005 | 不能处理别人的好友申请 |
| 4006 | 好友申请已处理 |
| 4007 | 对方已申请添加你为好友，请先处理申请 |
| 3001 | 头像上传失败 |
| 3002 | 头像保存失败 |
| 3003 | 头像不存在 |
| 3004 | 头像文件不能为空 |
| 3005 | 头像格式不支持 |
| 500 | 服务器内部错误 |
