import React from 'react';
import Button from '../common/Button';

export default function ErrorMessage({
  title = 'Something went wrong',
  message,
  onRetry,
  className = '',
}) {
  return (
    <div className={`p-4 rounded-xl bg-rose-50/70 border border-rose-200 text-rose-900 ${className}`}>
      <div className="flex items-start justify-between">
        <div className="flex items-start space-x-3">
          <svg className="h-5 w-5 text-rose-500 mt-0.5 flex-shrink-0" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
          </svg>
          <div>
            <h4 className="text-xs font-semibold uppercase tracking-wider text-rose-900">{title}</h4>
            {message && <p className="text-xs text-rose-700 mt-0.5">{message}</p>}
          </div>
        </div>
        {onRetry && (
          <Button size="sm" variant="outline" onClick={onRetry} className="border-rose-300 text-rose-800 hover:bg-rose-100">
            Retry
          </Button>
        )}
      </div>
    </div>
  );
}
