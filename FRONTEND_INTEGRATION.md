# Citizen Connect - Frontend Integration Guide

## 🔗 Backend Connection Details

### Base URL
```
Development: http://localhost:8080
Production:  https://your-domain.com  (if deployed)
```

### Server Status
- **Health Check**: `GET /health` - Returns `{ "status": "UP" }`
- **Root Info**: `GET /` - Returns service info and links
- **Swagger UI**: `http://localhost:8080/swagger-ui/index.html`
- **API Docs**: `GET /v3/api-docs`

---

## 🔐 Authentication Flow

### 1. User Signup (Registration)
```javascript
POST /auth/signup
Content-Type: application/json

{
  "name": "John Doe",
  "email": "john@example.com",
  "password": "securePassword123",
  "role": "CITIZEN",  // or POLITICIAN, MODERATOR, ADMIN
  "region": "NORTH"   // NORTH, SOUTH, EAST, WEST
}

Response (200):
{
  "message": "Registered. You can log in with email and password (login sends an OTP)."
}

Response (409):
{
  "status": 409,
  "error": "Conflict",
  "message": "A user with this email already exists."
}
```

### 2. User Login (Initiates OTP)
```javascript
POST /auth/login
Content-Type: application/json

{
  "email": "john@example.com",
  "password": "securePassword123"
}

Response (200):
{
  "message": "OTP sent to your email. It expires in 10 minutes."
}

Response (401):
{
  "status": 401,
  "error": "Unauthorized",
  "message": "Invalid credentials."
}
```

**Note**: In local development with `app.mail.log-otp-to-console=true`, the OTP appears in backend console logs.

### 3. Verify OTP & Get JWT Token
```javascript
POST /auth/verify-login
Content-Type: application/json

{
  "email": "john@example.com",
  "otp": "123456"  // 6-digit code from email (or console in dev)
}

Response (200):
{
  "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJqb2huQGV4YW1wbGUuY29tIiwiaWF0IjoxNjEwNzE...",
  "email": "john@example.com",
  "role": "CITIZEN"
}

Response (401/400):
{
  "status": 401,
  "error": "Unauthorized",
  "message": "Invalid OTP." / "OTP expired"
}
```

---

## 🔑 JWT Token Usage

All authenticated routes require the JWT token in the Authorization header:

```javascript
headers: {
  "Authorization": `Bearer ${token}`,
  "Content-Type": "application/json"
}
```

### Example: Get User Profile
```javascript
GET /auth/profile
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...

Response (200):
{
  "name": "John Doe",
  "email": "john@example.com",
  "phone": null,
  "address": null,
  "region": "NORTH",
  "designation": null,
  "constituency": null,
  "bio": null,
  "profilePhotoUrl": null
}
```

---

## 👥 Role-Based API Endpoints

### CITIZEN Routes (`/citizen/**`)
Requires: JWT token with role CITIZEN

```javascript
// Get citizen dashboard
GET /citizen/dashboard
Response: {
  "totalIssues": 5,
  "inProgress": 2,
  "resolved": 3,
  "recentIssues": [...]
}

// Get account statistics
GET /citizen/account-stats
Response: {
  "totalIssuesRaised": 5,
  "issuesResolved": 3,
  "memberSince": "2026-04-11T14:56:00"
}

// Raise a new issue
POST /citizen/issues
{
  "title": "Pothole on Main Street",
  "description": "Large pothole causing traffic issues",
  "region": "NORTH",        // Optional, defaults to user's region
  "severity": "HIGH"        // or LOW, MEDIUM
}

// Get user's issues
GET /citizen/issues
Response: [
  {
    "id": 1,
    "title": "Pothole on Main Street",
    "description": "Large pothole causing traffic issues",
    "region": "NORTH",
    "severity": "HIGH",
    "status": "IN_PROGRESS",
    "statusLabel": "In Progress",
    "createdAt": "2026-04-11T14:56:00",
    "resolvedAt": null,
    "userName": "John Doe"
  }
]

// Get trending issues
GET /citizen/trending?region=NORTH
Response: {
  "low": 10,
  "medium": 25,
  "high": 8,
  "highSeverityIssues": [...]
}

// Get announcements
GET /citizen/announcements
Response: [
  {
    "id": 1,
    "content": "New traffic rules coming next week",
    "authorName": "Jane Smith",
    "region": "NORTH",
    "createdAt": "2026-04-11T10:00:00"
  }
]

// Rate a resolved issue (1-5 stars)
POST /citizen/issues/{id}/rating
{
  "rating": 5
}
Response: {
  "message": "Thanks for your feedback."
}
```

### POLITICIAN Routes (`/politician/**`)
Requires: JWT token with role POLITICIAN

```javascript
// Get politician dashboard
GET /politician/dashboard
Response: {
  "totalIssues": 42,
  "resolved": 38,
  "pending": 4,
  "recentHighPriority": [...]
}

// Get zone issues
GET /politician/issues
Response: [
  {
    "id": 1,
    "title": "Pothole on Main Street",
    "region": "NORTH",
    "severity": "HIGH",
    "status": "PENDING",
    "createdAt": "2026-04-11T14:56:00"
  }
]

// Update issue status (PENDING → IN_PROGRESS → RESOLVED)
PUT /politician/issues/{id}/status
{
  "status": "IN_PROGRESS"  // or RESOLVED, PENDING
}

// Get analytics (issues solved & ratings per month)
GET /politician/analytics
Response: {
  "issuesSolvedPerMonth": [
    { "label": "Jan", "count": 5 },
    { "label": "Feb", "count": 8 }
  ],
  "surveyRatingPerMonth": [
    { "label": "Jan", "averageRating": 4.2 },
    { "label": "Feb", "averageRating": 4.5 }
  ]
}

// Publish announcement
POST /politician/announcements
{
  "content": "New traffic rules coming next week",
  "region": "NORTH"  // Optional, defaults to politician's region
}

// Get own announcements
GET /politician/announcements
```

### MODERATOR Routes (`/moderator/**`)
Requires: JWT token with role MODERATOR

```javascript
// Get all issues (across all regions)
GET /moderator/issues
Response: [...]

// Delete inappropriate issue
DELETE /moderator/issues/{id}
Response (204): No content
```

### ADMIN Routes (`/admin/**`)
Requires: JWT token with role ADMIN

```javascript
// Get system overview dashboard
GET /admin/dashboard
Response: {
  "totalUsers": 150,
  "activeCitizens": 120,
  "moderators": 5,
  "politicians": 15,
  "totalIssues": 342,
  "newRegistrationsToday": 3,
  "flaggedAccounts": 2,
  "blockedUsers": 1,
  "pendingIssues": 45,
  "inProgressIssues": 120,
  "resolvedIssues": 177,
  "systemHealth": {
    "serverStatus": "Online",
    "database": "Connected",
    "lastBackup": "N/A"
  },
  "recentActivity": [...]
}
```

---

## 📊 Data Models

### User Registration
```javascript
{
  "name": string (required),              // User's full name
  "email": string (required, unique),    // Valid email
  "password": string (required, min 6),  // At least 6 characters
  "role": "CITIZEN" | "POLITICIAN" | "MODERATOR",  // Required
  "region": "NORTH" | "SOUTH" | "EAST" | "WEST"    // Required for citizens
}
```

### Issue/Complaint
```javascript
{
  "id": number,
  "title": string,
  "description": string,
  "region": "NORTH" | "SOUTH" | "EAST" | "WEST",
  "severity": "LOW" | "MEDIUM" | "HIGH",
  "status": "PENDING" | "IN_PROGRESS" | "RESOLVED",
  "statusLabel": string,  // Human-readable: "In Progress", "Resolved", "Pending"
  "createdAt": datetime,
  "resolvedAt": datetime | null,
  "userName": string
}
```

### User Profile
```javascript
{
  "name": string,
  "email": string,
  "phone": string | null,
  "address": string | null,
  "region": "NORTH" | "SOUTH" | "EAST" | "WEST",
  "designation": string | null,      // For politicians
  "constituency": string | null,     // For politicians
  "bio": string | null,
  "profilePhotoUrl": string | null
}
```

---

## ❌ Error Responses

### 400 - Bad Request (Validation)
```javascript
{
  "timestamp": "2026-04-11T14:56:00+05:30",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed.",
  "path": "/auth/signup",
  "errors": {
    "email": "Invalid email format",
    "password": "Must be at least 6 characters"
  }
}
```

### 401 - Unauthorized
```javascript
{
  "timestamp": "2026-04-11T14:56:00+05:30",
  "status": 401,
  "error": "Unauthorized",
  "message": "Authentication required.",
  "path": "/citizen/issues"
}
```

### 403 - Forbidden (Role/Permission)
```javascript
{
  "timestamp": "2026-04-11T14:56:00+05:30",
  "status": 403,
  "error": "Forbidden",
  "message": "You do not have access to this resource.",
  "path": "/admin/dashboard"
}
```

### 404 - Not Found
```javascript
{
  "timestamp": "2026-04-11T14:56:00+05:30",
  "status": 404,
  "error": "Not Found",
  "message": "Complaint not found.",
  "path": "/citizen/issues/999"
}
```

### 409 - Conflict (Email exists)
```javascript
{
  "timestamp": "2026-04-11T14:56:00+05:30",
  "status": 409,
  "error": "Conflict",
  "message": "A user with this email already exists.",
  "path": "/auth/signup"
}
```

### 429 - Too Many Requests (OTP cooldown)
```javascript
{
  "timestamp": "2026-04-11T14:56:00+05:30",
  "status": 429,
  "error": "Too Many Requests",
  "message": "Please wait before requesting another OTP",
  "path": "/auth/login"
}
```

### 500 - Internal Server Error
```javascript
{
  "timestamp": "2026-04-11T14:56:00+05:30",
  "status": 500,
  "error": "Internal Server Error",
  "message": "Something went wrong. Please try again later.",
  "path": "/auth/signup"
}
```

---

## 🔄 CORS Configuration

The API allows requests from all origins:
```javascript
Access-Control-Allow-Origin: *
Access-Control-Allow-Methods: GET, POST, PUT, PATCH, DELETE, OPTIONS
Access-Control-Allow-Headers: *
Access-Control-Allow-Credentials: true
```

No CORS errors should occur when frontend calls the API.

---

## 📝 Example Frontend Implementation

### React/JavaScript Example
```javascript
const API_URL = 'http://localhost:8080';

// Login
async function login(email, password) {
  try {
    // Step 1: Login (send OTP)
    await fetch(`${API_URL}/auth/login`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ email, password })
    });
    
    // Step 2: Get OTP from email (or console in dev)
    // User enters OTP in UI
    
    // Step 3: Verify OTP
    const loginRes = await fetch(`${API_URL}/auth/verify-login`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ email, otp })
    });
    
    const data = await loginRes.json();
    localStorage.setItem('token', data.token);
    localStorage.setItem('email', data.email);
    localStorage.setItem('role', data.role);
    return data;
  } catch (error) {
    console.error('Login failed:', error);
  }
}

// Get user issues
async function getIssues() {
  const token = localStorage.getItem('token');
  const res = await fetch(`${API_URL}/citizen/issues`, {
    headers: {
      'Authorization': `Bearer ${token}`
    }
  });
  return res.json();
}

// Raise new issue
async function raiseIssue(title, description, severity) {
  const token = localStorage.getItem('token');
  const res = await fetch(`${API_URL}/citizen/issues`, {
    method: 'POST',
    headers: {
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    },
    body: JSON.stringify({ title, description, severity })
  });
  return res.json();
}
```

---

## 🧪 Testing with Postman

1. **Create environment variables**:
   - `base_url`: http://localhost:8080
   - `token`: (empty initially)

2. **Signup**: POST `{{base_url}}/auth/signup`
3. **Login**: POST `{{base_url}}/auth/login`
4. **Verify OTP**: POST `{{base_url}}/auth/verify-login` → Save token
5. **Use token**: Add header `Authorization: Bearer {{token}}`

---

## 🔍 Debugging

### Backend Logs
```bash
# Follow logs
tail -f /path/to/logs/app.log

# For SQL debugging:
SHOW_SQL=true java -jar target/citizenconnect.jar
```

### Database Check (Local H2)
```
URL: http://localhost:8080/h2-console
JDBC URL: jdbc:h2:mem:citizen_connect
User: sa
Password: (empty)
```

### API Testing
```bash
# Health check
curl http://localhost:8080/health

# Swagger UI
open http://localhost:8080/swagger-ui/index.html
```

---

## 📱 Common Issues & Solutions

| Issue | Solution |
|-------|----------|
| "Authentication required" (401) | JWT token missing or expired → Re-login |
| "Invalid credentials" (401) | Email/password incorrect |
| "OTP expired" (400) | Restart login flow to get new OTP |
| "Please wait before requesting OTP" (429) | Wait 60 seconds (or less in local mode) |
| CORS error | Ensure API is running and Check browser console |
| "User not found" (401) | Create account first with signup |
| Empty response | Check if token is valid |

---

## 📞 Support

For API documentation details, visit: `http://localhost:8080/swagger-ui/index.html`

