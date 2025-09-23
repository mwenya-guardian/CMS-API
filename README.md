# CMS Backend API

A comprehensive Spring Boot backend for a Content Management System with JWT authentication, MongoDB integration, file handling, and export functionality.

## Features

- **Authentication & Authorization**: JWT-based authentication with role-based access control
- **Content Management**: Full CRUD operations for Publications, Events, and Quotes
- **File Upload**: Image upload with validation and size limits
- **Export Functionality**: PDF and PowerPoint export capabilities
- **Search & Filtering**: Advanced search and filtering options
- **Pagination**: Efficient pagination for large datasets
- **MongoDB Integration**: Document-based storage with proper indexing
- **CORS Support**: Configurable CORS for frontend integration

## Tech Stack

- **Java 21**
- **Spring Boot 3.5.6**
- **Spring Security** (JWT Authentication)
- **Spring Data MongoDB**
- **iText PDF** (PDF generation)
- **Apache POI** (PowerPoint generation)
- **Maven** (Build tool)

## Quick Start

### Prerequisites

- Java 17 or higher
- MongoDB running on localhost:27017
- Maven 3.6+

### Installation

1. Clone the repository
2. Navigate to the project directory
3. Install dependencies:
   ```bash
   mvn clean install
   ```

4. Start MongoDB service

5. Run the application:
   ```bash
   mvn spring-boot:run
   ```

The API will be available at `http://localhost:3001/api`

### Default Admin User

A default admin user is created automatically:
- **Email**: admin@cms.com
- **Password**: admin123

## API Endpoints

### Authentication
- `POST /auth/login` - User login
- `POST /auth/logout` - User logout
- `GET /auth/me` - Get current user info

### Publications
- `GET /publications` - Get all publications (with filtering)
- `GET /publications/paginated` - Get paginated publications
- `GET /publications/{id}` - Get publication by ID
- `GET /publications/year/{year}` - Get publications by year
- `POST /publications` - Create new publication
- `PUT /publications/{id}` - Update publication
- `DELETE /publications/{id}` - Delete publication
- `POST /publications/export/pdf` - Export publications to PDF
- `POST /publications/export/ppt` - Export publications to PowerPoint
- `POST /publications/upload-image` - Upload publication image

### Events
- `GET /events` - Get all events (with filtering)
- `GET /events/paginated` - Get paginated events
- `GET /events/{id}` - Get event by ID
- `POST /events` - Create new event
- `PUT /events/{id}` - Update event
- `DELETE /events/{id}` - Delete event
- `POST /events/export/pdf` - Export events to PDF
- `POST /events/upload-image` - Upload event image

### Quotes
- `GET /quotes` - Get all quotes (with filtering)
- `GET /quotes/paginated` - Get paginated quotes
- `GET /quotes/{id}` - Get quote by ID
- `POST /quotes` - Create new quote
- `PUT /quotes/{id}` - Update quote
- `DELETE /quotes/{id}` - Delete quote
- `POST /quotes/export/pdf` - Export quotes to PDF
- `POST /quotes/upload-image` - Upload quote image

## Configuration

### Application Properties

Key configuration options in `application.yml`:

```yaml
server:
  port: 3001

spring:
  data:
    mongodb:
      uri: mongodb://localhost:27017/cms_db

jwt:
  secret: mySecretKey123456789012345678901234567890
  expiration: 86400000 # 24 hours

file:
  upload:
    dir: ./uploads
    max-size: 5242880 # 5MB
    allowed-types: image/jpeg,image/png,image/gif,image/webp

cors:
  allowed-origins: http://localhost:5173,http://localhost:3000
```

### Security

- JWT tokens expire after 24 hours
- Passwords are encrypted using BCrypt
- CORS is configured for frontend integration
- File uploads are validated for type and size

## Data Models

### User
- ID, email, name, password, role, avatar
- Roles: ADMIN, EDITOR, VIEWER
- Automatic timestamps

### Publication
- ID, title, content, imageUrl, date, layoutType
- Author, tags, featured flag
- Layout types: GRID, LIST, MASONRY

### Event
- ID, title, description, imageUrl, startDate, endDate
- Location, category, featured flag
- Categories: WEDDING, CONFERENCE, WORKSHOP, SOCIAL, OTHER

### Quote
- ID, text, author, source, category
- ImageUrl, featured flag
- Automatic timestamps

## File Upload

- **Supported formats**: JPEG, PNG, GIF, WebP
- **Maximum size**: 5MB
- **Storage**: Local filesystem (./uploads directory)
- **URL format**: `http://localhost:3001/api/uploads/{filename}`

## Export Features

### PDF Export
- Publications, Events, and Quotes can be exported to PDF
- Customizable title and formatting
- Includes all relevant metadata

### PowerPoint Export
- Publications can be exported to PowerPoint presentations
- Each publication becomes a slide
- Automatic content truncation for readability

## Error Handling

- Global exception handler for consistent error responses
- Validation error messages
- Appropriate HTTP status codes
- Detailed error information in development

## Development

### Project Structure
```
src/main/java/com/cms/
├── config/          # Configuration classes
├── controller/      # REST controllers
├── dto/            # Data Transfer Objects
├── exception/      # Exception handling
├── model/          # Entity models
├── repository/     # Data repositories
├── security/       # Security configuration
└── service/        # Business logic
```

### Building for Production

1. Update configuration for production environment
2. Build the application:
   ```bash
   mvn clean package
   ```
3. Run the JAR file:
   ```bash
   java -jar target/cms-backend-0.0.1-SNAPSHOT.jar
   ```

## Testing

Run tests with:
```bash
mvn test
```

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests if applicable
5. Submit a pull request

## License

This project is licensed under the MIT License.