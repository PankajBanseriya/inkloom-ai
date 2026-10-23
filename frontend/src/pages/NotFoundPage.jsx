import React from 'react';
import { Link } from 'react-router-dom';
import Button from '../components/common/Button';
import { ROUTES } from '../constants/routes';

export default function NotFoundPage() {
  return (
    <div className="min-h-[60vh] flex flex-col items-center justify-center text-center px-4">
      <span className="text-4xl font-bold text-brand-600">404</span>
      <h2 className="text-lg font-semibold text-ink-900 mt-2">Page Not Found</h2>
      <p className="text-xs text-ink-500 mt-1 max-w-sm">
        The requested page does not exist or has been relocated within the brand workspace.
      </p>
      <Link to={ROUTES.DASHBOARD} className="mt-6">
        <Button size="sm">Return to Dashboard</Button>
      </Link>
    </div>
  );
}
