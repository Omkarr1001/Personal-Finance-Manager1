# Personal Finance Manager

A comprehensive personal finance management application with secure authentication, trade tracking, expense management, investment goals, and AI-powered recommendations.

## Features

### 🔐 User Authentication
- Secure JWT-based authentication
- User registration and login
- Password hashing with BCrypt
- Protected routes and API endpoints

### 📊 Dashboard
- Portfolio overview with current holdings
- Real-time profit/loss calculations
- Key financial metrics and analytics
- Quick action buttons for common tasks

### 💰 Trade Tracker
- Add, edit, and delete trades (stocks, crypto, etc.)
- Real-time market data integration
- Automatic profit/loss calculations
- Portfolio performance tracking

### 💸 Expense Tracker
- Track daily expenses with categories
- Expense categorization and reporting
- Spending analysis and insights
- Budget tracking capabilities

### 🎯 Investment Goals
- Set and track financial goals
- Progress visualization with progress bars
- Goal completion tracking
- Savings target management

### 🤖 AI Analysis & Recommendations
- Machine learning-powered price predictions
- Trading recommendations based on portfolio
- Market trend analysis
- Risk assessment and suggestions

### 📈 Reports & Analytics
- Daily, monthly, and yearly reports
- Interactive charts and graphs
- Category-wise spending analysis
- Portfolio performance metrics

### 📤 Data Export
- Export reports as Excel/CSV
- Portfolio data export
- Trade history export
- Expense reports export

### 🔒 Security Features
- HTTPS support
- JWT authentication
- API validation and sanitization
- Data encryption for sensitive information

## Technology Stack

### Backend (Spring Boot)
- **Framework**: Spring Boot 3.2.0
- **Database**: H2 (in-memory for development)
- **Security**: Spring Security with JWT
- **API**: RESTful APIs with validation
- **Build Tool**: Maven

### Frontend (React)
- **Framework**: React 18
- **Styling**: Tailwind CSS
- **State Management**: React Query
- **Routing**: React Router DOM
- **Forms**: React Hook Form
- **Charts**: Chart.js with React Chart.js 2

### AI Service (Python)
- **Framework**: FastAPI
- **ML Libraries**: scikit-learn, pandas, numpy
- **Model Persistence**: joblib
- **API**: RESTful endpoints for ML predictions

### Infrastructure
- **Containerization**: Docker
- **Orchestration**: Docker Compose
- **API Integration**: Alpha Vantage, CoinGecko, Finnhub

## Project Structure

```
finance-manager-app/
├── client/                # React.js frontend
│   ├── src/
│   │   ├── components/   # UI Components
│   │   ├── pages/        # Views (Login, Dashboard, etc.)
│   │   ├── services/     # API calls, auth, data fetching
│   │   └── utils/        # Chart logic, helpers
│   └── public/
├── server/                # Spring Boot backend
│   ├── src/main/java/
│   │   ├── controller/   # REST API controllers
│   │   ├── service/      # Business logic
│   │   ├── model/        # JPA entities
│   │   └── repository/   # Data access layer
│   ├── src/main/resources/
│   └── Dockerfile
├── ai-service/            # Python microservice
│   ├── app.py            # FastAPI application
│   ├── model/            # ML models, scripts
│   └── requirements.txt
└── docker-compose.yml     # Orchestration for all services
```

## Getting Started

### Prerequisites
- Docker and Docker Compose
- Java 17 (for local development)
- Node.js 18 (for local development)
- Python 3.11 (for local development)

### Quick Start with Docker

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd finance-manager-app
   ```

2. **Start all services**
   ```bash
   docker-compose up --build
   ```

3. **Access the application**
   - Frontend: http://localhost:3000
   - Backend API: http://localhost:8080/api
   - AI Service: http://localhost:8000
   - H2 Database Console: http://localhost:8080/h2-console

### Local Development

#### Backend Setup
```bash
cd server
./mvnw spring-boot:run
```

#### Frontend Setup
```bash
cd client
npm install
npm start
```

#### AI Service Setup
```bash
cd ai-service
pip install -r requirements.txt
uvicorn app:app --reload
```

## API Documentation

### Authentication Endpoints
- `POST /api/auth/login` - User login
- `POST /api/auth/register` - User registration

### Trade Endpoints
- `GET /api/trades` - Get all trades
- `POST /api/trades` - Create new trade
- `PUT /api/trades/{id}` - Update trade
- `DELETE /api/trades/{id}` - Delete trade
- `GET /api/trades/portfolio` - Get portfolio summary

### Expense Endpoints
- `GET /api/expenses` - Get all expenses
- `POST /api/expenses` - Create new expense
- `PUT /api/expenses/{id}` - Update expense
- `DELETE /api/expenses/{id}` - Delete expense
- `GET /api/expenses/summary` - Get expense summary

### Goal Endpoints
- `GET /api/goals` - Get all goals
- `POST /api/goals` - Create new goal
- `PUT /api/goals/{id}` - Update goal
- `DELETE /api/goals/{id}` - Delete goal
- `GET /api/goals/active` - Get active goals

### AI Service Endpoints
- `POST /ai-service/predict-price` - Price prediction
- `POST /ai-service/get-recommendations` - Trading recommendations
- `POST /ai-service/analyze-market` - Market analysis
- `GET /ai-service/health` - Health check

## Environment Variables

### Backend (.env)
```env
JWT_SECRET=your-secret-key-here
JWT_EXPIRATION=86400000
SPRING_DATASOURCE_URL=jdbc:h2:mem:financedb
```

### Frontend (.env)
```env
REACT_APP_API_URL=http://localhost:8080/api
REACT_APP_AI_SERVICE_URL=http://localhost:8000
```

## Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Support

For support, email support@financemanager.com or create an issue in the repository.

## Roadmap

- [ ] Real-time market data integration
- [ ] Advanced charting and technical analysis
- [ ] Mobile application
- [ ] Multi-currency support
- [ ] Tax reporting features
- [ ] Integration with banking APIs
- [ ] Advanced AI/ML models
- [ ] Social trading features 