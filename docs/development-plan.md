# Inkloom AI — Development Plan & Roadmap

## Phase Breakdown

### [x] Phase 1: Project Foundation (Current)
* Monorepo folder layout (`frontend/`, `backend/`, `docs/`, `README.md`, `.gitignore`).
* Java 21+ Spring Boot foundation with Maven Wrapper, Health endpoint (`/api/v1/health`), and `@ControllerAdvice` error envelope.
* React + Vite + Tailwind CSS frontend foundation with pure JavaScript (no TypeScript) and application shell.
* Build and verification pipelines.

### [ ] Phase 2: MongoDB Persistence Layer
* Spring Data MongoDB configuration and connection pool.
* Document entity models: `User`, `BrandWorkspace`, `BrandGuideline`, `ApprovedSource`, `ApprovedClaim`, `Review`, `Revision`, `ReviewComment`, `ReviewDecision`, `AuditEvent`, `ReviewFeedback`.
* Repositories with custom indexing and queries.

### [ ] Phase 3: Authentication, Security & RBAC
* BCrypt password hashing.
* JWT generation, parsing, and expiration validation.
* Spring Security filters and entry points.
* Roles: `ADMIN`, `BRAND_MANAGER`, `CONTENT_CREATOR`, `REVIEWER`.
* Multi-tenant brand workspace isolation checks.

### [ ] Phase 4: Brand Workspace, Guidelines, Sources & Claims
* Brand Workspace CRUD endpoints.
* Guideline management (required/prohibited terms, tone attributes).
* Approved Sources storage and document viewer.
* Approved Claims ledger with expiration and scope flags.

### [ ] Phase 5: Deterministic Review Engine
* Deterministic rule engine for exact prohibited phrases and required terms.
* Basic readability metrics.
* Content review creation and persistence.

### [ ] Phase 6: AI Integration & Evidence Verification
* `AiReviewProvider` interface and Gemini adapter.
* Deterministic fallback when no API key is configured.
* Anti-hallucination evidence validation against MongoDB.

### [ ] Phase 7: Intent Lock Engine & Revisions
* Light, Balanced, and Strong correction generator.
* Text diff comparison utility.
* Revisions history and suggestion acceptance flow.

### [ ] Phase 8: Human Review & Approval Workflow
* Approval queue.
* Human reviewer comments and change requests.
* Approve/Reject actions with state transition validation.
* Security and audit event tracking.

### [ ] Phase 9: Clarity Heatmap & Analytics
* Aggregation queries for recurring violations.
* Guideline conflict signals detection.
* Visual heatmap and violation trends.

### [ ] Phase 10: Full React UI / UX Suite
* Implementation of all 14 application pages.
* Editorial styling, responsive navigation, and state handlers (empty, loading, error).

### [ ] Phase 11: End-to-End Verification & Documentation
* Full integration tests.
* Complete REST API specification (`docs/api.md`).
* Production readiness verification.
