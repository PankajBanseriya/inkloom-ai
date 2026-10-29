# Inkloom AI — Intelligent Brand Governance Platform

> An enterprise SaaS brand governance platform ensuring all marketing content, announcements, and corporate communications comply with approved brand guidelines, verified claims, tone of voice, terminology, and legal disclosures.

---

## 🌐 Live Deployments

- **Frontend Application**: [https://inkloom-ai-xi.vercel.app](https://inkloom-ai-xi.vercel.app)
- **Backend REST API**: [https://inkloom-ai-backend-taf4.onrender.com](https://inkloom-ai-backend-taf4.onrender.com)
- **API Health Check**: [https://inkloom-ai-backend-taf4.onrender.com/api/v1/health](https://inkloom-ai-backend-taf4.onrender.com/api/v1/health)

---

## 📌 Platform Overview

Organizations struggle with brand dilution, unverified performance claims, prohibited buzzwords, tone misalignment, and missing mandatory disclosures. Traditional grammar checkers identify superficial spelling mistakes but lack brand context, factual grounding, and audit accountability.

**Inkloom AI** solves this through a **hybrid deterministic and Google Gemini-powered governance engine**. It explains *why* an issue matters, provides evidence citations to authorized brand sources, offers surgical intent-preserving corrections across three strengths, enforces human-in-the-loop review approvals, and tracks full document revision history in MongoDB.

---

## 🏗️ Architecture & Technology Stack

```
+----------------------------------------------------------------------------------------------------+
|                                    INKLOOM AI SYSTEM ARCHITECTURE                                  |
+----------------------------------------------------------------------------------------------------+
|  FRONTEND (React 18 / Vite 5)                                                                      |
|  - Pure JavaScript (JSX) • Tailwind CSS • Lucide Icons • React Router v6 • Centralized API Layer   |
|  - Pages: Workspaces, Guidelines, Claims Ledger, Content Reviewer, Intent Lock, Approvals, Heatmap |
+----------------------------------------------------------------------------------------------------+
                                      |  REST JSON API (Bearer JWT / CORS Preflight)
                                      v
+----------------------------------------------------------------------------------------------------+
|  BACKEND (Java 21 / Spring Boot 3.3.5)                                                             |
|  - Spring MVC Controllers • Spring Security (Stateless JWT / RBAC) • Jakarta Bean Validation       |
|  - ComplianceRiskEvaluator (Deterministic Gates) • ReviewService • ApprovalService • Analytics     |
|  - Enriched Audit Trail Emitters • State Machine Transition Validators                             |
+----------------------------------------------------------------------------------------------------+
               |                                                   |
               v                                                   v
+-----------------------------+               +------------------------------------------------------+
|  DATABASE (MongoDB Atlas)   |               |  AI ENGINE (Official Google Gen AI Java SDK)        |
|  - Users & Workspaces       |               |  - Model: gemini-2.5-flash (Developer API)           |
|  - Guidelines & Sources     |               |  - Structured JSON Schema • Low Temperature (0.1)    |
|  - Approved Claims Ledger   |               |  - Server-Side Zero-Hallucination Source Verification |
|  - Reviews, Decisions & Log |               |  - Graceful Deterministic Fallback when Offline/No-Key|
+-----------------------------+               +------------------------------------------------------+
```

### Stack Components:
- **Frontend**: React 18, Vite 5, Tailwind CSS, Lucide React, React Router 6. (Zero TypeScript; pure maintainable ES6+/JSX).
- **Backend**: Java 21, Spring Boot 3.3.5, Spring Security 6, Spring Data MongoDB, Jakarta Validation, Jackson (JSR310 `Instant` support).
- **AI Integration**: Official Google Gen AI Java SDK (`com.google.genai:google-genai`).
- **Database**: MongoDB Atlas storing 12 distinct domain collections with indexes and referential integrity as the single source of truth.

---

## 🚀 Round 3: Product Improvements

### 1. Evidence-Grounded Brand Compliance Engine
Inkloom AI connects directly to MongoDB Atlas to evaluate content against verified brand assets with zero hallucinations:
- **Authorized Source Grounding**: Dynamically queries active approved guidelines, approved claims, and ground-truth sources isolated per workspace.
- **Deterministic & Gemini Hybrid Review**:
  - Exact prohibited terminology matching (e.g., unqualified absolutes like *"guaranteed"*, *"unbreakable"*).
  - Mandatory disclaimer enforcement for regulated content (e.g., *"past performance does not guarantee future results"*).
  - Numeric discrepancy detection (e.g., claiming *"99.99% uptime"* when verified SLA is *"99.9%"*).
  - Scope and domain contradiction detection (e.g., claiming unrestricted HIPAA compliance when approved scope is B2B Cloud only).
  - Deprecated or pending source verification: flags citations referencing sources that are inactive, pending, or expired.
- **Uncertainty Classification**: Every finding is categorized with explainable confidence:
  - `CONFIRMED`: Verified rule violation or claim backed by approved sources.
  - `LOW_CONFIDENCE`: AI confidence below 70% or ambiguous contextual match; automatically routed to human reviewer.
  - `UNSUPPORTED`: Claim lacking registered evidence in the workspace ledger.
  - `CONTRADICTION`: Content directly conflicts with registered ground truth.
  - `NUMERIC_DISCREPANCY`: Numerical figures diverge from benchmark data.
  - `EXPIRED_EVIDENCE`: Supporting evidence or claim registration has lapsed past expiration date.
- **Claim Verification Lifecycle**: Classifies claim status as `MATCHED`, `CONTRADICTED`, `UNSUPPORTED`, `EXPIRED`, or `PENDING_REVIEW`.

### 2. Risk-Based Review and Intelligent Approval Workflow
- **Explainable Risk Classification**: Evaluates every submission using a deterministic risk engine (`ComplianceRiskEvaluator`):
  - `CRITICAL`: Absolute prohibited terms, severe factual contradictions, or missing mandatory legal disclaimers.
  - `HIGH`: Numeric claim discrepancies, expired evidence, or low overall compliance (< 75).
  - `MEDIUM`: Unsupported factual claims or minor tone discrepancies.
  - `LOW`: Stylistic suggestions or minor formatting preferences.
- **Non-Negotiable Compliance Gates**:
  - Calculates a composite risk score (0–100) and extracts explicit blocking reasons.
  - Sets `approvalBlocked = true` whenever critical or high-risk mandatory compliance rules are broken.
  - **Hard Gate Rule**: AI scores and confidence metrics can **NEVER** override mandatory blocking conditions.
- **Separation of Duties**:
  - Authors and Content Creators are strictly blocked from self-approving their own submissions.
  - The backend validates reviewer identity against the review author; attempts to self-approve throw `InvalidStateTransitionException` unless the actor holds an `ADMIN` exemption role.
- **Approval Queue UI**:
  - Displays risk level badges (`CRITICAL`, `HIGH`, `MEDIUM`, `LOW`) with computed risk scores.
  - Renders a prominent **Blocked** badge and tooltip explaining the blocking conditions.
  - Disables the "Approve" button when compliance blocking gates exist or when the user is the author.

### 3. Continuous Brand Intelligence and Audit Trail
- **Enriched Lifecycle Audit Trail**:
  - Automatically records immutable audit events for every critical lifecycle transition:
    - `REVIEW_SUBMITTED`, `CORRECTION_ACCEPTED`, `REVISION_SAVED`, `DECISION_RECORDED`, `COMMENT_ADDED`.
  - Captures actor ID, actor name, actor role, timestamp, entity references, and rich transition metadata.
- **Workspace Intelligence Dashboard**:
  - Computes real operational metrics directly from MongoDB collections:
    - **Average Turnaround Time**: Real hours elapsed from submission to final decision.
    - **High-Risk Pending Reviews**: Real count of active in-flight reviews flagged as CRITICAL or HIGH risk.
    - **Resolved vs. Unresolved Findings**: Ratio of accepted/fixed findings vs. open violations across active revisions.
    - **Recurring Guideline Violations**: Top 5 most frequently breached guidelines and rules with violation frequencies.
    - **Category Heatmap Matrix**: Acceptance rates and repeated buzzword phrases across reviews.

---

## ✨ Core Platform Capabilities

### 1. Evidence Cards
Every flagged issue renders an interactive Evidence Card showing:
- Category, rule title, and severity (`CRITICAL`, `WARNING`, `INFO`).
- Original text excerpt and governance explanation.
- Supporting guideline or verified source citation (title, section, and reference ID).
- Evidence uncertainty badge (`CONFIRMED`, `LOW_CONFIDENCE`, `UNSUPPORTED`, `CONTRADICTION`, `NUMERIC_DISCREPANCY`, `EXPIRED_EVIDENCE`).
- Contradiction details alert when content conflicts with authorized records.
- Real-time AI confidence score with automatic routing to human review when below 70%.

### 2. Intent Lock™ Fixes
Surgical corrections designed to preserve author intent, target audience, and call-to-action:
- **Light Tier**: Minimal surgical substitution (replaces prohibited buzzwords).
- **Balanced Tier**: Natural phrasing matching paragraph flow and brand tone.
- **Strong Tier**: Authoritative enterprise positioning with necessary qualifiers.
- **Interactive Review**: Side-by-side comparison, in-place custom editing, accept/reject feedback tracking, and non-destructive revision creation.

### 3. Claim Evidence Ledger
Central organization repository for all verified marketing and performance claims:
- Full CRUD operations with workspace isolation.
- Tracks claim text, evidence citations, scope of use, owner, review date, and expiration date.
- Dynamic flagging for expired claims or unverified citations lacking source records.
- Role-gated management (Brand Managers and Admins only).

### 4. Human Approval Workflow
Mandatory gatekeeping workflow preventing unapproved AI-generated content from publishing:
- State Machine lifecycle: `DRAFT` $\to$ `IN_REVIEW` $\to$ `CHANGES_REQUESTED` $\to$ `APPROVED` / `REJECTED`.
- Enforces strict backend transition validation via `StateTransitionValidator`.
- Reviewer comments thread and decision logs recording reviewer identity, timestamp, decision notes, and active revision number.
- Separation of duties enforcement.

### 5. Revision Lineage & Rollback
- Every accepted Intent Lock fix or manual edit creates an immutable `Revision` snapshot.
- Tracks parent-child revision ancestry (`sourceRevisionNumber`).
- Snapshot inspector displays the exact historical text and change summary.
- One-click rollback restores snapshots as new revisions without overwriting audit history.

---

## 🔐 Role-Based Access Control (RBAC)

| Role | Submit Content | Accept Corrections | Manage Guidelines & Sources | Manage Claims Ledger | Sign Decisions | Self-Approve Exemption | Manage Users |
|:---|:---:|:---:|:---:|:---:|:---:|:---:|:---:|
| `ADMIN` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ (Exempt) | ✅ |
| `BRAND_MANAGER` | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | ❌ |
| `REVIEWER` | ✅ | ✅ | ❌ (Read Only) | ❌ (Read Only) | ✅ | ❌ | ❌ |
| `CONTENT_CREATOR`| ✅ | ✅ | ❌ (Read Only) | ❌ (Read Only) | ❌ (Read Only) | ❌ | ❌ |

---

## 🚀 Local Setup & Quickstart

### Prerequisites
- **Node.js**: v20+ (tested on Node v20/v24 LTS) & `npm`
- **Java Development Kit**: JDK 21+ (tested on OpenJDK 21/25)
- **Apache Maven**: 3.9+ (or use wrapper `backend/mvnw.cmd` / `./mvnw`)
- **MongoDB**: MongoDB Atlas connection string or local MongoDB instance (`mongodb://localhost:27017/inkloom`).

---

### Step 1: Environment Configuration

Create a `.env` file in the repository root (or export shell variables):

```bash
# Database Configuration (MongoDB Atlas single source of truth)
MONGODB_URI=mongodb+srv://<username>:<password>@<cluster>.mongodb.net/inkloom?retryWrites=true&w=majority

# JWT Security
INKLOOM_JWT_SECRET=your_secure_256_bit_secret_key_for_inkloom_platform_hmac_sha256
INKLOOM_JWT_EXPIRATION_MS=86400000

# Google Gemini AI Integration (Backend Only)
GOOGLE_API_KEY=your_gemini_api_key_here
GEMINI_ENABLED=true
GEMINI_MODEL=gemini-2.5-flash
AI_TIMEOUT_SECONDS=30
AI_TEMPERATURE=0.1

# CORS Configuration
CORS_ALLOWED_ORIGINS=https://inkloom-ai-xi.vercel.app,http://localhost:5173,http://127.0.0.1:5173
```

> [!NOTE]
> When `GEMINI_ENABLED=false` or `GOOGLE_API_KEY` is not supplied, Inkloom AI runs entirely offline using deterministic rule checks and risk evaluation without errors.

---

### Step 2: Backend Setup & Execution

```bash
cd backend

# Compile and run test suite
./mvnw clean test

# Run the backend application
./mvnw spring-boot:run
```

The Spring Boot REST API starts on `http://localhost:8080`.  
Verify health:
```bash
curl http://localhost:8080/api/v1/health
```

---

### Step 3: Frontend Setup & Execution

In a separate terminal window:

```bash
cd frontend

# Install dependencies from lockfile
npm install

# Run ESLint validation
npm run lint

# Build production bundle
npm run build

# Start Vite development server
npm run dev
```

The frontend client starts on `http://localhost:5173`.

---

## 🧪 Testing Matrix

### Backend Tests
Execute the test suite covering the Round 3 features and core platform logic:
```bash
backend/mvnw test -Dtest=ComplianceRiskEvaluatorTest,EvidenceGroundingTest,ApprovalServiceTest,AnalyticsServiceTest,StateTransitionValidatorTest,JwtTokenSecurityTest
```
- **39 tests executed with 0 failures and 0 errors**:
  - `ComplianceRiskEvaluatorTest`: Deterministic risk gates (`CRITICAL`, `HIGH`, `MEDIUM`, `LOW`), risk score computation, and mandatory blocking conditions.
  - `EvidenceGroundingTest`: Contradiction detection, numeric claim discrepancy detection, expired evidence detection, and confidence routing.
  - `ApprovalServiceTest`: Mandatory compliance blocking gates, separation of duties enforcement (author cannot self-approve), admin exemption, and enriched audit event logging.
  - `AnalyticsServiceTest`: Workspace intelligence metrics calculation (turnaround time, high-risk pending, resolved vs unresolved findings, recurring violated rules, empty state safety).
  - `StateTransitionValidatorTest`: 20 exhaustive state transition lifecycle validations.
  - `JwtTokenSecurityTest`: Cryptographic token generation, expiration handling, tamper rejection.

### Frontend Quality Verification
```bash
npm --prefix frontend run lint
npm --prefix frontend run build
```
- **ESLint**: 0 errors, 0 warnings.
- **Vite Build**: 1,626 modules compiled into production bundle (`dist/`) with 0 errors.

---

## 📂 Project Directory Structure

```
inkloom-ai/
├── backend/
│   ├── src/main/java/com/inkloom/
│   │   ├── config/              # SecurityConfig, CorsConfig, JacksonConfig
│   │   ├── controller/          # REST Controllers (Auth, Brands, Claims, Reviews, Approvals, Analytics)
│   │   ├── dto/                 # Request & Response DTOs (ViolatedGuidelineDto, ReviewResponse, etc.)
│   │   ├── exception/           # Global exception handler & custom exceptions
│   │   ├── integration/ai/      # Gemini config, official Gen AI provider, orchestrator
│   │   ├── model/               # MongoDB Document Entities (User, Review, Claim, AuditEvent, etc.)
│   │   ├── repository/          # Spring Data MongoDB Repositories
│   │   ├── security/            # JWT Token Provider, UserDetailsService, WorkspaceSecurity
│   │   ├── service/             # Business Services (ReviewService, ApprovalService, AnalyticsService, ComplianceRiskEvaluator)
│   │   └── validator/           # StateTransitionValidator
│   ├── src/main/resources/      # application.properties
│   └── src/test/java/           # Comprehensive Unit & Integration Test Suites
├── frontend/
│   ├── src/
│   │   ├── components/          # Reusable UI (Button, Card, Modal, DataTable, Badges, Loaders)
│   │   ├── context/             # AuthContext, ToastContext
│   │   ├── features/            # EvidenceCard, IntentLockComparison, ProtectedRoute
│   │   ├── pages/               # 15 SaaS application pages (DashboardPage, ApprovalQueuePage, ReviewFindingsPage, etc.)
│   │   └── services/            # Centralized API service layer (apiClient, reviewService, approvalService, etc.)
│   ├── package.json
│   └── vite.config.js
├── docs/
│   ├── architecture.md          # Architectural blueprints and component boundaries
│   ├── api.md                   # REST API contract and endpoint specifications
│   ├── development-plan.md      # Milestones and progression log
│   └── demo-script.md           # 5-minute hackathon judge demonstration script
├── README.md
└── .gitignore
```

---

## 📄 License
Inkloom AI Platform — Proprietary & Confidential.
