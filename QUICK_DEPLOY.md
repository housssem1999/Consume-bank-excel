# Quick Deployment Guide 🚀

Deploy your Personal Finance Dashboard in **15 minutes** using **Vercel + Railway** with zero cost using GitHub Student Pack benefits.

## 🎯 Deployment Option: Vercel (Frontend) + Railway (Backend)

### Step 1: Deploy Backend to Railway ⚡
1. Sign up at [railway.app](https://railway.app) 
   - **Free $5/month credit** for GitHub Student Pack users
2. Connect GitHub and select this repository
3. Add PostgreSQL database (automatic)
4. Set environment variables:
   ```env
   SPRING_PROFILES_ACTIVE=prod
   CORS_ALLOWED_ORIGINS=https://your-app.vercel.app
   ```
5. Deploy automatically detected Spring Boot app

### Step 2: Deploy Frontend to Vercel 🌐
1. Sign up at [vercel.com](https://vercel.com) 
   - **Free tier** with unlimited projects
2. Import GitHub repository
3. Configure build settings:
   - **Build Command**: `cd frontend && CI=false npm run build`
   - **Output Directory**: `frontend/build`
4. Set environment variables:
   ```env
   CI=false
   REACT_APP_API_URL=https://your-backend.railway.app
   ```
5. Deploy and get your live URL

### Step 3: Connect Frontend & Backend 🔗
1. Copy your Vercel URL (e.g., `https://finance-dashboard-abc123.vercel.app`)
2. Update Railway environment variable:
   ```env
   CORS_ALLOWED_ORIGINS=https://finance-dashboard-abc123.vercel.app
   ```
3. Restart Railway deployment

## 🚀 Quick Deploy Scripts

### Automated Railway Deployment
```bash
# Make script executable
chmod +x scripts/deploy-railway.sh

# Deploy backend to Railway
./scripts/deploy-railway.sh
```

### Local Development Setup
```bash
# Setup development environment
chmod +x scripts/setup-dev.sh
./scripts/setup-dev.sh

# Start backend
mvn spring-boot:run

# Start frontend (in new terminal)
cd frontend && CI=false npm start
```

## 💰 Cost Analysis

| Service | Cost | Included Features |
|---------|------|-------------------|
| **Vercel** | FREE | 100GB bandwidth, unlimited sites, HTTPS |
| **Railway** | $0* | 500h runtime, 1GB RAM, PostgreSQL DB |

**Total: $0/month** (*covered by GitHub Student Pack $5 credit)

## 🎓 Free Resources for Students

- **GitHub Student Pack**: [education.github.com/pack](https://education.github.com/pack)
- **Railway**: $5/month credits for students
- **Vercel**: Free hosting for personal projects
- **PostgreSQL**: Included with Railway

## ✅ 15-Minute Checklist

- [ ] **0-3 min**: Sign up for Railway and Vercel accounts
- [ ] **3-6 min**: Fork repository and connect to Railway
- [ ] **6-9 min**: Deploy backend, add PostgreSQL database
- [ ] **9-12 min**: Connect Vercel, configure build settings
- [ ] **12-15 min**: Deploy frontend, update CORS settings
- [ ] **Done!** 🎉 Test your live application

## 🔧 Essential Environment Variables

### Railway (Backend)
```env
SPRING_PROFILES_ACTIVE=prod
CORS_ALLOWED_ORIGINS=https://your-app.vercel.app
```

### Vercel (Frontend)
```env
CI=false
REACT_APP_API_URL=https://your-backend.railway.app
```

## 🆘 Quick Troubleshooting

### App not loading?
1. ✅ Check Railway service is running
2. ✅ Verify REACT_APP_API_URL is correct
3. ✅ Ensure CORS_ALLOWED_ORIGINS matches Vercel URL

### CORS errors?
```bash
# Update Railway environment variable
CORS_ALLOWED_ORIGINS=https://your-exact-vercel-url.vercel.app
```

### Build failing?
```bash
# Always use CI=false for React builds
CI=false npm run build
```

## 🔗 Quick Links

- **Railway Dashboard**: [railway.app/dashboard](https://railway.app/dashboard)
- **Vercel Dashboard**: [vercel.com/dashboard](https://vercel.com/dashboard)
- **GitHub Student Pack**: [education.github.com/pack](https://education.github.com/pack)

## 📖 Need More Details?

See [DEPLOYMENT.md](DEPLOYMENT.md) for comprehensive deployment guide with troubleshooting and advanced configuration.

---

**🎯 Result**: Production-ready Personal Finance Dashboard  
**⏱️ Time**: 15 minutes  
**💸 Cost**: $0/month with student benefits  
**🔄 CI/CD**: Automatic deployments on git push