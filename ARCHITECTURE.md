# Deployment Architecture

```
                    Personal Finance Dashboard
                          Deployment Options
                              
┌─────────────────────────────────────────────────────────────────┐
│                    OPTION 1: RECOMMENDED                       │
│  ┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐ │
│  │   Vercel        │    │   Railway       │    │  PostgreSQL     │ │
│  │   (Frontend)    │◄──►│   (Backend)     │◄──►│  (Database)     │ │
│  │   • React App   │    │   • Spring Boot │    │   • Free Tier   │ │
│  │   • Free Tier   │    │   • $5/mo       │    │                 │ │
│  └─────────────────┘    └─────────────────┘    └─────────────────┘ │
└─────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────┐
│                      OPTION 2: HEROKU                          │
│  ┌─────────────────────────────────────────────────────────────┐ │
│  │                  Heroku Dyno                               │ │
│  │  ┌─────────────────┐    ┌─────────────────┐                │ │
│  │  │   Frontend      │    │   Backend       │                │ │
│  │  │   (React)       │◄──►│   (Spring Boot) │                │ │
│  │  └─────────────────┘    └─────────────────┘                │ │
│  │                            │                               │ │
│  │                            ▼                               │ │
│  │                  ┌─────────────────┐                       │ │
│  │                  │   PostgreSQL    │                       │ │
│  │                  │   Add-on        │                       │ │
│  │                  └─────────────────┘                       │ │
│  └─────────────────────────────────────────────────────────────┘ │
│                        $13/mo student credits                  │
└─────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────┐
│                    OPTION 3: DOCKER                            │
│  ┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐ │
│  │   Frontend      │    │   Backend       │    │   Database      │ │
│  │   Container     │◄──►│   Container     │◄──►│   Container     │ │
│  │   (Nginx+React) │    │   (Spring Boot) │    │   (PostgreSQL)  │ │
│  └─────────────────┘    └─────────────────┘    └─────────────────┘ │
│                    Deploy to any cloud provider                │
└─────────────────────────────────────────────────────────────────┘

                         GitHub Actions CI/CD
                    ┌─────────────────────────────┐
                    │  • Automated Testing       │
                    │  • Docker Builds           │
                    │  • Multi-Environment       │
                    │  • Auto Deployment         │
                    └─────────────────────────────┘
```

## Student Benefits Integration

### GitHub Student Pack Resources Used:
- **Free Hosting**: Vercel, Netlify, GitHub Pages
- **Backend Services**: Railway ($5/mo), Heroku ($13/mo)
- **Cloud Credits**: DigitalOcean ($200), AWS ($75), Azure ($100)
- **Database**: PostgreSQL on hosting platforms
- **CI/CD**: GitHub Actions (free for public repos)

### Total Monthly Cost: $0
*All costs covered by student pack benefits and free tiers*