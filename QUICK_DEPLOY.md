# Quick Deployment Guide 🚀

## Option 1: Vercel (Frontend) + Railway (Backend) - RECOMMENDED

### Step 1: Deploy Backend to Railway
1. Sign up at [railway.app](https://railway.app) (Free $5/month for students)
2. Connect GitHub and select this repository
3. Add PostgreSQL database
4. Set environment variables:
   ```
   SPRING_PROFILES_ACTIVE=prod
   CORS_ALLOWED_ORIGINS=https://your-app.vercel.app
   ```

### Step 2: Deploy Frontend to Vercel
1. Sign up at [vercel.com](https://vercel.com) (Free tier)
2. Import GitHub repository
3. Configure:
   - Build Command: `cd frontend && CI=false npm run build`
   - Output Directory: `frontend/build`
4. Set environment variables:
   ```
   CI=false
   REACT_APP_API_URL=https://your-backend.railway.app
   ```

## Option 2: Heroku (Full Stack)
```bash
# Install Heroku CLI
npm install -g heroku

# Deploy
heroku create your-app-name
heroku addons:create heroku-postgresql:hobby-dev
heroku config:set SPRING_PROFILES_ACTIVE=prod
git push heroku main
```

## Option 3: Docker Local
```bash
# Build and run
docker-compose up --build

# Access application
Frontend: http://localhost
Backend: http://localhost:8080
```

## Free Resources for Students 🎓

- **GitHub Student Pack**: [education.github.com/pack](https://education.github.com/pack)
- **Heroku**: $13/month credits
- **DigitalOcean**: $200 for 1 year
- **AWS/Azure/GCP**: $75-$150 credits

## Need Help? 🆘

1. Check [DEPLOYMENT.md](DEPLOYMENT.md) for detailed instructions
2. Use deployment scripts in `scripts/` directory
3. Create an issue if you encounter problems

---
**Total Setup Time**: ~15 minutes  
**Monthly Cost**: $0 (with student benefits)