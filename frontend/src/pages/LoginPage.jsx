import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { useToast } from '../context/ToastContext';
import { ROUTES } from '../constants/routes';
import {
  ShieldCheck,
  ArrowRight,
  Lock,
  Mail,
  Eye,
  EyeOff,
  AlertCircle,
  CheckCircle2,
  Database,
  UserCheck,
} from 'lucide-react';

export default function LoginPage() {
  const navigate = useNavigate();
  const { login } = useAuth();
  const { success, error: toastError } = useToast();

  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [errors, setErrors] = useState({});
  const [isLoading, setIsLoading] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    const newErrors = {};

    if (!email) {
      newErrors.email = 'Corporate email address is required.';
    } else if (!/\S+@\S+\.\S+/.test(email)) {
      newErrors.email = 'Please provide a valid email format.';
    }

    if (!password) {
      newErrors.password = 'Password is required.';
    }

    if (Object.keys(newErrors).length > 0) {
      setErrors(newErrors);
      return;
    }

    setErrors({});
    setIsLoading(true);

    try {
      const authUser = await login(email, password, 'REVIEWER');
      setIsLoading(false);
      success('Authentication Successful', `Signed in as ${authUser.email}`);
      navigate(ROUTES.DASHBOARD);
    } catch (err) {
      setIsLoading(false);
      const msg = err.message || 'Authentication failed. Please verify credentials.';
      setErrors({ form: msg });
      toastError('Authentication Failed', msg);
    }
  };

  const handleQuickLogin = async (roleEmail, roleTitle) => {
    setEmail(roleEmail);
    setPassword('demo-pass');
    setIsLoading(true);
    try {
      await login(roleEmail, 'demo-pass', roleTitle);
      setIsLoading(false);
      success('Role Session Started', `Signed in as ${roleTitle}`);
      navigate(ROUTES.DASHBOARD);
    } catch (err) {
      setIsLoading(false);
      toastError('Authentication Error', err.message);
    }
  };

  return (
    <div className="min-h-screen bg-[#0B0F19] text-white flex flex-col lg:flex-row antialiased selection:bg-brand-500 selection:text-white">
      {/* Left Panel: Brand Identity & Highlights (Desktop Only) */}
      <div className="hidden lg:flex lg:w-1/2 relative bg-[#090D16] border-r border-white/10 p-12 flex-col justify-between overflow-hidden">
        {/* Glow orbs & grid */}
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

        {/* Center: Hero Graphic & Value Props */}
        <div className="relative z-10 max-w-lg my-auto py-12 space-y-8">
          <div className="inline-flex items-center px-3 py-1 rounded-full bg-brand-500/10 border border-brand-500/20 text-brand-300 text-xs font-semibold">
            <span className="w-2 h-2 rounded-full bg-cyan-400 mr-2 animate-pulse" />
            Enterprise Governance Suite
          </div>

          <h2 className="text-3xl xl:text-4xl font-extrabold text-white tracking-tight leading-tight">
            Autonomous Brand Governance with Human Authority.
          </h2>

          <p className="text-sm text-ink-300 leading-relaxed">
            Verify marketing claims against registered evidence, eliminate prohibited buzzwords with Intent Lock™,
            and enforce multi-stage separation-of-duties sign-offs before publishing.
          </p>

          {/* Interactive Live Compliance Card Preview */}
          <div className="p-5 rounded-2xl bg-white/[0.03] border border-white/10 backdrop-blur-xl shadow-xl space-y-3">
            <div className="flex items-center justify-between text-xs">
              <div className="flex items-center space-x-2">
                <ShieldCheck className="w-4 h-4 text-emerald-400" />
                <span className="font-semibold text-white">Live Compliance Pipeline</span>
              </div>
              <span className="px-2 py-0.5 rounded text-[10px] font-semibold bg-emerald-500/10 text-emerald-300 border border-emerald-500/20">
                ACTIVE
              </span>
            </div>

            <div className="space-y-2 pt-1 text-xs">
              <div className="flex items-center space-x-2 text-ink-300">
                <CheckCircle2 className="w-3.5 h-3.5 text-brand-400" />
                <span>MongoDB Atlas Grounding: <strong>Connected</strong></span>
              </div>
              <div className="flex items-center space-x-2 text-ink-300">
                <CheckCircle2 className="w-3.5 h-3.5 text-cyan-400" />
                <span>Uncertainty Classification: <strong>Active (98% Confidence)</strong></span>
              </div>
              <div className="flex items-center space-x-2 text-ink-300">
                <CheckCircle2 className="w-3.5 h-3.5 text-indigo-400" />
                <span>Separation of Duties Gate: <strong>Enforced</strong></span>
              </div>
            </div>
          </div>
        </div>

        {/* Bottom Note */}
        <div className="relative z-10 flex items-center justify-between text-xs text-ink-500 pt-6 border-t border-white/5">
          <div className="flex items-center space-x-2">
            <Database className="w-3.5 h-3.5 text-ink-400" />
            <span>Single Source of Truth Grounded</span>
          </div>
          <span>v3.0 Production</span>
        </div>
      </div>

      {/* Right Panel: Focused Modern Login Form */}
      <div className="w-full lg:w-1/2 flex items-center justify-center p-6 sm:p-12 min-h-screen relative overflow-y-auto">
        <div className="w-full max-w-md space-y-8 py-8">
          {/* Mobile Logo Link */}
          <div className="lg:hidden text-center mb-6">
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
          <div className="space-y-2">
            <h1 className="text-2xl sm:text-3xl font-extrabold text-white tracking-tight">
              Welcome back
            </h1>
            <p className="text-xs sm:text-sm text-ink-400 leading-relaxed">
              Sign in to access your brand workspaces, review pipeline, and compliance ledger.
            </p>
          </div>

          {/* Error Banner */}
          {errors.form && (
            <div className="p-3.5 rounded-xl bg-red-500/10 border border-red-500/30 text-red-300 text-xs flex items-start space-x-2.5">
              <AlertCircle className="w-4 h-4 text-red-400 flex-shrink-0 mt-0.5" />
              <span>{errors.form}</span>
            </div>
          )}

          {/* Login Form */}
          <form onSubmit={handleSubmit} noValidate className="space-y-5">
            {/* Email Field */}
            <div className="space-y-1.5">
              <label htmlFor="login-email" className="block text-xs font-semibold text-ink-300">
                Corporate Email <span className="text-red-400">*</span>
              </label>
              <div className="relative">
                <div className="absolute inset-y-0 left-0 pl-3.5 flex items-center pointer-events-none text-ink-500">
                  <Mail className="w-4 h-4" />
                </div>
                <input
                  id="login-email"
                  type="email"
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  placeholder="name@company.com"
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

            {/* Password Field */}
            <div className="space-y-1.5">
              <div className="flex items-center justify-between">
                <label htmlFor="login-password" className="block text-xs font-semibold text-ink-300">
                  Password <span className="text-red-400">*</span>
                </label>
                <a href="#forgot" className="text-xs font-medium text-brand-400 hover:text-brand-300 transition-colors">
                  Forgot password?
                </a>
              </div>
              <div className="relative">
                <div className="absolute inset-y-0 left-0 pl-3.5 flex items-center pointer-events-none text-ink-500">
                  <Lock className="w-4 h-4" />
                </div>
                <input
                  id="login-password"
                  type={showPassword ? 'text' : 'password'}
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  placeholder="••••••••"
                  autoComplete="current-password"
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
              {errors.password && (
                <p className="text-xs text-red-400 flex items-center mt-1">
                  <AlertCircle className="w-3 h-3 mr-1 flex-shrink-0" />
                  {errors.password}
                </p>
              )}
            </div>

            {/* Remember Session Checkbox */}
            <div className="flex items-center">
              <input
                id="remember-me"
                type="checkbox"
                defaultChecked
                className="w-4 h-4 rounded bg-white/5 border-white/10 text-brand-600 focus:ring-brand-500 focus:ring-offset-0 focus:ring-offset-transparent cursor-pointer"
              />
              <label htmlFor="remember-me" className="ml-2.5 text-xs text-ink-400 cursor-pointer">
                Remember session on this device
              </label>
            </div>

            {/* Submit Button */}
            <button
              type="submit"
              disabled={isLoading}
              className="w-full py-3 px-4 rounded-xl bg-gradient-to-r from-brand-600 via-brand-500 to-indigo-600 hover:from-brand-500 hover:to-indigo-500 text-white font-semibold text-sm shadow-lg shadow-brand-500/25 hover:shadow-brand-500/40 transition-all hover:scale-[1.01] active:scale-[0.99] disabled:opacity-60 disabled:cursor-not-allowed flex items-center justify-center space-x-2"
            >
              {isLoading ? (
                <>
                  <svg className="animate-spin h-4 w-4 text-white" fill="none" viewBox="0 0 24 24">
                    <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" />
                    <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8v8H4z" />
                  </svg>
                  <span>Authenticating...</span>
                </>
              ) : (
                <>
                  <Lock className="w-4 h-4" />
                  <span>Sign In to Workspace</span>
                </>
              )}
            </button>
          </form>

          {/* Quick Role Test Logins */}
          <div className="pt-6 border-t border-white/10 space-y-3">
            <div className="flex items-center justify-between">
              <span className="text-[11px] font-semibold uppercase tracking-wider text-ink-400">
                Quick Role Test Logins
              </span>
              <span className="text-[10px] text-brand-400 font-medium flex items-center">
                <UserCheck className="w-3 h-3 mr-1" /> 1-Click Evaluation
              </span>
            </div>

            <div className="grid grid-cols-2 gap-2.5">
              <button
                type="button"
                onClick={() => handleQuickLogin('elena.rostova@acme.internal', 'BRAND_MANAGER')}
                className="p-3 rounded-xl bg-white/[0.02] hover:bg-white/[0.06] border border-white/5 hover:border-brand-500/30 text-left transition-all group"
              >
                <div className="text-xs font-semibold text-white group-hover:text-brand-300 transition-colors">
                  Brand Manager
                </div>
                <div className="text-[11px] text-ink-500 mt-0.5">Elena Rostova</div>
              </button>

              <button
                type="button"
                onClick={() => handleQuickLogin('sarah.jenkins@acme.internal', 'REVIEWER')}
                className="p-3 rounded-xl bg-white/[0.02] hover:bg-white/[0.06] border border-white/5 hover:border-brand-500/30 text-left transition-all group"
              >
                <div className="text-xs font-semibold text-white group-hover:text-brand-300 transition-colors">
                  Reviewer / Approver
                </div>
                <div className="text-[11px] text-ink-500 mt-0.5">Sarah Jenkins</div>
              </button>

              <button
                type="button"
                onClick={() => handleQuickLogin('marcus.vance@acme.internal', 'CONTENT_CREATOR')}
                className="p-3 rounded-xl bg-white/[0.02] hover:bg-white/[0.06] border border-white/5 hover:border-brand-500/30 text-left transition-all group"
              >
                <div className="text-xs font-semibold text-white group-hover:text-brand-300 transition-colors">
                  Content Creator
                </div>
                <div className="text-[11px] text-ink-500 mt-0.5">Marcus Vance</div>
              </button>

              <button
                type="button"
                onClick={() => handleQuickLogin('alex.cross@acme.internal', 'ADMIN')}
                className="p-3 rounded-xl bg-white/[0.02] hover:bg-white/[0.06] border border-white/5 hover:border-brand-500/30 text-left transition-all group"
              >
                <div className="text-xs font-semibold text-white group-hover:text-brand-300 transition-colors">
                  Global Admin
                </div>
                <div className="text-[11px] text-ink-500 mt-0.5">Alexander Cross</div>
              </button>
            </div>
          </div>

          {/* Signup Navigation Link */}
          <div className="pt-2 text-center text-xs text-ink-400">
            Don&apos;t have an enterprise account?{' '}
            <Link
              to={ROUTES.REGISTER}
              className="font-semibold text-brand-400 hover:text-brand-300 inline-flex items-center ml-1 group transition-colors"
            >
              <span>Register Workspace</span>
              <ArrowRight className="w-3 h-3 ml-1 group-hover:translate-x-0.5 transition-transform" />
            </Link>
          </div>
        </div>
      </div>
    </div>
  );
}
