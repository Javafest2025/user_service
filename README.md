# ScholarAI User Service

A microservice for user authentication, authorization, and profile management in the ScholarAI platform. This service handles user registration, login, social authentication (Google, GitHub), JWT token management, and user profile operations.

## 🚀 Features

- **Authentication & Authorization**
  - JWT-based authentication with access and refresh tokens
  - Social authentication via Google OAuth
  - Social authentication via GitHub OAuth
  - Role-based access control

- **User Management**
  - User registration and login
  - User profile management
  - Password-based authentication
  - User identity provider linking

- **Security**
  - JWT token validation and refresh
  - Secure password handling
  - CORS configuration
  - Input validation

- **Database**
  - PostgreSQL with Flyway migrations
  - Redis for caching and session management
  - User, UserProfile, and UserIdentityProvider entities

## 🛠️ Tech Stack

- **Framework**: Spring Boot 3.5.0
- **Java**: 21
- **Database**: PostgreSQL
- **Cache**: Redis
- **Build Tool**: Maven
- **Documentation**: OpenAPI 3 (Swagger)
- **Migration**: Flyway
- **Code Quality**: Spotless (code formatting)

## 📋 Prerequisites

- Java 21 or higher
- Maven 3.6+
- PostgreSQL
- Redis
- Git

## 🔧 Setup

### 1. Clone the Repository

```bash
git clone <repository-url>
cd user_service
```

### 2. Environment Configuration

Copy the environment template and configure your settings:

```bash
cp env.example .env
```

Edit `.env` file with your configuration:

```bash
# Spring Profile
SPRING_PROFILE=local

# Database Configuration
USER_DB_PORT=5432
USER_DB_USER=your_db_user
USER_DB_PASSWORD=your_db_password

# Default Admin User
USER_NAME=admin
USER_PASSWORD=admin123

# JWT Configuration
JWT_SECRET=your_jwt_secret_key_here
JWT_ACCESS_EXPIRATION_MS=900000
JWT_REFRESH_EXPIRATION_MS=86400000

# RabbitMQ (if using)
RABBITMQ_PORT=5672
RABBITMQ_USER=guest
RABBITMQ_PASSWORD=guest

# Redis
REDIS_PORT=6379
REDIS_PASSWORD=

# Google OAuth
SPRING_GOOGLE_CLIENT_ID=your_google_client_id
SPRING_GOOGLE_CLIENT_SECRET=your_google_client_secret

# GitHub OAuth
SPRING_GITHUB_CLIENT_ID=your_github_client_id
SPRING_GITHUB_CLIENT_SECRET=your_github_client_secret
```

### 3. Database Setup

1. Create a PostgreSQL database
2. Update the database configuration in your `.env` file
3. The application will automatically run Flyway migrations on startup

### 4. Redis Setup

1. Install and start Redis server
2. Update Redis configuration in your `.env` file

## 🚀 Running the Application

### Using Scripts (Recommended)

The project includes convenient scripts for common operations:

```bash
# Format code
./scripts/local.sh format

# Build the application
./scripts/local.sh build

# Run tests
./scripts/local.sh test

# Run the application locally
./scripts/local.sh run
```

### Manual Commands

```bash
# Build the project
mvn clean install

# Run with local profile
mvn spring-boot:run -Dspring.profiles.active=local

# Or run the JAR file
java -jar target/user_service-0.0.1-SNAPSHOT.jar
```

### Docker (Coming Soon)

```bash
# Build Docker image
docker build -t scholar-ai-user-service .

# Run with Docker
docker run -p 8080:8080 scholar-ai-user-service
```

## 📊 API Documentation

Once the application is running, you can access the API documentation:

- **Swagger UI**: http://localhost:8081/docs
- **OpenAPI JSON**: http://localhost:8080/v3/api-docs

## 🔐 Authentication Endpoints

### Registration & Login
- `POST /api/auth/signup` - User registration
- `POST /api/auth/login` - User login
- `POST /api/auth/refresh` - Refresh JWT token

### Social Authentication
- `GET /api/auth/google` - Google OAuth login
- `GET /api/auth/github` - GitHub OAuth login

### User Profile
- `GET /api/user/profile` - Get user profile
- `PUT /api/user/profile` - Update user profile

## 🧪 Testing

```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=UserServiceApplicationTests

# Run with test profile
mvn test -Dspring.profiles.active=test
```

## 📁 Project Structure

```
src/
├── main/
│   ├── java/org/solace/scholar_ai/user_service/
│   │   ├── config/          # Configuration classes
│   │   ├── controller/      # REST controllers
│   │   ├── dto/            # Data Transfer Objects
│   │   ├── exception/      # Custom exceptions
│   │   ├── model/          # Entity models
│   │   ├── repository/     # Data access layer
│   │   ├── security/       # Security configuration
│   │   ├── service/        # Business logic
│   │   └── util/           # Utility classes
│   └── resources/
│       ├── application.yml # Main configuration
│       ├── application-local.yml
│       ├── application-prod.yml
│       └── db/migration/   # Flyway migrations
└── test/                   # Test classes
```

## 🔧 Configuration Profiles

- **local**: Development environment with local database
- **docker**: Docker environment configuration
- **prod**: Production environment configuration
- **test**: Testing environment configuration

## 🚨 Troubleshooting

### Common Issues

1. **Database Connection Error**
   - Verify PostgreSQL is running
   - Check database credentials in `.env`
   - Ensure database exists

2. **Redis Connection Error**
   - Verify Redis server is running
   - Check Redis configuration in `.env`

3. **JWT Token Issues**
   - Ensure `JWT_SECRET` is set in `.env`
   - Check token expiration settings

4. **OAuth Configuration**
   - Verify Google/GitHub OAuth credentials
   - Check redirect URIs in OAuth provider settings

### Logs

Application logs are available in the console output. For production, configure logging to files in `application-prod.yml`.

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Run tests: `./scripts/local.sh test`
5. Format code: `./scripts/local.sh format`
6. Submit a pull request

## 📄 License

This project is part of the ScholarAI platform.

## 🆘 Support

For issues and questions:
- Create an issue in the repository
- Contact the development team
- Check the API documentation at `/swagger-ui.html` 