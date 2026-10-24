import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { useToast } from '../context/ToastContext';
import { ROUTES } from '../constants/routes';
import Button from '../components/common/Button';
import Input from '../components/common/Input';
import FormField from '../components/common/FormField';
import { ShieldCheck, ArrowRight, UserPlus } from 'lucide-react';

export default function RegisterPage() {
  const navigate = useNavigate();
  const { register } = useAuth();
  const { success, error: toastError } = useToast();

  const [fullName, setFullName] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
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
      newErrors.email = 'Enter a valid email address.';
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
    <div className="min-h-screen bg-ink-50 flex flex-col justify-center py-12 px-4 sm:px-6 lg:px-8">
      <div className="sm:mx-auto sm:w-full sm:max-w-md text-center">
        <div className="w-12 h-12 rounded-xl bg-brand-600 text-white flex items-center justify-center mx-auto shadow-md">
          <ShieldCheck className="w-6 h-6" />
        </div>
        <h2 className="mt-4 text-2xl font-bold tracking-tight text-ink-900">
          Create Inkloom AI Account
        </h2>
        <p className="mt-1 text-xs text-ink-500">
          Provision brand guidelines, claims ledger, and governance pipelines
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
              label="Full Name"
              type="text"
              value={fullName}
              onChange={(e) => setFullName(e.target.value)}
              error={errors.fullName}
              placeholder="Elena Rostova"
              required
            />

            <Input
              label="Work Email"
              type="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              error={errors.email}
              placeholder="elena@company.com"
              required
            />

            <Input
              label="Password"
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              error={errors.password}
              helperText="Minimum 8 characters with letters and numbers."
              placeholder="••••••••"
              required
            />

            <Input
              label="Brand / Organization Name"
              type="text"
              value={workspaceName}
              onChange={(e) => setWorkspaceName(e.target.value)}
              error={errors.workspaceName}
              placeholder="Acme Enterprise Solutions"
              required
            />

            <FormField label="Assigned Role" id="role-select">
              <select
                id="role-select"
                value={role}
                onChange={(e) => setRole(e.target.value)}
                className="w-full px-3 py-2 text-xs bg-white border border-ink-300 rounded-lg text-ink-900 focus:outline-none focus:ring-2 focus:ring-brand-500"
              >
                <option value="BRAND_MANAGER">Brand Manager (Guidelines & Standards)</option>
                <option value="CONTENT_CREATOR">Content Creator (Submissions & Intent Lock)</option>
                <option value="REVIEWER">Reviewer & Approver (Sign-offs & Decisions)</option>
                <option value="ADMIN">System Administrator</option>
              </select>
            </FormField>

            <Button
              type="submit"
              variant="primary"
              className="w-full text-xs font-semibold"
              isLoading={isLoading}
            >
              <UserPlus className="w-3.5 h-3.5 mr-1.5" />
              Register Brand Workspace
            </Button>
          </form>

          <div className="mt-6 text-center text-xs text-ink-500">
            Already have an active account?{' '}
            <Link to={ROUTES.LOGIN} className="font-semibold text-brand-600 hover:text-brand-500 inline-flex items-center">
              Sign In <ArrowRight className="w-3 h-3 ml-1" />
            </Link>
          </div>
        </div>
      </div>
    </div>
  );
}
