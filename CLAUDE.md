# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

SIGNAL is a Spring Boot application built with Kotlin that provides a dating/matchmaking service platform. The application connects users through profiles and allows them to discover and contact each other using a ticket-based system.

## Development Commands

### Build and Run
```bash
./gradlew build          # Build the project
./gradlew bootRun        # Run the Spring Boot application
./gradlew clean          # Clean build artifacts
```

### Testing
```bash
./gradlew test           # Run all tests
./gradlew check          # Run all checks including tests
./gradlew testClasses    # Compile test classes only
```

### Single Test Execution
```bash
./gradlew test --tests "ClassName"                    # Run specific test class
./gradlew test --tests "ClassName.methodName"        # Run specific test method
```

## Architecture Overview

### Domain-Driven Design Structure
The codebase follows Domain-Driven Design (DDD) principles with clear separation of concerns:

```
src/main/kotlin/com/yourssu/signal/
├── api/                    # Controllers and API DTOs
├── config/                 # Configuration classes (security, properties, etc.)
├── domain/                 # Domain modules (business logic)
│   ├── auth/              # Authentication & user management
│   ├── blacklist/         # User blocking functionality  
│   ├── order/             # Order management
│   ├── payment/           # Payment processing (KakaoPay integration)
│   ├── profile/           # User profiles and matching
│   └── viewer/            # Viewer/user data access
├── infrastructure/        # External integrations
└── utils/                 # Utility classes
```

### Domain Module Pattern
Each domain module follows a consistent structure:
- `business/` - Application services and business logic
- `implement/` - Domain entities, repositories, and business rules
- `storage/` - Data persistence (JPA entities, repository implementations)

### Key Technologies
- **Spring Boot 3.4.1** with Kotlin 1.9.25
- **JPA/Hibernate** with QueryDSL for database operations
- **Spring Security** with JWT authentication
- **KakaoPay API** integration for payments
- **H2/MySQL** database support
- **Kotest** for testing framework
- **OpenAPI/Swagger** for API documentation

### Core Business Domains

#### Profile Management
- Users create profiles with personal information (encrypted contact data)
- Profile matching based on gender and preferences
- Priority-based random profile selection
- Contact information is encrypted using `DataCipher`

#### Ticket System
- Users consume tickets to reveal contact information
- Order history tracking for ticket purchases
- Payment integration with KakaoPay

#### Authentication
- Google OAuth integration
- JWT-based session management
- User UUID resolution via argument resolvers

### Key Patterns & Conventions

#### Dependency Injection
- All entities should be Spring-managed beans (`@Service`, `@Component`, etc.)
- Constructor injection is preferred
- Configuration properties use `@ConfigurationProperties`

#### Data Encryption
- Contact information is encrypted in storage using `DataCipher`
- Always encrypt before saving and decrypt after loading contact data

#### Error Handling
- Custom domain-specific exceptions (e.g., `ProfileNotFoundException`)
- Global exception handling through Spring's exception resolution

#### Testing
- Use Kotest framework for test assertions
- Test classes follow the pattern `ClassNameTest.kt`
- Integration tests available for repository layers

### Important Files

#### Configuration
- `application.properties` / `application.yml` - Application configuration
- `DataCipher.kt` - Encryption/decryption utilities for sensitive data
- `WebSecurityConfig.kt` - Security configuration

#### Core Entities  
- `Profile.kt` - Main user profile domain model
- `ProfileEntity.kt` - JPA entity for profile persistence
- `User.kt` / `UserEntity.kt` - User authentication data

### Development Notes

#### Database Schema
- Uses JPA with automatic schema generation
- BaseEntity provides common audit fields (created/updated timestamps)
- QueryDSL integration for type-safe queries

#### Security Considerations
- Contact data must always be encrypted in storage
- JWT tokens for authentication
- Admin access controls for sensitive operations

#### Testing Data
- H2 in-memory database for testing
- Test fixtures and mock data available in test resources