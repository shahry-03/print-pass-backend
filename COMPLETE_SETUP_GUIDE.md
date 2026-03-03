# PrintPass - Complete Setup & Running Guide

## 📋 Complete Architecture

```
PrintPass Application
│
├── Backend (Java Spring Boot)
│   ├── API Gateway (Port 8080)
│   ├── Discovery Server (Eureka)
│   ├── User Service
│   ├── Job Service
│   ├── Shop Service
│   └── Database (MySQL)
│
└── Frontend (React + TypeScript)
    ├── Vite Dev Server (Port 3001)
    ├── React Router (SPA)
    ├── Zustand Store (State)
    └── Axios Client (HTTP)
```

---

## 🚀 Quick Start (5 Minutes)

### Prerequisites:
- ✅ Node.js LTS installed (v18+)
- ✅ Java 11+ installed
- ✅ MySQL running
- ✅ All code files downloaded

### Terminal 1 - Backend Start:
```powershell
cd D:\PrintPass\backend

# Option 1: Using Maven
mvn clean install
mvn spring-boot:run

# Option 2: Run pre-built JAR (agar available ho)
java -jar api-gateway/target/api-gateway-1.0.0.jar
```

### Terminal 2 - Frontend Start:
```powershell
cd D:\PrintPass\frontend

npm install    # First time only
npm run dev
```

### Result:
- Backend API: http://localhost:8080
- Frontend UI: http://localhost:3001
- Open browser aur register/login karo

---

## 🏗️ Step-by-Step Backend Setup

### 1. Project Structure Check:
```
backend/
├── api-gateway/          ← Main entry point (Port 8080)
├── discovery-server/     ← Eureka (Port 8761)
├── user-service/
├── job-service/
├── shop-service/
├── printpass-common/     ← Shared library
└── pom.xml              ← Parent POM
```

### 2. Database Setup:
```sql
-- MySQL mein execute karo
CREATE DATABASE printpass;
USE printpass;

-- Schema files available hain backend/resources mein
```

### 3. Build Commands:
```powershell
cd D:\PrintPass\backend

# Option A: Full build with tests
mvn clean install

# Option B: Skip tests (faster)
mvn clean install -DskipTests

# Option C: Only rebuild specific service
cd api-gateway
mvn clean install -DskipTests
```

### 4. Run Backend:

**Method 1 - IDE (Recommended):**
- IntelliJ IDEA mein backend/pom.xml open karo
- api-gateway module right-click → Run
- Ya specific service ke main class ko run karo

**Method 2 - Maven:**
```powershell
cd D:\PrintPass\backend
mvn spring-boot:run
```

**Method 3 - Java directly:**
```powershell
cd D:\PrintPass\backend\api-gateway\target
java -jar api-gateway-1.0.0-SNAPSHOT.jar
```

### 5. Verify Backend Running:
```powershell
# In new PowerShell terminal
curl http://localhost:8080/health

# Expected: HTTP 200 + health check response
```

---

## 🎨 Step-by-Step Frontend Setup

### 1. Navigate to Frontend:
```powershell
cd D:\PrintPass\frontend
```

### 2. First Time Setup:
```powershell
# Install all dependencies
npm install

# Or short form
npm i
```

**Expected output:**
```
added 163 packages in 45s
found 0 vulnerabilities
```

### 3. Environment Configuration:
File: `.env` (already created)
```
VITE_API_URL=http://localhost:8080/api
```

### 4. Development Server Start:
```powershell
npm run dev
```

**Expected output:**
```
VITE v7.3.1  ready in 601 ms

  ➜  Local:   http://localhost:3001/
  ➜  Network: use --host to expose
  ➜  press h + enter to show help
```

### 5. Access Application:
- Open: http://localhost:3001/
- Should see: PrintPass HomePage
- If not, check browser console for errors

---

## 📱 Application Flow

### Student Workflow:
```
1. Register (Student role)
   ↓
2. Login with credentials
   ↓
3. Browse available shops
   ↓
4. Create print job (upload file, select shop)
   ↓
5. Get PIN confirmation
   ↓
6. Track job status on dashboard
   ↓
7. Download completed file
```

### Vendor Workflow:
```
1. Register (Vendor role)
   ↓
2. Create shop(s)
   ↓
3. Login to dashboard
   ↓
4. View pending jobs
   ↓
5. Verify PIN to accept job
   ↓
6. Update job status (Processing → Ready)
   ↓
7. Student downloads completed file
```

---

## 🔧 Common Issues & Solutions

### Issue 1: Backend Port 8080 Already in Use
```powershell
# Find process using port 8080
netstat -ano | findstr :8080

# Kill process (replace PID)
taskkill /PID 12345 /F

# Or change port in application.yml
# server.port=8081
```

### Issue 2: Frontend Can't Connect to Backend
**Check:**
1. Backend is running: `curl http://localhost:8080/health`
2. .env file has correct URL: `VITE_API_URL=http://localhost:8080/api`
3. Browser console for CORS errors (check backend CORS config)

**Solution:**
```java
// backend application.yml mein add karo
spring:
  web:
    cors:
      allowed-origins: "http://localhost:3001"
      allowed-methods: "*"
      allowed-headers: "*"
```

### Issue 3: npm install Slow/Failing
```powershell
# Clear cache
npm cache clean --force

# Delete node_modules
rmdir node_modules -r -Force

# Reinstall
npm install
```

### Issue 4: TypeScript Compilation Errors
```powershell
# Clear and rebuild
cd D:\PrintPass\frontend
rm -Force dist node_modules
npm install
npm run build
```

### Issue 5: Database Connection Error
```sql
-- Check MySQL running
-- Verify credentials in backend application.yml
-- Ensure database exists:
CREATE DATABASE IF NOT EXISTS printpass;
```

---

## 🛠️ Development Workflow

### Terminal Setup (Recommended):
```powershell
# Terminal 1 - Backend
cd D:\PrintPass\backend
mvn spring-boot:run

# Terminal 2 - Frontend  
cd D:\PrintPass\frontend
npm run dev

# Terminal 3 - Optional monitoring
# Keep for logs/errors
```

### Making Changes:

**Backend Changes:**
1. Edit Java files
2. IDE auto-recompiles OR
3. Restart Maven/Spring Boot
4. Test via Postman/curl

**Frontend Changes:**
1. Edit React/TypeScript files
2. Vite auto-reloads browser
3. Check browser console for errors
4. No restart needed (HMR - Hot Module Replacement)

---

## 📦 API Endpoints (Backend)

### Authentication:
```
POST /api/users/login
POST /api/users/register
GET  /api/users/{id}
```

### Jobs:
```
POST   /api/jobs (create job)
GET    /api/jobs (list all)
GET    /api/jobs/{id}
PUT    /api/jobs/{id}/status (update status)
GET    /api/jobs/verify/{pin} (verify PIN)
```

### Shops:
```
POST   /api/shops (create shop)
GET    /api/shops (list all)
GET    /api/shops/{id}
GET    /api/shops?vendorId={id} (vendor shops)
```

---

## 🧪 Testing Checklist

### 1. Backend Health Check:
```powershell
curl http://localhost:8080/health
# Expected: {"status":"UP"}
```

### 2. Frontend Loads:
- Open http://localhost:3001/
- Should see homepage without errors

### 3. Registration:
- Click "Register"
- Fill form (student or vendor)
- Submit
- Should show success message

### 4. Login:
- Use registered credentials
- Should redirect to dashboard

### 5. Create Job (Student):
- Login as student
- Go to "Create Job"
- Select shop, upload file
- Should get PIN confirmation

### 6. Verify Job (Vendor):
- Login as vendor
- Go to "Verify Job"
- Enter PIN from student
- Should show job details

---

## 📊 Project Structure Overview

```
D:\PrintPass/
│
├── backend/                          (Java Spring Boot)
│   ├── api-gateway/                 (Port 8080 - Entry point)
│   ├── discovery-server/            (Eureka Registry)
│   ├── user-service/                (Authentication)
│   ├── job-service/                 (Job Management)
│   ├── shop-service/                (Shop Management)
│   ├── printpass-common/            (Shared Code)
│   └── pom.xml
│
└── frontend/                         (React TypeScript)
    ├── src/
    │   ├── components/              (UI Components)
    │   ├── pages/                   (Route Pages)
    │   ├── services/                (API Client)
    │   ├── store/                   (State Management)
    │   ├── hooks/                   (Custom Hooks)
    │   ├── utils/                   (Helpers)
    │   └── types/                   (TypeScript Types)
    ├── .env                         (Configuration)
    ├── vite.config.ts               (Build Config)
    ├── tsconfig.json                (TypeScript Config)
    ├── package.json
    └── index.html
```

---

## 🔐 Security Notes

### Authentication:
- JWT tokens used
- Token stored in localStorage
- Automatic refresh on 401

### CORS:
- Configured on backend
- Frontend can access API

### Validation:
- Input validation on both sides
- Error handling comprehensive
- Secure password hashing (backend)

---

## 📝 Useful Commands Summary

### Frontend:
```powershell
npm install              # Install dependencies
npm run dev             # Start dev server
npm run build           # Production build
npm run preview         # Preview build
npm run lint            # Check code quality
npm audit fix           # Fix vulnerabilities
```

### Backend:
```powershell
mvn clean install                    # Full build
mvn spring-boot:run                  # Run with Maven
mvn clean install -DskipTests        # Skip tests
mvn dependency:tree                  # Show dependencies
```

### Database:
```sql
USE printpass;
SHOW TABLES;
DESCRIBE users;
SELECT * FROM jobs LIMIT 5;
```

---

## ✅ Setup Verification Checklist

- [ ] Node.js installed (node --version)
- [ ] npm working (npm --version)
- [ ] Java installed (java -version)
- [ ] MySQL running
- [ ] Backend built successfully
- [ ] Backend running on 8080
- [ ] Frontend dependencies installed
- [ ] Frontend running on 3001
- [ ] Browser opens without errors
- [ ] Can register new account
- [ ] Can login successfully
- [ ] API calls working (check Network tab)

---

## 🆘 Getting Help

### Check Logs:
1. **Browser Console** (F12) - Frontend errors
2. **Terminal Output** - Backend/Frontend logs
3. **Network Tab** - API calls status

### Common Log Messages:
```
✓ Frontend: "Page loaded successfully"
✓ Backend: "Started ApiGatewayApplication"
✗ Frontend: "Cannot POST /api/users/login" → Backend not running
✗ Backend: "Connection refused" → Database not running
```

---

## 🎓 Learning Resources

- React: https://react.dev
- TypeScript: https://www.typescriptlang.org
- Spring Boot: https://spring.io/projects/spring-boot
- Vite: https://vitejs.dev
- Tailwind CSS: https://tailwindcss.com

---

**Last Updated**: March 3, 2026
**Status**: ✅ COMPLETE & WORKING

Kisi problem ke liye documentation files check karo:
- `frontend/SETUP_COMPLETE.md`
- `NODEJS_SETUP_GUIDE.md` (this file)

