# Inkloom AI — REST API Specification

**Base URL**: `http://localhost:8080/api/v1`  
**Protocol**: HTTP/1.1 / JSON  
**Authentication**: Bearer JWT (`Authorization: Bearer <token>`)

---

## 1. Authentication & Security

All protected endpoints require an `Authorization: Bearer <token>` header obtained from `/auth/login` or `/auth/register`.

### Standard API Response Envelope
All successful responses return HTTP 200/201 wrapped in an `ApiResponse<T>` envelope:
```json
{
  "success": true,
  "message": "Operation completed successfully",
  "data": { ... },
  "timestamp": 1729768000000
}
```

### Standard Error Response Format
```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed for request object",
  "path": "/api/v1/reviews",
  "timestamp": "2026-10-24T10:30:00Z",
  "validationErrors": {
    "content": "Content cannot be blank"
  }
}
```

---

## 2. Authentication Endpoints

### 2.1 User Registration
- **Method**: `POST`
- **Path**: `/auth/register`
- **Access**: Public
- **Request Body**:
```json
{
  "fullName": "Sarah Jenkins",
  "email": "sarah.jenkins@acme.internal",
  "password": "demo-pass",
  "role": "REVIEWER",
  "workspaceName": "Acme Enterprise"
}
```
- **Response**: HTTP 201 Created with JWT token and user profile.

### 2.2 User Login
- **Method**: `POST`
- **Path**: `/auth/login`
- **Access**: Public
- **Request Body**:
```json
{
  "email": "sarah.jenkins@acme.internal",
  "password": "demo-pass"
}
```
- **Response**: HTTP 200 OK with `token` and `user` payload.

### 2.3 Current Authenticated User Profile
- **Method**: `GET`
- **Path**: `/auth/me`
- **Access**: Authenticated

---

## 3. Brand Workspaces

### 3.1 List Authorized Workspaces
- **Method**: `GET`
- **Path**: `/brands`
- **Access**: Authenticated

### 3.2 Create Brand Workspace
- **Method**: `POST`
- **Path**: `/brands`
- **Access**: `ROLE_ADMIN`
- **Request Body**:
```json
{
  "name": "Acme Cloud Platform",
  "slug": "acme-cloud",
  "industry": "Enterprise Software",
  "description": "Multi-region enterprise cloud infrastructure"
}
```

### 3.3 Get Workspace Details
- **Method**: `GET`
- **Path**: `/brands/{brandId}`
- **Access**: Workspace Member

---

## 4. Brand Guidelines & Sources

### 4.1 Get Active Guideline
- **Method**: `GET`
- **Path**: `/brands/{brandId}/guidelines`
- **Access**: Workspace Member

### 4.2 Create or Update Guideline
- **Method**: `POST`
- **Path**: `/brands/{brandId}/guidelines`
- **Access**: `ROLE_BRAND_MANAGER`, `ROLE_ADMIN`
- **Request Body**:
```json
{
  "version": "v1.1",
  "toneVoice": "Confident, measured, precise, consultative.",
  "toneRules": [
    { "attribute": "Tone", "guidance": "Avoid aggressive hype or superlative jargon." }
  ],
  "prohibitedExpressions": [
    {
      "term": "bulletproof",
      "severity": "CRITICAL",
      "reason": "Unsubstantiated absolute security guarantee.",
      "suggestedAlternative": "enterprise-resilient"
    }
  ],
  "requiredPhrases": [
    {
      "context": "Legal Disclaimer",
      "phrase": "Results may vary based on deployment configuration.",
      "mandatory": true
    }
  ]
}
```

### 4.3 List Approved Sources
- **Method**: `GET`
- **Path**: `/brands/{brandId}/sources`
- **Access**: Workspace Member

### 4.4 Register Approved Source
- **Method**: `POST`
- **Path**: `/brands/{brandId}/sources`
- **Access**: `ROLE_BRAND_MANAGER`, `ROLE_ADMIN`

---

## 5. Approved Claims Ledger

### 5.1 List Approved Claims
- **Method**: `GET`
- **Path**: `/brands/{brandId}/claims`
- **Access**: Workspace Member
- **Returns**: Array of claims with dynamic `isExpired` and `isEvidenceMissing` flags.

### 5.2 Register Approved Claim
- **Method**: `POST`
- **Path**: `/brands/{brandId}/claims`
- **Access**: `ROLE_BRAND_MANAGER`, `ROLE_ADMIN`
- **Request Body**:
```json
{
  "claimText": "Acme Cloud delivers 99.995% guaranteed uptime across multi-region clusters.",
  "supportingSource": "Acme SLA Specification v4.1",
  "supportingSourceId": "src-101",
  "scope": "Enterprise Tier customers in North America and Europe.",
  "owner": "Infrastructure Reliability Team",
  "expiryDate": "2027-10-24T00:00:00Z"
}
```

### 5.3 Update Claim
- **Method**: `PUT`
- **Path**: `/brands/{brandId}/claims/{claimId}`
- **Access**: `ROLE_BRAND_MANAGER`, `ROLE_ADMIN`

### 5.4 Update Claim Status
- **Method**: `PATCH`
- **Path**: `/brands/{brandId}/claims/{claimId}/status?status=EXPIRED`
- **Access**: `ROLE_BRAND_MANAGER`, `ROLE_ADMIN`

### 5.5 Delete Claim
- **Method**: `DELETE`
- **Path**: `/brands/{brandId}/claims/{claimId}`
- **Access**: `ROLE_BRAND_MANAGER`, `ROLE_ADMIN`

---

## 6. Brand Governance Review Engine

### 6.1 Submit Content for Review
- **Method**: `POST`
- **Path**: `/reviews`
- **Access**: Workspace Member
- **Request Body**:
```json
{
  "brandId": "brand-acme-1",
  "title": "Q3 Infrastructure Announcement",
  "contentType": "PRESS_RELEASE",
  "content": "Acme announces our bulletproof cloud platform delivering industry leading latency across all regions."
}
```
- **Response**: HTTP 201 Created with `ReviewResponse` containing compliance score, findings array, and initial revision #1.

### 6.2 Get Review Details
- **Method**: `GET`
- **Path**: `/reviews/{reviewId}`
- **Access**: Workspace Member

### 6.3 List Findings for Review
- **Method**: `GET`
- **Path**: `/reviews/{reviewId}/findings`
- **Access**: Workspace Member

### 6.4 Accept Intent Lock Correction
- **Method**: `POST`
- **Path**: `/reviews/{reviewId}/accept-correction`
- **Access**: Workspace Member
- **Request Body**:
```json
{
  "findingId": "find-01",
  "tier": "BALANCED",
  "replacementText": "enterprise-resilient cloud platform",
  "rationale": "Aligns with brand guideline prohibiting unsubstantiated absolute guarantees."
}
```
- **Response**: HTTP 200 OK with updated content, recalculated score, and incremented `activeRevisionNumber`.

### 6.5 Reject Correction
- **Method**: `POST`
- **Path**: `/reviews/{reviewId}/reject-correction?findingId=find-01&reason=Author+wishes+to+preserve+original+wording`
- **Access**: Workspace Member

### 6.6 Save New Revision
- **Method**: `POST`
- **Path**: `/reviews/{reviewId}/revisions`
- **Access**: Workspace Member
- **Request Body**:
```json
{
  "content": "Full revised document content...",
  "changeSummary": "Manual adjustment of executive summary."
}
```

### 6.7 Get Revision History
- **Method**: `GET`
- **Path**: `/reviews/{reviewId}/history` (or `/reviews/{reviewId}/revisions`)
- **Access**: Workspace Member

---

## 7. Approval & Decision Workflow

### 7.1 Post Reviewer Comment
- **Method**: `POST`
- **Path**: `/reviews/{reviewId}/comments`
- **Access**: `ROLE_REVIEWER`, `ROLE_BRAND_MANAGER`, `ROLE_ADMIN`
- **Request Body**:
```json
{
  "commentText": "Please verify paragraph 2 benchmark citation before publication."
}
```

### 7.2 Get Comments Thread
- **Method**: `GET`
- **Path**: `/reviews/{reviewId}/comments`
- **Access**: Workspace Member

### 7.3 Record Official Decision
- **Method**: `POST`
- **Path**: `/reviews/{reviewId}/decisions`
- **Access**: `ROLE_REVIEWER`, `ROLE_BRAND_MANAGER`, `ROLE_ADMIN`
- **Request Body**:
```json
{
  "action": "APPROVE",
  "decisionNotes": "All brand guidelines verified. Authorized for public release."
}
```
- **Valid Actions**: `APPROVE`, `REQUEST_CHANGES`, `REJECT`
- **Response**: HTTP 200 OK with `ReviewDecisionResponse` recording decision, reviewer name, timestamp, and active revision number.

### 7.4 Get Decisions History
- **Method**: `GET`
- **Path**: `/reviews/{reviewId}/decisions`
- **Access**: Workspace Member

---

## 8. Clarity Heatmap & Intelligence

### 8.1 Get Workspace Clarity Analytics
- **Method**: `GET`
- **Path**: `/brands/{brandId}/analytics`
- **Access**: Workspace Member
- **Returns**: Real calculated stats (total findings analyzed, average compliance rate, most violated category, ambiguity index, category heatmap matrix, and calculation explanations). Returns `hasSufficientData: false` when 0 reviews exist.

---

## 9. Health & Diagnostics

### 9.1 Health Check
- **Method**: `GET`
- **Path**: `/health`
- **Access**: Public
- **Returns**: Status, timestamp, active profiles, and AI feature enablement flag.
