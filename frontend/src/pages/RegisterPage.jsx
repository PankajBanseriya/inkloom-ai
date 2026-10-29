import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { useToast } from '../context/ToastContext';
import { ROUTES } from '../constants/routes';
import {
  ShieldCheck,
  ArrowRight,
  UserPlus,
  Lock,
  Mail,
  User,
  Building2,
  Eye,
  EyeOff,
  AlertCircle,
  CheckCircle2,
  Shield,
  Zap,
} from 'lucide-react';

export default function RegisterPage() {
  const navigate = useNavigate();
  const { register } = useAuth();
  const { success, error: toastError } = useToast();

  const [fullName, setFullName] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [workspaceName, setWorkspaceName] = useState('');
  const [role, setRole] = useState('CONTENT_CREATOR');
  const [errors, setErrors] = useState({});
  const [isLoading, setIsLoading] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    const newErrors = {};

    if (!fullName.trim()) newErrors.fullName = 'Full legal or corporate name is required.';
    if (!email.trim()) {
      newErrors.email = 'Corporate email is required.';
    } else if (!/\S+@\S+\.\S+/.test(email)) {
      newErrors.email = 'Enter a valid corporate email address.';
    }
    if (!password || password.length < 8) {
      newErrors.password = 'Password must be at least 8 characters.';
    }
    if (!workspaceName.trim()) {
      newErrors.workspaceName = 'Initial Brand Workspace name is required.';
    }

    if (Object.keys(newErrors).length > 0) {
      setErrors(newErrors);
      return;
    }

    setErrors({});
    setIsLoading(true);

    try {
      await register(fullName, email, password, role, workspaceName);
      setIsLoading(false);
      success('Workspace Account Initialized', `Welcome to Inkloom AI, ${fullName}!`);
      navigate(ROUTES.DASHBOARD);
    } catch (err) {
      setIsLoading(false);
      const msg = err.message || 'Registration failed. Please check form values.';
      setErrors({ form: msg });
      toastError('Registration Failed', msg);
    }
  };

  return (
    <div className="min-h-screen bg-[#0B0F19] text-white flex flex-col lg:flex-row antialiased selection:bg-brand-500 selection:text-white">
      {/* Left Panel: Brand Identity & Value Props (Desktop Only) */}
      <div className="hidden lg:flex lg:w-1/2 relative bg-[#090D16] border-r border-white/10 p-12 flex-col justify-between overflow-hidden">
        {/* Glow orbs & subtle grid */}
        <div className="absolute top-[-20%] left-[-10%] w-[500px] h-[500px] bg-brand-600/15 rounded-full blur-[140px] pointer-events-none" />
        <div className="absolute bottom-[-10%] right-[-10%] w-[450px] h-[450px] bg-cyan-500/10 rounded-full blur-[140px] pointer-events-none" />
        <div className="absolute inset-0 bg-[linear-gradient(to_right,#ffffff05_1px,transparent_1px),linear-gradient(to_bottom,#ffffff05_1px,transparent_1px)] bg-[size:3.5rem_3.5rem] [mask-image:radial-gradient(ellipse_60%_50%_at_50%_50%,#000_70%,transparent_100%)] pointer-events-none" />

        {/* Top: Logo */}
        <div className="relative z-10">
          <Link to={ROUTES.HOME} className="inline-flex items-center space-x-3 group">
            <div className="w-10 h-10 rounded-xl bg-gradient-to-tr from-brand-600 to-cyan-400 p-[1px] shadow-lg shadow-brand-500/20 group-hover:shadow-brand-500/40 transition-shadow">
              <div className="w-full h-full bg-[#0B0F19] rounded-[11px] flex items-center justify-center">
                <ShieldCheck className="w-5 h-5 text-brand-400" />
              </div>
            </div>
            <div className="flex items-center">
              <span className="text-xl font-bold tracking-tight text-white">Inkloom</span>
              <span className="text-xl font-bold tracking-tight text-transparent bg-clip-text bg-gradient-to-r from-brand-400 to-cyan-400 ml-1">
                AI
              </span>
            </div>
          </Link>
        </div>

        {/* Center: Highlights */}
        <div className="relative z-10 max-w-lg my-auto py-10 space-y-8">
          <div className="inline-flex items-center px-3 py-1 rounded-full bg-cyan-500/10 border border-cyan-500/20 text-cyan-300 text-xs font-semibold">
            <span className="w-2 h-2 rounded-full bg-cyan-400 mr-2 animate-pulse" />
            Brand Workspace Provisioning
          </div>

          <h2 className="text-3xl xl:text-4xl font-extrabold text-white tracking-tight leading-tight">
            Elevate your brand integrity from draft to publication.
          </h2>

          <p className="text-sm text-ink-300 leading-relaxed">
            Establish your corporate brand guidelines, seed your verified claims ledger, and configure automated
            compliance gating with separation of duties in under two minutes.
          </p>

          {/* Three Feature Highlights */}
          <div className="space-y-4 pt-2">
            <div className="flex items-start space-x-3.5 p-3.5 rounded-xl bg-white/[0.02] border border-white/5">
              <div className="w-8 h-8 rounded-lg bg-brand-500/10 text-brand-400 flex items-center justify-center flex-shrink-0 mt-0.5">
                <Shield className="w-4 h-4" />
              </div>
              <div className="text-xs">
                <span className="font-semibold text-white block">Zero-Hallucination Evidence Grounding</span>
                <span className="text-ink-400 mt-0.5 block">
                  Deterministic and AI checks verify claims against active approved sources in MongoDB Atlas.
                </span>
              </div>
            </div>

            <div className="flex items-start space-x-3.5 p-3.5 rounded-xl bg-white/[0.02] border border-white/5">
              <div className="w-8 h-8 rounded-lg bg-cyan-500/10 text-cyan-400 flex items-center justify-center flex-shrink-0 mt-0.5">
                <Zap className="w-4 h-4" />
              </div>
              <div className="text-xs">
                <span className="font-semibold text-white block">Intent Lock™ 3-Strength Fixes</span>
                <span className="text-ink-400 mt-0.5 block">
                  Surgical replacements across Light, Balanced, and Strong tiers preserve author intent.
                </span>
              </div>
            </div>

            <div className="flex items-start space-x-3.5 p-3.5 rounded-xl bg-white/[0.02] border border-white/5">
              <div className="w-8 h-8 rounded-lg bg-emerald-500/10 text-emerald-400 flex items-center justify-center flex-shrink-0 mt-0.5">
                <CheckCircle2 className="w-4 h-4" />
              </div>
              <div className="text-xs">
                <span className="font-semibold text-white block">Separation of Duties Sign-offs</span>
                <span className="text-ink-400 mt-0.5 block">
                  Enforces multi-stage state transitions. Authors cannot self-approve their own drafts.
                </span>
              </div>
            </div>
          </div>
        </div>

        {/* Bottom Note */}
        <div className="relative z-10 flex items-center justify-between text-xs text-ink-500 pt-6 border-t border-white/5">
          <span>Enterprise Role-Based Access Control</span>
          <span>Instant Provisioning</span>
        </div>
      </div>

      {/* Right Panel: Signup Form */}
      <div className="w-full lg:w-1/2 flex items-center justify-center p-6 sm:p-12 min-h-screen relative overflow-y-auto">
        <div className="w-full max-w-md space-y-6 py-8">
          {/* Mobile Logo Link */}
          <div className="lg:hidden text-center mb-4">
            <Link to={ROUTES.HOME} className="inline-flex items-center space-x-2.5">
              <div className="w-9 h-9 rounded-xl bg-gradient-to-tr from-brand-600 to-cyan-400 p-[1px]">
                <div className="w-full h-full bg-[#0B0F19] rounded-[10px] flex items-center justify-center">
                  <ShieldCheck className="w-4 h-4 text-brand-400" />
                </div>
              </div>
              <span className="text-xl font-bold text-white">Inkloom AI</span>
            </Link>
          </div>

          {/* Form Header */}
          <div className="space-y-1.5">
            <h1 className="text-2xl sm:text-3xl font-extrabold text-white tracking-tight">
              Create your workspace
            </h1>
            <p className="text-xs sm:text-sm text-ink-400 leading-relaxed">
              Bring your team, guidelines, and compliance review into one unified platform.
            </p>
          </div>

          {/* Error Banner */}
          {errors.form && (
            <div className="p-3.5 rounded-xl bg-red-500/10 border border-red-500/30 text-red-300 text-xs flex items-start space-x-2.5">
              <AlertCircle className="w-4 h-4 text-red-400 flex-shrink-0 mt-0.5" />
              <span>{errors.form}</span>
            </div>
          )}

          {/* Signup Form */}
          <form onSubmit={handleSubmit} noValidate className="space-y-4">
            {/* Full Name */}
            <div className="space-y-1">
              <label htmlFor="reg-fullname" className="block text-xs font-semibold text-ink-300">
                Full Name <span className="text-red-400">*</span>
              </label>
              <div className="relative">
                <div className="absolute inset-y-0 left-0 pl-3.5 flex items-center pointer-events-none text-ink-500">
                  <User className="w-4 h-4" />
                </div>
                <input
                  id="reg-fullname"
                  type="text"
                  value={fullName}
                  onChange={(e) => setFullName(e.target.value)}
                  placeholder="Elena Rostova"
                  autoComplete="name"
                  required
                  className={`w-full pl-10 pr-4 py-2.5 bg-white/[0.04] border rounded-xl text-sm text-white placeholder-ink-500 transition-all focus:outline-none focus:ring-2 focus:ring-brand-500 ${
                    errors.fullName
                      ? 'border-red-500/60 focus:border-red-500'
                      : 'border-white/10 hover:border-white/20 focus:border-brand-500'
                  }`}
                />
              </div>
              {errors.fullName && (
                <p className="text-xs text-red-400 flex items-center mt-1">
                  <AlertCircle className="w-3 h-3 mr-1 flex-shrink-0" />
                  {errors.fullName}
                </p>
              )}
            </div>

            {/* Work Email */}
            <div className="space-y-1">
              <label htmlFor="reg-email" className="block text-xs font-semibold text-ink-300">
                Work Email <span className="text-red-400">*</span>
              </label>
              <div className="relative">
                <div className="absolute inset-y-0 left-0 pl-3.5 flex items-center pointer-events-none text-ink-500">
                  <Mail className="w-4 h-4" />
                </div>
                <input
                  id="reg-email"
                  type="email"
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  placeholder="elena@company.com"
                  autoComplete="email"
                  required
                  className={`w-full pl-10 pr-4 py-2.5 bg-white/[0.04] border rounded-xl text-sm text-white placeholder-ink-500 transition-all focus:outline-none focus:ring-2 focus:ring-brand-500 ${
                    errors.email
                      ? 'border-red-500/60 focus:border-red-500'
                      : 'border-white/10 hover:border-white/20 focus:border-brand-500'
                  }`}
                />
              </div>
              {errors.email && (
                <p className="text-xs text-red-400 flex items-center mt-1">
                  <AlertCircle className="w-3 h-3 mr-1 flex-shrink-0" />
                  {errors.email}
                </p>
              )}
            </div>

            {/* Password */}
            <div className="space-y-1">
              <label htmlFor="reg-password" className="block text-xs font-semibold text-ink-300">
                Password <span className="text-red-400">*</span>
              </label>
              <div className="relative">
                <div className="absolute inset-y-0 left-0 pl-3.5 flex items-center pointer-events-none text-ink-500">
                  <Lock className="w-4 h-4" />
                </div>
                <input
                  id="reg-password"
                  type={showPassword ? 'text' : 'password'}
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  placeholder="••••••••"
                  autoComplete="new-password"
                  required
                  className={`w-full pl-10 pr-10 py-2.5 bg-white/[0.04] border rounded-xl text-sm text-white placeholder-ink-500 transition-all focus:outline-none focus:ring-2 focus:ring-brand-500 ${
                    errors.password
                      ? 'border-red-500/60 focus:border-red-500'
                      : 'border-white/10 hover:border-white/20 focus:border-brand-500'
                  }`}
                />
                <button
                  type="button"
                  onClick={() => setShowPassword(!showPassword)}
                  className="absolute inset-y-0 right-0 pr-3.5 flex items-center text-ink-500 hover:text-white transition-colors"
                  aria-label={showPassword ? 'Hide password' : 'Show password'}
                >
                  {showPassword ? <EyeOff className="w-4 h-4" /> : <Eye className="w-4 h-4" />}
                </button>
              </div>
              {errors.password ? (
                <p className="text-xs text-red-400 flex items-center mt-1">
                  <AlertCircle className="w-3 h-3 mr-1 flex-shrink-0" />
                  {errors.password}
                </p>
              ) : (
                <p className="text-[11px] text-ink-500 mt-1">
                  Minimum 8 characters with letters and numbers.
                </p>
              )}
            </div>

            {/* Brand / Organization Name */}
            <div className="space-y-1">
              <label htmlFor="reg-workspacename" className="block text-xs font-semibold text-ink-300">
                Brand / Organization Name <span className="text-red-400">*</span>
              </label>
              <div className="relative">
                <div className="absolute inset-y-0 left-0 pl-3.5 flex items-center pointer-events-none text-ink-500">
                  <Building2 className="w-4 h-4" />
                </div>
                <input
                  id="reg-workspacename"
                  type="text"
                  value={workspaceName}
                  onChange={(e) => setWorkspaceName(e.target.value)}
                  placeholder="Acme Enterprise Solutions"
                  required
                  className={`w-full pl-10 pr-4 py-2.5 bg-white/[0.04] border rounded-xl text-sm text-white placeholder-ink-500 transition-all focus:outline-none focus:ring-2 focus:ring-brand-500 ${
                    errors.workspaceName
                      ? 'border-red-500/60 focus:border-red-500'
                      : 'border-white/10 hover:border-white/20 focus:border-brand-500'
                  }`}
                />
              </div>
              {errors.workspaceName && (
                <p className="text-xs text-red-400 flex items-center mt-1">
                  <AlertCircle className="w-3 h-3 mr-1 flex-shrink-0" />
                  {errors.workspaceName}
                </p>
              )}
            </div>

            {/* Assigned Role */}
            <div className="space-y-1">
              <label htmlFor="reg-role" className="block text-xs font-semibold text-ink-300">
                Assigned Role
              </label>
              <div className="relative">
                <select
                  id="reg-role"
                  value={role}
                  onChange={(e) => setRole(e.target.value)}
                  className="w-full px-3.5 py-2.5 bg-[#0F172A] border border-white/10 hover:border-white/20 rounded-xl text-xs sm:text-sm text-white focus:outline-none focus:ring-2 focus:ring-brand-500 focus:border-brand-500 transition-all cursor-pointer"
                >
                  <option value="BRAND_MANAGER">Brand Manager (Guidelines & Standards)</option>
                  <option value="CONTENT_CREATOR">Content Creator (Submissions & Intent Lock)</option>
                  <option value="REVIEWER">Reviewer & Approver (Sign-offs & Decisions)</option>
                  <option value="ADMIN">System Administrator</option>
                </select>
              </div>
            </div>

            {/* Submit Button */}
            <div className="pt-2">
              <button
                type="submit"
                disabled={isLoading}
                className="w-full py-3 px-4 rounded-xl bg-gradient-to-r from-brand-600 via-brand-500 to-cyan-500 hover:from-brand-500 hover:to-cyan-400 text-white font-semibold text-sm shadow-lg shadow-brand-500/25 hover:shadow-brand-500/40 transition-all hover:scale-[1.01] active:scale-[0.99] disabled:opacity-60 disabled:cursor-not-allowed flex items-center justify-center space-x-2"
              >
                {isLoading ? (
                  <>
                    <svg className="animate-spin h-4 w-4 text-white" fill="none" viewBox="0 0 24 24">
                      <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" />
                      <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8v8H4z" />
                    </svg>
                    <span>Provisioning Workspace...</span>
                  </>
                ) : (
                  <>
                    <UserPlus className="w-4 h-4" />
                    <span>Register Brand Workspace</span>
                  </>
                )}
              </button>
            </div>
          </form>

          {/* Login Navigation Link */}
          <div className="pt-2 text-center text-xs text-ink-400">
            Already have an active account?{' '}
            <Link
              to={ROUTES.LOGIN}
              className="font-semibold text-brand-400 hover:text-brand-300 inline-flex items-center ml-1 group transition-colors"
            >
              <span>Sign In</span>
              <ArrowRight className="w-3 h-3 ml-1 group-hover:translate-x-0.5 transition-transform" />
            </Link>
          </div>
        </div>
      </div>
    </div>
  );
}
