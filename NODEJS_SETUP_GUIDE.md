# Node.js Installation & Setup Guide (Windows)

## 1. Node.js Installation

### Download:
- Visit: https://nodejs.org/
- Download **LTS (Long Term Support) version** (v20 ya latest)
- Windows installer (.msi) download karo

### Installation Steps:
1. Downloaded file ko double-click karo
2. **Next** button se aage badhte raho
3. License agreement accept karo
4. Installation path default rakhna (C:\Program Files\nodejs)
5. Features selection - "npm package manager" checked rakho
6. **Install** button click karo
7. Administrator permission de
8. Installation complete hone tak wait karo

### Verify Installation:
```powershell
node --version
npm --version
```

Expected Output:
```
v20.x.x (or latest)
9.x.x (or latest)
```

---

## 2. Environment Variables Setup

### Windows mein Path ko Check/Update karo:

1. **Settings Open karo**:
   - Windows + S press karo
   - "environment variables" type karo
   - "Edit the system environment variables" click karo

2. **Environment Variables window mein**:
   - "Path" select karo (under System variables)
   - "Edit" button click karo
   - Ensure ye dono entries hain:
     ```
     C:\Program Files\nodejs\
     C:\Users\{YourUsername}\AppData\Roaming\npm
     ```
   - "OK" button click karo

3. **PowerShell Restart karo**:
   ```powershell
   # Ya PowerShell ko completely close karke new window open karo
   ```

---

## 3. Node.js Configuration

### npm Global Config (Optional but Recommended):
```powershell
# Default npm cache location set karo
npm config set prefix "C:\Users\{YourUsername}\AppData\Roaming\npm"

# Check config
npm config list
```

### .npmrc File Create karo (Optional):
Location: `C:\Users\{YourUsername}\.npmrc`

Content:
```
registry=https://registry.npmjs.org/
prefix=C:\Users\{YourUsername}\AppData\Roaming\npm
cache=C:\Users\{YourUsername}\AppData\Roaming\npm-cache
```

---

## 4. npm Package Manager Usage

### Basic Commands:

```powershell
# Project dependencies install karo
npm install
# ya
npm i

# Global package install karo (e.g., nodemon)
npm install -g nodemon

# Package uninstall karo
npm uninstall package-name

# Update packages
npm update

# Check outdated packages
npm outdated

# Audit security vulnerabilities
npm audit

# Fix vulnerabilities
npm audit fix
npm audit fix --force

# Clear cache
npm cache clean --force
```

---

## 5. Project Setup (PrintPass Frontend Example)

```powershell
# Step 1: Frontend folder mein jao
cd D:\PrintPass\frontend

# Step 2: Dependencies install karo
npm install

# Step 3: Development server start karo
npm run dev

# Server output:
# VITE v7.x.x ready in 600ms
# ➜ Local: http://localhost:3001/
```

---

## 6. Common npm Scripts

```json
{
  "scripts": {
    "dev": "vite",              // Development server
    "build": "tsc && vite build", // Production build
    "preview": "vite preview",  // Preview built app
    "lint": "eslint src"        // Code linting
  }
}
```

### Usage:
```powershell
npm run dev      # Start development server
npm run build    # Build for production
npm run preview  # Preview production build
npm run lint     # Check code quality
```

---

## 7. Package.json Structure

```json
{
  "name": "project-name",
  "version": "1.0.0",
  "type": "module",           // ES modules
  "scripts": {
    // ... commands ...
  },
  "dependencies": {
    // Production dependencies
    "react": "^18.2.0",
    "axios": "^1.6.0"
  },
  "devDependencies": {
    // Development only
    "typescript": "^5.2.0",
    "vite": "^5.0.0"
  }
}
```

---

## 8. Troubleshooting

### Problem: npm command not found
**Solution**:
- Node.js properly install hua hai verify karo
- PowerShell restart karo
- Path environment variable check karo

### Problem: Port already in use (3000/3001)
**Solution**:
```powershell
# Check kaunsa process port use kar raha hai
netstat -ano | findstr :3001

# Process ko kill karo (PID = process ID)
taskkill /PID {PID} /F
```

### Problem: npm install slow/hanging
**Solution**:
```powershell
# npm registry change karo (Taobao mirror)
npm config set registry https://registry.npmmirror.com

# Wapas default karo
npm config set registry https://registry.npmjs.org/

# Clear cache
npm cache clean --force
```

### Problem: Permission Denied
**Solution**:
- PowerShell ko **Administrator** mode mein open karo
- Right-click on PowerShell → "Run as administrator"

---

## 9. Useful Global Packages

```powershell
# Nodemon - Auto-reload server
npm install -g nodemon

# Live Server - Static server
npm install -g live-server

# http-server - Simple HTTP server
npm install -g http-server

# Yarn - Alternative package manager
npm install -g yarn

# Vite - Build tool (CLI)
npm install -g vite
```

---

## 10. Node.js Version Management (NVM - Optional)

**Windows ke liye nvm install karo**:
1. Download: https://github.com/coreybutler/nvm-windows
2. .exe file run karo
3. nvm install करो aur multiple Node versions manage karo

```powershell
# Different versions install karo
nvm install 18.0.0
nvm install 20.0.0

# Version switch karo
nvm use 20.0.0

# Current version check karo
nvm current
```

---

## 11. package-lock.json

- **Auto-generated** file
- Dependencies ke exact versions store karo
- **Version control mein commit karo**
- Team ke sab logon ke paas same versions hote hain

---

## 12. npm vs yarn vs pnpm

| Feature | npm | yarn | pnpm |
|---------|-----|------|------|
| Speed | Medium | Fast | Fastest |
| Disk Space | More | Medium | Least |
| Lock File | package-lock.json | yarn.lock | pnpm-lock.yaml |
| Stability | Good | Better | Best |

**PrintPass ke liye npm use kar rahe ho** (built-in with Node.js)

---

## Summary Checklist:

- ✅ Node.js LTS installed
- ✅ npm version checked
- ✅ Path environment variables set
- ✅ `npm install` successful
- ✅ Development server running
- ✅ Frontend accessible on http://localhost:3001/

---

**Happy Coding! 🚀**

Agar koi problem aaye:
1. npm cache clean --force
2. node_modules folder delete karo
3. npm install phir se karo
4. npm run dev phir se chalao

