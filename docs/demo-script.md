# Inkloom AI — 5-Minute Hackathon Demonstration Script

**Title**: Inkloom AI — Intelligent Brand Governance Platform  
**Target Duration**: 5 minutes  
**Target Audience**: Hackathon Judges, Enterprise Architects, Brand Directors  
**Preset Credentials**:
- **Brand Manager / Admin**: `alex.brand@acme.internal` / `demo-pass`
- **Reviewer**: `sarah.jenkins@acme.internal` / `demo-pass`
- **Content Creator**: `marcus.content@acme.internal` / `demo-pass`

---

## ⏱️ Timeline & Stage Navigation

| Timestamp | Phase | Screen / Route | Core Message / Demo Action |
|:---|:---|:---|:---|
| **0:00 - 0:45** | **The Problem** | `/login` | Why brand governance matters; why standard spellcheckers fail enterprise teams. |
| **0:45 - 1:30** | **The Governance Foundation** | `/guidelines` & `/claims` | Show active brand guidelines, prohibited buzzwords, and the Claim Evidence Ledger. |
| **1:30 - 2:30** | **The Review Engine** | `/reviews/editor` | Paste sample draft, trigger hybrid evaluation, inspect Evidence Cards & confidence. |
| **2:30 - 3:30** | **Intent Lock™ Fixes** | `/intent-lock` | Compare Light, Balanced, Strong tiers side-by-side; apply fix; show non-destructive revision. |
| **3:30 - 4:15** | **Human-in-the-Loop Gate** | `/approvals` | Reviewer inspects submission, adds audit comment, and signs official approval. |
| **4:15 - 5:00** | **Clarity Intelligence** | `/heatmap` & `/revisions` | Real-time Clarity Heatmap; explain guideline ambiguity signals; architecture recap. |

---

## 🎙️ Step-by-Step Narration Script

### 1. The Problem & Introduction (0:00 - 0:45)
> **Speaker**:  
> "Judges, modern organizations produce thousands of marketing announcements, sales collateral, and press releases every week. But maintaining brand governance is broken.
> 
> Spellcheckers only catch typos. Generic AI tools hallucinate unverified performance claims or strip the author's voice.
> 
> Welcome to **Inkloom AI** — an intelligent enterprise brand governance platform built on Java 21 Spring Boot, React, MongoDB, and the official Google Gen AI Java SDK. Inkloom doesn't just catch errors — it explains why an issue matters, provides evidence citations, offers surgical intent-preserving fixes, and enforces human sign-off."

*Action*: Click "Sign In" using the pre-filled reviewer credentials on `/login`.

---

### 2. Workspace, Guidelines & Claim Evidence Ledger (0:45 - 1:30)
> **Speaker**:  
> "Let's open our Acme Cloud workspace. Under **Brand Guidelines**, we have active tone rules — confident, measured, consultative — and codified prohibited terms like *'bulletproof'*, which creates an unsubstantiated absolute guarantee.
> 
> Next, look at our **Claim Evidence Ledger**. Inkloom enforces that factual performance claims must have registered evidence. Notice here: our benchmark claim for latency has expired, and the platform automatically flags it with an advisory warning. AI is strictly prohibited from inventing evidence citations."

*Action*: Briefly show `/guidelines` and navigate to `/claims` to highlight the expired benchmark claim and missing evidence badges.

---

### 3. The Review Engine & Evidence Cards (1:30 - 2:30)
> **Speaker**:  
> "Now let's submit draft marketing copy as a content author in the **Content Review Editor**.
> 
> Let's click *'Load Sample Draft'*. Notice the draft asserts: *'Our bulletproof cloud platform delivers industry-leading latency metrics.'*
> 
> We hit *'Evaluate Brand Governance'*. The backend Spring Boot engine executes our 10-step pipeline: it runs deterministic regex checks, calls Google Gemini using the official Gen AI Java SDK, validates the JSON schema, and cross-references source citations against MongoDB.
> 
> Look at the **Evidence Cards**:
> 1. It flags *'bulletproof'* as a Critical violation backed by Brand Guideline Rule ID.
> 2. It flags the latency claim because our ledger shows that benchmark has expired.
> 3. Notice the AI confidence meter: 95% confidence. If confidence drops below 70%, Inkloom tags it with an amber Uncertainty badge so humans can double-check."

*Action*: Click "Load Sample Draft" $\to$ Click "Evaluate Brand Governance" $\to$ Show score drop to 76% and walk through the two Evidence Cards.

---

### 4. Intent Lock™ Multi-Tier Fixes (2:30 - 3:30)
> **Speaker**:  
> "Traditional generative rewrites erase the author's intent. Inkloom introduces **Intent Lock**:
> 
> When we click *'Compare Intent Lock Tiers'*, we see the original text preserved untouched on the left. On the right, we have three calibrated strengths:
> - **Light**: Surgical replacement changing *'bulletproof'* to *'enterprise-resilient'*.
> - **Balanced**: Natural conversational phrasing.
> - **Strong**: Comprehensive corporate positioning.
> 
> We can even edit the proposed text in-place before accepting. Let's accept the Balanced tier.
> 
> Notice what happened: our compliance score immediately climbed to 88%, the original text was preserved in an immutable Revision snapshot, and our live draft was updated surgically."

*Action*: Switch between Light, Balanced, and Strong tiers $\to$ Click "Accept Balanced Revision" $\to$ Show live text update in Content Review Page.

---

### 5. Human Approval Workflow (3:30 - 4:15)
> **Speaker**:  
> "Inkloom enforces that AI alone cannot publish content. Content must enter our **Human Approval Queue**.
> 
> As a qualified reviewer, I open the submission. I can view the complete findings, open the reviewer discussion thread, and leave a comment: *'Verified latency replacement against SLA specification.'*
> 
> When satisfied, I click *'Approve'*. The backend state machine validates the transition from `IN_REVIEW` to `APPROVED`, records my reviewer user ID, timestamp, decision notes, and the exact revision number I certified. Content creators are strictly prevented from approving their own drafts."

*Action*: Navigate to `/approvals` $\to$ Open discussion thread $\to$ Click "Approve" and confirm modal $\to$ Show status transition to `APPROVED`.

---

### 6. Zero-Fabrication Clarity Heatmap & Wrap-Up (4:15 - 5:00)
> **Speaker**:  
> "Finally, let's look at the **Clarity Heatmap**.
> 
> Many hackathon projects show hardcoded static charts. Inkloom's Clarity Heatmap aggregates real persisted reviews and reviewer feedback from MongoDB. It calculates author rejection rates and identifies friction points.
> 
> If authors frequently reject a suggestion, the engine flags a **Guideline Ambiguity Signal**, alerting brand managers to clarify the underlying rule. If claims lack evidence, it flags a **Source Gap Signal**.
> 
> To summarize:
> - Full-stack Java 21 Spring Boot + React + MongoDB.
> - Official Google Gen AI Java SDK integration.
> - Zero hallucination, non-destructive Intent Lock revisions, and auditable human governance.
> - 72 automated tests passing with zero failures.
> 
> Thank you, judges! We're ready for your questions."

*Action*: Open `/heatmap` $\to$ Show matrix rows with conflict signals $\to$ Open `/revisions` to show the complete immutable audit trail.
