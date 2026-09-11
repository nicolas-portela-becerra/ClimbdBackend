# Climbd Backend

Backend API for a climbing-focused application designed to help climbing gyms create and manage boulders on their spray walls.

The project is being developed as a personal project and portfolio piece, with a focus on backend architecture, API design, authentication, authorization, image handling and maintainable Java/Spring development.

## Features

### Authentication & Authorization

* User registration and login
* JWT-based authentication
* Access and refresh tokens
* Refresh token revocation
* Logout from a single session or all sessions
* Google OAuth authentication **(TODO)**
* Apple OAuth authentication **(TODO)**
* Role-based access control
* Protected gym and boulder operations

### Gym Management

* Create and manage climbing gyms
* Assign gym owners
* Retrieve gyms with pagination
* Role and ownership-based authorization

### Spray Wall Images

* Upload spray wall images
* Image metadata management
* Full image retrieval
* Thumbnail retrieval
* File size validation
* Support for common image formats

### Boulder Management

* Create boulders associated with a gym
* Define holds directly when creating a boulder
* Retrieve boulders by gym
* Retrieve boulders by wall image
* Retrieve detailed boulder information
* Update boulders
* Delete boulders
* Creator and administrator authorization

### API Protection

* Rate limiting with Bucket4j
* Request validation
* Protected endpoints using Bearer authentication
* Centralized API contract using OpenAPI

---

## Tech Stack

| Technology                  | Purpose                                   |
| --------------------------- | ----------------------------------------- |
| Java 21                     | Programming language                      |
| Spring Boot                 | Application framework                     |
| Spring Security             | Authentication and authorization          |
| Spring Data JPA / Hibernate | Persistence                               |
| PostgreSQL                  | Relational database                       |
| Redis                       | Application data / infrastructure support |
| Flyway                      | Database migrations                       |
| JJWT                        | JWT handling                              |
| Bucket4j                    | Rate limiting                             |
| MapStruct                   | DTO/entity mapping                        |
| Lombok                      | Boilerplate reduction                     |
| OpenAPI                     | API contract and documentation            |
| OpenAPI Generator           | API interface/model generation            |
| Docker                      | Containerized deployment                  |

The project is built as a multi-module Maven project.

---

## Architecture

The backend is organized into separate modules to keep the domain, application logic, infrastructure and delivery mechanisms isolated from each other.

```text
code/
├── api-client/
├── api/
├── application/
├── auth/
├── domain/
├── infrastructure/
└── boot/
```

---

## API

The API is defined using an OpenAPI 3.1.

The specification acts as the contract between the API definition and the generated Spring interfaces/models.

The current API is organized around the following resources:

```text
/api/auth
/api/gyms
/api/wall-images
/api/boulders
```

### Authentication

```text
POST /api/auth/register
POST /api/auth/login
POST /api/auth/logout
POST /api/auth/logout-all
POST /api/auth/refresh
POST /api/auth/oauth/google
POST /api/auth/oauth/apple
```

### Gyms

```text
GET  /api/gyms
POST /api/gyms

GET  /api/gyms/{gymId}
PUT  /api/gyms/{gymId}

POST /api/gyms/{gymId}/owners
```

### Wall Images

```text
GET  /api/gyms/{gymId}/wall-images
POST /api/gyms/{gymId}/wall-images

GET /api/wall-images/{wallImageId}
GET /api/wall-images/{wallImageId}/data
GET /api/wall-images/{wallImageId}/thumbnail
```

### Boulders

```text
GET  /api/gyms/{gymId}/boulders
POST /api/gyms/{gymId}/boulders

GET    /api/wall-images/{wallImageId}/boulders

GET    /api/boulders/{boulderId}
PUT    /api/boulders/{boulderId}
DELETE /api/boulders/{boulderId}
```

The complete API contract can be found in [`code/openapi.yml`](./code/openapi.yml).

---

## Running locally

Clone the repository:

```bash
git clone https://github.com/nicolas-portela-becerra/ClimbdBackend.git
cd ClimbdBackend
```

Move into the Maven project:

```bash
cd code
```

Build the project:

```bash
mvn clean install
```

Run the application:

```bash
mvn spring-boot:run boot
```

The API will be available at:

```text
http://localhost:8080
```

---

## Database Migrations

Database migrations are managed with Flyway.

Migration files are located under:

```text
code/boot/src/main/resources/db/migration
```

Hibernate is configured with:

```text
ddl-auto: validate
```

This means the application validates the database schema against the entity model rather than automatically modifying the schema.

---

## Project Goals

The main goal of this project is to build a realistic backend for a climbing application while exploring backend engineering practices that are useful beyond a simple CRUD application.

Some of the areas explored by the project include:

* Modular application architecture
* Domain-oriented separation
* REST API design
* Authentication and authorization
* JWT access and refresh tokens
* OAuth authentication
* Role and resource ownership
* API-first development with OpenAPI
* DTO mapping
* Database migrations
* Rate limiting
* Image upload and processing
* Pagination
* Containerization
* Automated API generation

---

## Project Status

This project is actively being developed.

The backend is being built incrementally, with particular attention being given to the architecture and implementation of image processing and boulder creation workflows.

Some parts of the application may therefore change as the project evolves.

---

## License

This project is licensed under the MIT License.

See the [LICENSE](./LICENSE) file for details.

---

## Author

**Nicolas Portela Becerra**

GitHub: [@nicolas-portela-becerra](https://github.com/nicolas-portela-becerra)
