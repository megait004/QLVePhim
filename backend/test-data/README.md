# API Testing Guide

## Directory Structure
```
test-data/
├── admin/           # Test data for admin operations
│   ├── register-admin.json
│   ├── create-movie.json
│   ├── update-movie.json
│   ├── create-screening.json
│   └── create-multiple-screenings.json
├── user/            # Test data for user operations
│   ├── register.json
│   ├── login.json
│   ├── update-user.json
│   ├── book-ticket.json
│   ├── book-multiple-tickets.json
│   └── update-ticket-status.json
└── public/          # Test data for public operations
    └── search-movies.json
```

## Setup
1. Start the application
2. Set your JWT token as an environment variable:
```cmd
set TOKEN= token của admin hoặc user

## Test Scenarios

### 1. Public Operations (No Authentication Required)

#### 1.1 Movie Operations
```cmd
# Get all movies
curl -X GET http://localhost:8080/api/movies/public/all

# Get now showing movies
curl -X GET http://localhost:8080/api/movies/public/now-showing

# Get movies by genre
curl -X GET http://localhost:8080/api/movies/public/genre/ACTION

# Get movie details
curl -X GET http://localhost:8080/api/movies/public/1

# Search movies
curl -X POST http://localhost:8080/api/movies/public/search -H "Content-Type: application/json" -d @test-data/public/search-movies.json
```

### 2. User Operations (Authentication Required)

#### 2.1 Authentication
```cmd
# Register new user
curl -X POST http://localhost:8080/api/auth/register -H "Content-Type: application/json" -d @test-data/user/register.json

# Login
curl -X POST http://localhost:8080/api/auth/login -H "Content-Type: application/json" -d @test-data/user/login.json

# Update profile
curl -X PUT http://localhost:8080/api/users/profile -H "Authorization: Bearer %TOKEN%" -H "Content-Type: application/json" -d @test-data/user/update-user.json
```

#### 2.2 Ticket Operations
```cmd
# Book single ticket
curl -X POST http://localhost:8080/api/tickets/book -H "Authorization: Bearer %TOKEN%" -H "Content-Type: application/json" -d @test-data/user/book-ticket.json

# Book multiple tickets
curl -X POST http://localhost:8080/api/tickets/book/batch -H "Authorization: Bearer %TOKEN%" -H "Content-Type: application/json" -d @test-data/user/book-multiple-tickets.json

# Get user's tickets
curl -X GET http://localhost:8080/api/tickets/user/1 -H "Authorization: Bearer %TOKEN%"

# Get ticket details
curl -X GET http://localhost:8080/api/tickets/1 -H "Authorization: Bearer %TOKEN%"

# Update ticket status
curl -X PUT http://localhost:8080/api/tickets/1/status -H "Authorization: Bearer %TOKEN%" -H "Content-Type: application/json" -d @test-data/user/update-ticket-status.json
```

### 3. Admin Operations (Admin Authentication Required)

#### 3.1 Admin Authentication
```cmd
# Register admin user
curl -X POST http://localhost:8080/api/auth/register -H "Content-Type: application/json" -d @test-data/admin/register-admin.json
```

#### 3.2 Movie Management
```cmd
# Create new movie
curl -X POST http://localhost:8080/api/movies -H "Authorization: Bearer %TOKEN%" -H "Content-Type: application/json" -d @test-data/admin/create-movie.json

# Update movie
curl -X PUT http://localhost:8080/api/movies/1 -H "Authorization: Bearer %TOKEN%" -H "Content-Type: application/json" -d @test-data/admin/update-movie.json

# Delete movie
curl -X DELETE http://localhost:8080/api/movies/1 -H "Authorization: Bearer %TOKEN%"
```

#### 3.3 Screening Management
```cmd
# Create new screening
curl -X POST http://localhost:8080/api/screenings -H "Authorization: Bearer %TOKEN%" -H "Content-Type: application/json" -d @test-data/admin/create-screening.json

# Create multiple screenings
curl -X POST http://localhost:8080/api/screenings/batch -H "Authorization: Bearer %TOKEN%" -H "Content-Type: application/json" -d @test-data/admin/create-multiple-screenings.json

# Delete screening
curl -X DELETE http://localhost:8080/api/screenings/1 -H "Authorization: Bearer %TOKEN%"
```

## Test Data Files Description

### Admin Test Data
- `register-admin.json`: Admin user registration data
- `create-movie.json`: New movie creation data
- `update-movie.json`: Movie update data
- `create-screening.json`: Single screening creation data
- `create-multiple-screenings.json`: Batch screening creation data

### User Test Data
- `register.json`: Regular user registration data
- `login.json`: User login credentials
- `update-user.json`: User profile update data
- `book-ticket.json`: Single ticket booking data
- `book-multiple-tickets.json`: Multiple tickets booking data
- `update-ticket-status.json`: Ticket status update data

### Public Test Data
- `search-movies.json`: Movie search criteria

## Testing Tips
1. Always check the response status code and body
2. For protected endpoints, ensure the token is valid and not expired
3. Test both successful and error cases
4. Keep track of created resource IDs for update/delete operations
5. Clean up test data after testing (delete created resources)
6. Test batch operations with both valid and invalid data
7. Verify cascade deletions (e.g., deleting a movie should delete related screenings and tickets)

## Common HTTP Status Codes
- 200 OK: Request successful
- 201 Created: Resource created successfully
- 400 Bad Request: Invalid input data
- 401 Unauthorized: Missing or invalid token
- 403 Forbidden: Insufficient permissions
- 404 Not Found: Resource not found
- 409 Conflict: Resource already exists
- 500 Internal Server Error: Server-side error