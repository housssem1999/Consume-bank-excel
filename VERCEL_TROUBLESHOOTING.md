# Vercel Deployment Troubleshooting Guide

## Common Issues and Solutions

### 🚨 "Uncaught SyntaxError: Unexpected token '<'" Error

**Symptoms:**
- White screen after deployment
- Console error: `Uncaught SyntaxError: Unexpected token '<'`
- Browser receives HTML instead of JavaScript files

**Root Cause:**
The `vercel.json` configuration has incorrect routing that redirects ALL requests (including static assets) to `index.html`.

**Solution:**
Update `vercel.json` with proper routing configuration:

```json
{
  "version": 2,
  "name": "finance-dashboard-frontend",
  "buildCommand": "cd frontend && CI=false npm run build",
  "outputDirectory": "frontend/build",
  "routes": [
    {
      "src": "/static/(.*)",
      "dest": "/static/$1"
    },
    {
      "src": "/favicon.ico",
      "dest": "/favicon.ico"
    },
    {
      "src": "/manifest.json",
      "dest": "/manifest.json"
    },
    {
      "src": "/(.*)",
      "dest": "/index.html"
    }
  ],
  "headers": [
    {
      "source": "/static/(.*)",
      "headers": [
        {
          "key": "Cache-Control",
          "value": "public, max-age=31536000, immutable"
        }
      ]
    }
  ],
  "env": {
    "CI": "false",
    "NODE_ENV": "production"
  },
  "build": {
    "env": {
      "CI": "false"
    }
  }
}
```

**Key Changes:**
1. **Static Asset Routing**: `/static/(.*)` routes serve static files directly
2. **Public Asset Routing**: Routes for favicon and manifest files
3. **SPA Fallback**: Only non-static routes fallback to `index.html`
4. **Caching Headers**: Proper cache headers for static assets

### 🔧 Environment Variables Setup

**Required Vercel Environment Variables:**
```env
CI=false
REACT_APP_API_URL=https://your-backend.railway.app
REACT_APP_ENVIRONMENT=production
```

**Important Notes:**
- Set `REACT_APP_API_URL` to your actual backend URL
- Always use `CI=false` to avoid build failures from ESLint warnings
- Don't include sensitive values in `.env.production` file

### 🛠️ Build Configuration

**Correct Build Settings in Vercel:**
- **Build Command**: `cd frontend && CI=false npm run build`
- **Output Directory**: `frontend/build`
- **Install Command**: `cd frontend && npm install`

### 📋 Deployment Checklist

Before deploying to Vercel:

- [ ] ✅ Build works locally: `cd frontend && CI=false npm run build`
- [ ] ✅ `vercel.json` has correct routing configuration
- [ ] ✅ Environment variables are set in Vercel dashboard
- [ ] ✅ Backend API is deployed and accessible
- [ ] ✅ CORS is configured in backend to allow Vercel domain

### 🔍 Debugging Steps

1. **Check Build Logs:**
   - Go to Vercel Dashboard → Your Project → Deployments
   - Click on latest deployment to view build logs

2. **Test Static Assets:**
   - Visit: `https://your-app.vercel.app/static/js/main.{hash}.js`
   - Should return JavaScript, not HTML

3. **Check Network Tab:**
   - Open browser DevTools → Network tab
   - Look for failed requests or incorrect content types

4. **Verify Environment Variables:**
   - Check that `REACT_APP_API_URL` is correctly set
   - Ensure API is accessible from browser

### 🚨 Emergency Rollback

If deployment fails:

1. **Revert to Previous Deployment:**
   - Vercel Dashboard → Deployments → Click on working version → Promote to Production

2. **Quick Fix Template:**
   ```json
   {
     "version": 2,
     "routes": [
       { "src": "/static/(.*)", "dest": "/static/$1" },
       { "src": "/(.*)", "dest": "/index.html" }
     ]
   }
   ```

### 📞 Additional Resources

- [Vercel SPA Configuration](https://vercel.com/guides/deploying-react-with-vercel)
- [React Router with Vercel](https://vercel.com/guides/deploying-react-with-vercel#routing)
- [Environment Variables on Vercel](https://vercel.com/docs/concepts/projects/environment-variables)

---

**Last Updated:** {{ current_date }}
**Version:** 1.0