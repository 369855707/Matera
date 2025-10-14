# JWT Authentication - Technical Documentation

## What is JWT?

JWT (JSON Web Token) is a compact, URL-safe token format used for securely transmitting information between parties. In our app, it's used to authenticate users without storing session data on the server.

## JWT Structure

A JWT consists of three parts separated by dots (`.`):

```
header.payload.signature
```

**Example:**
```
eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ1c2VyQGV4YW1wbGUuY29tIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c
```

### 1. Header (Red part)
```json
{
  "alg": "HS256",
  "typ": "JWT"
}
```
- `alg`: Algorithm used (HMAC SHA-256)
- `typ`: Token type (JWT)

### 2. Payload (Purple part)
```json
{
  "sub": "user@example.com",
  "iat": 1516239022,
  "exp": 1516325422
}
```
- `sub`: Subject (user email in our case)
- `iat`: Issued at timestamp
- `exp`: Expiration timestamp

### 3. Signature (Blue part)
```
HMACSHA256(
  base64UrlEncode(header) + "." + base64UrlEncode(payload),
  secret_key
)
```

## How JWT Works in Our App

### 1. User Registration/Login Flow

```
┌─────────┐                ┌─────────────┐                ┌──────────┐
│ Client  │                │   Backend   │                │ Database │
│(Flutter)│                │ (Spring)    │                │   (H2)   │
└────┬────┘                └──────┬──────┘                └────┬─────┘
     │                            │                            │
     │ 1. POST /api/auth/login    │                            │
     │ {email, password}          │                            │
     ├───────────────────────────>│                            │
     │                            │                            │
     │                            │ 2. Find user by email      │
     │                            ├───────────────────────────>│
     │                            │                            │
     │                            │ 3. User data               │
     │                            │<───────────────────────────┤
     │                            │                            │
     │                            │ 4. Compare passwords       │
     │                            │    BCrypt.check()          │
     │                            │                            │
     │                            │ 5. Generate JWT token      │
     │                            │    JwtTokenProvider        │
     │                            │                            │
     │ 6. Return token + user     │                            │
     │<───────────────────────────┤                            │
     │                            │                            │
```

### 2. Accessing Protected Endpoints

```
┌─────────┐                ┌─────────────┐
│ Client  │                │   Backend   │
│(Flutter)│                │ (Spring)    │
└────┬────┘                └──────┬──────┘
     │                            │
     │ 1. GET /api/matrons        │
     │ Authorization: Bearer <JWT>│
     ├───────────────────────────>│
     │                            │
     │                     ┌──────▼──────┐
     │                     │JwtAuthFilter│
     │                     │             │
     │                     │ 2. Extract  │
     │                     │    token    │
     │                     │             │
     │                     │ 3. Validate │
     │                     │    token    │
     │                     │             │
     │                     │ 4. Get user │
     │                     │    email    │
     │                     └──────┬──────┘
     │                            │
     │                            │ 5. Set Security
     │                            │    Context
     │                            │
     │                     ┌──────▼──────┐
     │                     │ Controller  │
     │                     │             │
     │                     │ 6. Process  │
     │                     │    request  │
     │                     └──────┬──────┘
     │                            │
     │ 7. Return data             │
     │<───────────────────────────┤
     │                            │
```

## Code Walkthrough

### Step 1: Generate JWT Token

**File:** `JwtTokenProvider.java`

```java
public String generateToken(String email) {
    Date now = new Date();
    Date expiryDate = new Date(now.getTime() + jwtExpiration); // 24 hours

    return Jwts.builder()
            .subject(email)              // Set user email as subject
            .issuedAt(now)              // Set creation time
            .expiration(expiryDate)      // Set expiration time
            .signWith(key)               // Sign with secret key
            .compact();                  // Build the token
}
```

**What happens:**
1. Takes user's email as input
2. Creates timestamp for "now" and "expiry" (24 hours later)
3. Builds JWT with:
   - Subject: user email
   - Issued at: current timestamp
   - Expires: 24 hours from now
4. Signs it with our secret key (from `application.properties`)
5. Returns encoded token string

**Secret Key:**
```properties
# application.properties
jwt.secret=your-256-bit-secret-key-change-this-in-production-please-make-it-long-enough
jwt.expiration=86400000  # 24 hours in milliseconds
```

### Step 2: User Login Process

**File:** `AuthService.java`

```java
public AuthResponse login(LoginRequest request) {
    // 1. Authenticate user credentials
    Authentication authentication = authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(
            request.getEmail(),
            request.getPassword()
        )
    );

    // 2. Find user in database
    User user = userRepository.findByEmail(request.getEmail())
        .orElseThrow(() -> new ResourceNotFoundException("User not found"));

    // 3. Generate JWT token
    String token = jwtTokenProvider.generateToken(user.getEmail());

    // 4. Return token + user info
    return new AuthResponse(token, UserDTO.fromEntity(user));
}
```

**What happens:**
1. Spring Security checks if email & password match (using BCrypt)
2. If valid, fetch user from database
3. Generate JWT token with user's email
4. Return both token and user info to client

### Step 3: Validate JWT Token

**File:** `JwtTokenProvider.java`

```java
public boolean validateToken(String token) {
    try {
        Jwts.parser()
            .verifyWith(key)              // Use our secret key
            .build()
            .parseSignedClaims(token);    // Parse and verify
        return true;
    } catch (JwtException | IllegalArgumentException e) {
        log.error("Invalid JWT token: {}", e.getMessage());
        return false;
    }
}
```

**What happens:**
1. Takes the JWT token from request header
2. Uses the secret key to verify signature
3. Checks if token is expired
4. Returns `true` if valid, `false` if invalid/expired

### Step 4: Extract User from Token

**File:** `JwtTokenProvider.java`

```java
public String getEmailFromToken(String token) {
    Claims claims = Jwts.parser()
        .verifyWith(key)
        .build()
        .parseSignedClaims(token)
        .getPayload();

    return claims.getSubject();  // Returns email
}
```

**What happens:**
1. Parses the JWT token
2. Extracts the "claims" (payload data)
3. Gets the `subject` field (which contains user email)
4. Returns the email

### Step 5: JWT Authentication Filter

**File:** `JwtAuthenticationFilter.java`

This filter runs on EVERY request to protected endpoints:

```java
protected void doFilterInternal(HttpServletRequest request,
                                HttpServletResponse response,
                                FilterChain filterChain) {
    try {
        // 1. Extract JWT from Authorization header
        String jwt = getJwtFromRequest(request);

        // 2. Validate token
        if (StringUtils.hasText(jwt) && jwtTokenProvider.validateToken(jwt)) {

            // 3. Get user email from token
            String email = jwtTokenProvider.getEmailFromToken(jwt);

            // 4. Load user details from database
            UserDetails userDetails = userDetailsService.loadUserByUsername(email);

            // 5. Create authentication object
            UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities()
                );

            // 6. Set authentication in Security Context
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }
    } catch (Exception ex) {
        log.error("Could not set user authentication", ex);
    }

    // 7. Continue to next filter/controller
    filterChain.doFilter(request, response);
}
```

**What happens:**
1. Extracts JWT from `Authorization: Bearer <token>` header
2. Validates the token (signature + expiration)
3. Gets user email from token
4. Loads full user details from database
5. Creates Spring Security authentication object
6. Stores it in Security Context (makes user "logged in")
7. Continues to the actual API endpoint

### Step 6: Extract JWT from Header

**File:** `JwtAuthenticationFilter.java`

```java
private String getJwtFromRequest(HttpServletRequest request) {
    String bearerToken = request.getHeader("Authorization");

    // Check if header starts with "Bearer "
    if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
        return bearerToken.substring(7);  // Remove "Bearer " prefix
    }
    return null;
}
```

**Example:**
- Header: `Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...`
- Extracted: `eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...`

## Security Configuration

**File:** `SecurityConfig.java`

```java
@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http) {
    http
        .csrf(AbstractHttpConfigurer::disable)
        .authorizeHttpRequests(auth -> auth
            // Public endpoints (no JWT required)
            .requestMatchers("/api/auth/**", "/h2-console/**",
                           "/swagger-ui/**", "/v3/api-docs/**").permitAll()

            // All other endpoints require authentication
            .anyRequest().authenticated()
        )
        .sessionManagement(session -> session
            .sessionCreationPolicy(SessionCreationPolicy.STATELESS)  // No sessions
        )
        // Add JWT filter before Spring Security's default filter
        .addFilterBefore(jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class);

    return http.build();
}
```

**What happens:**
- `/api/auth/**` is public (anyone can register/login)
- All other endpoints require valid JWT
- Uses stateless sessions (no session cookies)
- JWT filter runs before every request

## Complete Authentication Flow Example

### Example 1: User Login

**Request:**
```http
POST http://localhost:8080/api/auth/login
Content-Type: application/json

{
  "email": "mother@test.com",
  "password": "password"
}
```

**Backend Process:**
1. `AuthController.login()` receives request
2. `AuthService.login()` is called
3. Spring Security validates password using BCrypt
4. User found in database
5. `JwtTokenProvider.generateToken("mother@test.com")` creates token
6. Token is signed with secret key

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJtb3RoZXJAdGVzdC5jb20iLCJpYXQiOjE3MDk4MTIzNDUsImV4cCI6MTcwOTg5ODc0NX0.abc123xyz",
  "user": {
    "id": 1,
    "name": "Demo Mother",
    "email": "mother@test.com",
    "role": "MOTHER",
    "createdAt": "2024-01-01T00:00:00"
  }
}
```

### Example 2: Access Protected Endpoint

**Request:**
```http
GET http://localhost:8080/api/matrons
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJtb3RoZXJAdGVzdC5jb20iLCJpYXQiOjE3MDk4MTIzNDUsImV4cCI6MTcwOTg5ODc0NX0.abc123xyz
```

**Backend Process:**
1. `JwtAuthenticationFilter` intercepts request
2. Extracts token from header: `eyJhbGci...abc123xyz`
3. `JwtTokenProvider.validateToken()` checks:
   - Signature is valid (using secret key)
   - Token not expired
4. `JwtTokenProvider.getEmailFromToken()` extracts: `mother@test.com`
5. `CustomUserDetailsService.loadUserByUsername()` loads user from DB
6. Sets authentication in Security Context
7. Request reaches `MatronController.getAllMatrons()`
8. Controller processes and returns data

**Response:**
```json
[
  {
    "id": 1,
    "name": "Zhang Wei",
    "age": 35,
    "yearsOfExperience": 8,
    "pricePerMonth": 12000,
    "location": "Beijing, Chaoyang District",
    ...
  }
]
```

## Why JWT is Secure

### 1. **Cryptographic Signature**
- Token is signed with secret key (only server knows)
- Any tampering invalidates the signature
- Client cannot modify token contents

**Example - Tampering Attempt:**
```
Original Token:
eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ1c2VyQGV4YW1wbGUuY29tIn0.signature123

If attacker changes email in payload:
eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJhZG1pbkBleGFtcGxlLmNvbSJ9.signature123

Result: Signature validation FAILS ❌
```

### 2. **Expiration**
- Tokens expire after 24 hours
- Limits damage if token is stolen
- User must re-authenticate

### 3. **Stateless**
- No session storage on server
- Token contains all needed info
- Scales horizontally easily

### 4. **HTTPS Only**
- Always use HTTPS in production
- Prevents token interception

## Common Issues & Solutions

### Issue 1: "Invalid JWT token"
**Cause:** Token expired or tampered with
**Solution:** Login again to get new token

### Issue 2: "Unauthorized"
**Cause:** Missing or invalid Authorization header
**Solution:** Add header: `Authorization: Bearer <your-token>`

### Issue 3: Token works in Postman but not Flutter
**Cause:** Header not properly set in Flutter
**Solution:**
```dart
final response = await http.get(
  Uri.parse('$baseUrl/matrons'),
  headers: {
    'Authorization': 'Bearer $token',
    'Content-Type': 'application/json',
  },
);
```

## Testing JWT in Flutter

### 1. Login and Store Token
```dart
Future<void> login(String email, String password) async {
  final response = await http.post(
    Uri.parse('$baseUrl/auth/login'),
    headers: {'Content-Type': 'application/json'},
    body: jsonEncode({
      'email': email,
      'password': password,
    }),
  );

  if (response.statusCode == 200) {
    final data = jsonDecode(response.body);
    final token = data['token'];

    // Store token (use shared_preferences or secure_storage)
    await storage.write(key: 'jwt_token', value: token);
  }
}
```

### 2. Use Token in Requests
```dart
Future<List<Matron>> getMatrons() async {
  final token = await storage.read(key: 'jwt_token');

  final response = await http.get(
    Uri.parse('$baseUrl/matrons'),
    headers: {
      'Authorization': 'Bearer $token',
      'Content-Type': 'application/json',
    },
  );

  if (response.statusCode == 200) {
    final List<dynamic> data = jsonDecode(response.body);
    return data.map((json) => Matron.fromJson(json)).toList();
  } else if (response.statusCode == 401) {
    // Token expired - redirect to login
  }
}
```

## Summary

1. **Login** → Server generates JWT signed with secret key
2. **Store** → Client stores JWT (memory/secure storage)
3. **Request** → Client sends JWT in `Authorization` header
4. **Validate** → Server verifies signature & expiration
5. **Extract** → Server gets user email from token
6. **Authorize** → Server loads user and processes request

JWT allows stateless authentication - the server doesn't need to remember who's logged in. The token itself proves identity!
