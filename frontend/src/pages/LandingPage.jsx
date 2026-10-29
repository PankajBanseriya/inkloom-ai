import { useState } from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { ROUTES } from '../constants/routes';
import {
  ShieldCheck,
  ArrowRight,
  CheckCircle2,
  AlertTriangle,
  Lock,
  GitCompare,
  Database,
  BarChart3,
  FileText,
  Menu,
  X,
  FileCheck,
  Shield,
  Activity,
  History,
} from 'lucide-react';

export default function LandingPage() {
  const { isAuthenticated } = useAuth();
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);
  const [activeIntentTier, setActiveIntentTier] = useState('balanced');

  const intentSuggestions = {
    light: {
      label: 'Light Tier',
      tagline: 'Minimal surgical substitution',
      replacement: 'delivers highly reliable uptime backed by standard enterprise infrastructure.',
      changeDesc: 'Replaces absolute buzzword "guarantees 100% unbreakable" with accurate operational phrasing.',
    },
    balanced: {
      label: 'Balanced Tier (Recommended)',
      tagline: 'Natural enterprise flow & verified claims',
      replacement: 'delivers 99.9% verified high availability across all production cloud regions.',
      changeDesc: 'Aligns uptime metric with verified SLA claim #CLM-2026-08 and adheres to Brand Standards §4.2.',
    },
    strong: {
      label: 'Strong Tier',
      tagline: 'Authoritative positioning with qualifiers',
      replacement: 'architected for resilient, continuous operations backed by verified SLA standards.',
      changeDesc: 'Emphasizes architectural durability with formal legal disclaimers for regulated enterprise audiences.',
    },
  };

  return (
    <div className="min-h-screen bg-[#0B0F19] text-white selection:bg-brand-500 selection:text-white font-sans antialiased overflow-x-hidden">
      {/* Background Decorative Gradients & Glow Orbs */}
      <div className="fixed inset-0 pointer-events-none overflow-hidden z-0">
        <div className="absolute top-[-10%] left-[20%] w-[600px] h-[600px] bg-violet-600/15 rounded-full blur-[140px]" />
        <div className="absolute top-[30%] right-[-10%] w-[500px] h-[500px] bg-cyan-500/10 rounded-full blur-[160px]" />
        <div className="absolute bottom-[10%] left-[-10%] w-[700px] h-[700px] bg-indigo-600/10 rounded-full blur-[180px]" />
        <div className="absolute inset-0 bg-[linear-gradient(to_right,#ffffff05_1px,transparent_1px),linear-gradient(to_bottom,#ffffff05_1px,transparent_1px)] bg-[size:4rem_4rem] [mask-image:radial-gradient(ellipse_60%_50%_at_50%_0%,#000_70%,transparent_100%)]" />
      </div>

      {/* Navigation Bar */}
      <header className="sticky top-0 z-50 backdrop-blur-xl bg-[#0B0F19]/80 border-b border-white/10 transition-all">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex items-center justify-between h-16 sm:h-20">
            {/* Brand Logo */}
            <Link to={ROUTES.HOME} className="flex items-center space-x-3 group">
              <div className="w-10 h-10 rounded-xl bg-gradient-to-tr from-brand-600 via-brand-500 to-cyan-400 p-[1px] shadow-lg shadow-brand-500/20 group-hover:shadow-brand-500/40 transition-shadow">
                <div className="w-full h-full bg-[#0B0F19] rounded-[11px] flex items-center justify-center">
                  <ShieldCheck className="w-5 h-5 text-brand-400 group-hover:scale-110 transition-transform" />
                </div>
              </div>
              <div className="flex items-center">
                <span className="text-xl font-bold tracking-tight text-white">Inkloom</span>
                <span className="text-xl font-bold tracking-tight text-transparent bg-clip-text bg-gradient-to-r from-brand-400 to-cyan-400 ml-1">
                  AI
                </span>
                <span className="hidden sm:inline-block ml-3 px-2 py-0.5 text-[10px] font-semibold uppercase tracking-wider text-brand-300 bg-brand-500/10 border border-brand-500/20 rounded-full">
                  Brand Governance
                </span>
              </div>
            </Link>

            {/* Desktop Navigation Links */}
            <nav className="hidden md:flex items-center space-x-8 text-sm font-medium text-ink-300">
              <a href="#preview" className="hover:text-white transition-colors">
                Product Preview
              </a>
              <a href="#features" className="hover:text-white transition-colors">
                Features
              </a>
              <a href="#how-it-works" className="hover:text-white transition-colors">
                How It Works
              </a>
              <a href="#grounding" className="hover:text-white transition-colors">
                Evidence Grounding
              </a>
            </nav>

            {/* Action Buttons */}
            <div className="hidden md:flex items-center space-x-4">
              {isAuthenticated ? (
                <Link
                  to={ROUTES.DASHBOARD}
                  className="inline-flex items-center px-4 py-2 text-xs font-semibold rounded-lg bg-gradient-to-r from-brand-600 to-indigo-600 hover:from-brand-500 hover:to-indigo-500 text-white shadow-md shadow-brand-600/30 transition-all hover:scale-[1.02]"
                >
                  Go to Dashboard <ArrowRight className="w-3.5 h-3.5 ml-1.5" />
                </Link>
              ) : (
                <>
                  <Link
                    to={ROUTES.LOGIN}
                    className="text-xs font-semibold text-ink-300 hover:text-white px-3 py-2 rounded-lg transition-colors"
                  >
                    Sign In
                  </Link>
                  <Link
                    to={ROUTES.REGISTER}
                    className="inline-flex items-center px-4 py-2 text-xs font-semibold rounded-lg bg-gradient-to-r from-brand-600 via-brand-500 to-cyan-500 hover:from-brand-500 hover:to-cyan-400 text-white shadow-lg shadow-brand-500/25 transition-all hover:scale-[1.02]"
                  >
                    Get Started Free <ArrowRight className="w-3.5 h-3.5 ml-1.5" />
                  </Link>
                </>
              )}
            </div>

            {/* Mobile Menu Button */}
            <button
              type="button"
              onClick={() => setMobileMenuOpen(!mobileMenuOpen)}
              className="md:hidden p-2 rounded-lg text-ink-400 hover:text-white hover:bg-white/5 focus:outline-none"
              aria-label="Toggle Navigation Menu"
            >
              {mobileMenuOpen ? <X className="w-6 h-6" /> : <Menu className="w-6 h-6" />}
            </button>
          </div>
        </div>

        {/* Mobile Dropdown Menu */}
        {mobileMenuOpen && (
          <div className="md:hidden px-4 pt-2 pb-6 bg-[#0E1526] border-b border-white/10 space-y-3">
            <a
              href="#preview"
              onClick={() => setMobileMenuOpen(false)}
              className="block px-3 py-2 rounded-lg text-sm font-medium text-ink-300 hover:text-white hover:bg-white/5"
            >
              Product Preview
            </a>
            <a
              href="#features"
              onClick={() => setMobileMenuOpen(false)}
              className="block px-3 py-2 rounded-lg text-sm font-medium text-ink-300 hover:text-white hover:bg-white/5"
            >
              Features
            </a>
            <a
              href="#how-it-works"
              onClick={() => setMobileMenuOpen(false)}
              className="block px-3 py-2 rounded-lg text-sm font-medium text-ink-300 hover:text-white hover:bg-white/5"
            >
              How It Works
            </a>
            <a
              href="#grounding"
              onClick={() => setMobileMenuOpen(false)}
              className="block px-3 py-2 rounded-lg text-sm font-medium text-ink-300 hover:text-white hover:bg-white/5"
            >
              Evidence Grounding
            </a>
            <div className="pt-3 border-t border-white/10 flex flex-col space-y-2">
              {isAuthenticated ? (
                <Link
                  to={ROUTES.DASHBOARD}
                  className="w-full text-center px-4 py-2.5 text-xs font-semibold rounded-lg bg-brand-600 text-white"
                >
                  Open Workspace
                </Link>
              ) : (
                <>
                  <Link
                    to={ROUTES.LOGIN}
                    className="w-full text-center px-4 py-2 text-xs font-semibold rounded-lg border border-white/10 text-white"
                  >
                    Sign In
                  </Link>
                  <Link
                    to={ROUTES.REGISTER}
                    className="w-full text-center px-4 py-2.5 text-xs font-semibold rounded-lg bg-gradient-to-r from-brand-600 to-cyan-500 text-white"
                  >
                    Get Started Free
                  </Link>
                </>
              )}
            </div>
          </div>
        )}
      </header>

      {/* Hero Section */}
      <section className="relative z-10 pt-16 sm:pt-24 pb-20 px-4 sm:px-6 lg:px-8 max-w-7xl mx-auto text-center">
        {/* Status Pill Badge */}
        <div className="inline-flex items-center px-3.5 py-1.5 rounded-full bg-white/[0.04] border border-white/10 backdrop-blur-md mb-8 hover:border-brand-500/40 transition-colors">
          <span className="relative flex h-2 w-2 mr-2.5">
            <span className="animate-ping absolute inline-flex h-full w-full rounded-full bg-cyan-400 opacity-75" />
            <span className="relative inline-flex rounded-full h-2 w-2 bg-cyan-500" />
          </span>
          <span className="text-xs font-medium text-ink-200">
            Next-Generation Brand Governance & Compliance Platform
          </span>
          <span className="ml-2 text-brand-400 text-xs">→</span>
        </div>

        {/* Hero Headline */}
        <h1 className="text-4xl sm:text-6xl lg:text-7xl font-extrabold tracking-tight text-white max-w-5xl mx-auto leading-[1.12]">
          Every piece of content.{' '}
          <span className="text-transparent bg-clip-text bg-gradient-to-r from-brand-400 via-indigo-300 to-cyan-300">
            On brand. Every time.
          </span>
        </h1>

        {/* Supporting Copy */}
        <p className="mt-6 text-base sm:text-lg lg:text-xl text-ink-300 max-w-3xl mx-auto leading-relaxed">
          Inkloom AI empowers enterprise teams to review content against approved brand guidelines, verify claims
          against ground-truth evidence, identify compliance risks, and achieve human-approved excellence before publication.
        </p>

        {/* Hero CTAs */}
        <div className="mt-10 flex flex-col sm:flex-row items-center justify-center gap-4">
          <Link
            to={isAuthenticated ? ROUTES.DASHBOARD : ROUTES.REGISTER}
            className="w-full sm:w-auto px-8 py-3.5 text-sm font-semibold rounded-xl bg-gradient-to-r from-brand-600 via-brand-500 to-indigo-600 hover:from-brand-500 hover:to-indigo-500 text-white shadow-xl shadow-brand-500/25 hover:shadow-brand-500/40 transition-all hover:scale-[1.02] flex items-center justify-center group"
          >
            <span>{isAuthenticated ? 'Go to Dashboard' : 'Start Reviewing Free'}</span>
            <ArrowRight className="w-4 h-4 ml-2 group-hover:translate-x-1 transition-transform" />
          </Link>
          <a
            href="#preview"
            className="w-full sm:w-auto px-7 py-3.5 text-sm font-semibold rounded-xl bg-white/[0.04] hover:bg-white/[0.08] text-ink-200 hover:text-white border border-white/10 hover:border-white/20 transition-all flex items-center justify-center"
          >
            Explore the Platform
          </a>
        </div>

        {/* Trust Points */}
        <div className="mt-12 flex flex-wrap items-center justify-center gap-x-8 gap-y-3 text-xs text-ink-400">
          <div className="flex items-center space-x-2">
            <CheckCircle2 className="w-4 h-4 text-emerald-400" />
            <span>MongoDB Atlas Single Source of Truth</span>
          </div>
          <div className="flex items-center space-x-2">
            <CheckCircle2 className="w-4 h-4 text-brand-400" />
            <span>Zero-Hallucination Evidence Citations</span>
          </div>
          <div className="flex items-center space-x-2">
            <CheckCircle2 className="w-4 h-4 text-cyan-400" />
            <span>Separation of Duties Sign-offs</span>
          </div>
        </div>
      </section>

      {/* Illustrative Product Preview Section */}
      <section id="preview" className="relative z-10 py-12 px-4 sm:px-6 lg:px-8 max-w-7xl mx-auto">
        <div className="text-center mb-6">
          <span className="text-xs font-semibold uppercase tracking-wider text-brand-400">
            Interactive Product Preview
          </span>
          <h2 className="text-2xl sm:text-3xl font-bold text-white mt-1">
            Real-Time Brand Compliance Review in Action
          </h2>
          <p className="text-xs sm:text-sm text-ink-400 mt-2 max-w-2xl mx-auto">
            Illustrative preview demonstrating evidence grounding, uncertainty classification, and Intent Lock™ fixes.
          </p>
        </div>

        {/* Mock Application Window */}
        <div className="relative rounded-2xl border border-white/10 bg-[#0F172A]/90 backdrop-blur-xl shadow-2xl overflow-hidden">
          {/* Window Header Bar */}
          <div className="flex items-center justify-between px-4 py-3 bg-[#0B0F19]/90 border-b border-white/10 text-xs">
            <div className="flex items-center space-x-2">
              <div className="w-3 h-3 rounded-full bg-red-500/80" />
              <div className="w-3 h-3 rounded-full bg-amber-500/80" />
              <div className="w-3 h-3 rounded-full bg-emerald-500/80" />
              <span className="text-ink-500 ml-2 font-mono text-[11px] hidden sm:inline">
                inkloom://review/rev-8492/live-audit
              </span>
            </div>

            <div className="flex items-center space-x-3">
              <span className="px-2.5 py-0.5 rounded-full text-[10px] font-semibold bg-red-500/10 text-red-400 border border-red-500/20 flex items-center">
                <AlertTriangle className="w-3 h-3 mr-1" /> Risk Level: CRITICAL (92/100)
              </span>
              <span className="px-2.5 py-0.5 rounded-full text-[10px] font-semibold bg-brand-500/10 text-brand-300 border border-brand-500/20">
                Score: 78 / 100
              </span>
              <span className="px-2.5 py-0.5 rounded-full text-[10px] font-semibold bg-amber-500/10 text-amber-300 border border-amber-500/20 hidden md:inline-flex items-center">
                <Lock className="w-3 h-3 mr-1" /> Approval Gate Active
              </span>
            </div>
          </div>

          {/* Window Body: 2 Columns */}
          <div className="grid grid-cols-1 lg:grid-cols-12 min-h-[480px]">
            {/* Left Column: Submitted Content Draft with Highlights */}
            <div className="lg:col-span-6 p-6 border-b lg:border-b-0 lg:border-r border-white/10 bg-[#0B0F19]/50 flex flex-col justify-between">
              <div>
                <div className="flex items-center justify-between mb-4">
                  <div className="flex items-center space-x-2">
                    <FileText className="w-4 h-4 text-brand-400" />
                    <span className="text-xs font-semibold text-white">Q4_Enterprise_Launch_Release.md</span>
                  </div>
                  <span className="text-[10px] text-ink-400 bg-white/5 px-2 py-0.5 rounded border border-white/5">
                    Workspace: Acme Cloud Solutions
                  </span>
                </div>

                <div className="space-y-4 text-xs sm:text-sm text-ink-300 leading-relaxed font-sans">
                  <p>
                    Today, Acme Cloud Solutions announces the general availability of our next-generation distributed database.
                  </p>
                  <p className="p-3 rounded-lg bg-red-500/10 border border-red-500/30 text-ink-100">
                    Our platform{' '}
                    <span className="bg-red-500/30 text-red-200 px-1.5 py-0.5 rounded font-semibold underline decoration-red-400 decoration-wavy">
                      guarantees 100% unbreakable uptime
                    </span>{' '}
                    with zero failover latency across all global edge locations.
                  </p>
                  <p>
                    Unlike legacy systems, this deployment architecture eliminates maintenance downtime while providing
                    instant enterprise elasticity for mission-critical banking and healthcare environments.
                  </p>
                </div>
              </div>

              {/* Blocking Banner at bottom of document */}
              <div className="mt-6 p-3 rounded-xl bg-red-950/40 border border-red-500/30 text-xs flex items-start space-x-2.5">
                <AlertTriangle className="w-4 h-4 text-red-400 flex-shrink-0 mt-0.5" />
                <div className="text-[11px] text-red-200">
                  <span className="font-semibold text-red-300">Mandatory Gate:</span> Approval blocked until absolute
                  prohibited terms are resolved and verified against registered claims.
                </div>
              </div>
            </div>

            {/* Right Column: Evidence Card & Intent Lock Inspector */}
            <div className="lg:col-span-6 p-6 space-y-5 bg-[#0F172A]/80">
              {/* Evidence Card */}
              <div className="p-4 rounded-xl bg-white/[0.03] border border-red-500/30 shadow-md space-y-3">
                <div className="flex items-center justify-between">
                  <span className="px-2 py-0.5 rounded text-[10px] font-bold uppercase tracking-wider bg-red-500/20 text-red-300 border border-red-500/30">
                    Critical Rule Violation
                  </span>
                  <span className="text-[11px] font-semibold text-red-400 flex items-center">
                    <span className="w-1.5 h-1.5 rounded-full bg-red-400 mr-1.5" />
                    CONTRADICTION • 98% Confidence
                  </span>
                </div>

                <div>
                  <h4 className="text-xs font-bold text-white">Unqualified Absolute & SLA Discrepancy</h4>
                  <p className="text-[11px] text-ink-300 mt-1">
                    Excerpt: <code className="text-red-300 bg-red-900/30 px-1 py-0.5 rounded">&ldquo;guarantees 100% unbreakable uptime&rdquo;</code>
                  </p>
                </div>

                <div className="p-2.5 rounded-lg bg-black/30 border border-white/5 text-[11px] space-y-1">
                  <div className="text-ink-400 flex items-center space-x-1">
                    <Database className="w-3 h-3 text-cyan-400 mr-1" />
                    <span>Ground Truth Citation:</span>
                    <span className="text-cyan-300 font-medium">Brand Standards 2026 §4.2 (Prohibited Absolutes)</span>
                  </div>
                  <p className="text-ink-300">
                    Benchmark claim <span className="text-white font-mono">#CLM-2026-08</span> permits claiming{' '}
                    <strong className="text-white">99.9% verified high availability</strong> with qualified maintenance SLA.
                  </p>
                </div>
              </div>

              {/* Intent Lock Interactive Fixes */}
              <div className="p-4 rounded-xl bg-white/[0.03] border border-brand-500/30 shadow-md space-y-3">
                <div className="flex items-center justify-between">
                  <div className="flex items-center space-x-1.5">
                    <GitCompare className="w-4 h-4 text-brand-400" />
                    <span className="text-xs font-bold text-white">Intent Lock™ Surgical Fix</span>
                  </div>
                  <span className="text-[10px] text-brand-300 font-medium">Preserves Author Context</span>
                </div>

                {/* 3 Tier Selector Buttons */}
                <div className="grid grid-cols-3 gap-1.5 p-1 bg-black/40 rounded-lg border border-white/5">
                  {(['light', 'balanced', 'strong']).map((tier) => (
                    <button
                      key={tier}
                      type="button"
                      onClick={() => setActiveIntentTier(tier)}
                      className={`py-1.5 text-[11px] font-semibold rounded-md transition-all capitalize ${
                        activeIntentTier === tier
                          ? 'bg-brand-600 text-white shadow-sm'
                          : 'text-ink-400 hover:text-white hover:bg-white/5'
                      }`}
                    >
                      {tier}
                    </button>
                  ))}
                </div>

                {/* Active Suggestion Box */}
                <div className="p-3 rounded-lg bg-brand-950/30 border border-brand-500/20 text-xs space-y-2">
                  <div className="flex items-center justify-between text-[11px]">
                    <span className="font-semibold text-brand-300">
                      {intentSuggestions[activeIntentTier].label}
                    </span>
                    <span className="text-ink-400 text-[10px]">
                      {intentSuggestions[activeIntentTier].tagline}
                    </span>
                  </div>
                  <p className="text-white font-medium italic">
                    &ldquo;{intentSuggestions[activeIntentTier].replacement}&rdquo;
                  </p>
                  <p className="text-[10px] text-ink-400">
                    {intentSuggestions[activeIntentTier].changeDesc}
                  </p>
                </div>

                {/* Action Row */}
                <div className="flex items-center justify-between pt-1">
                  <span className="text-[10px] text-ink-400 flex items-center">
                    <History className="w-3 h-3 mr-1 text-ink-400" /> Creates Immutable Revision #2
                  </span>
                  <Link
                    to={isAuthenticated ? ROUTES.REVIEW_EDITOR : ROUTES.REGISTER}
                    className="inline-flex items-center px-3 py-1.5 text-xs font-semibold rounded-lg bg-brand-600 hover:bg-brand-500 text-white transition-colors"
                  >
                    Accept Fix <ArrowRight className="w-3 h-3 ml-1" />
                  </Link>
                </div>
              </div>
            </div>
          </div>
        </div>

        <p className="text-center text-xs text-ink-500 mt-4">
          * Illustrative mock interface preview. In production, Inkloom AI connects to your real MongoDB Atlas brand guidelines and claims ledger.
        </p>
      </section>

      {/* Trust & Value Proposition Pillars */}
      <section id="grounding" className="relative z-10 py-20 px-4 sm:px-6 lg:px-8 max-w-7xl mx-auto">
        <div className="text-center max-w-3xl mx-auto mb-16">
          <span className="text-xs font-semibold uppercase tracking-wider text-cyan-400">
            Engineered for Brand Integrity
          </span>
          <h2 className="text-3xl sm:text-4xl font-extrabold text-white mt-2">
            Why Enterprise Governance Teams Choose Inkloom AI
          </h2>
          <p className="text-sm sm:text-base text-ink-300 mt-3">
            Traditional grammar checkers miss brand dilution and legal risks. Inkloom AI provides factual evidence citations,
            deterministic compliance rules, and state-machine approval control.
          </p>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
          {/* Pillar 1 */}
          <div className="p-6 rounded-2xl bg-white/[0.02] border border-white/10 hover:border-brand-500/40 transition-all hover:translate-y-[-2px] group">
            <div className="w-12 h-12 rounded-xl bg-brand-500/10 border border-brand-500/20 text-brand-400 flex items-center justify-center mb-5 group-hover:scale-110 transition-transform">
              <ShieldCheck className="w-6 h-6" />
            </div>
            <h3 className="text-base font-bold text-white">Brand-Guideline Consistency</h3>
            <p className="text-xs text-ink-400 mt-2 leading-relaxed">
              Instant deterministic and contextual AI checks ensuring voice, tone, terminology, and mandatory disclaimers across all communications.
            </p>
          </div>

          {/* Pillar 2 */}
          <div className="p-6 rounded-2xl bg-white/[0.02] border border-white/10 hover:border-cyan-500/40 transition-all hover:translate-y-[-2px] group">
            <div className="w-12 h-12 rounded-xl bg-cyan-500/10 border border-cyan-500/20 text-cyan-400 flex items-center justify-center mb-5 group-hover:scale-110 transition-transform">
              <Database className="w-6 h-6" />
            </div>
            <h3 className="text-base font-bold text-white">Evidence-Grounded Claim Checking</h3>
            <p className="text-xs text-ink-400 mt-2 leading-relaxed">
              Every factual claim is validated against registered evidence in MongoDB Atlas with zero hallucinations and expired-claim detection.
            </p>
          </div>

          {/* Pillar 3 */}
          <div className="p-6 rounded-2xl bg-white/[0.02] border border-white/10 hover:border-indigo-500/40 transition-all hover:translate-y-[-2px] group">
            <div className="w-12 h-12 rounded-xl bg-indigo-500/10 border border-indigo-500/20 text-indigo-400 flex items-center justify-center mb-5 group-hover:scale-110 transition-transform">
              <Activity className="w-6 h-6" />
            </div>
            <h3 className="text-base font-bold text-white">Risk-Aware Content Reviews</h3>
            <p className="text-xs text-ink-400 mt-2 leading-relaxed">
              Deterministic risk gates classify submissions into Critical, High, Medium, or Low tiers with non-negotiable blocking conditions.
            </p>
          </div>

          {/* Pillar 4 */}
          <div className="p-6 rounded-2xl bg-white/[0.02] border border-white/10 hover:border-emerald-500/40 transition-all hover:translate-y-[-2px] group">
            <div className="w-12 h-12 rounded-xl bg-emerald-500/10 border border-emerald-500/20 text-emerald-400 flex items-center justify-center mb-5 group-hover:scale-110 transition-transform">
              <Lock className="w-6 h-6" />
            </div>
            <h3 className="text-base font-bold text-white">Human-Controlled Approvals</h3>
            <p className="text-xs text-ink-400 mt-2 leading-relaxed">
              Multi-stage state machine enforcing separation of duties—content authors cannot self-approve their own submissions without independent review.
            </p>
          </div>
        </div>
      </section>

      {/* How It Works Section */}
      <section id="how-it-works" className="relative z-10 py-20 px-4 sm:px-6 lg:px-8 max-w-7xl mx-auto">
        <div className="text-center max-w-3xl mx-auto mb-16">
          <span className="text-xs font-semibold uppercase tracking-wider text-brand-400">
            Intuitive Workflow
          </span>
          <h2 className="text-3xl sm:text-4xl font-extrabold text-white mt-2">
            From Draft to Compliant Publication in Three Steps
          </h2>
          <p className="text-sm sm:text-base text-ink-300 mt-3">
            Streamlined collaboration between content creators, brand managers, and legal reviewers.
          </p>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-3 gap-8 relative">
          {/* Step 1 */}
          <div className="relative p-6 rounded-2xl bg-[#0F172A]/70 border border-white/10 backdrop-blur-md">
            <div className="flex items-center justify-between mb-4">
              <span className="text-2xl font-black text-brand-400 font-mono">01</span>
              <span className="px-2.5 py-0.5 rounded text-[10px] font-semibold bg-brand-500/10 text-brand-300 border border-brand-500/20">
                Draft Submission
              </span>
            </div>
            <h3 className="text-lg font-bold text-white">Submit Your Content</h3>
            <p className="text-xs text-ink-300 mt-2 leading-relaxed">
              Paste marketing copy, press releases, or documentation. Select your brand workspace to load isolated guidelines and verified claims.
            </p>
          </div>

          {/* Step 2 */}
          <div className="relative p-6 rounded-2xl bg-[#0F172A]/70 border border-white/10 backdrop-blur-md">
            <div className="flex items-center justify-between mb-4">
              <span className="text-2xl font-black text-cyan-400 font-mono">02</span>
              <span className="px-2.5 py-0.5 rounded text-[10px] font-semibold bg-cyan-500/10 text-cyan-300 border border-cyan-500/20">
                Evidence Analysis
              </span>
            </div>
            <h3 className="text-lg font-bold text-white">Review Findings & Evidence</h3>
            <p className="text-xs text-ink-300 mt-2 leading-relaxed">
              Inkloom executes hybrid deterministic rules and Gemini evaluation. Inspect evidence cards, uncertainty badges, and contradiction alerts.
            </p>
          </div>

          {/* Step 3 */}
          <div className="relative p-6 rounded-2xl bg-[#0F172A]/70 border border-white/10 backdrop-blur-md">
            <div className="flex items-center justify-between mb-4">
              <span className="text-2xl font-black text-emerald-400 font-mono">03</span>
              <span className="px-2.5 py-0.5 rounded text-[10px] font-semibold bg-emerald-500/10 text-emerald-300 border border-emerald-500/20">
                Human Sign-off
              </span>
            </div>
            <h3 className="text-lg font-bold text-white">Intent Lock™ & Sign-Off</h3>
            <p className="text-xs text-ink-300 mt-2 leading-relaxed">
              Apply surgical fixes across Light, Balanced, or Strong tiers. Route to independent reviewers for separation-of-duties sign-off.
            </p>
          </div>
        </div>
      </section>

      {/* Feature Showcase Grid */}
      <section id="features" className="relative z-10 py-20 px-4 sm:px-6 lg:px-8 max-w-7xl mx-auto">
        <div className="text-center max-w-3xl mx-auto mb-16">
          <span className="text-xs font-semibold uppercase tracking-wider text-indigo-400">
            Comprehensive Governance Suite
          </span>
          <h2 className="text-3xl sm:text-4xl font-extrabold text-white mt-2">
            Everything Required for Enterprise Content Compliance
          </h2>
          <p className="text-sm sm:text-base text-ink-300 mt-3">
            Real features engineered into Inkloom AI to protect your brand reputation at scale.
          </p>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {/* Feature 1 */}
          <div className="p-6 rounded-2xl bg-white/[0.02] border border-white/10 hover:border-brand-500/30 transition-all space-y-3">
            <div className="w-10 h-10 rounded-xl bg-brand-500/10 text-brand-400 flex items-center justify-center">
              <FileCheck className="w-5 h-5" />
            </div>
            <h4 className="text-base font-bold text-white">Evidence Cards & Grounding</h4>
            <p className="text-xs text-ink-400 leading-relaxed">
              Transparent evidence cards linking every flagged violation to verified source sections, with uncertainty classifications
              (CONFIRMED, NUMERIC_DISCREPANCY, EXPIRED_EVIDENCE).
            </p>
          </div>

          {/* Feature 2 */}
          <div className="p-6 rounded-2xl bg-white/[0.02] border border-white/10 hover:border-brand-500/30 transition-all space-y-3">
            <div className="w-10 h-10 rounded-xl bg-cyan-500/10 text-cyan-400 flex items-center justify-center">
              <GitCompare className="w-5 h-5" />
            </div>
            <h4 className="text-base font-bold text-white">Intent Lock™ 3-Strength Fixes</h4>
            <p className="text-xs text-ink-400 leading-relaxed">
              Surgical rewrites across Light, Balanced, and Strong tiers that preserve author voice, paragraph rhythm, and marketing intent.
            </p>
          </div>

          {/* Feature 3 */}
          <div className="p-6 rounded-2xl bg-white/[0.02] border border-white/10 hover:border-brand-500/30 transition-all space-y-3">
            <div className="w-10 h-10 rounded-xl bg-indigo-500/10 text-indigo-400 flex items-center justify-center">
              <Database className="w-5 h-5" />
            </div>
            <h4 className="text-base font-bold text-white">Central Claims Ledger</h4>
            <p className="text-xs text-ink-400 leading-relaxed">
              Dedicated repository of verified product, performance, and legal claims with expiration date monitoring and scope-of-use tagging.
            </p>
          </div>

          {/* Feature 4 */}
          <div className="p-6 rounded-2xl bg-white/[0.02] border border-white/10 hover:border-brand-500/30 transition-all space-y-3">
            <div className="w-10 h-10 rounded-xl bg-emerald-500/10 text-emerald-400 flex items-center justify-center">
              <BarChart3 className="w-5 h-5" />
            </div>
            <h4 className="text-base font-bold text-white">Continuous Brand Intelligence</h4>
            <p className="text-xs text-ink-400 leading-relaxed">
              Workspace analytics tracking average review turnaround hours, high-risk pending items, recurring guideline violations, and category heatmaps.
            </p>
          </div>

          {/* Feature 5 */}
          <div className="p-6 rounded-2xl bg-white/[0.02] border border-white/10 hover:border-brand-500/30 transition-all space-y-3">
            <div className="w-10 h-10 rounded-xl bg-red-500/10 text-red-400 flex items-center justify-center">
              <Shield className="w-5 h-5" />
            </div>
            <h4 className="text-base font-bold text-white">Deterministic Compliance Gates</h4>
            <p className="text-xs text-ink-400 leading-relaxed">
              Hard compliance gates that AI cannot override. Automatic approval blocking for critical buzzwords, numeric mismatches, or missing disclaimers.
            </p>
          </div>

          {/* Feature 6 */}
          <div className="p-6 rounded-2xl bg-white/[0.02] border border-white/10 hover:border-brand-500/30 transition-all space-y-3">
            <div className="w-10 h-10 rounded-xl bg-purple-500/10 text-purple-400 flex items-center justify-center">
              <History className="w-5 h-5" />
            </div>
            <h4 className="text-base font-bold text-white">Revision Lineage & Audit Trail</h4>
            <p className="text-xs text-ink-400 leading-relaxed">
              Immutable revision history tracking parent-child ancestry, full diff comparisons, one-click rollbacks, and tamper-evident audit logs.
            </p>
          </div>
        </div>
      </section>

      {/* Final Call to Action */}
      <section className="relative z-10 py-20 px-4 sm:px-6 lg:px-8 max-w-7xl mx-auto">
        <div className="relative rounded-3xl overflow-hidden border border-brand-500/30 bg-gradient-to-b from-brand-950/40 via-[#0F172A] to-[#0B0F19] p-8 sm:p-14 text-center shadow-2xl">
          <div className="absolute top-0 left-1/2 -translate-x-1/2 w-96 h-96 bg-brand-500/20 rounded-full blur-[120px] pointer-events-none" />

          <h2 className="text-3xl sm:text-5xl font-extrabold text-white max-w-2xl mx-auto tracking-tight">
            Ready to eliminate brand risk across all publications?
          </h2>
          <p className="text-sm sm:text-base text-ink-300 mt-4 max-w-xl mx-auto">
            Join enterprise marketing and governance teams standardizing their content review workflows with grounded AI governance.
          </p>

          <div className="mt-8 flex flex-col sm:flex-row items-center justify-center gap-4">
            <Link
              to={isAuthenticated ? ROUTES.DASHBOARD : ROUTES.REGISTER}
              className="w-full sm:w-auto px-8 py-3.5 text-sm font-semibold rounded-xl bg-gradient-to-r from-brand-600 via-brand-500 to-cyan-500 hover:from-brand-500 hover:to-cyan-400 text-white shadow-xl shadow-brand-500/25 transition-all hover:scale-[1.02] flex items-center justify-center"
            >
              <span>{isAuthenticated ? 'Open Dashboard' : 'Create Free Workspace'}</span>
              <ArrowRight className="w-4 h-4 ml-2" />
            </Link>
            <Link
              to={ROUTES.LOGIN}
              className="w-full sm:w-auto px-7 py-3.5 text-sm font-semibold rounded-xl bg-white/[0.04] hover:bg-white/[0.08] text-white border border-white/10 hover:border-white/20 transition-all flex items-center justify-center"
            >
              Sign In to Existing Account
            </Link>
          </div>
        </div>
      </section>

      {/* Footer */}
      <footer className="relative z-10 border-t border-white/10 bg-[#090D16] py-12 px-4 sm:px-6 lg:px-8">
        <div className="max-w-7xl mx-auto flex flex-col md:flex-row items-center justify-between gap-6">
          <div className="flex items-center space-x-3">
            <div className="w-8 h-8 rounded-lg bg-gradient-to-tr from-brand-600 to-cyan-400 p-[1px]">
              <div className="w-full h-full bg-[#090D16] rounded-[7px] flex items-center justify-center">
                <ShieldCheck className="w-4 h-4 text-brand-400" />
              </div>
            </div>
            <span className="text-sm font-bold text-white">Inkloom AI</span>
            <span className="text-xs text-ink-500">| Intelligent Brand Governance Platform</span>
          </div>

          <div className="flex items-center space-x-6 text-xs text-ink-400">
            <Link to={ROUTES.LOGIN} className="hover:text-white transition-colors">
              Sign In
            </Link>
            <Link to={ROUTES.REGISTER} className="hover:text-white transition-colors">
              Register
            </Link>
            <a href="#preview" className="hover:text-white transition-colors">
              Preview
            </a>
            <a href="#features" className="hover:text-white transition-colors">
              Capabilities
            </a>
          </div>

          <div className="flex items-center space-x-2 text-xs text-ink-500">
            <span className="w-2 h-2 rounded-full bg-emerald-400 inline-block animate-pulse" />
            <span>MongoDB Atlas Grounded • Operational</span>
          </div>
        </div>

        <div className="max-w-7xl mx-auto mt-8 pt-6 border-t border-white/5 text-center text-[11px] text-ink-600">
          © {new Date().getFullYear()} Inkloom AI Platform. All rights reserved. Enterprise brand governance and compliance engine.
        </div>
      </footer>
    </div>
  );
}
