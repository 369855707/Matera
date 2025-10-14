# Maternity Matron Backend API

Spring Boot 3 REST API for the Maternity Matron mobile application.

## Tech Stack

- **Java 17**
- **Spring Boot 3.2.0**
- **Spring Security** with JWT authentication
- **Spring Data JPA** with Hibernate
- **H2 Database** (in-memory for development)
- **SpringDoc OpenAPI** (Swagger UI) for API documentation
- **Lombok** for boilerplate reduction
- **Maven** for dependency management

## Project Structure

```
src/main/java/com/maternity/
├── config/
│   ├── DataInitializer.java      # Sample data loader
│   └── SecurityConfig.java        # Spring Security configuration
├── controller/
│   ├── AuthController.java        # Authentication endpoints
│   ├── MatronController.java      # Matron CRUD operations
│   ├── OrderController.java       # Order management
│   └── ReviewController.java      # Review endpoints
├── dto/
│   ├── AuthResponse.java
│   ├── LoginRequest.java
│   ├── RegisterRequest.java
│   ├── UserDTO.java
│   ├── MatronProfileDTO.java
│   ├── OrderDTO.java
│   └── ReviewDTO.java
├── exception/
│   └── ResourceNotFoundException.java
├── model/
│   ├── User.java
│   ├── MatronProfile.java
│   ├── WorkExperience.java
│   ├── Order.java
│   └── Review.java
├── repository/
│   ├── UserRepository.java
│   ├── MatronProfileRepository.java
│   ├── OrderRepository.java
│   └── ReviewRepository.java
├── security/
│   ├── JwtTokenProvider.java
│   ├── JwtAuthenticationFilter.java
│   └── CustomUserDetailsService.java
└── service/
    ├── AuthService.java
    ├── MatronService.java
    ├── OrderService.java
    └── ReviewService.java
```

## Getting Started

### Prerequisites
- JDK 17 or higher
- Maven 3.6+

### Installation

1. Navigate to the backend directory:
```bash
cd maternity-backend
```

2. Build the project:
```bash
mvn clean install
```

3. Run the application:
```bash
mvn spring-boot:run
```

The API will start on `http://localhost:8080`

## API Documentation

Interactive API documentation is available via Swagger UI:

**Swagger UI**: `http://localhost:8080/swagger-ui.html`

The Swagger UI provides:
- Complete API endpoint documentation
- Request/response schemas
- Try-it-out functionality to test endpoints
- JWT authentication support

To use protected endpoints in Swagger:
1. Register or login via `/api/auth/register` or `/api/auth/login`
2. Copy the JWT token from the response
3. Click "Authorize" button in Swagger UI
4. Enter: `Bearer <your-token>`
5. Click "Authorize"

**OpenAPI Spec**: `http://localhost:8080/v3/api-docs`

## API Endpoints

### Authentication (Public)

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/auth/register` | Register a new user |
| POST | `/api/auth/login` | Login and get JWT token |

**Register Request:**
```json
{
  "name": "John Doe",
  "email": "john@example.com",
  "password": "password123",
  "phone": "1234567890",
  "role": "MOTHER"
}
```

**Login Request:**
```json
{
  "email": "john@example.com",
  "password": "password123"
}
```

**Auth Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "user": {
    "id": 1,
    "name": "John Doe",
    "email": "john@example.com",
    "role": "MOTHER",
    "createdAt": "2024-01-01T00:00:00"
  }
}
```

### Matrons (Protected)

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/matrons` | Get all matrons |
| GET | `/api/matrons/{id}` | Get matron by ID |
| GET | `/api/matrons/available` | Get available matrons |
| GET | `/api/matrons/search?location={location}` | Search by location |
| GET | `/api/matrons/filter/price?minPrice={min}&maxPrice={max}` | Filter by price range |

### Orders (Protected)

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/orders/mother/{motherId}` | Get orders for a mother |
| GET | `/api/orders/matron/{matronId}` | Get orders for a matron |
| GET | `/api/orders/{id}` | Get order by ID |
| PUT | `/api/orders/{id}/status?status={status}` | Update order status |

**Order Statuses:** `PENDING`, `CONFIRMED`, `IN_PROGRESS`, `COMPLETED`, `CANCELLED`

### Reviews (Protected)

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/reviews/matron/{matronId}` | Get reviews for a matron |

## Authentication

All protected endpoints require a JWT token in the Authorization header:

```
Authorization: Bearer <your-jwt-token>
```

## Sample Data

The application automatically loads sample data on startup:

**Users:**
- Mother: `mother@test.com` / `password`
- Matrons: `zhang@test.com`, `li@test.com`, `wang@test.com`, `chen@test.com` / `password`

**Sample Matrons:**
- Zhang Wei (35 years, 8 years exp, ¥12,000/month)
- Li Ming (42 years, 15 years exp, ¥15,000/month)
- Wang Fang (38 years, 10 years exp, ¥13,000/month)
- Chen Xiu (40 years, 12 years exp, ¥14,000/month)

## H2 Database Console

Access the H2 console at: `http://localhost:8080/h2-console`

- JDBC URL: `jdbc:h2:mem:maternitydb`
- Username: `sa`
- Password: (leave blank)

## Configuration

Key configuration in `application.properties`:

```properties
server.port=8080
spring.datasource.url=jdbc:h2:mem:maternitydb
jwt.secret=your-secret-key
jwt.expiration=86400000
```

## Development

### Running Tests

```bash
mvn test
```

### Building for Production

```bash
mvn clean package
java -jar target/maternity-backend-1.0.0.jar
```

## Connecting to Flutter App

Update the Flutter app's base URL to point to this backend:

```dart
const String baseUrl = 'http://localhost:8080/api';
// or for physical device: 'http://YOUR_IP:8080/api'
```

## Features

**Security:**
- ✅ JWT-based authentication
- ✅ Password encryption with BCrypt
- ✅ Stateless session management
- ✅ Role-based access (MOTHER, MATRON)
- ✅ Protected endpoints

**Documentation:**
- ✅ Interactive Swagger UI
- ✅ OpenAPI 3.0 specification
- ✅ Try-it-out functionality
- ✅ JWT authentication in Swagger

## Next Steps

1. **Production Database**: Replace H2 with PostgreSQL/MySQL
2. **File Upload**: Add endpoints for profile photos
3. **Email Service**: Add email notifications
4. **Payment Integration**: Add payment gateway
5. **WebSocket**: Real-time chat between mothers and matrons
6. **Validation**: Add more comprehensive input validation
7. **Error Handling**: Implement global exception handling

## License

MIT License
