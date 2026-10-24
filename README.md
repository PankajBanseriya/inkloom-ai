# Inkloom AI — Intelligent Brand Governance Platform

> An enterprise SaaS brand governance platform ensuring all marketing content, announcements, and corporate communications comply with approved brand guidelines, verified claims, tone of voice, terminology, and legal disclosures.

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
                                      |  REST JSON API (Bearer JWT)
                                      v
+----------------------------------------------------------------------------------------------------+
|  BACKEND (Java 21 / Spring Boot 3.3.5)                                                             |
|  - Spring MVC Controllers • Spring Security (Stateless JWT / RBAC) • Jakarta Bean Validation       |
|  - Business Services • Audit Event Emitters • State Machine Transition Validators                  |
+----------------------------------------------------------------------------------------------------+
               |                                                   |
               v                                                   v
+-----------------------------+               +------------------------------------------------------+
|  DATABASE (MongoDB 6/7)     |               |  AI ENGINE (Official Google Gen AI Java SDK 0.1.1)   |
|  - Users & Workspaces       |               |  - Model: gemini-2.5-flash (Developer API)           |
|  - Guidelines & Sources     |               |  - Structured JSON Schema • Low Temperature (0.1)    |
|  - Approved Claims Ledger   |               |  - Server-Side Zero-Hallucination Source Verification |
|  - Reviews, Revisions & Log |               |  - Graceful Deterministic Fallback when Offline/No-Key|
+-----------------------------+               +------------------------------------------------------+
```

### Stack Components:
- **Frontend**: React 18, Vite 5, Tailwind CSS, Lucide React, React Router 6. (Zero TypeScript; pure maintainable ES6+/JSX).
- **Backend**: Java 21, Spring Boot 3.3.5, Spring Security 6, Spring Data MongoDB, Jakarta Validation, Jackson.
- **AI Integration**: Official Google Gen AI Java SDK (`com.google.genai:google-genai:0.1.1`).
- **Database**: MongoDB (Local or Atlas) storing 12 distinct domain collections with indexes and referential integrity.

---

## ✨ Core Differentiating Features

### 1. Evidence Cards
Every flagged issue renders an interactive Evidence Card showing:
- Category, rule title, and severity (`CRITICAL`, `WARNING`, `INFO`).
- Original text excerpt and governance explanation.
- Supporting guideline or verified source citation (title, section, and reference ID).
- Evidence classification (`VERIFIED_RULE`, `UNSUPPORTED_CLAIM`, `CLAIM_CONFLICT`, `SUBJECTIVE_SUGGESTION`, `UNCERTAIN_FINDING`).
- Real-time AI confidence score with an uncertainty warning badge when confidence drops below 70%.
- Server strictly validates source IDs against database records, stripping any unverified citations.

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
- State Machine lifecycle: `DRAFT` $\to$ `SUBMITTED` $\to$ `IN_REVIEW` $\to$ `CHANGES_REQUESTED` $\to$ `APPROVED` / `REJECTED`.
- Enforces strict backend transition validation via `StateTransitionValidator`.
- Reviewer comments thread and decision logs recording reviewer identity, timestamp, decision notes, and active revision number.
- Role-Based Access: Content creators are prohibited from signing off their own submissions.

### 5. Revision Lineage & Rollback
- Every accepted Intent Lock fix or manual edit creates an immutable `Revision` snapshot.
- Tracks parent-child revision ancestry (`sourceRevisionNumber`).
- Snapshot inspector displays the exact historical text and change summary.
- One-click rollback restores snapshots as new revisions without overwriting audit history.

### 6. Zero-Fabrication Clarity Heatmap
- Correlates real persisted review findings, author rejection rates, and reviewer overrides.
- Pinpoints recurring prohibited phrases, unverified claim spikes, and potential guideline ambiguities.
- Displays full metric calculation explanations in the UI.
- Honest empty state displayed when insufficient data exists.

---

## 🔐 Role-Based Access Control (RBAC)

| Role | Submit Content | Accept Corrections | Manage Guidelines & Sources | Manage Claims Ledger | Sign Decisions | Manage Users |
|:---|:---:|:---:|:---:|:---:|:---:|:---:|
| `ADMIN` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| `BRAND_MANAGER` | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ |
| `REVIEWER` | ✅ | ✅ | ❌ (Read Only) | ❌ (Read Only) | ✅ | ❌ |
| `CONTENT_CREATOR`| ✅ | ✅ | ❌ (Read Only) | ❌ (Read Only) | ❌ (Read Only) | ❌ |

---

## 🚀 Local Setup & Quickstart

### Prerequisites
- **Node.js**: v20+ (tested on Node v20/v24 LTS) & `npm`
- **Java Development Kit**: JDK 21+ (tested on OpenJDK 21/25)
- **Apache Maven**: 3.9+ (or use wrapper)
- **MongoDB**: Local MongoDB instance (`mongodb://localhost:27017`) or a free MongoDB Atlas connection string.

---

### Step 1: Environment Configuration

Create a `.env` file in the repository root (or export shell variables):

```bash
# Database Configuration
MONGODB_URI=mongodb://localhost:27017/inkloom

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
CORS_ALLOWED_ORIGINS=http://localhost:5173,http://127.0.0.1:5173
```

> [!NOTE]
> When `GEMINI_ENABLED=false` or `GOOGLE_API_KEY` is not supplied, Inkloom AI runs entirely offline using deterministic rule checks without errors.

---

### Step 2: Backend Setup & Execution

```bash
cd backend

# Compile and run test suite (72 tests)
mvn clean test

# Package into runnable Spring Boot JAR
mvn clean package -DskipTests=true

# Start backend application
mvn spring-boot:run
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
Execute the complete test suite:
```bash
mvn -f backend/pom.xml test
```
- **72 tests executed** across controllers, services, repositories, validators, and AI providers with **0 failures and 0 errors**.
- Real Gemini integration test (`GeminiLiveIntegrationTest`) is conditionally gated on `GOOGLE_API_KEY` in environment.

### Frontend Quality Verification
```bash
npm --prefix frontend run lint
npm --prefix frontend run build
```
- **ESLint**: 0 errors, 0 warnings.
- **Vite Build**: 1,626 modules compiled into production chunks with 0 errors.

---

## 📂 Project Directory Structure

```
inkloom-ai/
├── backend/
│   ├── src/main/java/com/inkloom/
│   │   ├── config/              # SecurityConfig, CorsConfig
│   │   ├── controller/          # REST Controllers (Auth, Brands, Claims, Reviews, Approvals, Analytics)
│   │   ├── dto/                 # Request & Response DTOs
│   │   ├── exception/           # Global exception handler & custom exceptions
│   │   ├── integration/ai/      # Gemini config, official Gen AI provider, orchestrator
│   │   ├── model/               # MongoDB Document Entities (User, Review, Claim, Revision, etc.)
│   │   ├── repository/          # Spring Data MongoDB Repositories
│   │   ├── security/            # JWT Token Provider, UserDetailsService, WorkspaceSecurity
│   │   ├── service/             # Business Logic Layer
│   │   └── validator/           # StateTransitionValidator
│   ├── src/main/resources/      # application.properties
│   └── src/test/java/           # Comprehensive Unit & Integration Test Suites
├── frontend/
│   ├── src/
│   │   ├── components/          # Reusable UI (Button, Card, Modal, DataTable, Badges, Loaders)
│   │   ├── context/             # AuthContext, ToastContext
│   │   ├── features/            # EvidenceCard, IntentLockComparison, ProtectedRoute
│   │   ├── pages/               # 15 SaaS application pages
│   │   └── services/            # Centralized API service layer (apiClient, reviewService, etc.)
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
