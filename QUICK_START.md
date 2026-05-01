# Quick Start Guide - Citizen Connect Backend

## 🎯 Start the Application in 30 Seconds

### Step 1: Navigate to Project
```bash
cd Citize_Connect-backend
```

### Step 2: Run the Application
```bash
# Windows
.\mvnw.cmd spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=local"

# macOS/Linux
./mvnw spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=local"
```

The app will start in ~12 seconds.

### Step 3: Access the API
- **Swagger UI**: http://localhost:8080/swagger-ui/index.html
- **Health Check**: http://localhost:8080/health
- **API Docs**: http://localhost:8080/v3/api-docs

---

## 🧪 Test the Application

### In Swagger UI:
1. Click **Try it out** on any endpoint
2. For protected routes, click **Authorize** first (no password needed for demo)
3. Test endpoints live in browser

### Or use the following test flow:

#### 1. **Signup (Create User)**
```
POST /auth/signup
{
  "name": "John Doe",
  "email": "john@example.com",
  "password": "password123",
  "role": "CITIZEN",
  "region": "NORTH"
}
```

#### 2. **Login (Get OTP)**
```
POST /auth/login
{
  "email": "john@example.com",
  "password": "password123"
}
```

**Check Terminal Output** for OTP code:
```
DEV ONLY — OTP for john@example.com: 123456
```

#### 3. **Verify OTP (Get JWT Token)**
```
POST /auth/verify-login
{
  "email": "john@example.com",
  "otp": "123456"
}
```

Response:
```json
{
  "token": "eyJhbGc...",
  "email": "john@example.com",
  "role": "CITIZEN"
}
```

#### 4. **Use JWT Token**
Copy the token and paste it in **Authorize** dialog in Swagger

#### 5. **Test Protected Route**
```
GET /citizen/issues
```

---

## 🏃 Quick Database Check

### H2 Console (In-Memory Database)
- **URL**: http://localhost:8080/h2-console
- **JDBC URL**: `jdbc:h2:mem:citizen_connect`
- **User**: `sa`
- **Password**: (leave empty)

View your data in real-time:
```sql
SELECT * FROM users;
SELECT * FROM complaints;
SELECT * FROM activity_logs;
```

---

## 👤 Use Demo Admin Account

**Email**: admin@local.test  
**Password**: admin123

1. Login with admin credentials
2. Use OTP from console
3. You'll have ADMIN role access to `/admin/dashboard`

---

## 📁 Project Files to Review

- **SETUP_GUIDE.md** - Full setup instructions
- **FRONTEND_INTEGRATION.md** - Complete API documentation
- **ERROR_FIX_REPORT.md** - What was fixed
- **.env.example** - Environment variables

---

## 🛑 Stop the Application

Press **Ctrl+C** in the terminal.

---

## ⚠️ Troubleshooting

### Application won't start
```bash
# Make sure port 8080 is not in use
# Kill process on port 8080:
# Windows:
netstat -ano | findstr :8080

# macOS/Linux:
lsof -i :8080
```

### OTP not showing in console
- Check if you used `--spring.profiles.active=local`
- Look for line starting with `DEV ONLY`

### Database errors
- H2 is in-memory; database resets on restart
- Check h2-console to verify data

### Email/SMTP errors (expected)
- Gmail SMTP not configured by default
- This is normal! OTP still prints to console
- To fix: Add Gmail credentials to `application-mail-local.properties`

---

##  Next Steps

1. ✅ **Backend Running** → You are here
2. → **Frontend Connection** → See FRONTEND_INTEGRATION.md
3. → **Database Setup** → See SETUP_GUIDE.md for MySQL
4. → **Deployment** → See SETUP_GUIDE.md for Docker/Railway

---

## 🔗 Frontend Integration

Connect your frontend by:
1. Using API base URL: `http://localhost:8080`
2. Following endpoints in FRONTEND_INTEGRATION.md
3. Sending JWT token in Authorization header

Example (JavaScript):
```javascript
const token = 'your_jwt_token_here';

fetch('http://localhost:8080/citizen/issues', {
  headers: {
    'Authorization': `Bearer ${token}`
  }
}).then(res => res.json()).then(data => console.log(data));
```

---

**That's it! Your backend is ready to use! 🚀**

