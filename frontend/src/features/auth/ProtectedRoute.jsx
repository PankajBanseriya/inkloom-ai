import { Navigate, useLocation } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import { ROUTES } from '../../constants/routes';

export default function ProtectedRoute({ children, allowedRoles }) {
  const { isAuthenticated, user } = useAuth();
  const location = useLocation();

  if (!isAuthenticated) {
    return <Navigate to={ROUTES.LOGIN} state={{ from: location }} replace />;
  }

  if (allowedRoles && allowedRoles.length > 0 && !allowedRoles.includes(user?.role)) {
    return (
      <div className="min-h-[50vh] flex flex-col items-center justify-center text-center p-6 bg-white rounded-xl border border-ink-200">
        <span className="text-3xl mb-2">🔒</span>
        <h3 className="text-base font-semibold text-ink-900">Restricted Access</h3>
        <p className="text-xs text-ink-500 mt-1 max-w-sm">
          Your current role (<span className="font-semibold text-brand-600">{user?.role}</span>) does not have authorization to view this section.
        </p>
      </div>
    );
  }

  return children;
}
