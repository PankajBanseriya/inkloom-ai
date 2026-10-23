import React from 'react';

export default function LoadingSpinner({ size = 'md', message, className = '' }) {
  const sizeMap = {
    sm: 'w-4 h-4',
    md: 'w-8 h-8',
    lg: 'w-12 h-12',
  };

  return (
    <div className={`flex flex-col items-center justify-center py-6 ${className}`}>
      <svg
        className={`animate-spin text-brand-600 ${sizeMap[size] || sizeMap.md}`}
        xmlns="http://www.w3.org/2000/svg"
        fill="none"
        viewBox="0 0 24 24"
      >
        <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
        <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8v8H4z"></path>
      </svg>
      {message && <p className="mt-3 text-xs font-medium text-ink-500">{message}</p>}
    </div>
  );
}
