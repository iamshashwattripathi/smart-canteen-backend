# Smart Canteen Billing System — Backend

**Web Matrix Project | UCER Prayagraj | Dr. APJ Abdul Kalam Technical University, Lucknow**

> Team: Harsh Srivastava · Harsh Pandey · Shaswat Tripathi · Anubhav Srivastava  
> Supervisor: Mr. Bhanu Pratap Rai

Java Spring Boot · MySQL · JWT · WebSocket · Claude AI Chatbot

---

## Quick Start (Docker — Windows / macOS / Linux / Android)

### Prerequisites
- [Docker Desktop](https://www.docker.com/products/docker-desktop/) installed
- A Claude API key from [console.anthropic.com](https://console.anthropic.com/)

### 1. Set your Claude API key

**Windows (CMD):**
```cmd
set CLAUDE_API_KEY=sk-ant-api03-...
```

**macOS / Linux:**
```bash
export CLAUDE_API_KEY=sk-ant-api03-...
```

**Android (Termux + Docker):**
```bash
export CLAUDE_API_KEY=sk-ant-api03-...
```

### 2. Start everything

```bash
cd smart-canteen-backend
docker-compose up --build
```

MySQL + Spring Boot start automatically. Visit:
- **API Base:** http://localhost:8080/api
- **WebSocket:** ws://localhost:8080/ws

### 3. Stop

```bash
docker-compose down        # stop
docker-compose down -v     # stop + delete database
```

---

## Manual Setup (without Docker)

### Prerequisites
- Java 17+, Maven 3.8+, MySQL 8

```bash
# 1. Create database
mysql -u root -p < database.sql

# 2. Set credentials + API key
#    Edit: src/main/resources/application.properties
#    spring.datasource.password=YOUR_MYSQL_PASSWORD
#    app.ai.chatbot.api-key=YOUR_CLAUDE_KEY

# 3. Run
mvn spring-boot:run
```

---

## Complete API Reference

### Authentication  `/api/auth`

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| POST | `/api/auth/register` | Public | Register new user |
| POST | `/api/auth/login` | Public | Login, get JWT token |
| POST | `/api/auth/wallet/topup` | JWT | Top up wallet balance |

**Register:** `{ "name", "email", "password", "phone" }`  
**Login response:** `{ "token", "name", "email", "role", "walletBalance" }`

---

### Menu  `/api/menu`

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| GET | `/api/menu` | Public | All items grouped by category |
| GET | `/api/menu/all` | Public | Flat list |
| GET | `/api/menu/{id}` | Public | Single item |
| POST | `/api/menu` | ADMIN | Add item |
| PUT | `/api/menu/{id}` | ADMIN | Update item |
| PATCH | `/api/menu/{id}/toggle` | ADMIN | Toggle availability |
| DELETE | `/api/menu/{id}` | ADMIN | Delete item |

---

### Orders  `/api/orders`

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| POST | `/api/orders/place` | JWT | Place an order |
| GET | `/api/orders/user/{userId}` | JWT | Order history |
| GET | `/api/orders/track/{orderCode}` | Public | Track by order code |
| PATCH | `/api/orders/{id}/status` | ADMIN/STAFF | Update status |
| GET | `/api/orders/queue/snapshot` | ADMIN | Queue depths per stall |

**Place order body:**
```json
{
  "userId": 1,
  "items": [
    { "menuItemId": 1, "quantity": 2 },
    { "menuItemId": 5, "quantity": 1 }
  ],
  "paymentMethod": "WALLET"
}
```
**Status flow:** `PENDING → PREPARING → READY → COMPLETED / CANCELLED`

---

### Tokens / Queue  `/api/tokens`

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| POST | `/api/tokens/generate` | Public | Get next token number |
| GET | `/api/tokens/queue` | Public | Waiting/serving/done counts |
| PATCH | `/api/tokens/{id}/status` | ADMIN/STAFF | Update token status |

---

### Inventory  `/api/inventory`  *(ADMIN only)*

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| GET | `/api/inventory` | ADMIN | All stock |
| GET | `/api/inventory/low` | ADMIN | Low-stock alerts |
| POST | `/api/inventory` | ADMIN | Add/update material |
| DELETE | `/api/inventory/{id}` | ADMIN | Remove material |

---

### Reports  `/api/reports`  *(ADMIN only)*

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| GET | `/api/reports/dashboard` | ADMIN | Summary stats |
| GET | `/api/reports/sales-trend` | ADMIN | 7-day revenue chart |
| GET | `/api/reports/top-items` | ADMIN | Top selling items |

---

### Feedback  `/api/feedback`

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| POST | `/api/feedback` | Public | Submit feedback |
| GET | `/api/feedback/my/{userId}` | JWT | My feedback history |
| GET | `/api/feedback/all` | ADMIN | All feedback |

**Body:**
```json
{
  "userId": 1,
  "rating": 4,
  "comment": "Great food!",
  "category": "FOOD_QUALITY",
  "orderId": 12
}
```
**Categories:** `FOOD_QUALITY, SERVICE, APP_EXPERIENCE, CLEANLINESS, GENERAL`

---

### Notifications  `/api/notifications`

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| GET | `/api/notifications/{userId}` | JWT | All notifications |
| GET | `/api/notifications/{userId}/unread` | JWT | Unread only |
| GET | `/api/notifications/{userId}/count` | JWT | Unread badge count |
| PATCH | `/api/notifications/{id}/read` | JWT | Mark one read |
| PATCH | `/api/notifications/{userId}/read-all` | JWT | Mark all read |

Also pushed live via WebSocket: `/topic/notifications/{userId}`

---

### AI Chatbot  `/api/chat`

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| POST | `/api/chat/message` | Public | Send message, get AI reply |
| GET | `/api/chat/history/{sessionId}` | Public | Load chat history |
| DELETE | `/api/chat/session/{sessionId}` | Public | Clear session |

**Send message body:**
```json
{
  "sessionId":   "550e8400-e29b-41d4-a716-446655440000",
  "message":     "What veg options do you have?",
  "pageContext": "menu"
}
```
**pageContext values:** `home, menu, cart, orders, track, wallet, queue, profile, admin`

**Response:**
```json
{
  "reply":     "We have Dal Tadka (₹63), Rajma (₹115)...",
  "sessionId": "550e8400-...",
  "timestamp": "2025-05-01T12:00:00"
}
```

**Frontend JS integration:**
```javascript
const sessionId = localStorage.getItem('chatSessionId') || crypto.randomUUID();
localStorage.setItem('chatSessionId', sessionId);

const res = await fetch('/api/chat/message', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({ sessionId, message: userInput, pageContext: 'menu' })
});
const { reply } = await res.json();
```

---

### AI Food Suggestion  `/api/suggest`

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| GET | `/api/suggest` | Public | Most popular item recommendation |

---

## WebSocket (Real-time)

Connect with SockJS + STOMP:

```javascript
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

const client = new Client({
  webSocketFactory: () => new SockJS('http://localhost:8080/ws'),
  onConnect: () => {
    // Order status updates
    client.subscribe(`/topic/order/${orderCode}`, msg => {
      console.log(JSON.parse(msg.body));
    });

    // Push notifications
    client.subscribe(`/topic/notifications/${userId}`, msg => {
      showToast(JSON.parse(msg.body).message);
    });
  }
});
client.activate();
```

---

## Default Credentials

| Role | Email | Password |
|------|-------|----------|
| Admin | admin@canteen.com | admin123 |

---

## Authorization

All protected endpoints require:
```
Authorization: Bearer <jwt_token>
```

Token comes from login response. Valid for 7 days.
