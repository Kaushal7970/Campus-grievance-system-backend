# Campus Grievance Management System — Project Report (Hindi) + PPT Outline

**Date:** 2026-04-05  
**Workspace folders:** `grievance-frontend` (React) + `grievance-system` (Spring Boot)

---

## 1) Project ka overview (What problem it solves)
Ye system college/university me student grievances/complaints ko **online submit, track, assign, resolve** karne ke liye bana hai.

Core objectives:
- Student ko complaint register karna + status track karna
- Admin/HOD/Principal/Committee ko complaints manage karna
- Faculty ko assigned complaints handle karna
- **Realtime updates** (without manual refresh)
- **Notifications** + **Announcements**
- Optional: **AI assistant** (complaint classification + chatbot)
- Automatic **time-based escalation** (SLA follow-up)

---

## 2) High-level architecture

### 2.1 Frontend (grievance-frontend)
- React app (HashRouter based routes)
- Axios API client: base URL = `${REACT_APP_API_URL}/api`
- JWT token local/session storage me store hota hai
- Role-based navigation via `ProtectedRoute`
- Realtime: STOMP over SockJS (`/ws`) subscription

### 2.2 Backend (grievance-system)
- Spring Boot 3.x (pom me parent `3.5.13`)
- Spring Security + JWT (stateless)
- Spring Data JPA (PostgreSQL/MySQL/H2)
- WebSocket/STOMP broker topics (`/topic/...`)
- Scheduler (hourly) for escalation
- File uploads local disk folder me
- AI integration (OpenAI compatible / Gemini) + project knowledge (`resources/ai/knowledge.md`)

---

## 3) User roles (system me kaun kaun)
Code ke basis par supported roles:
- `STUDENT`
- `FACULTY`
- `HOD`
- `PRINCIPAL`
- `COMMITTEE`
- `ADMIN`
- `SUPER_ADMIN`

Role ke hisaab se dashboards:
- Student: complaint submit + apni complaints list
- Faculty: assigned complaints list + status update/remarks
- HOD/Principal/Committee: Admin-style dashboard (complaints monitoring + assignment)
- Admin/SuperAdmin: complaints + user management + AI rules editing

---

## 4) Frontend pages aur features (UX / flows)

### 4.1 Public pages
- **Login**: email/password → backend `/api/auth/login` → token store + role-based redirect
- **Register**: self registration (backend forces role = STUDENT)
- **Forgot Password**: OTP generate
- **Reset Password**: OTP verify + new password set

### 4.2 Protected pages
- **Student Dashboard**
  - New complaint submit (title, description, priority, category)
  - Optional: *anonymous submit*
  - Optional: attachments upload (image/video)
  - AI suggestion button: `/api/ai/chat` (category/priority suggestion)
  - My grievances list: `/api/grievance/student/{email}`

- **Admin Dashboard (also used by HOD/Principal/Committee/SuperAdmin UI wrapper)**
  - All grievances view: `/api/grievance/all`
  - Filters: priority/status/category/assigned + search
  - Assign faculty: `/api/grievance/assign/{id}?facultyEmail=...`
  - Status update + remarks: `/api/grievance/update-with-remarks/{id}?status=...&remarks=...`
  - CSV/PDF export (frontend-only)
  - User management (only ADMIN/SUPER_ADMIN): `/api/users` CRUD + role change
  - AI rules editor (only ADMIN/SUPER_ADMIN): `/api/admin/ai-rules`

- **Faculty Dashboard**
  - Assigned complaints: `/api/grievance/faculty/{email}`
  - Quick status update with remarks
  - Charts: priority + status distribution

- **Grievance Details (common for all logged-in users)**
  - Full details: `/api/grievance/{id}`
  - Status history: `/api/grievance/{id}/history`
  - Escalation history: `/api/grievance/{id}/escalations`
  - Comments (staff can mark internal-only): `/api/grievance/{id}/comments`
  - Live chat: `/api/grievance/{id}/chat`
  - Attachments list/upload/download
  - Realtime refresh via websocket topics

- **Announcements**
  - List announcements: `/api/announcements`
  - Privileged create/delete (Admin/HOD/Principal/Committee)
  - Realtime updates via `/topic/announcements`

- **Profile**
  - Email update: `/api/users/{id}/update`
  - Password change: `/api/users/{id}/change-password?password=...`

- **Notifications bell (Sidebar top)**
  - Unread count: `/api/notifications/unread-count`
  - List: `/api/notifications`
  - Mark read: `/api/notifications/{id}/read`
  - Realtime topic: `/topic/notifications/{email}`

- **AI Chatbot widget**
  - Status: `/api/ai/status`
  - Chat: `/api/ai/bot`

---

## 5) Backend modules (Controllers / Services)

### 5.1 Auth & Security
**AuthController** (base: `/api/auth` + `/auth`)
- `POST /login` → JWT token return
- `POST /register` → new user (role forced STUDENT)
- `POST /forgot-password` → OTP generate (dev option: OTP return)
- `POST /reset-password` → OTP verify + password change

**SecurityConfig** highlights:
- Stateless JWT filter
- Permit: `/api/auth/**`, `/api/ai/**`, `/ws/**`, `/api/public/**`, swagger routes
- RBAC for grievances/users/announcements/notifications

JWT:
- issuer + secret + ttl from `application.properties`
- token claim: `roles`

Login lockout:
- max attempts + lockout minutes (configurable)

### 5.2 Grievance core
**GrievanceController** (base: `/api/grievance`)
- `POST /create` (Student/Admin/SuperAdmin)
- `GET /all` (Admin/SuperAdmin/HOD/Principal/Committee)
- `GET /student/{email}` (owner or admin)
- `GET /faculty/{email}` (faculty owner or admin)
- `GET /{id}` (authenticated)
- `PUT /assign/{id}` (HOD/Principal/Committee/Admin/SuperAdmin)
- `PUT /update/{id}?status=` (staff)
- `PUT /update-with-remarks/{id}?status=&remarks=` (staff)
- `GET /{id}/history`
- `GET /{id}/escalations`
- `POST /{id}/escalate?level=&reason=` (manual escalation; privileged)
- `POST /{id}/comments` + `GET /{id}/comments`
- `POST /{id}/chat` + `GET /{id}/chat`
- `POST /{id}/attachments` + `GET /{id}/attachments`
- `GET /attachments/{attachmentId}/download`
- `DELETE /delete/{id}`

**Important behaviors:**
- Complaint code auto-generate: `CMP-<YEAR>-<6 digit>`
- Anonymous complaints: studentEmail response me hide hota hai for non-privileged roles
- Status change history auto-save hoti hai

### 5.3 Collaboration (comments/chat/attachments)
**GrievanceCollaborationService**
- Comments: internalOnly = staff notes (student ko notify nahi)
- Chat: message save + realtime chat topic publish
- Attachments: local filesystem upload (`uploads/grievances/<id>/...`) + DB metadata

### 5.4 Escalation (SLA)
**EscalationScheduler**
- Har 1 hour me run
- Active grievances (not RESOLVED/CLOSED) me `autoEscalateIfNeeded()`

**EscalationProperties** (`app.escalation.*`)
- facultyDays default 2
- hodDays default 4
- principalDays default 7
- adminDays default 10

Auto escalation effects:
- `status` becomes `ESCALATED_*`
- escalation history store
- automatic assignment: target role ka “first user by id” assignee

### 5.5 Notifications
**NotificationController** (base: `/api/notifications`)
- list mine, unread-count, mark read
- admin utility: send notification to user/role

**NotificationService**
- sendToUser() saves + publishes realtime to `/topic/notifications/{email}`

Notifications triggers (actual code):
- New complaint by STUDENT → ADMIN + SUPER_ADMIN notify
- Staff updates status/remarks/comment/chat/attachment → student notify
- Escalation → relevant roles notify

### 5.6 Announcements
**AnnouncementController** (base: `/api/announcements`)
- `GET` list (role-based audience filtering)
- `POST` create (privileged)
- `DELETE` delete (privileged)
- Realtime publish: `/topic/announcements`

### 5.7 AI module
**AiController** (base: `/api/ai`)
- `POST /chat` → grievance text se classification JSON
- `POST /bot` → general chatbot
- `GET /status` → provider + enabled

**AiChatService**
- Provider: `openai` OR `gemini` (config)
- Knowledge source: `resources/ai/knowledge.md`
- Admin rules: `/api/admin/ai-rules` (highest priority)
- If API key missing → heuristic fallback (basic classification)

---

## 6) Data model (DB entities) — important tables

Main entities (code me present):
- `User`: name, email(unique), password(hash), role, login-lockout fields
- `Grievance`: title, description, complaintCode(unique), status, category(enum), studentEmail, anonymous, assignedTo, remarks, priority(enum), timestamps, deadlineAt, escalationLevel(enum)
- `GrievanceStatusHistory`: fromStatus/toStatus/changedAt/changedBy/note
- `GrievanceEscalationHistory`: fromLevel/toLevel/automatic/triggeredBy/reason
- `GrievanceComment`: message, authorEmail, internalOnly
- `GrievanceChatMessage`: senderEmail, message
- `Attachment`: originalFileName, storedFileName, storagePath, contentType, size, uploadedBy
- `Announcement`: title, message, createdByEmail, audienceRole(optional)
- `Notification`: recipientEmail, message, type, grievanceId(optional), readAt
- `PasswordResetToken`: email, otp, expiresAt, used
- `AiAdminRules`: rules text, updatedAt, updatedByEmail

---

## 7) Realtime system (WebSocket / STOMP)
Backend endpoint:
- SockJS endpoint: `/ws`

Frontend connects using `@stomp/stompjs` + `sockjs-client`.

Topics used:
- `/topic/grievance` (global grievance events)
- `/topic/grievance/{id}` (single grievance changes)
- `/topic/grievance/{id}/chat` (chat events)
- `/topic/announcements` (announcement created/deleted)
- `/topic/notifications/{email}` (per-user notifications)

Realtime payload generally includes type + ids + timestamp.

---

## 8) End-to-end working (step-by-step)

### Flow A — Student complaint submit
1. Student login → JWT token set
2. Student dashboard form fill (title/desc/category/priority)
3. Optional: **Analyze with AI** → `/api/ai/chat` returns suggested category/priority
4. Submit → `POST /api/grievance/create`
5. Backend saves grievance + generates complaint code + status history
6. Realtime event publish (`CREATED`) + Admin/SuperAdmin notifications
7. Optional attachments: `POST /api/grievance/{id}/attachments`

### Flow B — Admin/HOD/Committee monitoring + assignment
1. Admin/HOD view all grievances → `GET /api/grievance/all`
2. Assign faculty → `PUT /api/grievance/assign/{id}`
3. Student ko notification (assignment updated)
4. Realtime event publish (`ASSIGNED`)

### Flow C — Faculty resolution
1. Faculty dashboard → `GET /api/grievance/faculty/{email}`
2. Update status + remarks → `PUT /api/grievance/update-with-remarks/{id}`
3. Student ko notification + history update + realtime

### Flow D — Comments and internal notes
- Comment add → `POST /api/grievance/{id}/comments`
- If `internalOnly=true` (only staff) → student ko notify nahi
- Privileged users internal comments dekh sakte hain

### Flow E — Live chat
- Send chat → `POST /api/grievance/{id}/chat`
- Backend publishes chat topic event → UI auto-refresh chat

### Flow F — Auto escalation (SLA)
- Every 1 hour scheduler runs
- Age (days) threshold cross → escalation level increase
- Status becomes `ESCALATED_*`
- Auto assign (target role ka first user)
- Student + relevant roles ko notifications

### Flow G — Announcements
- Privileged user announcement create/delete
- All relevant users list page par realtime update

### Flow H — Password reset (OTP)
1. User enters email → `POST /api/auth/forgot-password`
2. OTP token DB me store with TTL
3. User submits OTP + new password → `POST /api/auth/reset-password`

---

## 9) Configuration / Deployment notes (important for PPT)
Backend env variables (see `application.properties`):
- `PORT` (default 8080)
- Database: `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`
- JWT: `JWT_SECRET`, `JWT_ISSUER`, `JWT_ACCESS_TTL_SECONDS`
- Upload path: `UPLOAD_DIR`
- Escalation: `ESCALATION_*`
- AI: `AI_PROVIDER`, `OPENAI_API_KEY`, `GEMINI_API_KEY` / `GOOGLE_API_KEY`

Frontend env variables:
- `REACT_APP_API_URL` (backend base URL)
- Optional `REACT_APP_API_TIMEOUT_MS`

---

## 10) PPT ke liye ready slide outline (copy/paste)

### Slide 1 — Title
- Campus Grievance Management System
- Team/Name/College
- Date

### Slide 2 — Problem Statement
- Manual grievance handling slow + non-transparent
- No single tracking system
- Need role-based accountability

### Slide 3 — Solution Overview
- Online complaint submission + tracking
- Assignment + remarks + resolution workflow
- Realtime updates + notifications
- Announcements + AI assistant

### Slide 4 — Tech Stack
- Frontend: React, Tailwind, Axios, STOMP/SockJS
- Backend: Spring Boot, Spring Security, JWT, JPA, WebSocket
- DB: PostgreSQL/MySQL (H2 for tests)

### Slide 5 — Roles & Access
- Student: create + track
- Faculty: work on assigned
- HOD/Principal/Committee: monitor + assign
- Admin/SuperAdmin: full control + user management

### Slide 6 — Frontend Screens
- Login/Register/Forgot/Reset
- Dashboards (Student/Admin/Faculty)
- Grievance Details (history/comments/chat/attachments)
- Announcements + Profile

### Slide 7 — Backend API Modules
- Auth, Users
- Grievance core
- Notifications
- Announcements
- AI endpoints

### Slide 8 — Complaint Lifecycle
- SUBMITTED → UNDER_REVIEW → ASSIGNED → IN_PROGRESS → RESOLVED/CLOSED
- Remarks + history stored
- Anonymous handling

### Slide 9 — Realtime Architecture
- WebSocket endpoint `/ws`
- Topics: grievances, grievance/{id}, chat, notifications/email, announcements
- UI auto-refresh on events

### Slide 10 — Notifications System
- New complaint → admin notify
- Staff update → student notify
- Escalation → role-based notify

### Slide 11 — Auto Escalation (SLA)
- Scheduler every hour
- Day-based thresholds (2/4/7/10 default)
- Escalation history + auto assignment

### Slide 12 — AI Features
- AI suggestion on complaint submit (category/priority + suggested action)
- AI chatbot for general help
- Admin rules + knowledge base

### Slide 13 — Database Design (Entities)
- Users, Grievances, StatusHistory, EscalationHistory
- Comments, ChatMessages, Attachments
- Announcements, Notifications, PasswordResetToken

### Slide 14 — Security
- JWT-based stateless auth
- Role-based API access (Spring Security)
- Account lockout on failed attempts

### Slide 15 — Demo Walkthrough
- Login as student → submit grievance + attachment
- Admin assigns faculty
- Faculty resolves with remarks
- Show realtime notifications + history

---

## 11) Short demo script (presentation me bolne ke liye)
- “Student login karta hai, complaint submit karta hai, optionally AI se category/priority suggestion le sakta hai.”
- “Admin/HOD dashboard me sare complaints dekh kar faculty assign karta hai.”
- “Faculty assigned complaint par work karta hai, status/remarks update karta hai.”
- “Student ko realtime notifications milti hain, aur grievance details page par history, comments, chat aur attachments dikhte hain.”
- “System SLA ke basis par time pass hone par auto-escalation bhi karta hai.”

---

## 12) Optional: PPT tips (quick)
- Har slide me 4–6 bullets max rakho
- Screenshots: Student dashboard, Admin dashboard, Grievance details, Notifications, Announcements
- 1 slide architecture diagram (frontend → backend → DB + WS)

