# Citizen Connect Backend - Setup & Deployment Guide

## ✅ Project Status
The backend is **fully compiled and ready** with no critical errors. All controllers, services, repositories, and DTOs are properly configured for database and frontend connectivity.

## 📋 Quick Start

### Local Development (H2 In-Memory Database)
```bash
cd Citize_Connect-backend
./mvnw.cmd spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=local"
```
The application will start on `http://localhost:8080` with H2 database.

### Access Swagger UI
```
http://localhost:8080/swagger-ui/index.html
```

### Default Local Admin Credentials
- **Email**: admin@local.test
- **Password**: admin123

---

## 🗄️ Database Configuration

### Option 1: MySQL (Production/Railway)
Set environment variables on your host:

```bash
DATABASE_URL=jdbc:mysql://host:port/database_name
DATABASE_USERNAME=your_username
DATABASE_PASSWORD=your_password
```

**Or use discrete variables:**
```bash
MYSQLHOST=your_host
MYSQLPORT=3306
MYSQLDATABASE=citizen_connect
MYSQLUSER=your_username
MYSQLPASSWORD=your_password
```

### Option 2: Local H2 Database (Development)
No configuration needed. Add to `src/main/resources/application-mail-local.properties`:
```properties
spring.mail.username=your-gmail@gmail.com
spring.mail.password=your-app-specific-password
```

---

## 📧 Email Configuration (OTP Setup)

### Gmail Setup
1. Enable 2-Step Verification: https://myaccount.google.com/security
2. Generate App Password: https://myaccount.google.com/apppasswords
3. Copy the generated 16-character password

### Local Development
Create `src/main/resources/application-mail-local.properties`:
```properties
spring.mail.username=your-gmail@gmail.com
spring.mail.password=your-16-char-app-password
```

### Production
Set environment variables with a Gmail App Password:
```bash
MAIL_USERNAME=your-gmail@gmail.com
MAIL_PASSWORD=your-16-char-app-password
```

or use the SMTP aliases supported by the app:
```bash
SMTP_USER=your-gmail@gmail.com
SMTP_PASSWORD=your-16-char-app-password
```

If you deploy with Railway or Docker, ensure the app password is set as the SMTP_PASSWORD / MAIL_PASSWORD value.

With local profile enabled, OTP appears in console logs for testing.

---

## 🚀 Build & Deploy

### Build JAR
```bash
./mvnw.cmd clean package -DskipTests
```
Output: `target/citizenconnect.jar`

### Run JAR (Local)
```bash
java -jar target/citizenconnect.jar --spring.profiles.active=local
```

### Run JAR (Production/MySQL)
```bash
java -jar target/citizenconnect.jar \
  --spring.datasource.url=jdbc:mysql://host:port/db \
  --spring.datasource.username=user \
  --spring.datasource.password=pass
```

### Docker
```bash
docker build -f Dockerfile -t citizenconnect:latest .
docker run -p 8080:8080 \
  -e DATABASE_URL="jdbc:mysql://host:port/db" \
  -e DATABASE_USERNAME=user \
  -e DATABASE_PASSWORD=pass \
  -e MAIL_USERNAME=your-email@gmail.com \
  -e MAIL_PASSWORD=app-password \
  citizenconnect:latest
```

---

## 🔐 API Authentication

### Signup (No Email Verification)
```bash
POST /auth/signup
{
  "name": "John Doe",
  "email": "john@example.com",
  "password": "password123",
  "role": "CITIZEN",
  "region": "NORTH"
}
```

### Login (Sends OTP to Email)
```bash
POST /auth/login
{
  "email": "john@example.com",
  "password": "password123"
}
```

### Verify OTP & Get JWT
```bash
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

### Use JWT in Requests
```bash
Authorization: Bearer <your_jwt_token>
```

---

## 👥 User Roles & Endpoints

| Role | Routes | Features |
|------|--------|----------|
| **CITIZEN** | `/citizen/**` | Raise issues, view announcements, rate resolutions |
| **POLITICIAN** | `/politician/**` | View zone issues, update status, publish announcements |
| **MODERATOR** | `/moderator/**` | View all issues, delete inappropriate content |
| **ADMIN** | `/admin/**` | System overview, user management, activity logs |

---

## 📊 Database Schema

### Tables Created Automatically
- `users` - User accounts
- `complaints` - Issues/complaints
- `announcements` - Politician announcements
- `issue_ratings` - Resolution feedback (1-5 stars)
- `otp` - OTP verification codes
- `activity_logs` - System activity tracking

### Regions (Zones)
- NORTH
- SOUTH
- EAST
- WEST

### Issue Severity
- LOW
- MEDIUM
- HIGH

### Issue Status
- PENDING
- IN_PROGRESS
- RESOLVED

---

## 🧪 Testing

### Unit & Integration Tests
```bash
./mvnw.cmd test
```

### Manual Testing with Swagger
1. Open http://localhost:8080/swagger-ui/index.html
2. Click **Authorize** button
3. Paste JWT token (obtained from /verify-login)
4. Test endpoints

### Test with cURL
```bash
# Signup
curl -X POST http://localhost:8080/auth/signup \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Test User",
    "email": "test@example.com",
    "password": "password123",
    "role": "CITIZEN",
    "region": "NORTH"
  }'

# Login
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "password123"
  }'

# Verify OTP (use OTP from console in local mode)
curl -X POST http://localhost:8080/auth/verify-login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "otp": "123456"
  }'
```

---

## 🐛 Troubleshooting

### MySQL Connection Refused
- Ensure MySQL is running on the specified host:port
- Check firewall rules for port 3306
- Verify credentials are correct
- For Railway: Use the auto-generated `DATABASE_URL`

### OTP Email Not Sending
- Check SMTP credentials in `application-mail-local.properties`
- Verify 2-Step Verification is enabled on Gmail
- Use App Password (not regular password)
- Check spam folder
- With local profile: OTP prints in console logs

### Database Lock Errors
- H2 (local) is single-threaded; close other connections
- For MySQL: Check for long-running queries

### CORS Issues
- Frontend must run on allowed origin
- Check SecurityConfig.corsConfigurationSource()
- Default allows all origins: `*`

### JWT Token Expired
- Token expires in 24 hours
- Login again to get a new token

---

## 📝 Supported Properties

| Property | Default | Purpose |
|----------|---------|---------|
| `JPA_DDL_AUTO` | `update` | Hibernate DDL strategy |
| `SHOW_SQL` | `false` | Log SQL queries |
| `FORMAT_SQL` | `false` | Format SQL in logs |
| `PORT` | `8080` | Server port |
| `app.otp.expiry-minutes` | `10` | OTP validity |
| `app.otp.resend-cooldown-seconds` | `60` | OTP resend wait |
| `app.mail.log-otp-to-console` | `false` | Log OTP to console |

---

## 🚨 Production Checklist

- [ ] Database migrated to MySQL
- [ ] Email service configured with Gmail App Password
- [ ] Environment variables set on host
- [ ] JAR built and tested
- [ ] HTTPS/TLS configured (check `server.forward-headers-strategy`)
- [ ] Logging levels set to INFO (not DEBUG)
- [ ] Database backups configured
- [ ] JWT secret key is strong (review in SecurityConfig)
- [ ] CORS origins restricted (if needed)

---

## 📞 Support

For issues:
1. Check logs: `spring.jpa.show-sql=true` for SQL debugging
2. Review Swagger: http://localhost:8080/swagger-ui/index.html
3. Check H2 console (local): http://localhost:8080/h2-console
4. Connection: `jdbc:h2:mem:citizen_connect`

---

## 📄 License
Citizen Connect © 2026

