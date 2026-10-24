import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { useToast } from '../context/ToastContext';
import { ROUTES } from '../constants/routes';
import Button from '../components/common/Button';
import Input from '../components/common/Input';
import { ShieldCheck, ArrowRight, Lock } from 'lucide-react';

export default function LoginPage() {
  const navigate = useNavigate();
  const { login } = useAuth();
  const { success, error: toastError } = useToast();

  const [email, setEmail] = useState('sarah.jenkins@acme.internal');
  const [password, setPassword] = useState('password123');
  const [errors, setErrors] = useState({});
  const [isLoading, setIsLoading] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    const newErrors = {};

    if (!email) {
      newErrors.email = 'Email address is required.';
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
      success('Demo Session Started', `Signed in as ${roleTitle}`);
      navigate(ROUTES.DASHBOARD);
    } catch (err) {
      setIsLoading(false);
      toastError('Authentication Error', err.message);
    }
  };

  return (
    <div className="min-h-screen bg-ink-50 flex flex-col justify-center py-12 px-4 sm:px-6 lg:px-8">
      <div className="sm:mx-auto sm:w-full sm:max-w-md text-center">
        <div className="w-12 h-12 rounded-xl bg-brand-600 text-white flex items-center justify-center mx-auto shadow-md">
          <ShieldCheck className="w-6 h-6" />
        </div>
        <h2 className="mt-4 text-2xl font-bold tracking-tight text-ink-900">
          Sign in to Inkloom AI
        </h2>
        <p className="mt-1 text-xs text-ink-500">
          Intelligent Brand Governance & Claims Verification Platform
        </p>
      </div>

      <div className="mt-8 sm:mx-auto sm:w-full sm:max-w-md">
        <div className="bg-white py-8 px-6 shadow-sm border border-ink-200/80 rounded-2xl sm:px-10">
          <form className="space-y-4" onSubmit={handleSubmit} noValidate>
            {errors.form && (
              <div className="p-3 bg-red-50 text-red-700 text-xs rounded-lg border border-red-200">
                {errors.form}
              </div>
            )}
            <Input
              label="Corporate Email"
              type="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              error={errors.email}
              placeholder="name@company.com"
              required
            />

            <Input
              label="Password"
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              error={errors.password}
              placeholder="••••••••"
              required
            />

            <div className="flex items-center justify-between text-xs">
              <label className="flex items-center space-x-2 text-ink-600">
                <input
                  type="checkbox"
                  defaultChecked
                  className="rounded border-ink-300 text-brand-600 focus:ring-brand-500"
                />
                <span>Remember session</span>
              </label>
              <a href="#forgot" className="font-medium text-brand-600 hover:text-brand-500">
                Forgot password?
              </a>
            </div>

            <Button
              type="submit"
              variant="primary"
              className="w-full text-xs font-semibold"
              isLoading={isLoading}
            >
              <Lock className="w-3.5 h-3.5 mr-1.5" />
              Sign In to Workspace
            </Button>
          </form>

          {/* Quick Demo Role Selector */}
          <div className="mt-6 pt-6 border-t border-ink-100">
            <span className="block text-[11px] font-semibold uppercase tracking-wider text-ink-500 mb-2.5 text-center">
              Quick Role Test Logins
            </span>
            <div className="grid grid-cols-2 gap-2 text-xs">
              <button
                type="button"
                onClick={() => handleQuickLogin('elena.rostova@acme.internal', 'BRAND_MANAGER')}
                className="p-2 border border-ink-200 hover:border-brand-300 hover:bg-brand-50/50 rounded-lg text-left transition-colors"
              >
                <div className="font-semibold text-ink-900">Brand Manager</div>
                <div className="text-[10px] text-ink-500">Elena Rostova</div>
              </button>

              <button
                type="button"
                onClick={() => handleQuickLogin('sarah.jenkins@acme.internal', 'REVIEWER')}
                className="p-2 border border-ink-200 hover:border-brand-300 hover:bg-brand-50/50 rounded-lg text-left transition-colors"
              >
                <div className="font-semibold text-ink-900">Reviewer / Approver</div>
                <div className="text-[10px] text-ink-500">Sarah Jenkins</div>
              </button>

              <button
                type="button"
                onClick={() => handleQuickLogin('marcus.vance@acme.internal', 'CONTENT_CREATOR')}
                className="p-2 border border-ink-200 hover:border-brand-300 hover:bg-brand-50/50 rounded-lg text-left transition-colors"
              >
                <div className="font-semibold text-ink-900">Content Creator</div>
                <div className="text-[10px] text-ink-500">Marcus Vance</div>
              </button>

              <button
                type="button"
                onClick={() => handleQuickLogin('alex.cross@acme.internal', 'ADMIN')}
                className="p-2 border border-ink-200 hover:border-brand-300 hover:bg-brand-50/50 rounded-lg text-left transition-colors"
              >
                <div className="font-semibold text-ink-900">Global Admin</div>
                <div className="text-[10px] text-ink-500">Alexander Cross</div>
              </button>
            </div>
          </div>

          <div className="mt-6 text-center text-xs text-ink-500">
            Don&apos;t have an enterprise account?{' '}
            <Link to={ROUTES.REGISTER} className="font-semibold text-brand-600 hover:text-brand-500 inline-flex items-center">
              Register Workspace <ArrowRight className="w-3 h-3 ml-1" />
            </Link>
          </div>
        </div>
      </div>
    </div>
  );
}
