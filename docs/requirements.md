# Backend Requirements

This document lists the requirements fulfilled by the backend, grouped into Functional, Non-Functional, Software, and Hardware categories. All items are grounded in the implementation and configuration under `Backend/`.

## Functional Requirements

- **Authentication & Authorization**
  - JWT-based login, logout, and current-user endpoints (`/auth/login`, `/auth/logout`, `/auth/me`).
  - Role-based access control with roles: ADMIN, EDITOR, USER, VIEWER; VIEWER cannot log in.
  - Stateless security enforced via JWT filter.
- **User Management**
  - Public registration (`/users/register`); admin-only creation; fetch by ID; paginated listing; update.
  - User activation flag and `lastLogin` tracking.
- **Publications Management**
  - CRUD; get by ID; filtering; pagination; get by year.
  - Export to PDF and PowerPoint; image upload with validation.
- **Events Management**
  - CRUD; get by ID; filtering; pagination; PDF export; image upload.
- **Quotes Management**
  - CRUD; get by ID; filtering; pagination; PDF export; image upload.
- **Posts & Media**
  - Media retrieval; internal redirect header for protected video delivery.
  - Image/video upload (public/private), server-side validation, and secure file serving.
- **Church Details & Members**
  - Church details CRUD; members endpoints incl. position and positionType lookups.
- **Giving (Tithe & Offering)**
  - CRUD and pagination for tithe and offering records.
- **Bulletins**
  - Bulletin CRUD incl. schedules, announcements, on-duty lists; statuses: DRAFT, PUBLISHED, SCHEDULED.
  - HTML/PDF generation for bulletins; summaries and date-based retrieval.
- **Newsletters**
  - Newsletter schedule CRUD, start/stop/reschedule, run-now endpoint.
  - Batch email sending to verified/active subscribers; logging of sends.
- **Reactions & Comments**
  - Record reactions across entities; fetch comments for analysis and details by entity.
- **Analytics & AI**
  - Sentiment and moderation analytics with stats, trends, recent alerts.
  - Bulk comment analysis submission; job lookup; AI chat endpoint (Vertex AI Gemini).
- **CORS & API Docs**
  - CORS configured for defined origins, methods, and credentials.
  - OpenAPI/Swagger UI exposed for REST documentation.

## Non-Functional Requirements

- **Security**
  - JWT expiry (24h), BCrypt password hashing, role-based authorization, active-user checks.
  - Public GET access to certain resources; others require valid JWT.
- **Validation & Error Handling**
  - Bean validation on DTOs and entities; global exception handler with consistent API responses and proper HTTP status codes.
- **Performance & Scalability**
  - Pagination for list endpoints; MongoDB auto-index creation enabled.
  - Asynchronous/scheduled newsletter jobs; stateless services to scale horizontally.
  - Streaming file responses with correct content types; cache-control headers for images.
- **Reliability & Observability**
  - Structured logging with category levels; actuator endpoints allowed (where enabled).
  - Email batching (e.g., size 200) and lightweight throttling to reduce provider rate-limit risk.
- **Interoperability**
  - RESTful JSON APIs; CORS for specified frontend origins.
  - Swagger/OpenAPI for discoverability and client generation.
- **Maintainability**
  - Layered architecture: `config`, `controller`, `dto`, `exception`, `model`, `repository`, `security`, `service`.
  - DTOs for request/response; separation of concerns across services and repositories.
- **Compliance & Secrets Management**
  - External service credentials (DB, Mailgun, Gmail, Vertex AI) must be provided securely via environment/config, not hardcoded in production.

## Software Requirements

- **Runtime & Build**
  - Java 21; Maven 3.6+.
  - Spring Boot 3.5.x with starters: Web, Security, Validation, Mail, Thymeleaf.
- **Data Store**
  - MongoDB (local `mongodb://localhost:27017/cms` or Atlas), with `auto-index-creation` enabled.
- **Libraries/Dependencies**
  - JWT: `io.jsonwebtoken` (jjwt-api/impl/jackson).
  - PDF: iText 7; Office: Apache POI (pptx, scratchpad, schemas).
  - Utilities: commons-io, commons-lang3; Lombok.
  - Docs: `springdoc-openapi-starter-webmvc-ui` (Swagger UI).
  - Email: Mailgun Java SDK and Jakarta Mail.
  - AI: Spring AI Vertex AI Gemini.
- **Infrastructure & Services**
  - SMTP server (e.g., Gmail SMTP) for sending newsletters and notifications.
  - Vertex AI access for AI analysis/chat features.
  - Optional reverse proxy (e.g., Nginx) for secure internal file redirects (`X-Accel-Redirect`).
  - Docker support (multi-stage build) for containerized deployment.
- **Configuration**
  - Server: port 3001, context path `/api`.
  - CORS: allowed origins/methods/headers; credentials allowed.
  - Uploads: base dir `./uploads`; subdirs `public`/`private`; max sizes image 10MB, video 50MB; allowed MIME types configured.
  - JWT: secret and expiration configurable.

## Hardware Requirements

- **Server**
  - Host capable of running Java 21 JRE; CPU sufficient for PDF/PPT generation and occasional AI requests.
  - Memory to run Spring Boot app and MongoDB comfortably (e.g., 2–4 GB RAM minimum for small deployments).
  - Disk space for MongoDB data files and media uploads (plan for growth; images up to 10MB, videos up to 50MB each).
  - Network connectivity for SMTP and Vertex AI endpoints; bandwidth appropriate for serving media and sending emails.
- **Optional**
  - Separate machine/managed service for MongoDB (Atlas) for durability and scaling.
  - Reverse proxy (e.g., Nginx) if serving large/private media with internal redirects.



