# Inkloom AI — System Architecture

## 1. System Overview

Inkloom AI provides intelligent brand governance by evaluating submitted communications against approved brand guidelines, prohibited terminology, tone rules, and verified claims ledgers.

```
+-------------------------------------------------------------+
|                      React Single Page App                   |
|                (React + Vite + Tailwind + JS)                |
+------------------------------+------------------------------+
                               |
                               | REST API (JSON / JWT)
                               v
+-------------------------------------------------------------+
|                  Spring Boot REST Services                   |
|                                                             |
|  +--------------------+             +--------------------+  |
|  | Spring Security    |             | Centralized Advice |  |
|  | (JWT + RBAC + CORS)|             | & Error Handling   |  |
|  +---------+----------+             +--------------------+  |
|            |                                                |
|            v                                                |
|  +--------------------+             +--------------------+  |
|  | Workspace Security |             | Audit Event Logger |  |
|  | (Tenant Isolation) |             +--------------------+  |
|  +---------+----------+                                     |
|            |                                                |
|            v                                                |
|  +-------------------------------------------------------+  |
|  |             Review Orchestration Service              |  |
|  |                                                       |  |
|  |  +--------------------------+  +-------------------+  |  |
|  |  | Deterministic Rule Engine|  | Evidence Matcher  |  |  |
|  |  +--------------------------+  +-------------------+  |  |
|  |  +-------------------------------------------------+  |  |
|  |  |     AI Provider Adapter (Gemini / Fallback)     |  |  |
|  |  +-------------------------------------------------+  |  |
|  +---------------------------+---------------------------+  |
+------------------------------|------------------------------+
                               |
                               v
                     +-------------------+
                     |   MongoDB Atlas   |
                     |  / Local MongoDB  |
                     +-------------------+
```

---

## 2. Security Boundaries & Zero-Trust AI Integration

1. **Backend Exclusivity**: No AI APIs or credentials are accessible from client JavaScript.
2. **Untrusted Input Treatment**: User-submitted content is treated as untrusted text and cannot override backend prompt system constraints.
3. **Evidence Cross-Validation**: If an AI provider returns citation references or claims verification, the backend matches them against actual MongoDB records for that specific brand before presenting them. Unverified citations are downgraded to `UNSUPPORTED_CLAIM`.
4. **Tenant Isolation**: Users can only access brand guidelines, claims, and reviews for workspaces they explicitly belong to.

---

## 3. Data Tier

MongoDB is used for document persistence:
* `users`
* `brand_workspaces`
* `brand_guidelines`
* `approved_sources`
* `approved_claims`
* `reviews`
* `revisions`
* `review_comments`
* `review_decisions`
* `audit_events`
* `review_feedback`
