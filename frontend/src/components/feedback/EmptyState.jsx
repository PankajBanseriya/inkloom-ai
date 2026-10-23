import React from 'react';
import Button from '../common/Button';

export default function EmptyState({
  title,
  description,
  actionLabel,
  onAction,
  icon,
  className = '',
}) {
  return (
    <div className={`text-center py-12 px-6 border-2 border-dashed border-ink-200 rounded-xl bg-white/50 ${className}`}>
      {icon && <div className="mx-auto flex items-center justify-center h-12 w-12 rounded-full bg-ink-100 text-ink-600 mb-3">{icon}</div>}
      <h3 className="text-sm font-semibold text-ink-900">{title}</h3>
      {description && <p className="mt-1 text-xs text-ink-500 max-w-sm mx-auto">{description}</p>}
      {actionLabel && onAction && (
        <div className="mt-4">
          <Button size="sm" onClick={onAction}>
            {actionLabel}
          </Button>
        </div>
      )}
    </div>
  );
}
